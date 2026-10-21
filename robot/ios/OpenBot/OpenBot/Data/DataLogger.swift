//
// Created by Nitish Yadav on 06/09/22.
//

import Foundation
import UIKit

var bluetoothData = ""

/// Implementation of the Data logger
class DataLogger {
    static let shared: DataLogger = DataLogger()
    var enabled: Bool = false;
    let sensorData = sensorDataRetrieve.shared
    let bluetooth = bluetoothDataController.shared;
    var vehicleSensorsData: String = ""
    var acceleration: String = ""
    var gyroscope: String = ""
    var magnetometer: String = ""
    var locationCoordinates: String = ""
    var gps: String = ""
    var baseDirectory: String = ""
    var baseDirList = [String]()
    var bumper: String = ""
    var ctrlLog: String = ""
    var indicatorLog: String = ""
    var inferenceTime: String = ""
    var light: String = ""
    var sonar: String = ""
    var voltage: String = ""
    var wheels: String = ""
    var motion: String = ""
    var rgbFrames: String = ""
    var allDirectories = [URL]()
    var isVehicleLogSelected: Bool = true
    var isAccelerationLogSelected: Bool = true
    var isGpsLogSelected: Bool = true
    var isMagneticLogSelected: Bool = true
    var isGyroscopeLogSelected: Bool = true
    var openbotPath: String = ""
    var imagePath: String = ""
    var sensorDataPath: String = ""
    var preferencesManager : SharedPreferencesManager = SharedPreferencesManager()

    /// initializing function
    init() {
        NotificationCenter.default.addObserver(self, selector: #selector(updateLogger), name: .updateSensorsForLog, object: nil)
    }

    /// function to reset the data stored in local variables.
    func reset() {

        // Set a new base directory name for the current recording session
        baseDirectory = getBaseDirectoryName()

        // Populate the base directory with sensor data and image directories
        let paths = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)
        let documentsDirectory = paths.first ?? ""
        openbotPath = documentsDirectory + Strings.forwardSlash + baseDirectory
        imagePath = openbotPath + Strings.forwardSlash + Strings.images
        sensorDataPath = openbotPath + Strings.forwardSlash + Strings.sensor

        // Reset the different file entries
        vehicleSensorsData = "";
        acceleration = Strings.accelerationHeader
        locationCoordinates = Strings.locationCoordinatesHeader
        gyroscope = Strings.gyroscopeHeader
        magnetometer = Strings.magnetometerHeader
        gps = Strings.gpsHeader
        bumper = Strings.bumperHeader
        ctrlLog = Strings.ctrlHeader
        indicatorLog = Strings.indicatorHeader
        inferenceTime = Strings.inferenceTimeHeader
        light = Strings.lightHeader
        sonar = Strings.sonarHeader
        voltage = Strings.voltageHeader
        wheels = Strings.wheelsHeader
        motion = Strings.motionHeader
        rgbFrames = Strings.timestamp
    }

    /// function to get the directory information where user want to the save the files
    ///
    /// - Returns: URL of the directory
    func getDirectoryInfo() -> URL {
        allDirectories.removeAll()
        let fileManager = FileManager.default
        var documentsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
        documentsURL = documentsURL.appendingPathComponent(Strings.forwardSlash + baseDirectory)
        do {
            let fileURLs = try fileManager.contentsOfDirectory(at: documentsURL, includingPropertiesForKeys: nil)
            allDirectories = fileURLs
        } catch {
            print("Error while enumerating files \(documentsURL.path): \(error.localizedDescription)")
        }
        return documentsURL
    }

    /// function to create the folders where data would be saved
    func createOpenBotFolders() {

        // Keep track of the name of the newly created base directory
        baseDirList.append(baseDirectory)

        // Create the main experiment folder
        createFolder(path: openbotPath)

        // Create image folder
        createFolder(path: imagePath)

        // Create sensor folder
        createFolder(path: sensorDataPath)
    }

    /// function to save the sonar data
    func saveSensorData() {
        if URL(string: openbotPath) != nil {
            saveSensorFiles(data: rgbFrames, fileName: FileName.rgbFrames)
            if isAccelerationLogSelected {
                saveSensorFiles(data: acceleration, fileName: FileName.accelerator)
            }
            if isMagneticLogSelected {
                saveSensorFiles(data: magnetometer, fileName: FileName.magnetic)
            }
            if isGyroscopeLogSelected {
                saveSensorFiles(data: gyroscope, fileName: FileName.gyroscopeLog)
            }
            if isGpsLogSelected {
                saveSensorFiles(data: gps, fileName: FileName.gpsLog)
            }
            if isVehicleLogSelected {
                saveSensorFiles(data: bumper, fileName: FileName.bumperLog)
                saveSensorFiles(data: ctrlLog, fileName: FileName.ctrlLog)
                saveSensorFiles(data: indicatorLog, fileName: FileName.indicatorLog)
                saveSensorFiles(data: inferenceTime, fileName: FileName.inferenceLog)
                saveSensorFiles(data: light, fileName: FileName.lightLog)
                saveSensorFiles(data: motion, fileName: FileName.motionLog)
                saveSensorFiles(data: sonar, fileName: FileName.sonarLog)
                saveSensorFiles(data: voltage, fileName: FileName.voltageLog)
                saveSensorFiles(data: wheels, fileName: FileName.wheelsLog)
            }
        }
    }

    /// function to create a folder
    ///
    /// - Parameter path: folder path
    func createFolder(path: String) {
        if !FileManager.default.fileExists(atPath: path) {
            try? FileManager.default.createDirectory(atPath: path, withIntermediateDirectories: false, attributes: nil)
        }
    }

    /// function to delete the files
    ///
    /// - Parameter fileNameToDelete: file name to be deleted in documents directory (also can provide path after doc directory).
    func deleteFiles(fileNameToDelete: String) {
        var filePath = ""
        // Fine documents directory on device
        let dirs: [String] = NSSearchPathForDirectoriesInDomains(FileManager.SearchPathDirectory.documentDirectory, FileManager.SearchPathDomainMask.allDomainsMask, true)
        if dirs.count > 0 {
            let dir = dirs[0] //documents directory
            filePath = dir.appendingFormat("/" + fileNameToDelete)
        } else {
            print("Could not find local directory to store file")
            return
        }
        do {
            let fileManager = FileManager.default
            if fileManager.fileExists(atPath: filePath) {
                // Delete file
                try fileManager.removeItem(atPath: filePath)
            } else {
                print("File does not exist")
            }

        } catch let error as NSError {
            print("An error took place: \(error)")
        }
    }

    /// function to delete all the files related to last data dump. (used after when moved to zip file)
    func deleteAllFilesFromDocument() {
        let fileManager = FileManager.default
        do {
            let documentDirectoryURL = try fileManager.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false)
            let fileURLs = try fileManager.contentsOfDirectory(at: documentDirectoryURL, includingPropertiesForKeys: nil, options: .skipsHiddenFiles)
            for url in fileURLs {
                try fileManager.removeItem(at: url)
            }
        } catch {
            print(error)
        }
    }

    /// function to delete the zip file
    func deleteZipFileFromDocument() {
        let fileManager = FileManager.default
        let documentDirectoryURL = FileManager.getDocumentsDirectory()
        do {
            let fileURLs = try fileManager.contentsOfDirectory(at: documentDirectoryURL, includingPropertiesForKeys: nil, options: .skipsHiddenFiles)
            for url in fileURLs {
                //deletes the all files
                if !((url.lastPathComponent.contains(".tflite")) || (url.lastPathComponent.contains("config.json"))){
                    print("deleting files ======", url.lastPathComponent);
                    deleteFiles(fileNameToDelete: url.lastPathComponent);
                }
            }
        } catch {
            print("Error in deleting .zip file")
        }
    }

    /// function to save the images into documents directory
    ///
    /// - Parameters:
    ///   - image: image in UIImage format
    ///   - name: name that should be given to the image
    func saveImages(image: UIImage, name: String) {
        let imagePath = URL(string: imagePath)
        let imageName = imagePath?.appendingPathComponent(name);
        let ima = imageName?.absoluteString
        let fileManager = FileManager.default
        if let ima = ima {
            fileManager.createFile(atPath: ima, contents: image.jpegData(compressionQuality: 0.99))
        }
    }

    /// function to save the sensors data into documents directory.
    ///
    /// - Parameters:
    ///   - data: data that should be saved into the file
    ///   - fileName: name of the output file
    func saveSensorFiles(data: String, fileName: String) {
        let fileManager = FileManager.default
        let sensorPath = URL(string: sensorDataPath)
        let sensorFileName = sensorPath?.appendingPathComponent(fileName)
        let sen = sensorFileName?.absoluteString
        let str = data
        fileManager.createFile(atPath: sen ?? "", contents: str.data(using: String.Encoding.utf8))
    }

    /// returns current date/time in a particular format
    ///
    /// - Parameter format: date/time format [yyyy], [MM], [dd], [H], [mm], [ss]
    /// - Returns: String formatted date
    func knowDateOrTime(format: String) -> String {
        let date = Date()
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = format
        let dateOrTime = dateFormatter.string(from: date)
        return dateOrTime
    }

    /// function to get the main directory name in which all the files will be stored
    ///
    /// - Returns: name of the directory
    func getBaseDirectoryName() -> String {
        knowDateOrTime(format: "yyyy") + knowDateOrTime(format: "MM") + knowDateOrTime(format: "dd") + "_"
                + knowDateOrTime(format: "H") + knowDateOrTime(format: "mm") + knowDateOrTime(format: "ss")
    }

    /// function to get the documents directory list of urls
    ///
    /// - Returns: list of urls inside the directory
    func getDocumentDirectoryInformation() -> [URL] {
        var fileURLs: [URL] = []
        let fileManager = FileManager.default
        let documentsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
        // documentsURL = documentsURL.appendingPathComponent(Strings.forwardSlash +  Global.shared.baseDirectory)
        do {
            fileURLs = try fileManager.contentsOfDirectory(at: documentsURL, includingPropertiesForKeys: nil)
            return fileURLs
        } catch {
            print("Error while enumerating files \(documentsURL.path): \(error.localizedDescription)")
        }
        return fileURLs
    }

    /// function to start the recording of logs
    func recordLogs() {
        // Get the current timestamp
        let timestamp = returnCurrentTimestamp()

        // Fill files with the selected sensor readings
        if isVehicleLogSelected {
            vehicleSensorsData = vehicleSensorsData + bluetoothData + Strings.newLine
        }
        if isGpsLogSelected {
            if (sensorData.location != nil) {
                gps = gps + String(timestamp) + " " + String(sensorData.location.coordinate.latitude) + " " + String(sensorData.location.coordinate.longitude) + " " + String(sensorData.location.altitude) + " " + String(sensorData.location.speed) + Strings.newLine
            }
        }
        if isAccelerationLogSelected {
            acceleration = acceleration + String(timestamp) + " " + convertToString(XValue: sensorData.accelerationX, YValue: sensorData.accelerationY, ZValue: sensorData.accelerationZ) + Strings.newLine
        }
        if isMagneticLogSelected {
            magnetometer = magnetometer + String(timestamp) + " " + convertToString(XValue: sensorData.magneticFieldX, YValue: sensorData.magneticFieldY, ZValue: sensorData.magneticFieldZ) + Strings.newLine
        }
        if isGyroscopeLogSelected {
            gyroscope = gyroscope + String(timestamp) + " " + convertToString(XValue: sensorData.angularRateX, YValue: sensorData.angularRateY, ZValue: sensorData.angularRateZ) + Strings.newLine
        }
        if isVehicleLogSelected {
            recordVehicleLogs()
        }
    }

    /// function to start the recording of the vehicle logs.
    func recordVehicleLogs() {
        let timestamp = returnCurrentTimestamp()
        if bluetoothData != "" {
            let index = bluetooth.sonarData.index(after: bluetooth.sonarData.startIndex)
            sonar = sonar + String(timestamp) + " " + String(bluetooth.sonarData[index...]) + Strings.newLine
            wheels = wheels + String(timestamp) + " " + String(bluetooth.speedometer[index...]) + Strings.newLine
            voltage = voltage + String(timestamp) + " " + String(bluetooth.voltageDivider[index...]) + Strings.newLine
            if bluetooth.bumperData != "" {
                bumper = bumper + String(timestamp) + " " + String(bluetooth.bumperData[index...]) + Strings.newLine;
            }
        }
    }

    /// To record teh images into the logs with timestamp.
    ///
    /// - Parameter index: index of the image
    func recordImageLogs(index: Int) {
        let timestamp = returnCurrentTimestamp()
        rgbFrames = rgbFrames + String(timestamp) + Strings.comma + String(index) + Strings.newLine
    }

    /// function to convert the number values into spaced string
    ///
    /// - Parameters:
    ///   - XValue: value 1
    ///   - YValue: value 2
    ///   - ZValue: value 3
    /// - Returns: string with spaces
    func convertToString(XValue: Double, YValue: Double, ZValue: Double) -> String {
        String(XValue) + " " + String(YValue) + " " + String(ZValue);
    }

    /// To update the logger
    @objc func updateLogger(_ notification: Notification?) {
        let selectedButton = notification?.object as! UIButton
        let tag = selectedButton.tag
        switch tag {
        case 1:
            isVehicleLogSelected = !isVehicleLogSelected
            preferencesManager.updateSensorData(value: isVehicleLogSelected, sensor: "isVehicleLogSelected")
        case 2:
            isGpsLogSelected = !isGpsLogSelected
            preferencesManager.updateSensorData(value: isGpsLogSelected, sensor: "isGpsLogSelected")
        case 3:
            isAccelerationLogSelected = !isAccelerationLogSelected
            preferencesManager.updateSensorData(value: isAccelerationLogSelected, sensor: "isAccelerationLogSelected")
        case 4:
            isMagneticLogSelected = !isMagneticLogSelected
            preferencesManager.updateSensorData(value: isMagneticLogSelected, sensor: "isMagneticLogSelected")
        default:
            isGyroscopeLogSelected = !isGyroscopeLogSelected
            preferencesManager.updateSensorData(value: isGyroscopeLogSelected, sensor: "isGyroscopeLogSelected")
        }
    }

    /// To set the vehicle control logs
    ///
    /// - Parameters:
    ///   - left: left tyre log
    ///   - right: right tyre log
    func setControlLogs(left: String, right: String) {
        ctrlLog = ctrlLog + String(returnCurrentTimestamp()) + " " + left + " " + right + Strings.newLine;
    }

    /// function to set the indicator logs
    ///
    /// - Parameter indicator: indicator setting
    func setIndicatorLogs(indicator: String) {
        indicatorLog = indicatorLog + String(returnCurrentTimestamp()) + " " + indicator + Strings.newLine;
    }
}
