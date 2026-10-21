//
// Created by Sparsh Jain on 29/09/22.
//

import Foundation
import TensorFlowLite

class Autopilot: Network {
    var cmdIndex: Int = -1
    var imgIndex: Int = -1
    let IMAGE_MEAN: Float = 0.0
    let IMAGE_STD: Float = 255.0

    /// Initializes an Autopilot.
    ///
    /// - Parameters:
    ///     - model: the model considered in the inference process
    ///     - device: CPU, GPU or XNNPACK (neural engine)
    ///     - numThreads: number of threads used tin the inference process
    override init(model: Model, device: RuntimeDevice, numThreads: Int) {
        try! super.init(model: model, device: device, numThreads: numThreads)
        do {
            let index0 = try tflite?.input(at: 0)
            let index1 = try tflite?.input(at: 1)
            assignIndex(tensor: index0, index: 0)
            assignIndex(tensor: index1, index: 1)
        } catch {
            print("Error:\(error)")
        }
    }

    /// Getter function
    ///
    /// - Returns: image normalization mean value
    override func getImageMean() -> Float {
        IMAGE_MEAN
    }

    /// Getter function
    ///
    /// - Returns: image normalization std value
    override func getImageStd() -> Float {
        IMAGE_STD
    }

    /// Index  allocation to browse the tflite object
    ///
    /// - Parameters:
    ///     - tensor: a tensor structure from a tflite object
    ///     - index: the index to be assigned to the tensor
    func assignIndex(tensor: Tensor?, index: Int) {
        switch (tensor?.name) {
        case "serving_default_cmd_input:0", "cmd_input":
            cmdIndex = index
            break
        case "serving_default_img_input:0", "img_input":
            imgIndex = index
            break
        default:
            break
        }
    }

    /// Propagate an image and high-level command through a neural network to generate suitable control output.
    ///
    /// - Parameters
    ///   - pixelBuffer: The buffer containing the image.
    ///   - indicator: A high-level scalar command (such that: -1: left, 0: forward, +1: right) to be sent as an input to the driving policy
    /// - Returns: A control reference to be sent to the robot
    func recognizeImage(pixelBuffer: CVPixelBuffer, indicator: Float) -> Control {

        // Crops the image to the biggest square in the center and scales it down to model dimensions.
        let scaledSize = CGSize(width: getImageSizeX(), height: getImageSizeY())
        guard let scaledPixelBuffer = pixelBuffer.resized(to: scaledSize, preserveAspectRatio: false, with: self.preAllocatedMemoryPool!) else {
            return Control(left: 0, right: 0)
        }

        do {
            // Get indicator data.
            var indicatorData: Data = Data()
            indicatorData.append(contentsOf: indicator.bytes)

            // Pre-process input image.
            let inputTensor = try tflite!.input(at: imgIndex)
            guard let rgbData = rgbDataFromBuffer(scaledPixelBuffer, isModelQuantized: inputTensor.dataType == .uInt8) else {
                return Control(left: 0, right: 0)
            }

            // Copy the input data into TensorFlow.
            try tflite?.copy(indicatorData, toInputAt: cmdIndex)
            try tflite?.copy(rgbData, toInputAt: imgIndex)

            // Run the inference call.
            try tflite?.invoke()

            // Post-Processing.
            let outputTensor = try tflite?.output(at: 0)
            let outputSize = outputTensor?.shape.dimensions.reduce(1, { x, y in x * y }) ?? 0
            let outputData = UnsafeMutableBufferPointer<Float32>.allocate(capacity: outputSize)
            _ = outputTensor?.data.copyBytes(to: outputData)
            return Control(left: outputData[0], right: outputData[1])

        } catch {
            print("error:\(error)")
            return Control(left: 0, right: 0)
        }
    }
}
