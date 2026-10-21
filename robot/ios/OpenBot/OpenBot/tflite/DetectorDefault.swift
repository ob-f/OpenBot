//
// Created by Sparsh Jain on 01/11/22.
//

import Foundation
import TensorFlowLite

/// This TensorFlow Lite detector works with the quantized MobileNet and EfficientDet model.
class DetectorDefault: Detector {

    // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
    // contains the location of detected boxes
    private var outputLocations: UnsafeMutableBufferPointer<Float32>?;
    // outputClasses: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the classes of detected boxes
    private var outputClasses: UnsafeMutableBufferPointer<Float32>?;
    // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the scores of detected boxes
    private var outputScores: UnsafeMutableBufferPointer<Float32>?;
    // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the scores of detected boxes
    private var numDetections: UnsafeMutableBufferPointer<Float32>?;

    // indices in tflite model
    private var outputLocationsIdx: Int = -1;
    private var outputClassesIdx: Int = -1;
    private var outputScoresIdx: Int = -1;
    private var numDetectionsIdx: Int = -1;

    /// Initializes a DetectorDefault.
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
        "labelmap.txt";
    }

    /// Get the number of bytes that is used to store a single color channel value.
    ///
    /// - Returns: The number of bytes used to store a single color channel value.
    override func getNumBytesPerChannel() -> Int {
        1; // the quantized model only uses a single byte
    }

    /// Getter function
    ///
    /// - Returns: number of detections of a given class
    override func getNumDetections() -> Int {
        NUM_DETECTIONS;
    }

    override func parseTFlite() {
        let index0 = try! tflite?.output(at: 0);
        let index1 = try! tflite?.output(at: 1);
        let index2 = try! tflite?.output(at: 2);
        let index3 = try! tflite?.output(at: 3);

        assignIndex(tensor: index0, index: 0);
        assignIndex(tensor: index1, index: 1);
        assignIndex(tensor: index2, index: 2);
        assignIndex(tensor: index3, index: 3);
        NUM_DETECTIONS = try! tflite?.output(at: outputLocationsIdx).shape.dimensions[1] ?? 0;
    }

    /// Index  allocation to browse the tflite object
    ///
    /// - Parameters:
    ///     - tensor: a tensor structure from a tflite object
    ///     - index: the index to be assigned to the tensor
    func assignIndex(tensor: Tensor?, index: Int) {
        switch (tensor?.name) {
        case "TFLite_Detection_PostProcess":
            outputLocationsIdx = index;
            break
        case "TFLite_Detection_PostProcess:1":
            outputClassesIdx = index;
            break
        case "TFLite_Detection_PostProcess:2":
            outputScoresIdx = index;
            break
        case "TFLite_Detection_PostProcess:3":
            numDetectionsIdx = index;
            break
        case "StatefulPartitionedCall:3":
            outputLocationsIdx = index;
            break
        case "StatefulPartitionedCall:2":
            outputClassesIdx = index;
            break
        case "StatefulPartitionedCall:1":
            outputScoresIdx = index;
            break
        case "StatefulPartitionedCall:0":
            numDetectionsIdx = index;
            break
        default:
            break;
        }
    }

    /// Copy data to the input of the neural network
    override func feedData() throws {
        do {

            let outputLocationsTensor = try tflite?.output(at: outputLocationsIdx);
            let outputClassesTensor = try tflite?.output(at: outputClassesIdx);
            let outputScoresTensor = try tflite?.output(at: outputScoresIdx);
            let numDetectionsTensor = try tflite?.output(at: numDetectionsIdx);

            let outputSize = outputLocationsTensor?.shape.dimensions.reduce(1, { x, y in x * y }) ?? 0
            outputLocations = UnsafeMutableBufferPointer<Float32>.allocate(capacity: outputSize)
            _ = outputLocationsTensor?.data.copyBytes(to: outputLocations!);

            let outputClassesTensorSize = outputClassesTensor?.shape.dimensions.reduce(1, { x, y in x * y }) ?? 0
            outputClasses = UnsafeMutableBufferPointer<Float32>.allocate(capacity: outputClassesTensorSize)
            _ = outputClassesTensor?.data.copyBytes(to: outputClasses!);

            let outputScoresTensorSize = outputScoresTensor?.shape.dimensions.reduce(1, { x, y in x * y }) ?? 0
            outputScores = UnsafeMutableBufferPointer<Float32>.allocate(capacity: outputScoresTensorSize)
            _ = outputScoresTensor?.data.copyBytes(to: outputScores!);

            let numDetectionsTensorSize = numDetectionsTensor?.shape.dimensions.reduce(1, { x, y in x * y }) ?? 0
            numDetections = UnsafeMutableBufferPointer<Float32>.allocate(capacity: numDetectionsTensorSize)
            _ = numDetectionsTensor?.data.copyBytes(to: numDetections!);

        } catch {
            print("error:\(error)")
        }
    }

    /// Show the best detections, after scaling them back to the input size.
    ///
    /// - Parameters:
    ///     - className:
    ///     - width:
    ///     - height:
    /// - Returns: an array of "Recognition" objects containing
    override func getRecognitions(className: String, width: Int, height: Int) -> [Recognition] {
        var recognitions: [Recognition] = [];
        for i in 0..<getNumDetections() {
            let xPos = CGFloat(outputLocations![(4 * i) + 1]) * CGFloat(getImageSizeX());
            let yPos = CGFloat(outputLocations![(4 * i)]) * CGFloat(getImageSizeY());
            let w = CGFloat(outputLocations![(4 * i) + 3]) * CGFloat(getImageSizeX()) - xPos;
            let h = CGFloat(outputLocations![(4 * i) + 2]) * CGFloat(getImageSizeY()) - yPos;
            let scaleX = CGFloat(width) / CGFloat(getImageSizeX())
            let scaleY = CGFloat(height) / CGFloat(getImageSizeY())
            //let scale = min(scaleX, scaleY)
            let dx = (CGFloat(width) - scaleX * CGFloat(getImageSizeX())) / 2
            let dy = (CGFloat(height) - scaleY * CGFloat(getImageSizeY())) / 2
            let transform = CGAffineTransform.identity.translatedBy(x: dx, y: dy).scaledBy(x: scaleX, y: scaleY)
            let detection = CGRect(x: xPos, y: yPos, width: w, height: h).applying(transform);
            // SSD Mobilenet V1 Model assumes class 0 is background class
            // in label file and class labels start from 1 to number_of_classes+1,
            // while outputClasses correspond to class index from 0 to number_of_classes
            let classId: Int = Int(outputClasses![i]);
            let labelId: Int = classId + 1;
            if (className == labels[labelId]) {
                recognitions.append(Recognition(id: String(i), title: labels[labelId], confidence: outputScores![i], location: detection, classId: classId));
            }

        }
        // Execute non-maximum suppression
        return nms(recognitions: recognitions);
    }


    override func getMultipleRecognitions(classA: String, classB: String, width: Int, height: Int) -> [Recognition] {
        var recognitions: [Recognition] = [];
        for i in 0..<getNumDetections() {
            let xPos = CGFloat(outputLocations![(4 * i) + 1]) * CGFloat(getImageSizeX());
            let yPos = CGFloat(outputLocations![(4 * i)]) * CGFloat(getImageSizeY());
            let w = CGFloat(outputLocations![(4 * i) + 3]) * CGFloat(getImageSizeX()) - xPos;
            let h = CGFloat(outputLocations![(4 * i) + 2]) * CGFloat(getImageSizeY()) - yPos;
            let scaleX = CGFloat(width) / CGFloat(getImageSizeX())
            let scaleY = CGFloat(height) / CGFloat(getImageSizeY())
            //let scale = min(scaleX, scaleY)
            let dx = (CGFloat(width) - scaleX * CGFloat(getImageSizeX())) / 2
            let dy = (CGFloat(height) - scaleY * CGFloat(getImageSizeY())) / 2
            let transform = CGAffineTransform.identity.translatedBy(x: dx, y: dy).scaledBy(x: scaleX, y: scaleY)
            let detection = CGRect(x: xPos, y: yPos, width: w, height: h).applying(transform);
            // SSD Mobilenet V1 Model assumes class 0 is background class
            // in label file and class labels start from 1 to number_of_classes+1,
            // while outputClasses correspond to class index from 0 to number_of_classes
            let classId: Int = Int(outputClasses![i]);
            let labelId: Int = classId + 1;
            if (classA == labels[labelId] || classB == labels[labelId]) {
                recognitions.append(Recognition(id: String(i), title: labels[labelId], confidence: outputScores![i], location: detection, classId: classId));
            }
        }
        // Execute non-maximum suppression
        return nms(recognitions: recognitions);
    }

    override func getAllRecognitions(width: Int, height: Int) -> [Recognition] {
        var recognitions: [Recognition] = [];
        for i in 0..<getNumDetections() {
            let xPos = CGFloat(outputLocations![(4 * i) + 1]) * CGFloat(getImageSizeX());
            let yPos = CGFloat(outputLocations![(4 * i)]) * CGFloat(getImageSizeY());
            let w = CGFloat(outputLocations![(4 * i) + 3]) * CGFloat(getImageSizeX()) - xPos;
            let h = CGFloat(outputLocations![(4 * i) + 2]) * CGFloat(getImageSizeY()) - yPos;
            let scaleX = CGFloat(width) / CGFloat(getImageSizeX())
            let scaleY = CGFloat(height) / CGFloat(getImageSizeY())
            //let scale = min(scaleX, scaleY)
            let dx = (CGFloat(width) - scaleX * CGFloat(getImageSizeX())) / 2
            let dy = (CGFloat(height) - scaleY * CGFloat(getImageSizeY())) / 2
            let transform = CGAffineTransform.identity.translatedBy(x: dx, y: dy).scaledBy(x: scaleX, y: scaleY)
            let detection = CGRect(x: xPos, y: yPos, width: w, height: h).applying(transform);
            // SSD Mobilenet V1 Model assumes class 0 is background class
            // in label file and class labels start from 1 to number_of_classes+1,
            // while outputClasses correspond to class index from 0 to number_of_classes
            let classId: Int = Int(outputClasses![i]);
            let labelId: Int = classId + 1;
            recognitions.append(Recognition(id: String(i), title: labels[labelId], confidence: outputScores![i], location: detection, classId: classId));
        }
        // Execute non-maximum suppression
        return nms(recognitions: recognitions);
    }
}
