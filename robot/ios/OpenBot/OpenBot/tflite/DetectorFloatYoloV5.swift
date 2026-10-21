//
//  Created by Quentin Leboutet on 15.01.23.
//

import Foundation
import TensorFlowLite

class DetectorFloatYoloV5: Detector {

    // Additional normalization of the used input.
    let IMAGE_MEAN: Float = 0.0;
    let IMAGE_STD: Float = 255.0;
    private var inputSize: Int = -1
    private var output_box: Int = -1
    var numClass: Int = -1
    var isModelQuantized: Bool = false
    private var inputScale: Float = 0
    private var inputZeroPoint: Int = -1
    private var outputScale: Float = 0
    private var outputZeroPoint: Int = -1
    private var outData: UnsafeMutableBufferPointer<UInt8>?;

    /// Initialization of a DetectorYoloV5.
    ///
    /// - Parameters:
    ///     - model: the model considered in the inference process
    ///     - device: CPU, GPU or XNNPACK (neural engine)
    ///     - numThreads: number of threads used tin the inference process
    override init(model: Model, device: RuntimeDevice, numThreads: Int) throws {
        try super.init(model: model, device: device, numThreads: numThreads)
        inputSize = self.getImageSizeX()
        let tmp_0: Float = pow((Float(inputSize) / 32), 2) * 3
        let tmp_1: Float = pow((Float(inputSize) / 16), 2) * 3
        let tmp_2: Float = pow((Float(inputSize) / 8), 2) * 3
        output_box = Int(tmp_0 + tmp_1 + tmp_2)
    }

    /// Get boolean that determines if aspect ratio should be preserved when rescaling.
    ///
    /// - Returns: true if aspect ratio should be preserved when rescaling.
    override func getMaintainAspect() -> Bool {
        false;
    }

    /// Getter function
    ///
    /// - Returns: path of the file containing the diferent labels
    override func getLabelPath() -> String {
        "coco.txt";
    }

    /// Get the number of bytes that is used to store a single color channel value.
    ///
    /// - Returns: The number of bytes used to store a single color channel value.
    override func getNumBytesPerChannel() -> Int {
        parseTFlite();
        return isModelQuantized ? 1 : 4;
    }

    /// Getter function
    ///
    /// - Returns: number of detections of a given class
    override func getNumDetections() -> Int {
        NUM_DETECTIONS;
    }

    /// Getter function
    ///
    /// - Returns: image normalization mean value
    override func getImageMean() -> Float {
        IMAGE_MEAN;
    }

    /// Getter function
    ///
    /// - Returns: image normalization std value
    override func getImageStd() -> Float {
        IMAGE_STD;
    }

    override func parseTFlite() {
        inputScale = try! tflite?.input(at: 0).quantizationParameters?.scale ?? 0
        inputZeroPoint = try! tflite?.input(at: 0).quantizationParameters?.zeroPoint ?? 0
        outputScale = try! tflite?.output(at: 0).quantizationParameters?.scale ?? 0
        outputZeroPoint = try! tflite?.output(at: 0).quantizationParameters?.zeroPoint ?? 0
        isModelQuantized = (Int(outputScale) + outputZeroPoint) != 0
        let shape = try! tflite?.output(at: 0).shape.dimensions
        numClass = shape![shape!.count - 1] - 5
        let outputSize = try! tflite?.output(at: 0).data.count ?? 0
        outData = UnsafeMutableBufferPointer<UInt8>.allocate(capacity: outputSize)
    }

    /// Copy data to the input of the neural network
    override func feedData() throws {
        _ = try! tflite?.output(at: 0).data.copyBytes(to: outData!);
    }

    /// Query the output of the neural network and perform post-processing actions
    ///
    /// - Parameters:
    ///     - className: name of the class to be detected by the neural network
    ///     - width: width of the
    ///     - height: height of he
    /// - Returns: an array of "Recognition" structures, containing the pixel coordinates, bounding boxes and classes of the different network detections
    override func getRecognitions(className: String, width: Int, height: Int) -> [Recognition] {

        var out = Array(repeating: Array<Float32>(repeating: 0, count: numClass + 5), count: output_box);
        var recognitions: [Recognition] = [];
        var classes = Array<Float32>(repeating: 0, count: numClass);
        let output_channels: Int = numClass + 5

        for i in (0..<output_box) {
            for j in (0..<numClass + 5) {
                if (isModelQuantized) {
                    out[i][j] = outputScale * Float(Int((outData![i * output_channels + j] & 0xFF)) - outputZeroPoint)
                } else {
                    out[i][j] = Float(outData![i * output_channels + j])
                }
            }

            // Denormalize xywh
            for j in (0..<4) {
                out[i][j] *= Float(inputSize)
            }
        }

        for i in (0..<output_box) {
            let confidence: Float = out[i][4]
            var classId = -1;
            var maxClass: Float = 0;

            for c in (0..<labels.count) {
                classes[c] = out[i][5 + c]
            }

            for c in (0..<labels.count) {
                if (classes[c] > maxClass) {
                    classId = c;
                    maxClass = classes[c];
                }
            }

            let score: Float = maxClass * confidence
            if (score > getObjThresh()) {
                if (classId > -1 && className == labels[classId]) {
                    let xPos = out[i][0]
                    let yPos = out[i][1]
                    let w = out[i][2]
                    let h = out[i][3]
                    let scaleX = CGFloat(width) / CGFloat(getImageSizeX())
                    let scaleY = CGFloat(height) / CGFloat(getImageSizeY())
                    //let scale = min(scaleX, scaleY)
                    let dx = (CGFloat(width) - scaleX * CGFloat(getImageSizeX())) / 2
                    let dy = (CGFloat(height) - scaleY * CGFloat(getImageSizeY())) / 2
                    let transform = CGAffineTransform.identity.translatedBy(x: dx, y: dy).scaledBy(x: scaleX, y: scaleY)
                    let detection = CGRect(x: CGFloat(max(0, xPos - w / 2)), y: CGFloat(max(0, yPos - h / 2)), width: CGFloat(w), height: CGFloat(h)).applying(transform);
                    recognitions.append(Recognition(id: String(i), title: labels[classId], confidence: score, location: detection, classId: classId));
                }
            }
        }
        // Execute non-maximum suppression 
        return nms(recognitions: recognitions);
    }


    override func getMultipleRecognitions(classA: String, classB: String, width: Int, height: Int) -> [Recognition] {
        var out = Array(repeating: Array<Float32>(repeating: 0, count: numClass + 5), count: output_box);
        var recognitions: [Recognition] = [];
        var classes = Array<Float32>(repeating: 0, count: numClass);
        let output_channels: Int = numClass + 5

        for i in (0..<output_box) {
            for j in (0..<numClass + 5) {
                if (isModelQuantized) {
                    out[i][j] = outputScale * Float(Int((outData![i * output_channels + j] & 0xFF)) - outputZeroPoint)
                } else {
                    out[i][j] = Float(outData![i * output_channels + j])
                }
            }

            // Denormalize xywh
            for j in (0..<4) {
                out[i][j] *= Float(inputSize)
            }
        }

        for i in (0..<output_box) {
            let confidence: Float = out[i][4]
            var classId = -1;
            var maxClass: Float = 0;

            for c in (0..<labels.count) {
                classes[c] = out[i][5 + c]
            }

            for c in (0..<labels.count) {
                if (classes[c] > maxClass) {
                    classId = c;
                    maxClass = classes[c];
                }
            }

            let score: Float = maxClass * confidence
            if (score > getObjThresh()) {
                if (classId > -1 && (classA == labels[classId] || classB == labels[classId])) {
                    let xPos = out[i][0]
                    let yPos = out[i][1]
                    let w = out[i][2]
                    let h = out[i][3]
                    let scaleX = CGFloat(width) / CGFloat(getImageSizeX())
                    let scaleY = CGFloat(height) / CGFloat(getImageSizeY())
                    //let scale = min(scaleX, scaleY)
                    let dx = (CGFloat(width) - scaleX * CGFloat(getImageSizeX())) / 2
                    let dy = (CGFloat(height) - scaleY * CGFloat(getImageSizeY())) / 2
                    let transform = CGAffineTransform.identity.translatedBy(x: dx, y: dy).scaledBy(x: scaleX, y: scaleY)
                    let detection = CGRect(x: CGFloat(max(0, xPos - w / 2)), y: CGFloat(max(0, yPos - h / 2)), width: CGFloat(w), height: CGFloat(h)).applying(transform);
                    recognitions.append(Recognition(id: String(i), title: labels[classId], confidence: score, location: detection, classId: classId));
                }
            }
        }
        // Execute non-maximum suppression
        return nms(recognitions: recognitions);
    }

    override func getAllRecognitions(width: Int, height: Int) -> [Recognition] {
        var out = Array(repeating: Array<Float32>(repeating: 0, count: numClass + 5), count: output_box);
        var recognitions: [Recognition] = [];
        var classes = Array<Float32>(repeating: 0, count: numClass);
        let output_channels: Int = numClass + 5

        for i in (0..<output_box) {
            for j in (0..<numClass + 5) {
                if (isModelQuantized) {
                    out[i][j] = outputScale * Float(Int((outData![i * output_channels + j] & 0xFF)) - outputZeroPoint)
                } else {
                    out[i][j] = Float(outData![i * output_channels + j])
                }
            }

            // Denormalize xywh
            for j in (0..<4) {
                out[i][j] *= Float(inputSize)
            }
        }

        for i in (0..<output_box) {
            let confidence: Float = out[i][4]
            var classId = -1;
            var maxClass: Float = 0;

            for c in (0..<labels.count) {
                classes[c] = out[i][5 + c]
            }

            for c in (0..<labels.count) {
                if (classes[c] > maxClass) {
                    classId = c;
                    maxClass = classes[c];
                }
            }
            let score: Float = maxClass * confidence
            if (score > getObjThresh()) {
                if (classId > -1) {
                    let xPos = out[i][0]
                    let yPos = out[i][1]
                    let w = out[i][2]
                    let h = out[i][3]
                    let scaleX = CGFloat(width) / CGFloat(getImageSizeX())
                    let scaleY = CGFloat(height) / CGFloat(getImageSizeY())
                    //let scale = min(scaleX, scaleY)
                    let dx = (CGFloat(width) - scaleX * CGFloat(getImageSizeX())) / 2
                    let dy = (CGFloat(height) - scaleY * CGFloat(getImageSizeY())) / 2
                    let transform = CGAffineTransform.identity.translatedBy(x: dx, y: dy).scaledBy(x: scaleX, y: scaleY)
                    let detection = CGRect(x: CGFloat(max(0, xPos - w / 2)), y: CGFloat(max(0, yPos - h / 2)), width: CGFloat(w), height: CGFloat(h)).applying(transform);
                    recognitions.append(Recognition(id: String(i), title: labels[classId], confidence: score, location: detection, classId: classId));
                }
            }
        }
        // Execute non-maximum suppression
        return nms(recognitions: recognitions);
    }

}
