//
// Created by Sparsh Jain on 29/09/22.
//

import Foundation
import TensorFlowLite
import Accelerate
import CoreImage

class Network {
    
    // Dimensions of inputs.
    private let DIM_BATCH_SIZE: Int = 1;
    private let DIM_PIXEL_SIZE: Int = 3;
    
    var imageSize: CGSize;
    
    // Preallocated buffers for storing image data in.
    var intValues: [Int];
    
    // Options for configuring the Interpreter.
    var tfliteOptions: Interpreter.Options = Interpreter.Options();
    
    // Optional GPU delegate for accleration.
    var gpuDelegate: MetalDelegate? = nil;
    
    // An instance of the driver class to run model inference with Tensorflow Lite.
    var tflite: Interpreter?;
    
    // Image processing memory pool
    var preAllocatedMemoryPool: CVPixelBufferPool?
    
    /// Initializes a Network.
    ///
    /// - Parameters:
    ///     - model: the model considered in the inference process
    ///     - device: CPU, GPU or XNNPACK (neural engine)
    ///     - numThreads: number of threads used tin the inference process
    init(model: Model, device: RuntimeDevice, numThreads: Int) throws {
        do {
            var delegates: [Delegate] = [];
            switch (device) {
            case .XNNPACK:
                tfliteOptions.isXNNPackEnabled = true;
                break;
            case .GPU:
                var gpuOptions = MetalDelegate.Options();
                gpuOptions.isQuantizationEnabled = false;
                gpuOptions.isPrecisionLossAllowed = false;
                tfliteOptions.threadCount = 0;
                gpuOptions.waitType = .none;
                gpuDelegate = MetalDelegate(options: gpuOptions);
                delegates.append(gpuDelegate!);
                break;
            case .CPU:
                tfliteOptions.threadCount = numThreads;
                break;
            }
            let split = model.path.split(separator: "/");
            let index = split.count - 1;
            if index < 0 {
                let modelsInDocumentDirectory = DataLogger.shared.getDocumentDirectoryInformation();
                for modelUrl in modelsInDocumentDirectory {
                    if modelUrl.lastPathComponent.contains(model.name) {
                        tflite = try Interpreter(modelPath: modelUrl.absoluteString.replacingOccurrences(of: "file:///", with: ""), options: tfliteOptions, delegates: delegates);
                        try tflite?.allocateTensors()
                    }
                }
            }
            var fileName = "";
            if index >= 0 {
                fileName = String(split[index]);
            }
            let bundle = Bundle.main
            let path = bundle.path(forResource: fileName, ofType: "")
            if path != nil {
                tflite = try Interpreter(modelPath: path!, options: tfliteOptions, delegates: delegates);
                try tflite?.allocateTensors()
            } else {
                let modelsInDocumentDirectory = DataLogger.shared.getDocumentDirectoryInformation();
                for modelUrl in modelsInDocumentDirectory {
                    if modelUrl.lastPathComponent.contains(model.name) {
                        tflite = try Interpreter(modelPath: modelUrl.absoluteString.replacingOccurrences(of: "file:///", with: ""), options: tfliteOptions, delegates: delegates);
                        try tflite?.allocateTensors()
                    }
                }
            }
            imageSize = model.getInputSize();
            intValues = [Int(imageSize.width) * Int(imageSize.height)];
            
            // Create the pixel buffer pool using the desired format, size, and allocation options
            let allocationOptions = [
                kCVPixelBufferWidthKey: 1920,
                kCVPixelBufferHeightKey: 1920,
                kCVPixelBufferPixelFormatTypeKey: kCVPixelFormatType_32BGRA,
                kCVPixelBufferMetalCompatibilityKey: true,
                kCVPixelBufferIOSurfacePropertiesKey: [:]
            ] as CFDictionary
            
            var pixelBufferPool: CVPixelBufferPool?
            let status = CVPixelBufferPoolCreate(kCFAllocatorDefault, nil, allocationOptions, &pixelBufferPool)
            if status != kCVReturnSuccess {
                print("Error: could not allocate memory pool!")
            }
            self.preAllocatedMemoryPool = pixelBufferPool
        }
    }
    
    /// Returns the RGB data representation of the given image buffer with the specified `byteCount`.
    ///
    /// - Parameters
    ///   - buffer: The BGRA pixel buffer to convert to RGB data.
    ///   - isModelQuantized: Whether the model is quantized (i.e. fixed point values rather than
    ///       floating point values).
    /// - Returns: The RGB data representation of the image buffer or `nil` if the buffer could not be
    ///     converted.
    func rgbDataFromBuffer(
        _ buffer: CVPixelBuffer,
        isModelQuantized: Bool
    ) -> Data? {
        
        // Lock the CVPixelBuffer to get access to its memory.
        CVPixelBufferLockBaseAddress(buffer, .readOnly)
        defer {
            CVPixelBufferUnlockBaseAddress(buffer, .readOnly)
        }
        
        // Get the base address and the bytes per row of the CVPixelBuffer.
        guard let sourceData = CVPixelBufferGetBaseAddress(buffer) else {
            return nil
        }
        let sourceWidth = CVPixelBufferGetWidth(buffer)
        let sourceHeight = CVPixelBufferGetHeight(buffer)
        let sourceBytesPerRow = CVPixelBufferGetBytesPerRow(buffer)
        let destinationChannelCount = 3
        let destinationBytesPerRow = destinationChannelCount * sourceWidth
        
        // Allocate a vImage buffer for source RGBA data.
        var sourceBuffer = vImage_Buffer(data: sourceData,
                                         height: vImagePixelCount(sourceHeight),
                                         width: vImagePixelCount(sourceWidth),
                                         rowBytes: sourceBytesPerRow)
        
        guard let destinationData = malloc(sourceHeight * destinationBytesPerRow) else {
            return nil
        }
        defer {
            free(destinationData)
        }
        
        var destinationBuffer = vImage_Buffer(data: destinationData,
                                              height: vImagePixelCount(sourceHeight),
                                              width: vImagePixelCount(sourceWidth),
                                              rowBytes: destinationBytesPerRow)
        
        // Remove the A channel
        if (CVPixelBufferGetPixelFormatType(buffer) == kCVPixelFormatType_32BGRA) {
            vImageConvert_BGRA8888toRGB888(&sourceBuffer, &destinationBuffer, UInt32(kvImageNoFlags))
        } else if (CVPixelBufferGetPixelFormatType(buffer) == kCVPixelFormatType_32ARGB) {
            vImageConvert_ARGB8888toRGB888(&sourceBuffer, &destinationBuffer, UInt32(kvImageNoFlags))
        }
        
        // Store the transformed image buffer in a Data() container
        let byteData = Data(bytes: destinationBuffer.data, count: destinationBuffer.rowBytes * sourceHeight)
        
        if isModelQuantized {
            return byteData
        } else {
            // Set up the destination buffer for the float values
            var floatBuffer = [Float](repeating: 0.0, count: byteData.count)
            
            // Convert the image buffer to float values
            let stride = vDSP_Stride(1)
            vDSP_vfltu8(destinationBuffer.data, stride, &floatBuffer, stride, vDSP_Length(byteData.count))
            
            // Remove the mean and apply normalization factor
            let length = vDSP_Length(floatBuffer.count)
            var normalizationFactor: Float = 1.0 / getImageStd()
            vDSP_vsbsm(floatBuffer, stride, [Float](repeating: getImageMean(), count: byteData.count), stride, &normalizationFactor, &floatBuffer, stride, length)
            
            // Create a Data object from the float buffer
            return Data(bytes: floatBuffer, count: byteData.count * MemoryLayout<Float>.size)
        }
        
    }
    
    /// Get the image size along the x axis.
    ///
    /// - Returns: number of pixels
    func getImageSizeX() -> Int {
        Int(imageSize.width);
    }
    
    /// Get the image size along the y axis.
    ///
    /// - Returns: number of pixels
    func getImageSizeY() -> Int {
        Int(imageSize.height);
    }
    
    /// Getter function
    ///
    /// - Returns: image normalization mean value
    func getImageMean() -> Float32 {
        0.0;
    }
    
    /// Getter function
    ///
    /// - Returns: image normalization std value
    func getImageStd() -> Float32 {
        255.0;
    }
    
    /// Get the number of bytes that is used to store a single color channel value.
    ///
    /// - Returns: The number of bytes used to store a single color channel value.
    func getNumBytesPerChannel() -> Int {
        0
    }
    
    /// Get boolean that determines if aspect ratio should be preserved when rescaling.
    ///
    /// - Returns: true if aspect ratio should be preserved when rescaling.
    func getMaintainAspect() -> Bool {
        false
    }
}


