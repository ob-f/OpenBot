///
/// Created by Sparsh Jain on 29/09/22.
///

import Foundation
import UIKit

/// The ModelItem structure is a data container for the characteristics of a neural network. It serves as an inerface between the Model class and
/// a JSON parameter file describing the main characteristics of a network.
/// Conforms to the Codable protocol: can be converted to JSON format by encoding it, and it can be converted back to an object by decoding it.
struct ModelItem: Codable {
    var id: Int;
    var `class`: String;
    var type: String;
    var name: String;
    var pathType: String;
    var path: String;
    var inputSize: String;

    /// Write the characteristics of a given model as a JSON file
    ///
    /// - Parameters: a list of model items
    static func toJson(_ models: [ModelItem]) -> Any {
        var result: [Any] = []
        for item in models {
            let jsonObject: Any =
                    [
                        "id": item.id,
                        "class": item.class,
                        "type": item.type,
                        "name": item.name,
                        "pathType": item.pathType,
                        "path": item.path,
                        "inputSize": item.inputSize
                    ];
            result.append(jsonObject)
        }
        return result;
    }

    /// Extract the width of a model input from a JSON string in the format "WxH"
    ///
    /// - Returns: width of the model input
    static func getWidthOfInput(_ inputSize: String) -> String {
        if inputSize != "" {
            let index = inputSize.firstIndex(of: "x")!;
            return String(inputSize.prefix(upTo: index));
        }
        return ""
    }

    /// Extract the height of a model input from a JSON string in the format "WxH"
    ///
    /// - Returns: height of the model input
    static func getHeightOfInput(_ inputSize: String) -> String {
        if inputSize != "" {
            let index = inputSize.firstIndex(of: "x")!;
            let nextIndex = inputSize.index(after: index);
            return String(inputSize.suffix(from: nextIndex));
        }
        return ""
    }

}

/// The Model class encapsulates the main properties of a neural network in the context of an OpenBot application.
class Model {
    var id: Int;
    var classType: CLASS; // AUTOPILOT_F, MOBILENET, YOLOV4, YOLOV5, EFFICIENTDET or NAVIGATION
    var type: TYPE; // AUTOPILOT, DETECTOR or NAVIGATION
    var name: String;
    var pathType: PATH_TYPE; // URL, FILE or ASSET
    var path: String;
    var inputSize: String;

    /// Initialization routine
    ///
    /// - Parameters:
    ///     - id: id of the model
    ///     - classType: AUTOPILOT_F, MOBILENET, YOLOV4, YOLOV5, EFFICIENTDET or NAVIGATION
    ///     - type: AUTOPILOT, DETECTOR or NAVIGATION
    ///     - name: the name of the model
    ///     - pathType: URL, FILE or ASSET
    ///     - path: relative to ...
    ///     - inputSize: the model input dimensionality
    ///     - id:
    init(id: Int, classType: CLASS, type: TYPE, name: String, pathType: PATH_TYPE, path: String, inputSize: String) {
        self.id = id;
        self.classType = classType;
        self.type = type;
        self.name = name;
        self.pathType = pathType;
        self.path = path;
        self.inputSize = inputSize;
    }

    /// Getter method to return name
    ///
    /// - Returns: name of the model
    func getName() -> String {
        name;
    }

    /// Getter method to return the input size of a model
    ///
    /// - Returns: input size of the model
    func getInputSize() -> CGSize {
        CGSize.parseSize(inputSize);
    }

    /// Setter method to set path
    ///
    /// - Parameters: path of the model
    func setPath(path: String) {
        self.path = path;
    }

    /// Setter method to set path type
    ///
    /// - Parameters: whether the path of the policy is of type URL, FILE or ASSET
    func setPathType(pathType: PATH_TYPE) {
        self.pathType = pathType;
    }

    /// Setter method to set input size of the model
    ///
    /// - Parameters: input size of the model
    func setInputSize(inputSize: String) {
        self.inputSize = inputSize;
    }

    /// Setter method to set the name of the model
    ///
    /// - Parameters: name of the model
    func setName(name: String) {
        self.name = name;
    }

    /// Setter method to set the model class
    ///
    /// - Parameters: type enum (e.g. AUTOPILOT_F, MOBILENET, YOLOV4, YOLOV5, EFFICIENTDET or NAVIGATION )
    func setClassType(classType: CLASS) {
        self.classType = classType;
    }

    /// Setter method to set the type of the model
    ///
    /// - Parameters: type enum (e.g. AUTOPILOT, DETECTOR or NAVIGATION )
    func setType(type: TYPE) {
        self.type = type;
    }
}

/// Extension of the CGSize class
extension CGSize {

    /// Converts an input string into CGSize object
    ///
    /// - Parameters: input size of the model, as a string in the format "WxH"
    /// - Returns: a CGSize struct
    static func parseSize(_ input: String) -> CGSize {
        let out = input.split(separator: "x");
        let width = Int(out[0]) ?? 0;
        let height = Int(out[1]) ?? 0;
        return CGSize(width: width, height: height);
    }
}

/// Extension of the Model class
extension Model {

    /// Converts an array of ModelItem objects to an array of Model objects.
    ///
    /// - Parameter list: The list of ModelItem objects to be converted.
    /// - Returns: An array of Model objects.
    static func fromModelItems(list: [ModelItem]) -> [Model] {
        var models: [Model] = [];
        for item in list {
            let model: Model = Model(
                    id: item.id,
                    classType: CLASS(rawValue: item.class) ?? CLASS.AUTOPILOT_F,
                    type: TYPE(rawValue: item.type) ?? TYPE.AUTOPILOT,
                    name: item.name,
                    pathType: PATH_TYPE(rawValue: item.pathType) ?? PATH_TYPE.ASSET,
                    path: item.path,
                    inputSize: item.inputSize);
            models.append(model);
        }
        return models;
    }

    /// Converts a single ModelItem object to a Model object.
    ///
    ///- Parameter item: The ModelItem object to be converted.
    ///- Returns: A Model object.
    static func fromModelItem(item: ModelItem) -> Model {
        let model: Model = Model(
                id: item.id,
                classType: CLASS(rawValue: item.class) ?? CLASS.AUTOPILOT_F,
                type: TYPE(rawValue: item.type) ?? TYPE.AUTOPILOT,
                name: item.name,
                pathType: PATH_TYPE(rawValue: item.pathType) ?? PATH_TYPE.ASSET,
                path: item.path,
                inputSize: item.inputSize);
        return model;
    }
}
