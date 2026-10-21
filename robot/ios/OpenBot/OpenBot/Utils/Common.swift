//
// Created by Sparsh Jain on 31/10/22.
//

import Foundation

/// The Common class contains a set of useful routines to load neural networks into the code
class Common {

    /// Try to load the different policy models described in the "config.json" parameter file
    ///
    /// - Returns: list of ModelItem objects built from the JSON parameter file
    static func loadAllModelItems() -> [ModelItem] {
        let documentDirectoryURls = DataLogger.shared.getDocumentDirectoryInformation();
        var isFoundConfigFile: Bool = false;
        for url in documentDirectoryURls {
            if url.absoluteString.contains("config.json") {
                isFoundConfigFile = true
                break;
            }
        }
        switch isFoundConfigFile {
        case true:
            return loadAllModelFromDocumentDirectory()
        case false:
            return loadAllModelItemsFromBundle()
        }
    }

    /// Parse the config.json parameter file
    ///
    /// - Returns: list of ModelItem objects built from the JSON parameter file
    static func loadAllModelItemsFromBundle() -> [ModelItem] {
        if let url = Bundle.main.url(forResource: "config", withExtension: "json") {
            do {
                let data = try Data(contentsOf: url)
                let decoder = JSONDecoder()
                let jsonData = try decoder.decode([ModelItem].self, from: data)
                return jsonData;
            } catch {
                print("error:\(error)")
            }
        }
        return [];
    }

    /// Parse the config.json parameter file provided in the document directory
    ///
    /// - Returns: list of ModelItem objects built from the JSON parameter file
    static func loadAllModelFromDocumentDirectory() -> [ModelItem] {
        let fileName = "config.json"
        var filePath = ""
        let dirs: [String] = NSSearchPathForDirectoriesInDomains(FileManager.SearchPathDirectory.documentDirectory, FileManager.SearchPathDomainMask.allDomainsMask, true)
        if dirs.count > 0 {
            let dir = dirs[0] //documents directory
            filePath = dir.appending("/" + fileName)
        } else {
            print("Could not find local directory to store file")
        }
        do {
            let data = try Data.init(contentsOf: URL(string: "file://" + filePath)!)
            let decoder = JSONDecoder()
            let jsonData = try decoder.decode([ModelItem].self, from: data)
            return jsonData
        } catch let error as NSError {
            print("An error took place: \(error)")
        }
        return []
    }

    /// Load the modelItems of a specific mode.
    ///
    /// - Parameters: mode (e.g. "AUTOPILOT" or "DETECTOR")
    /// - Returns: array of all models of that type which are downloaded
    static func loadSelectedModels(mode: String) -> [String] {
        var selectedModels: [String] = []
        let allModels = loadAllModelItems()
        for model in allModels {

            if model.type == mode && model.pathType == "ASSET" {
                if isModelItemAvailableInDocument(modelName: model.name + Strings.tflite) {
                    selectedModels.append(model.name)
                }
            }
            let split = model.path.split(separator: "/");
            let index = split.count - 1;
            if index < 0 {
                continue;
            }
            let fileName = String(split[index]);
            if model.type == mode {
                let bundle = Bundle.main
                let path = bundle.path(forResource: fileName, ofType: "")
                if path != nil {
                    selectedModels.append(String(model.name.prefix(upTo: model.name.firstIndex(of: ".")!)))
                }
                if isModelItemAvailableInDocument(modelName: String(model.name.prefix(upTo: model.name.firstIndex(of: ".")!))) {
                    selectedModels.append(String(model.name.prefix(upTo: model.name.firstIndex(of: ".")!)))
                }
            }
        }
        return selectedModels
    }

    /// Load the names of the different models
    ///
    /// - Returns: list of names of the loaded models
    static func loadAllModelsName() -> [String] {
        var models: [String] = []
        let allModels = loadAllModelItems()
        for model in allModels {
            if model.name != "" {
                let index = model.name.firstIndex(of: ".")
                if index == nil {
                    models.append(model.name);
                } else {
                    models.append(String(model.name.prefix(upTo: index!)))
                }
            }
        }
        return models
    }

    /// Get a modelItem from its name
    ///
    /// - Parameters: name of the model
    /// - Returns: the loaded modelItem
    static func returnModelItem(modelName: String) -> ModelItem {
        let allModels = loadAllModelItems()
        for item in allModels {
            if item.name.contains(modelName) {
                return item;
            }
        }
        let model: ModelItem = ModelItem.init(id: allModels.count + 1, class: allModels[0].class, type: allModels[0].type, name: modelName, pathType: allModels[allModels.count - 1].pathType, path: "", inputSize: allModels[0].inputSize)
        return model;
    }

    /// Get all the names of all the modelItems associated to a specific mode
    ///
    /// - Parameters: mode (e.g. "AUTOPILOT" or "DETECTOR")
    /// - Returns: List the names of all the models of a given mode
    static func returnAllModelItemsName(mode: String) -> [String] {
        var selectedModels: [String] = []
        let allModels = loadAllModelItems()
        for model in allModels {
            if model.type == mode, let index = model.name.firstIndex(of: ".") {
                selectedModels.append(String(model.name.prefix(upTo: index)))
            }
        }
        return selectedModels
    }

    /// Informs whether the desired modelItem associated to a given bundle
    ///
    /// - Parameters: name of the model
    /// - Returns: true if the corresponding modelItem is available
    static func isModelItemAvailableInBundle(modelName: String) -> Bool {
        let allModels = loadAllModelItemsFromBundle()
        for model in allModels {
            let split = model.path.split(separator: "/");
            let index = split.count - 1;
            let fileName = String(split[index]);
            if model.name.prefix(upTo: model.name.firstIndex(of: ".")!) == modelName {
                let bundle = Bundle.main
                let path = bundle.path(forResource: fileName, ofType: "")
                if path != nil {
                    return true;
                }
            }
        }
        return false
    }

    /// Informs whether the desired modelItem associated to a given document
    ///
    /// - Parameters: name of the model
    /// - Returns: true if the corresponding modelItem is available
    static func isModelItemAvailableInDocument(modelName: String) -> Bool {
        for url in DataLogger.shared.getDocumentDirectoryInformation() {
            let model = returnModelItem(modelName: modelName)
            if url.lastPathComponent == model.name {
                return true
            }
        }
        return false
    }

    /// Get the name of the file pointed by a given url
    ///
    /// - Parameters: url
    /// - Returns: name of the file pointed by the url
    static func returnNameOfFile(url: URL) -> String {
        let filepath = url.absoluteString
        let index = filepath.lastIndex(of: "/")
        if let index {
            return String(filepath[index...])
        }
        return "unnamed"
    }

    /// Returns the point-goal navigation ModelItem
    ///
    /// - Returns: the point-goal navigation modelItem
    static func returnNavigationModel() -> ModelItem {
        returnModelItem(modelName: "PilotNet-Goal")
    }

    /// to save the file configs into document directory.
    static func saveConfigFileToDocument(modelItems: [ModelItem]) throws {
        let fileManager = FileManager.default
        do {
            let documentDirectory = try fileManager.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false)
            let fileURL = documentDirectory.appendingPathComponent("config.json")
            let jsonEncoder = JSONEncoder()
            do {
                let jsonData = try jsonEncoder.encode(modelItems)
                let jsonString = String(data: jsonData, encoding: .utf8)
                if let jsonString = jsonString {
                    try jsonString.write(to: fileURL, atomically: true, encoding: .utf8)
                }
            } catch {
                print("inside catch", error)
            }
        } catch {
            print(error)
        }
    }

    /// function to let user modify the model, and store them into documents directory.
    static func modifyModels(modelAddress: String, model: ModelItem, widthOfModel: String, heightOfModel: String) -> [ModelItem] {
        var allModels: [ModelItem] = [];
        let documentDirectoryURls = DataLogger.shared.getDocumentDirectoryInformation();
        var isFoundConfigFile: Bool = false;
        for url in documentDirectoryURls {
            if url.absoluteString.contains("config.json") {
                isFoundConfigFile = true
                break;
            }
        }
        switch isFoundConfigFile {
        case true:
            allModels = Common.loadAllModelFromDocumentDirectory()
        case false:
            allModels = Common.loadAllModelItemsFromBundle();
        }

        let newModel = modelAddress == "" ? ModelItem.init(id: model.id, class: model.class, type: model.type, name: model.name, pathType: model.pathType, path: model.path, inputSize: widthOfModel + "x" + heightOfModel) :
                ModelItem.init(id: allModels.count + 1, class: model.class, type: model.type, name: model.name, pathType: model.pathType, path: modelAddress, inputSize: widthOfModel + "x" + heightOfModel);
        var index = 0;
        for model in allModels {
            if model.id == newModel.id {
                allModels[index] = newModel;
                return allModels;
            }
            index = index + 1;
        }
        allModels.append(newModel);
        return allModels
    }

    /**
     Function overloaded to modify model if it is downloaded to the drive or being deleted
     - Parameters:
       - model:
       - isDelete:
     - Returns:
     */
    static func modifyModel(model: ModelItem, isDelete: Bool) -> [ModelItem] {
        var allModels: [ModelItem] = [];
        let documentDirectoryURls = DataLogger.shared.getDocumentDirectoryInformation();
        var isFoundConfigFile: Bool = false;
        for url in documentDirectoryURls {
            if url.absoluteString.contains("config.json") {
                isFoundConfigFile = true
                break;
            }
        }
        switch isFoundConfigFile {
        case true:
            allModels = Common.loadAllModelFromDocumentDirectory()
        case false:
            allModels = Common.loadAllModelItemsFromBundle();
        }
        var newModel: ModelItem!
        if isDelete {
            newModel = ModelItem(id: model.id, class: model.class, type: model.type, name: model.name, pathType: "URL", path: model.path, inputSize: model.inputSize);
        } else {
            newModel = ModelItem(id: model.id, class: model.class, type: model.type, name: model.name, pathType: "FILE", path: model.path, inputSize: model.inputSize);
        }
        var index = 0;
        for model in allModels {
            if model.id == newModel.id {
                allModels[index] = newModel;
                return allModels;
            }
            index = index + 1;
        }
        allModels.append(newModel);
        return allModels
    }
}
