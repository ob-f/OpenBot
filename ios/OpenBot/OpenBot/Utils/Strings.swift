//
// Created by Sparsh Jain on 25/08/22.
//

struct Strings {
    // Screens
    static let OpenBot: String = "OpenBot"
    static let freeRoam: String = "Free Roam"
    static let dataCollection: String = "Data Collection"
    static let controllerMapping: String = "Controller Mapping"
    static let autoPilot: String = "Auto Pilot"
    static let objectTracking: String = "Object Tracking"
    static let modelManagement: String = "Model Management"
    static let robotInfo: String = "Robot Info"
    static let navigation: String = "Point Goal Navigation"

    // Misc
    static let controller: String = "Controller"
    static let speed: String = "Speed"
    static let driveMode: String = "Drive Mode"
    static let gamepad: String = "Gamepad"
    static let phone: String = "Phone"
    static let web: String = "Web"
    static let joystick: String = "Joystick"
    static let game: String = "Game"
    static let dual: String = "Dual"
    static let slow: String = "Slow"
    static let medium: String = "Medium"
    static let fast: String = "Fast"
    static let logData: String = "Log Data"
    static let previewResolutionMedium: String = "Preview Resolution (1280 x 720)"
    static let previewResolutionLow: String = "Preview Resolution (960 x 540)"
    static let previewResolutionHigh: String = "Preview Resolution (1920 x 1080)"
    static let low: String = "Low"
    static let high: String = "High"
    static let modelResolution: String = "Model Resolution "
    static let server: String = "Server"
    static let expendSetting: String = "expendSetting"
    static let preview: String = "Preview"
    static let training: String = "Training"
    static let sensorData: String = "Sensor Data"
    static let vehicle: String = "Vehicle"
    static let gps: String = "GPS"
    static let accelerometer: String = "Accelerometer"
    static let magnetic: String = "Magnetic"
    static let gyroscope: String = "Gyroscope"
    static let delay: String = "Delay (ms)"
    static let images: String = "images"
    static let sensor: String = "sensor_data"
    static let timestamp: String = "timestamp[ns],frame\n"
    static let crop: String = "crop.jpeg"
    static let underscore: String = "_"
    static let comma: String = ","
    static let newLine: String = "\n"
    static let forwardSlash: String = "/"
    static let Autopilot: String = "Autopilot"
    static let ObjectTracking: String = "Object Tracking"
    static let autoMode: String = "Auto Mode"
    static let model: String = "Model"
    static let input: String = "Input"
    static let device: String = "Device"
    static let threads: String = "Threads"
    static let object: String = "Object"
    static let confidence: String = "Confidence"
    static let dynamicSpeed: String = "Dynamic Speed"

    // Settings
    static let camera: String = "Camera"
    static let microphone: String = "Microphone"
    static let location: String = "Location"
    static let permission: String = "Permissions"
    static let videoStreaming: String = "Video Streaming"
    static let bluetooth: String = "Bluetooth"
    static let webSignalingServer: String = "Web Signaling Server"
    static let webSignalingServerPlaceholder: String = "ws://192.168.1.40:8080/ws"

    // Notifications
    static let controllerConnected: String = "connectedWithControllerSuccessfully"
    static let clickSetting: String = "clickSetting"
    static let cancelButton: String = "cancelButton"
    static let switchCamera: String = "switchCamera"
    static let ble: String = "ble"
    static let logDataNotify: String = "log_data"
    static let updateSpeedMode: String = "update_speed"
    static let updateControlMode: String = "update_control"
    static let updateDriveMode: String = "update_drive_mode"
    static let updateResolution: String = "updateResolution"
    static let updatePreview: String = "updatePreview"
    static let updateTraining: String = "updateTraining"
    static let updateSensorsForLog: String = "updateSensorsForLog"

    // Screen Identifiers
    static let ScreenFreeRoam: String = "freeRoam"
    static let ScreenDataCollection: String = "dataCollection"
    static let ScreenControllerMapping: String = "controllerMapping"
    static let AutopilotFragment: String = "AutopilotFragment"
    static let ObjectTrackingFragment: String = "ObjectTrackingFragment"
    static let ScreenModelManagement: String = "ScreenModelManagement"
    static let ScreenBottomSheet: String = "ScreenBottomSheet"
    static let ScreenRobotInfo: String = "ScreenRobotInfo"
    static let ScreenNavigation: String = "ScreenNavigation"

    // UIVIew Identifiers
    static let secondView: String = "secondView"
    static let bluetoothScreen: String = "bluetoothScreen"

    // Logging Headers
    static let accelerationHeader: String = "timestamp[ns] x[m/s^2], y[m/s^2], z[m/s^2]\n"
    static let locationCoordinatesHeader: String = "timestamp latitude, longitude\n"
    static let gyroscopeHeader: String = "timestamp[ns], x[rad/s], yx[rad/s], zx[rad/s]\n"
    static let magnetometerHeader: String = "timestamp[ns] x[uT], y[uT], z[uT]\n"
    static let gpsHeader: String = "timestamp latitude, longitude, altitude[m], speed[m/s]\n"
    static let bumperHeader: String = "timestamp[ns], bumper\n"
    static let ctrlHeader: String = "timestamp[ns], leftCtrl, rightCtrl\n"
    static let indicatorHeader: String = "timestamp[ns], signal\n"
    static let inferenceTimeHeader: String = "frame, inferenceTime [ns]\n"
    static let lightHeader: String = "timestamp[ns], light[lux]\n"
    static let sonarHeader: String = "timestamp[ns], distance[cm]\n"
    static let voltageHeader: String = "timestamp[ns], batteryVoltage\n"
    static let wheelsHeader: String = "timestamp[ns], leftWheel, rightWheel\n"
    static let motionHeader: String = "timestamp[ns]"

    // Bluetooth Status
    static let connect: String = "Connect"
    static let disconnect: String = "Disconnect"
    static let connecting: String = "Connecting"
    static let disconnecting: String = "Disconnecting"

    // Model management
    static let modelDetails: String = "Model Details"
    static let name: String = "Name"
    static let type: String = "Type"
    static let `class`: String = "Class"
    static let inputOfModel: String = "Input(w x h)"
    static let tflite: String = ".tflite"
    static let cancel: String = "Cancel"
    static let done: String = "Done"
    static let file: String = "File"
    static let url: String = "URL"
    static let addNewModel: String = "Add New Model"

    // Robot Info
    static let voltageDivider: String = "Voltage Divider"
    static let sonarText: String = "Sonar"
    static let bumperText: String = "Bumpers"
    static let wheelOdometer: String = "Wheel Odometry"
    static let front: String = "Front"
    static let back: String = "Back"
    static let led: String = "Leds"
    static let indicatorText: String = "Indicators"
    static let status: String = "Status"
    static let motors: String = "Motors"
    static let forward: String = "Forward"
    static let backward: String = "Backward"
    static let stop: String = "Stop"
    static let readings: String = "Readings"
    static let battery: String = "Battery **.* V"
    static let speedText: String = "Speed (l,r) ***,*** rpm"
    static let sonarLabel: String = "Sonar *** cm"
    static let sendCommand: String = "Send Commands"
    static let lightLabel: String = "Lights"

    //Navigation
    static let setGoal: String = "Set Goal"
    static let setGoalText: String = "Mount the phone on the robot and \n specify a goal. The robot will try to\n reach the goal after pressing start."
    static let left: String = "Left"
    static let meter: String = "[m]"
    static let start: String = "START"
    static let canceled: String = "CANCEL"
    static let info: String = "Info"
    static let restart: String = "Restart"


    //open-code functions
    static let moveForward: String = "moveForward";
    static let loop: String = "loop";
    static let moveOpenBot: String = "moveOpenBot";
    static let moveCircular: String = "moveCircular";
    static let pause: String = "pause";
    static let moveBackward: String = "moveBackward";
    static let moveLeft: String = "moveLeft";
    static let moveRight: String = "moveRight";
    static let playSound: String = "playSound";
    static let playSoundSpeed: String = "playSoundSpeed";
    static let motorBackward: String = "motorBackward";
    static let motorForward: String = "motorForward";
    static let motorStop: String = "motorStop";
    static let playSoundMode: String = "playSoundMode";
    static let ledBrightness: String = "ledBrightness";
    static let leftIndicatorOn: String = "leftIndicatorOn";
    static let rightIndicatorOn: String = "rightIndicatorOn";
    static let indicatorOff: String = "indicatorOff";
    static let stopRobot: String = "stopRobot";
    static let rightIndicatorOff: String = "rightIndicatorOff";
    static let leftIndicatorOff: String = "leftIndicatorOff";
    static let sonarReading: String = "sonarReading";
    static let switchController: String = "switchController";
    static let switchDriveMode: String = "switchDriveMode";
    static let bumperCollision: String = "bumperCollision";
    static let speedReading: String = "speedReading";
    static let voltageDividerReading: String = "voltageDividerReading";
    static let backWheelReading: String = "backWheelReading";
    static let frontWheelReading: String = "frontWheelReading";
    static let gyroscopeReading:  String = "gyroscopeReading";
    static let accelerationReading: String = "accelerationReading";
    static let magneticReading: String = "magneticReading"


}
