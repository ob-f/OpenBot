//
// Created by Nitish Yadav on 08/11/22.
//

import Foundation

/// Collection of the different notification signals involved in the OpenBot app
extension Notification.Name {
    static let updateLabel = Notification.Name("updateLabel");
    static let updateSerialMonitor = Notification.Name("updateSerialMonitor");
    static let bluetoothConnected = Notification.Name("bluetoothConnected");
    static let bluetoothDisconnected = Notification.Name("bluetoothDisconnected");
    static let switchCamera = Notification.Name(Strings.switchCamera);
    static let ble = Notification.Name(Strings.ble);
    static let updateModel = Notification.Name("updateModel");
    static let showModelsDD = Notification.Name("showModelsDD");
    static let showServerDD = Notification.Name("showServerDD");
    static let showDeviceDD = Notification.Name("showDeviceDD");
    static let autoMode = Notification.Name("autoMode");
    static let autoModeObjectTracking = Notification.Name("autoModeObjectTracking");
    static let updateThread = Notification.Name("updateThread");
    static let updateThreadLabel = Notification.Name("updateThreadLabel");
    static let logData = Notification.Name(Strings.logDataNotify);
    static let updateResolution = Notification.Name(Strings.updateResolution);
    static let updatePreview = Notification.Name(Strings.updatePreview);
    static let updateTraining = Notification.Name(Strings.updateTraining);
    static let updateSensorsForLog = Notification.Name(Strings.updateSensorsForLog);
    static let updateModelResolution = Notification.Name("updateModelResolution");
    static let updateSpeedLabel = Notification.Name("updateSpeedLabel");
    static let updateRpmLabel = Notification.Name("updateRpmLabel");
    static let updateDevice = Notification.Name("updateDevice");
    static let showObjectDD = Notification.Name("showObjectDD");
    static let updateObject = Notification.Name("updateObject");
    static let updateConfidence = Notification.Name("updateConfidence");
    static let updateObjectList = Notification.Name("updateObjectList");
    static let fileDownloaded = Notification.Name("fileDownloaded");
    static let removeBlankScreen = Notification.Name("removeBlankScreen");
    static let toggleNetworks = Notification.Name("toggleNetworks");
    static let updateAutoPilotFps = Notification.Name("updateAutoPilotFps");
    static let updateObjectTrackingFps = Notification.Name("updateObjectTrackingFps");
    static let decreaseSpeedMode = Notification.Name("decreaseSpeedMode");
    static let increaseSpeedMode = Notification.Name("increaseSpeedMode");
    static let updateDriveMode = Notification.Name("updateDriveMode");
    static let updateStringFromControllerApp = Notification.Name("updateStringFromControllerApp");
    static let updateLightsCommandFromControllerApp = Notification.Name("updateLightsCommandFromControllerApp");
    static let updateDataFromControllerApp = Notification.Name("updateDataFromControllerApp");
    static let cameraBuffer = Notification.Name("cameraBuffer");
    static let clientConnected = Notification.Name("clientConnected");
    static let clientDisConnected = Notification.Name("clientDisConnected");
    static let googleSignIn = Notification.Name("googleSignIn");
    static let cancelThread = Notification.Name("cancelThread");
    static let reInitializeCamera = Notification.Name("reInitializeCamera");
    static let commandName = Notification.Name("commandName");
    static let projectDeleted = Notification.Name("projectDeleted");
    static let autoSynced = Notification.Name("autoSynced");
    static let commandObject = Notification.Name("commandObject");
    static let createCameraView = Notification.Name("createCameraView");
    static let pointGoalNav = Notification.Name("pointGoalNav");
    static let server = Notification.Name("server");
    static let saveAs = Notification.Name("saveAs");
    static let displayItems = Notification.Name("displayItems");
}
