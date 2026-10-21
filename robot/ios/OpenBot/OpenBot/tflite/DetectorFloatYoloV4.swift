//
// Created by Sparsh Jain on 01/11/22.
//

import Foundation
import TensorFlowLite

class DetectorFloatYoloV4: Detector {

    // Additional normalization of the used input.
    let IMAGE_MEAN: Float = 0.0;
    let IMAGE_STD: Float = 255.0;

    // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
    // contains the location of detected boxes
    private var outputLocations: UnsafeMutableBufferPointer<Float32>?;
    // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the scores of detected boxes
    private var outputScores: UnsafeMutableBufferPointer<Float32>?;

    // Indices in tflite model
    private var outputLocationsIdx: Int = -1;
    private var outputScoresIdx: Int = -1;

    /// Initialization of a DetectorYoloV4.
    ///
    /// - Parameters:
    ///     - model: the model considered in the inference process
    ///     - device: CPU, GPU or XNNPACK (neural engine)
    ///     - numThreads: number of threads used tin the inference process
    override init(model: Model, device: RuntimeDevice, numThreads: Int) throws {
        try super.init(model: model, device: device, numThreads: numThreads)
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
        4;
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

    /// This function will parse a .tflite neural model and
    override func parseTFlite() {
        let index0 = try! tflite?.output(at: 0);
        let index1 = try! tflite?.output(at: 1);
        assignIndex(tensor: index0, index: 0);
        assignIndex(tensor: index1, index: 1);
        NUM_DETECTIONS = try! tflite?.output(at: outputLocationsIdx).shape.dimensions[1] ?? 0;
        let outputSize = try! tflite?.output(at: outputLocationsIdx).data.count ?? 0
        outputLocations = UnsafeMutableBufferPointer<Float32>.allocate(capacity: outputSize)
        let outputScoresTensorSize = try! tflite?.output(at: outputScoresIdx).data.count ?? 0
        outputScores = UnsafeMutableBufferPointer<Float32>.allocate(capacity: outputScoresTensorSize)
    }

    /// Index  allocation to browse the tflite object
    ///
    /// - Parameters:
    ///     - tensor: a tensor structure from a tflite object
    ///     - index: the index to be assigned to the tensor
    func assignIndex(tensor: Tensor?, index: Int) {
        switch (tensor?.name) {
        case "Identity", "StatefulPartitionedCall:0":
            outputLocationsIdx = index;
            break
        case "Identity_1", "StatefulPartitionedCall:1":
            outputScoresIdx = index;
            break
        default:
            break;
        }
    }

    /// Copy data to the input of the neural network
    override func feedData() throws {
        _ = try! tflite?.output(at: outputLocationsIdx).data.copyBytes(to: outputLocations!);
        _ = try! tflite?.output(at: outputScoresIdx).data.copyBytes(to: outputScores!);
    }

    /// Query the output of the neural network and perform post-processing actions
    ///
    /// - Parameters:
    ///     - className: name of the class to be detected by the neural network
    ///     - width: width of the
    ///     - height: height of he
    /// - Returns: an array of "Recognition" structures, containing the pixel coordinates, bounding boxes and classes of the different network detections
    override func getRecognitions(className: String, width: Int, height: Int) -> [Recognition] {
        var recognitions: [Recognition] = [];
        var outputScores2D = Array(repeating: Array<Float32>(repeating: 0, count: labels.count), count: NUM_DETECTIONS);
        for a in 0..<NUM_DETECTIONS {
            for b in 0..<labels.count {
                outputScores2D[a][b] = outputScores![a * labels.count + b];
            }
        }

        if (NUM_DETECTIONS > 0) {
            for i in 0..<NUM_DETECTIONS {
                var maxClass: Float = 0;
                var classId = -1;
                let classes = outputScores2D[i];

                for c in 0..<classes.count {
                    if (classes[c] > maxClass) {
                        classId = c;
                        maxClass = classes[c];
                    }
                }

                if (classId > -1 && className == labels[classId]) {
                    let xPos = outputLocations![4 * i];
                    let yPos = outputLocations![4 * i + 1];
                    let w = outputLocations![4 * i + 2];
                    let h = outputLocations![4 * i + 3];
                    let scaleX = CGFloat(width) / CGFloat(getImageSizeX())
                    let scaleY = CGFloat(height) / CGFloat(getImageSizeY())
                    //let scale = min(scaleX, scaleY)
                    let dx = (CGFloat(width) - scaleX * CGFloat(getImageSizeX())) / 2
                    let dy = (CGFloat(height) - scaleY * CGFloat(getImageSizeY())) / 2
                    let transform = CGAffineTransform.identity.translatedBy(x: dx, y: dy).scaledBy(x: scaleX, y: scaleY)
                    let detection = CGRect(x: CGFloat(max(0, xPos - w / 2)), y: CGFloat(max(0, yPos - h / 2)), width: CGFloat(w), height: CGFloat(h)).applying(transform);
                    recognitions.append(Recognition(id: String(i), title: labels[classId], confidence: maxClass, location: detection, classId: classId));
                }
            }
        }
        // Execute non-maximum suppression 
        return nms(recognitions: recognitions);
    }

    override func getMultipleRecognitions(classA: String, classB: String, width: Int, height: Int) -> [Recognition] {
        var recognitions: [Recognition] = [];
        var outputScores2D = Array(repeating: Array<Float32>(repeating: 0, count: labels.count), count: NUM_DETECTIONS);
        for a in 0..<NUM_DETECTIONS {
            for b in 0..<labels.count {
                outputScores2D[a][b] = outputScores![a * labels.count + b];
            }
        }

        if (NUM_DETECTIONS > 0) {
            for i in 0..<NUM_DETECTIONS {
                var maxClass: Float = 0;
                var classId = -1;
                let classes = outputScores2D[i];

                for c in 0..<classes.count {
                    if (classes[c] > maxClass) {
                        classId = c;
                        maxClass = classes[c];
                    }
                }

                if (classId > -1 && (classA == labels[classId] || classB == labels[classId])) {
                    let xPos = outputLocations![4 * i];
                    let yPos = outputLocations![4 * i + 1];
                    let w = outputLocations![4 * i + 2];
                    let h = outputLocations![4 * i + 3];
                    let scaleX = CGFloat(width) / CGFloat(getImageSizeX())
                    let scaleY = CGFloat(height) / CGFloat(getImageSizeY())
                    //let scale = min(scaleX, scaleY)
                    let dx = (CGFloat(width) - scaleX * CGFloat(getImageSizeX())) / 2
                    let dy = (CGFloat(height) - scaleY * CGFloat(getImageSizeY())) / 2
                    let transform = CGAffineTransform.identity.translatedBy(x: dx, y: dy).scaledBy(x: scaleX, y: scaleY)
                    let detection = CGRect(x: CGFloat(max(0, xPos - w / 2)), y: CGFloat(max(0, yPos - h / 2)), width: CGFloat(w), height: CGFloat(h)).applying(transform);
                    recognitions.append(Recognition(id: String(i), title: labels[classId], confidence: maxClass, location: detection, classId: classId));
                }
            }
        }
        // Execute non-maximum suppression
        return nms(recognitions: recognitions);
    }

    override func getAllRecognitions(width: Int, height: Int) -> [Recognition] {
        var recognitions: [Recognition] = [];
        var outputScores2D = Array(repeating: Array<Float32>(repeating: 0, count: labels.count), count: NUM_DETECTIONS);
        for a in 0..<NUM_DETECTIONS {
            for b in 0..<labels.count {
                outputScores2D[a][b] = outputScores![a * labels.count + b];
            }
        }
        if (NUM_DETECTIONS > 0) {
            for i in 0..<NUM_DETECTIONS {
                var maxClass: Float = 0;
                var classId = -1;
                let classes = outputScores2D[i];
                for c in 0..<classes.count {
                    if (classes[c] > maxClass) {
                        classId = c;
                        maxClass = classes[c];
                    }
                }
                if (classId > -1) {
                    let xPos = outputLocations![4 * i];
                    let yPos = outputLocations![4 * i + 1];
                    let w = outputLocations![4 * i + 2];
                    let h = outputLocations![4 * i + 3];
                    let scaleX = CGFloat(width) / CGFloat(getImageSizeX())
                    let scaleY = CGFloat(height) / CGFloat(getImageSizeY())
                    //let scale = min(scaleX, scaleY)
                    let dx = (CGFloat(width) - scaleX * CGFloat(getImageSizeX())) / 2
                    let dy = (CGFloat(height) - scaleY * CGFloat(getImageSizeY())) / 2
                    let transform = CGAffineTransform.identity.translatedBy(x: dx, y: dy).scaledBy(x: scaleX, y: scaleY)
                    let detection = CGRect(x: CGFloat(max(0, xPos - w / 2)), y: CGFloat(max(0, yPos - h / 2)), width: CGFloat(w), height: CGFloat(h)).applying(transform);
                    recognitions.append(Recognition(id: String(i), title: labels[classId], confidence: maxClass, location: detection, classId: classId));
                }

            }
        }
        // Execute non-maximum suppression
        return nms(recognitions: recognitions);
    }
}
