//
// Created by Sparsh Jain on 01/11/22.
//

import Foundation
import UIKit

/// Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
/// github.com/tensorflow/models/tree/master/research/object_detection
class Detector: Network {
    var labels: [String] = []; // Labels corresponding to the output of the vision model.
    var NUM_DETECTIONS: Int = 0; // Number of output detections
    var selectedClass: String?;
    var selectedMultipleClass: [String] = [];
    let ciContext = CIContext()
    let imageSizeX: Int = -1;
    let imageSizeY: Int = -1;

    /// Creates a detector with the provided configuration.
    ///
    /// - Parameters
    ///   - model: The model to use for classification.
    ///   - device: The device to use for classification.
    ///   - numThreads: The number of threads to use for classification.
    /// - Returns: A detector with the desired configuration.
    ///
    static func create(model: Model, device: RuntimeDevice, numThreads: Int) throws -> AnyObject? {
        switch (model.classType) {
        case .MOBILENET, .EFFICIENTDET:
            return try DetectorDefault(model: model, device: device, numThreads: numThreads);
        case .YOLOV4:
            return try DetectorFloatYoloV4(model: model, device: device, numThreads: numThreads);
        case .YOLOV5:
            return try DetectorFloatYoloV5(model: model, device: device, numThreads: numThreads);
        default:
            return nil;
        }
    }

    /// An immutable result returned by a Classifier/Detector describing what was recognized.
    class Recognition {

        // A unique identifier for what has been recognized. Specific to the class, not the instance of the object.
        private var id: String;

        // Display name for the recognition.
        private var title: String;

        // A sortable score for how good the recognition is relative to others. Higher should be better.
        private var confidence: Float;

        // Location within the source image for the location of the recognized object.
        private var location: CGRect;

        // Detected class of the recognized object.
        private var classId: Int;

        /// Initialization routine
        ///
        /// - Parameters:
        ///     - id:
        ///     - title:
        ///     - confidence:
        ///     - location:
        ///     - classId:
        init(id: String, title: String, confidence: Float, location: CGRect, classId: Int) {
            self.id = id;
            self.title = title;
            self.confidence = confidence;
            self.location = location;
            self.classId = classId;
        }

        /// Compare two Recognition objects in terms of detection confidence
        ///
        /// - Parameters:
        ///     - lhs: first Recognition object
        ///     - rhs: second Recognition object
        static func >(lhs: Recognition, rhs: Recognition) -> Bool {
            lhs.confidence > rhs.confidence
        }

        /// Getter function.
        ///
        /// - Returns: id of the recognition
        public func getId() -> String {
            id;
        }

        /// Getter function.
        ///
        /// - Returns: class Id
        public func getClassId() -> Int {
            classId;
        }

        /// Getter function.
        ///
        /// - Returns: name of the recognition
        public func getTitle() -> String {
            title;
        }

        /// Getter function.
        ///
        /// - Returns: confidence of the recognition
        public func getConfidence() -> Float {
            confidence;
        }

        /// Getter function.
        ///
        /// - Returns: bounding box of the recognition
        public func getLocation() -> CGRect {
            location;
        }

        /// Setter function.
        ///
        /// - Parameters: bounding box of the recognition
        public func setLocation(location: CGRect) {
            self.location = location;
        }
    }

    /// Initializes a Detector.
    ///
    /// - Parameters:
    ///     - model: the model considered in the inference process
    ///     - device: CPU, GPU or XNNPACK (neural engine)
    ///     - numThreads: number of threads used tin the inference process
    override init(model: Model, device: RuntimeDevice, numThreads: Int) throws {
        try super.init(model: model, device: device, numThreads: numThreads);
        NotificationCenter.default.addObserver(self, selector: #selector(updateObject), name: .updateObject, object: nil)
        labels = loadLabelList(filePath: getLabelPath());
        selectedClass = labels.first {
            $0.capitalized == "person".capitalized
        };
        selectedMultipleClass = ["person"]
        parseTFlite();
    }

    /// Reads label list from Assets.
    ///
    /// - Parameters: path of the text file containig the labels.
    /// - Returns: list of labels contained in the file.
    func loadLabelList(filePath: String) -> [String] {
        var result: [String] = []
        if let filepath = Bundle.main.path(forResource: filePath, ofType: "") {
            do {
                let contents = try String(contentsOfFile: filepath)
                let output = contents.components(separatedBy: "\n")
                for label in output {
                    if (label != " " && label != "") {
                        result.append(label);
                    }
                }
            } catch {
                print("cannot convert file content to string");
            }
        } else {
            print("labelmap.txt or coco.txt not found");
        }
        return result
    }

    /// Propagate an image through a neural network to recognize objects of a predefined class.
    ///
    /// - Parameters: The buffer containing the image.
    /// - Returns: A list of the recognized objects for a given label
    func recognizeImage(pixelBuffer: CVPixelBuffer, detectionType: String) -> [Recognition] {

        let imageWidth = CVPixelBufferGetWidth(pixelBuffer)
        let imageHeight = CVPixelBufferGetHeight(pixelBuffer)
        let sourcePixelFormat = CVPixelBufferGetPixelFormatType(pixelBuffer)
        assert(sourcePixelFormat == kCVPixelFormatType_32ARGB ||
                sourcePixelFormat == kCVPixelFormatType_32BGRA ||
                sourcePixelFormat == kCVPixelFormatType_32RGBA)

        // Crops the image to the biggest square in the center and scales it down to model dimensions.
        let scaledSize = CGSize(width: getImageSizeX(), height: getImageSizeY())
        guard let scaledPixelBuffer = pixelBuffer.resized(to: scaledSize, preserveAspectRatio: false, with: self.preAllocatedMemoryPool!) else {
            return []
        }

        do {

            // Pre-process input image (orientation, scaling, cropping...).
            let inputTensor = try tflite!.input(at: 0);
            guard let rgbData = rgbDataFromBuffer(scaledPixelBuffer, isModelQuantized: inputTensor.dataType == .uInt8) else {
                return []
            }

            // Copy the input data into TensorFlow.
            try tflite?.copy(rgbData, toInputAt: 0);
            try feedData();

            // Run the inference call.
            try tflite?.invoke();

            if (detectionType == "multiple") {
                return getMultipleRecognitions(classA: selectedMultipleClass[0], classB: selectedMultipleClass[1], width: imageWidth, height: imageHeight)
            } else if (detectionType == "all") {
                return getAllRecognitions(width: imageWidth, height: imageHeight)
            } else {
                return getRecognitions(className: selectedClass!, width: imageWidth, height: imageHeight);
            }
            // Post-Processing.

        } catch {
            print("error:\(error)")
            if (detectionType == "multiple") {
                return getMultipleRecognitions(classA: selectedMultipleClass[0], classB: selectedMultipleClass[1], width: imageWidth, height: imageHeight)
            } else if (detectionType == "all") {
                return getAllRecognitions(width: imageWidth, height: imageHeight)
            } else {
                return getRecognitions(className: selectedClass!, width: imageWidth, height: imageHeight);
            }
        };
    }

    var mNmsThresh: Float = 0.25;

    func getObjThresh() -> Float {
        // TODO: return ObjectTrackingFragment.MINIMUM_CONFIDENCE_TF_OD_API
        0.5;
    }

    /// Non maximum suppression
    ///
    /// - Parameters: An array of "Recognition" objects, built from the output of the neural detector.
    /// - Returns: Filtered array of "Recognition" objects.
    func nms(recognitions: [Recognition]) -> [Recognition] {

        var nmsList: [Recognition] = [];
        var pq = recognitions;
        pq.sort(by: >);
        while (pq.count > 0) {
            var detections = pq;
            detections.sort(by: >);
            let max = detections[0];
            nmsList.append(max);
            pq.removeAll();
            if (detections.count > 0) {
                let size = detections.count - 1;
                for i in stride(from: 1, to: size, by: 1) {
                    let detection: Recognition = detections[i];
                    let b = detection.getLocation();
                    if (box_iou(a: max.getLocation(), b: b) < mNmsThresh) {
                        pq.append(detection);
                    }
                }
            }
        }
        return nmsList;
    }

    /// Intersection over union.
    ///
    /// - Parameters:
    ///     - a: first bounding box.
    ///     - b: second bounding box.
    /// - Returns: IoU of bounding boxes a and b.
    func box_iou(a: CGRect, b: CGRect) -> Float {
        box_intersection(a: a, b: b) / box_union(a: a, b: b);
    }

    /// Intersection.
    ///
    /// - Parameters:
    ///     - a: first bounding box.
    ///     - b: second bounding box.
    /// - Returns: Intersection surface of bounding boxes a and b.
    func box_intersection(a: CGRect, b: CGRect) -> Float {
        let w = overlap(x1: Float(a.midX), w1: Float(a.width), x2: Float(b.midX), w2: Float(b.width));
        let h = overlap(x1: Float(a.midY), w1: Float(a.height), x2: Float(b.midY), w2: Float(b.height));
        if (w < 0 || h < 0) {
            return 0
        };
        return w * h;
    }

    /// Union.
    ///
    /// - Parameters:
    ///     - a: first bounding box.
    ///     - b: second bounding box.
    /// - Returns: Union surface of bounding boxes a and b.
    func box_union(a: CGRect, b: CGRect) -> Float {
        let i = box_intersection(a: a, b: b);
        let u = Float(a.width * a.height) + Float(b.width * b.height) - i;
        return u;
    }

    /// Segment overlap computation.
    ///
    /// - Parameters:
    ///     - x1: origin of the first segment.
    ///     - w1: length of the first segment.
    ///     - x2: origin of the second segment.
    ///     - w2: length of the second segment.
    /// - Returns: Overlap of the two segments.
    func overlap(x1: Float, w1: Float, x2: Float, w2: Float) -> Float {
        let l1: Float = x1 - w1 / 2;
        let l2: Float = x2 - w2 / 2;
        let left: Float = l1 > l2 ? l1 : l2;
        let r1 = x1 + w1 / 2;
        let r2 = x2 + w2 / 2;
        let right = r1 < r2 ? r1 : r2;
        return right - left;
    }

    /// Get the name of the label file stored in Assets.
    func getLabelPath() -> String {
        "";
    }

    /// Feeds the data.
    /// This additional method is necessary, because we can have different number of detections
    func feedData() throws {
    }

    /// Get the total number of labels.
    func getNumLabels() -> Int {
        labels.count
    }

    /// Get the list of all the labels
    func getLabels() -> [String] {
        var result: [String] = []

        for label in labels {
            if (label != "???" && label != "" && label != " ") {
                result.append(label);
            }
        }
        return result
    }

    /// Get the number of detections.
    func getNumDetections() -> Int {
        NUM_DETECTIONS;
    }

    /// Get specs from tflite file.
    func parseTFlite() {
    }

    /// Get the recognitions.
    func getRecognitions(className: String, width: Int, height: Int) -> [Recognition] {
        [];
    }

    /// Get the multiple recognitions
    func getMultipleRecognitions(classA: String, classB: String, width: Int, height: Int) -> [Recognition] {
        [];
    }

    func getAllRecognitions(width: Int, height: Int) -> [Recognition] {
        [];
    }

    ///
    @objc func updateObject(_ notification: Notification) {
        selectedClass = notification.object as? String
    }

    /// Setter function
    public func setSelectedClass(newClass: String) {
        selectedClass = newClass;
    }

    public func setMultipleSelectedClass(newClasses: [String]) {
        selectedMultipleClass = newClasses;
    }

    public func returnMultipleSelectedClass() -> [String] {
        selectedMultipleClass
    }
}

