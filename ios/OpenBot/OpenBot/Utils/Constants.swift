//
// Created by Sparsh Jain on 25/08/22.
//

import UIKit

/// References to the images used in the user interface
struct Images {
    static let freeRoam = UIImage(named: "freeRoam")
    static let dataCollection = UIImage(named: "dataCollection")
    static let controllerMapping = UIImage(named: "controllerMapping")
    static let autopilotIcon = UIImage(named: "autopilot")
    static let objectTrackingIcon = UIImage(named: "objectTracking")
    static let modelManagementIcon = UIImage(named: "modelManagement")
    static let robotInfoIcon = UIImage(named: "robotInfo")
    static let bluetoothConnected = UIImage(named: "bluetoothConnected")
    static let bluetoothDisconnected = UIImage(named: "bluetoothDisconnected")
    static let frontCamera = UIImage(named: "frontCamera")
    static let ble = UIImage(named: "ble")
    static let bluetooth = UIImage(named: "bluetooth")
    static let bluetoothSearching = UIImage(named: "bluetoothSearching")
    static let bluetooth_v2 = UIImage(named: "bluetooth_v2")
    static let bluetoothConnected_v2 = UIImage(named: "bluetoothConnected_v2")
    static let bluetoothDisconnected_v2 = UIImage(named: "bluetoothDisconnected_v2")
    static let bluetoothSearching_v2 = UIImage(named: "bluetoothSearching_v2")
    static let settings = UIImage(named: "settings")
    static let closeIcon = UIImage(named: "closeIcon")
    static let phoneIcon = UIImage(named: "phone")
    static let gamepadIcon = UIImage(named: "gamepad")
    static let webIcon = UIImage(systemName: "globe");
    static let slowIcon = UIImage(named: "slow")
    static let mediumIcon = UIImage(named: "medium")
    static let fastIcon = UIImage(named: "fast")
    static let joystickIcon = UIImage(named: "joystick")
    static let dualDriveIcon = UIImage(named: "dual")
    static let driveIcon = UIImage(named: "drive")
    static let gameDriveIcon = UIImage(named: "game")
    static let downArrow = UIImage(systemName: "arrowtriangle.down.fill")
    static let minus = UIImage(systemName: "minus")
    static let plus = UIImage(systemName: "plus")
    static let edit = UIImage(named: "edit")
    static let upwardArrow = UIImage(named: "upward-arrow")
    static let openBotLogo = UIImage(named: "openBotLogo")
    static let gmapMarker = UIImage(named: "gmap_marker")
    static let pointGoalIcon = UIImage(named: "pointGoal")
}

/// Colors of the user inerface
struct Colors {
    static let gridShadowColor = UIColor(named: "gridItemShadowColor")
    static let title = UIColor(named: "HomePageTitleColor")
    static let titleDeactivated = UIColor(named: "HomePageTitleColorDeactivated")
    static let sonar = UIColor(named: "sonar")
    static let voltageDividerColor = UIColor(named: "voltageDivider")
    static let freeRoamButtonsColor = UIColor(named: "gamepad")
    static let border = UIColor(named: "borderColor")
    static let bdColor = UIColor(named: "bdColor")
    static let textColor = UIColor(named: "textColor")
    static let navigationColor = UIColor(named: "navigationColor")
    static let modelDetail = UIColor(named: "modelDetailPopup")
    static let blue = UIColor(red: 0, green: 113, blue: 197, alpha: 1);
    static let lightBlack = UIColor(red: 0.16, green: 0.16, blue: 0.16, alpha: 1.00)
    static let freeRoamColor = UIColor(red: 247.0 / 255.0, green: 105.0 / 255.0, blue: 36.0 / 255.0, alpha: 1.00)
    static let dataCollectionColor = UIColor(red: 147.0 / 255.0, green: 196.0 / 255.0, blue: 125.0 / 255.0, alpha: 1.00)
    static let controllerMappingColor = UIColor(red: 103.0 / 255.0, green: 93.0 / 255.0, blue: 154.0 / 255.0, alpha: 1.00)
    static let robotInfoColor = UIColor(named: "HomePageTitleColor")! //UIColor(red: 75.0/255.0, green: 123.0/255.0, blue: 1.0, alpha: 1.00)
    static let autopilotColor = UIColor(red: 40.0 / 255.0, green: 47.0 / 255.0, blue: 55.0 / 255.0, alpha: 1.00)
    static let objectTrackingColor = UIColor(red: 1.0, green: 217.0 / 255.0, blue: 102.0 / 255.0, alpha: 1.00)
    static let pointGoalColor = UIColor(red: 52.0 / 255.0, green: 181.0 / 255.0, blue: 183.0 / 255.0, alpha: 1.00)
    static let modelColor = UIColor(red: 183.0 / 255.0, green: 138.0 / 255.0, blue: 164.0 / 255.0, alpha: 1.00)
}

/// Set of constants used in the code
struct Constants {

    // BLE UUIDs
    static let openbotService = "61653dc3-4021-4d1e-ba83-8b4eec61d613"
    static let openbotService_RX = "06386c14-86ea-4d71-811c-48f97c58f8c9"
    static let openbotService_TX = "9bf1103b-834c-47cf-b149-c9e4bcf778a7"

    // Game Data
    static var gameModes: [[ModeItem]] {
        [
            // General
            [
                ModeItem(label: Strings.freeRoam, icon: Images.freeRoam!, identifier: Strings.ScreenFreeRoam, color: Colors.freeRoamColor),
                ModeItem(label: Strings.dataCollection, icon: Images.dataCollection!, identifier: Strings.ScreenDataCollection, color: Colors.dataCollectionColor),
                ModeItem(label: Strings.controllerMapping, icon: Images.controllerMapping!, identifier: Strings.ScreenControllerMapping, color: Colors.controllerMappingColor),
                ModeItem(label: Strings.robotInfo, icon: Images.robotInfoIcon!, identifier: Strings.ScreenRobotInfo, color: Colors.robotInfoColor),
            ],
            // AI
            [
                ModeItem(label: Strings.Autopilot, icon: Images.autopilotIcon!, identifier: Strings.AutopilotFragment, color: Colors.autopilotColor),
                ModeItem(label: Strings.ObjectTracking, icon: Images.objectTrackingIcon!, identifier: Strings.ObjectTrackingFragment, color: Colors.objectTrackingColor),
                ModeItem(label: Strings.navigation, icon: Images.pointGoalIcon!, identifier: Strings.ScreenNavigation, color: Colors.pointGoalColor),
                ModeItem(label: Strings.modelManagement, icon: Images.modelManagementIcon!, identifier: Strings.ScreenModelManagement, color: Colors.modelColor)
            ],
            // Legacy
            []
        ]
    }
    static let frameColors: [UIColor] = [UIColor.red, UIColor.orange, UIColor.blue, UIColor.green, UIColor.brown]
    static let autopilotMode = "AUTOPILOT"
    static let objectTrackingMode = "DETECTOR"
    static let types: [String] = ["AUTOPILOT", "DETECTOR", "NAVIGATION"]
    static let classes: [String] = ["AUTOPILOT_F", "MOBILENET", "YOLOV4", "YOLOV5", "EFFICIENTDET", "NAVIGATION"]
    static let devices: [String] = ["CPU", "XNNPACK"]

}

/// The different speed modes of the OpenBot
enum SpeedMode: Float {
    case SLOW = 128
    case NORMAL = 192
    case FAST = 255
}

/// The different control interfaces of an OpenBot (either by gamepad or using another phone/computer)
enum ControlMode : String {
    case PHONE, GAMEPAD, WEB
}

/// The different speed modes of an OpenBot
enum DriveMode : String {
    case JOYSTICK, GAME, DUAL
}

/// High level commands used to guide the policy at intersections
enum IndicatorEvent: Int {
    case LEFT = -1
    case RIGHT = 1
    case STOP = 0
}

/// Control events of an OpenBot
enum CMD_Events {
    case TOGGLE_LOGS, TOGGLE_NOISE, TOGGLE_CAMERA, TOGGLE_NETWORK, CMD_SPEED_UP, CMD_SPEED_DOWN, CMD_DRIVE_MODE
}

/// Resolution of the preview images in the data collection mode
enum Resolutions {
    case LOW, MEDIUM, HIGH
}

/// Whether inference is executed on the CPU, GPU or Neural engine
enum RuntimeDevice: String {
    case CPU = "CPU",
         GPU = "GPU",
         XNNPACK = "XNNPACK"
}

/// The different types of neural networks available on the robot
enum CLASS: String {
    case AUTOPILOT_F = "AUTOPILOT_F",
         MOBILENET = "MOBILENET",
         YOLOV4 = "YOLOV4",
         YOLOV5 = "YOLOV5",
         EFFICIENTDET = "EFFICIENTDET",
         NAVIGATION = "NAVIGATION"
}

/// The different classes of neural networks available on the robot
enum TYPE: String {
    case AUTOPILOT = "AUTOPILOT",
         DETECTOR = "DETECTOR",
         NAVIGATION = "NAVIGATION"
}

/// Names of the sensor log files
struct FileName {
    static let accelerator: String = "accelerometerLog.txt"
    static let magnetic: String = "magneticLog.txt"
    static let gyroscopeLog: String = "gyroscopeLog.txt"
    static let gpsLog: String = "gpsLog.txt"
    static let bumperLog: String = "bumperLog.txt"
    static let ctrlLog: String = "ctrlLog.txt"
    static let indicatorLog: String = "indicatorLog.txt"
    static let inferenceLog: String = "inferenceLog.txt"
    static let lightLog: String = "lightLog.txt"
    static let motionLog: String = "motionLog.txt"
    static let sonarLog: String = "sonarLog.txt"
    static let voltageLog: String = "voltageLog.txt"
    static let wheelsLog: String = "wheelsLog.txt"
    static let rgbFrames: String = "rgbFrames.txt"
}

/// Enumeration of the different types of paths a neural network can have in the app.
enum PATH_TYPE: String {
    case URL = "URL",
         ASSET = "ASSET",
         FILE = "FILE"
}

/// Class for value of all safe area value
struct safeAreaLayoutValue {
    static let top: CGFloat = UIApplication.shared.keyWindow?.safeAreaInsets.top ?? 0.0
    static let bottom: CGFloat = UIApplication.shared.keyWindow?.safeAreaInsets.bottom ?? 0.0
    static let left: CGFloat = UIApplication.shared.keyWindow?.safeAreaInsets.left ?? 0.0;
    static let right: CGFloat = UIApplication.shared.keyWindow?.safeAreaInsets.right ?? 0.0;
}