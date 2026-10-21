//
// Created by Nitish Yadav on 07/04/23.
//

import Foundation
import JavaScriptCore
import AVFoundation
import UIKit

/**
 Class to evaluate the JS code inside openBot
 */
class jsEvaluator {
    private var command: String = "";
    private var vehicleControl: Control = Control();
    let semaphore = DispatchSemaphore(value: 0);
    let bluetooth = bluetoothDataController.shared;
    var jsContext: JSContext!;
    var runOpenBotThreadClass: runOpenBotThread?
    var cancelLoop: Bool = false;
    private let inferenceQueue = DispatchQueue(label: "openbot.jsEvaluator.inferencequeue")
    weak var delegate: autopilotDelegate?
    // Define your functions as closures

    /**
     initializer of jsEvaluator class
     - Parameter jsCode:
     */
    init(jsCode: String) {
        command = jsCode;
        setupCommand();
        initializeJS();
        evaluateJavaScript();
        NotificationCenter.default.addObserver(self, selector: #selector(cancelThread), name: .cancelThread, object: nil)
//        NotificationCenter.default.addObserver(self, selector: #selector(startTask), name: .startTask, object: nil);
        print("Js code is ", jsCode);
    }

    private func setupCommand() {
        if command.contains("forever()") {
            command = command.replacingOccurrences(of: "function forever (){ while(true){ ", with: "function forever (){ while(!" + String(self.cancelLoop) + "){ pause(0.03); ")
        }
    }

    deinit {
        print("called in deinit");
        runOpenBotThreadClass = nil
    }

    /**
     initializer of JSContext
     */
    func initializeJS() {
        jsContext = JSContext()
    }


    @objc func cancelThread() {
        print("cancle thread");
        cancelLoop = true;
        jsContext = nil;
        runOpenBotThreadClass?.cancel()
        runOpenBotThreadClass = nil
        bluetooth.sendDataFromJs(payloadData: "c" + String(0) + "," + String(0) + "\n");
    }
    
    /**
     function defined for all the methods of openBot blockly
     */
    func evaluateJavaScript() {
        self.runOpenBotThreadClass = runOpenBotThread();
        runOpenBotThreadClass?.start();
        inferenceQueue.async {
            if let context = JSContext() {
                let moveForward: @convention(block) (Float) -> Void = { (speed) in
                    self.runOpenBotThreadClass?.moveForward(speed: speed);

                }
                let loop: @convention(block) () -> Void = { () in
                    return (loop());
                }
                let start: @convention(block) () -> Void = { () in
                    self.runOpenBotThreadClass?.startBlock();
                }
                let moveOpenBot: @convention(block) (Int, Int) -> Void = { (left, right) in
                    self.runOpenBotThreadClass?.moveOpenBot(left: left, right: right);
                    if self.cancelLoop == true {
                        self.sendControl(control: Control());
                    }
                }
                let moveCircular: @convention(block) (Float) -> Void = { (radius) in
                    self.runOpenBotThreadClass?.moveCircular(radius: radius);
                }
                let stop: @convention(block) () -> Void = { () in
                    self.runOpenBotThreadClass?.stop();
                }
                let pause: @convention(block) (Double) -> Void = { (time) in
                    self.wait(forTime: time)
                    jsEvaluator.runOpenBotThread.sleep(forTimeInterval: time / 1000);
                }
                let moveBackward: @convention(block) (Float) -> Void = { (speed) in
                    self.runOpenBotThreadClass?.moveBackward(speed: speed);
                }
                let moveLeft: @convention(block) (Float) -> Void = { (speed) in
                    self.runOpenBotThreadClass?.moveLeft(speed: speed)
                }
                let moveRight: @convention(block) (Float) -> Void = { (speed) in
                    self.runOpenBotThreadClass?.moveRight(speed: speed)
                }
                let playSound: @convention(block) (String) -> Void = { (inputString) in
                    self.runOpenBotThreadClass?.playSound(inputString: inputString);
                }
                let playSoundSpeed: @convention(block) (String) -> Void = { (speed) in
                    self.runOpenBotThreadClass?.playSoundSpeed(speedMode: speed);
                }
                let setSpeed: @convention(block) (String) -> Void = { (speed) in
                    self.runOpenBotThreadClass?.setSpeed(speedMode: speed);
                }
                let motorBackward: @convention(block) () -> Void = { () in
                    self.runOpenBotThreadClass?.motorBackward();
                }
                let motorForward: @convention(block) () -> Void = { () in
                    self.runOpenBotThreadClass?.motorForward()
                }
                let motorStop: @convention(block) () -> Void = { () in
                    self.runOpenBotThreadClass?.motorStop()
                }
                let playSoundMode: @convention(block) (String) -> Void = { (mode) in
                    self.runOpenBotThreadClass?.playSoundMode(driveMode: mode);
                }
                let ledBrightness: @convention(block) (Int) -> Void = { brightnessFactor in
                    self.runOpenBotThreadClass?.setLedBrightness(factor: brightnessFactor);
                }
                let leftIndicatorOn: @convention(block) () -> Void = { () in

                    self.runOpenBotThreadClass?.setLeftIndicatorOn();
                }
                let rightIndicatorOn: @convention(block) () -> Void = { () in
                    self.runOpenBotThreadClass?.setRightIndicatorOn();
                }
                let indicatorOff: @convention(block) () -> Void = { () in
                    self.runOpenBotThreadClass?.indicatorOff();
                }
                let stopRobot: @convention(block) () -> Void = { () in
                    self.runOpenBotThreadClass?.stop()
                }
                let leftIndicatorOff: @convention(block) () -> Void = { () in
                    self.runOpenBotThreadClass?.indicatorOff();
                }
                let rightIndicatorOff: @convention(block) () -> Void = { () in
                    self.runOpenBotThreadClass?.indicatorOff();
                }
                let sonarReading: @convention(block) () -> Int = { () -> Int in
                    return self.runOpenBotThreadClass?.sonarReading() ?? 0;
                }
                let switchController: @convention(block) (String) -> Void = { (controller) in
                    self.runOpenBotThreadClass?.switchController(controller: controller)
                }
                let switchDriveMode: @convention(block) (String) -> Void = { (driveMode) in
                    self.runOpenBotThreadClass?.switchDriveMode(driveMode: driveMode)
                }
                let bumperCollision: @convention(block) () -> Void = { () in
                    self.runOpenBotThreadClass?.bumperCollision();
                }
                let speedReading: @convention(block) () -> Int = { () -> Int in
                    self.runOpenBotThreadClass?.speedReading() ?? 0;
                }
                let voltageDividerReading: @convention(block) () -> Double = { () -> Double in
                    self.runOpenBotThreadClass?.voltageDividerReading() ?? 0;
                }
                let frontWheelReading: @convention(block) () -> Float = { () -> Float in
                    self.runOpenBotThreadClass?.frontWheelReading() ?? 0;
                }
                let backWheelReading: @convention(block) () -> Float = { () -> Float in
                    self.runOpenBotThreadClass?.backWheelReading() ?? 0;
                }
                let gyroscopeReadingX: @convention(block) () -> Double = { () -> Double in
                    self.runOpenBotThreadClass?.gyroscopeReading(axis: "x") ?? 0.0;

                }
                let gyroscopeReadingY: @convention(block) () -> Double = { () -> Double in
                    self.runOpenBotThreadClass?.gyroscopeReading(axis: "y") ?? 0.0;

                }
                let gyroscopeReadingZ: @convention(block) () -> Double = { () -> Double in
                    self.runOpenBotThreadClass?.gyroscopeReading(axis: "z") ?? 0.0;

                }
                let accelerationReadingX: @convention(block) () -> Double = { () -> Double in
                    let temp = self.runOpenBotThreadClass?.accelerationReading(axis: "x") ?? 0.0;
                    print(temp);
                    return temp;
                }
                let accelerationReadingY: @convention(block) () -> Double = { () -> Double in
                    let temp = self.runOpenBotThreadClass?.accelerationReading(axis: "y") ?? 0.0;
                    print(temp);
                    return temp;
                }
                let accelerationReadingZ: @convention(block) () -> Double = { () -> Double in
                    let temp = self.runOpenBotThreadClass?.accelerationReading(axis: "z") ?? 0.0;
                    print(temp);
                    return temp;
                }
                let magneticReadingX: @convention(block) () -> Double = { () -> Double in
                    let temp = self.runOpenBotThreadClass?.magneticReading(axis: "x") ?? 0.0;
                    print("magnetic reading x", temp)
                    return temp;
                }
                let magneticReadingY: @convention(block) () -> Double = { () -> Double in
                    let temp = self.runOpenBotThreadClass?.magneticReading(axis: "y") ?? 0.0;
                    print(temp)
                    return temp;
                }
                let magneticReadingZ: @convention(block) () -> Double = { () -> Double in
                    let temp = self.runOpenBotThreadClass?.magneticReading(axis: "z") ?? 0.0;
                    print(temp)
                    return temp;
                }

                let navigationModel: @convention(block) (String) -> Void = { (model) in
                    self.runOpenBotThreadClass?.navigationModel(model: model);
                }

                let reachGoal: @convention(block) (Double, Double) -> Void = { (forward, left) in
                    self.runOpenBotThreadClass?.reachGoal(forward: forward, left: left);
                }

                let reachPosition: @convention(block) (Double, Double) -> Void = { (x, y) in
                    self.runOpenBotThreadClass?.reachPosition(x: x, y: y);
                }

                let follow: @convention(block) (String, String) -> Void = { (object, model) in
                    self.runOpenBotThreadClass?.follow(object: object, model: model);
                }

                let onDetect: @convention(block) (String, String, String) -> Void = { (object, model, task) in
                    self.runOpenBotThreadClass?.onDetect(object: object, model: model, task: task);
                }

                let toggleLed: @convention(block) (String) -> Void = { (status) in
                    switch status {
                    case "ON":
                        self.runOpenBotThreadClass?.setLedBrightness(factor: 100);
                        break;
                    case "OFF":
                        self.runOpenBotThreadClass?.setLedBrightness(factor: 0);
                        break;
                    default:
                        self.runOpenBotThreadClass?.setLedBrightness(factor: 100);
                    }
                }

                let enableAutopilot: @convention(block) (String) -> Void = { (modelName) in
                    self.runOpenBotThreadClass?.enableAutopilot(model: modelName);
                    self.delegate?.didPerformAction();
                }

                let enableMultipleDetection: @convention(block) (String, String, String, String) -> Void = { (object1, model, object2, task) in
                    self.runOpenBotThreadClass?.enableMultipleDetection(object1: object1, model: model, object2: object2, task: task);
                }

                let enableMultipleAI: @convention(block) (String, String, String, String) -> Void = { (autoPilotModelName, task, object, detectorModel) in
                    print(autoPilotModelName, task, object, detectorModel);
//                    enableAutopilot(autoPilotModelName);
                    self.runOpenBotThreadClass?.startMultipleAI(autoPilotModelName: autoPilotModelName, task: task, object: object, detectorModel: detectorModel);
                }

                let disableAI: @convention(block) () -> Void = { () in
                    self.runOpenBotThreadClass?.disableAI()
                }

                let onLostFrames: @convention(block) (String, Int, String) -> Void = { (object, frames, task) in
                    self.runOpenBotThreadClass?.onLostFrames(object: object, frames: frames, task: task);
                }
                
                let displaySensorData: @convention(block) (String) -> Void = { (inputString) in
                    self.runOpenBotThreadClass?.displaySensorData(inputString: inputString);
                }
                
                let displayString: @convention(block) (String) -> Void = { (inputString) in
                    self.runOpenBotThreadClass?.displayString(inputString: inputString);
                }

                context.setObject(moveForward,
                        forKeyedSubscript: Strings.moveForward as NSString)
                context.setObject(loop,
                        forKeyedSubscript: Strings.loop as NSString)
                context.setObject(start,
                        forKeyedSubscript: Strings.start as NSString)
                context.setObject(moveOpenBot,
                        forKeyedSubscript: Strings.moveOpenBot as NSString)
                context.setObject(moveCircular,
                        forKeyedSubscript: Strings.moveCircular as NSString)
                context.setObject(stop,
                        forKeyedSubscript: Strings.stop as NSString)
                context.setObject(pause,
                        forKeyedSubscript: Strings.pause as NSString)
                context.setObject(moveBackward,
                        forKeyedSubscript: Strings.moveBackward as NSString);
                context.setObject(moveLeft,
                        forKeyedSubscript: Strings.moveLeft as NSString);
                context.setObject(moveRight,
                        forKeyedSubscript: Strings.moveRight as NSString);
                context.setObject(playSound,
                        forKeyedSubscript: Strings.playSound as NSString);
                context.setObject(playSoundSpeed,
                        forKeyedSubscript: Strings.playSoundSpeed as NSString);
                context.setObject(motorBackward,
                        forKeyedSubscript: Strings.motorBackward as NSString);
                context.setObject(motorForward,
                        forKeyedSubscript: Strings.motorForward as NSString);
                context.setObject(motorStop,
                        forKeyedSubscript: Strings.motorStop as NSString);
                context.setObject(playSoundMode,
                        forKeyedSubscript: Strings.playSoundMode as NSString);
                context.setObject(ledBrightness,
                        forKeyedSubscript: Strings.ledBrightness as NSString);
                context.setObject(leftIndicatorOn,
                        forKeyedSubscript: Strings.leftIndicatorOn as NSString);
                context.setObject(rightIndicatorOn,
                        forKeyedSubscript: Strings.rightIndicatorOn as NSString);
                context.setObject(indicatorOff,
                        forKeyedSubscript: Strings.indicatorOff as NSString);
                context.setObject(stopRobot,
                        forKeyedSubscript: Strings.stopRobot as NSString);
                context.setObject(rightIndicatorOff,
                        forKeyedSubscript: Strings.rightIndicatorOff as NSString);
                context.setObject(leftIndicatorOff,
                        forKeyedSubscript: Strings.leftIndicatorOff as NSString);
                context.setObject(sonarReading,
                        forKeyedSubscript: Strings.sonarReading as NSString);
                context.setObject(switchController,
                        forKeyedSubscript: Strings.switchController as NSString);
                context.setObject(switchDriveMode,
                        forKeyedSubscript: Strings.switchDriveMode as NSString);
                context.setObject(bumperCollision,
                        forKeyedSubscript: Strings.bumperCollision as NSString);
                context.setObject(speedReading,
                        forKeyedSubscript: Strings.speedReading as NSString);
                context.setObject(voltageDividerReading,
                        forKeyedSubscript: Strings.voltageDividerReading as NSString);
                context.setObject(backWheelReading,
                        forKeyedSubscript: Strings.backWheelReading as NSString);
                context.setObject(frontWheelReading,
                        forKeyedSubscript: Strings.frontWheelReading as NSString);
                context.setObject(gyroscopeReadingX,
                        forKeyedSubscript: "gyroscopeReadingX" as NSString);
                context.setObject(gyroscopeReadingY,
                        forKeyedSubscript: "gyroscopeReadingY" as NSString);
                context.setObject(gyroscopeReadingZ,
                        forKeyedSubscript: "gyroscopeReadingZ" as NSString);
                context.setObject(accelerationReadingX,
                        forKeyedSubscript: "accelerationReadingX" as NSString);
                context.setObject(accelerationReadingY,
                        forKeyedSubscript: "accelerationReadingY" as NSString);
                context.setObject(accelerationReadingZ,
                        forKeyedSubscript: "accelerationReadingZ" as NSString);
                context.setObject(magneticReadingX,
                        forKeyedSubscript: "magneticReadingX" as NSString);
                context.setObject(magneticReadingY,
                        forKeyedSubscript: "magneticReadingY" as NSString);
                context.setObject(magneticReadingZ,
                        forKeyedSubscript: "magneticReadingZ" as NSString);
                context.setObject(navigationModel,
                        forKeyedSubscript: "navigationModel" as NSString);
                context.setObject(reachGoal,
                        forKeyedSubscript: "reachGoal" as NSString)
                context.setObject(follow,
                        forKeyedSubscript: "follow" as NSString);
                context.setObject(toggleLed,
                        forKeyedSubscript: "toggleLed" as NSString);
                context.setObject(reachPosition,
                        forKeyedSubscript: "reachPosition" as NSString);
                context.setObject(setSpeed,
                        forKeyedSubscript: "setSpeed" as NSString)
                context.setObject(enableAutopilot,
                        forKeyedSubscript: "enableAutopilot" as NSString);
                context.setObject(enableMultipleDetection,
                        forKeyedSubscript: "enableMultipleDetection" as NSString);
                context.setObject(enableMultipleAI,
                        forKeyedSubscript: "enableMultipleAI" as NSString);
                context.setObject(disableAI,
                        forKeyedSubscript: "disableAI" as NSString);
                context.setObject(onDetect,
                        forKeyedSubscript: "onDetect" as NSString);
                context.setObject(onLostFrames,
                        forKeyedSubscript: "onLostFrames" as NSString);
                context.setObject(displaySensorData,
                        forKeyedSubscript: "displaySensorData" as NSString);
                context.setObject(displayString,
                        forKeyedSubscript: "displayString" as NSString);
                /// evaluateScript should be called below of setObject
                context.evaluateScript(self.command);
            }

        }

        func loop() {

        }

    }

    /**
     Function to implement the wait
     - Parameter forTime:
     */
    func wait(forTime: Double) {
        if cancelLoop {
            return;
        }
        print("inside wait", forTime);
        if forTime != 0.03 {
            NotificationCenter.default.post(name: .commandName, object: "wait for \(forTime)");
        }
    }

    /**
     Function to send commands to bluetooth
     - Parameter control:
     */
    func sendControl(control: Control) {
        if (control.getRight() != vehicleControl.getRight() || control.getLeft() != vehicleControl.getLeft()) {
            let left = (control.getLeft() * gameController.selectedSpeedMode.rawValue).rounded(FloatingPointRoundingRule.toNearestOrAwayFromZero)
            let right = (control.getRight() * gameController.selectedSpeedMode.rawValue).rounded(FloatingPointRoundingRule.toNearestOrAwayFromZero)
            bluetooth.sendDataFromJs(payloadData: "c" + String(left) + "," + String(right) + "\n")
        }
    }

    /**
     Internal class of type thread to implement wait
     */
    class runOpenBotThread: Thread {
        weak var jsEvaluator: jsEvaluator?
        let bluetooth = bluetoothDataController.shared
        private var vehicleControl: Control = Control();
        private let sensor = sensorDataRetrieve.shared
        private let audioPlayer = AudioPlayer.shared
        var preferencesManager : SharedPreferencesManager = SharedPreferencesManager()
        /**
         function to send controls to openBot
         - Parameter control:
         */

        func sendControl(control: Control) {
            if isCancelled {
                bluetooth.sendDataFromJs(payloadData: "c" + String(0) + "," + String(0) + "\n")
                return
            }
            jsEvaluator?.wait(forTime: 10);
            bluetooth.sendDataFromJs(payloadData: "c" + String(control.getLeft()) + "," + String(control.getRight()) + "\n")
        }


        /**
         main function to thread
         */
        override func main() {
            jsEvaluator?.semaphore.signal()
        }

        /**
         Start of block
         */
        func startBlock() {
            if isCancelled {
                return
            }
        }

        /**
         Loop of blocks
         */
        func loop() {
            if isCancelled {
                return
            }
        }

        /**
         function for move forward the robot
         - Parameter speed:
         */
        func moveForward(speed: Float) {
            if isCancelled {
                sendControl(control: Control());
                return
            }
            print("inside move forward", speed)
            NotificationCenter.default.post(name: .commandName, object: "move Forward \(speed)");
            let carControl = Control(left: speed, right: speed)
            sendControl(control: carControl);
        }

        /**
         Function to move openBot
         - Parameters:
           - left: left wheel speed
           - right: right wheel speed
         */
        func moveOpenBot(left: Int, right: Int) {
            print("inside move")
            if isCancelled {
                sendControl(control: Control());
                return
            }
            NotificationCenter.default.post(name: .commandName, object: "move OpenBot \(left) left and \(right) right");
            let carControl = Control(left: Float(left), right: Float(right));
            sendControl(control: carControl);
        }

        /**

         - Parameter radius:
         */
        func moveCircular(radius: Float) {
            if isCancelled {
                sendControl(control: Control());
                return
            }
            print("inside moveCircular")
            bluetooth.sendDataFromJs(payloadData: "c" + String(200) + "," + String(200) + "\n");
        }

        /**
         Function to stop the car
         */
        func stop() {
            if isCancelled {
                sendControl(control: Control());
                return
            }
            print("inside stop robot")
            NotificationCenter.default.post(name: .commandName, object: "Stop");
            runRobot.isObjectTracking = false;
            runRobot.isMultipleObjectTracking = false;
            runRobot.isPointGoalNavigation = false;
            let control = Control(left: 0, right: 0);
            sendControl(control: control);
            while (bluetooth.peri != nil && bluetooth.speedometer != "w0.00,0.00") {
                bluetooth.sendDataFromJs(payloadData: "c" + String(0) + "," + String(0) + "\n")
            }
            bluetooth.sendDataFromJs(payloadData: "c" + String(0) + "," + String(0) + "\n")
        }

        /**
         Function to move moveBackward the car
         - Parameter speed:
         */
        func moveBackward(speed: Float) {
            if isCancelled {
                sendControl(control: Control());
                return
            }
            print("inside moveBackward", speed)
            NotificationCenter.default.post(name: .commandName, object: "move Backward \(speed)");
            let carControl = Control(left: -speed, right: -speed);
            sendControl(control: carControl);

        }

        /**
         function to turn left the car
         - Parameter speed:
         */
        func moveLeft(speed: Float) {
            if isCancelled {
                sendControl(control: Control());
                return
            }
            print("inside moveLeft", speed)
            NotificationCenter.default.post(name: .commandName, object: "move Left \(speed)");
            let carControl = Control(left: 0, right: speed);
            sendControl(control: carControl);
        }

        /**
         Function to turn right car
         - Parameter speed:
         */
        func moveRight(speed: Float) {
            if isCancelled {
                sendControl(control: Control());
                return
            }
            print("inside Right", speed)
            NotificationCenter.default.post(name: .commandName, object: "move Right \(speed)");
            let carControl = Control(left: speed, right: 0);
            sendControl(control: carControl);
        }

        func playSound(inputString: String) {
            if isCancelled {
                return
            }
            NotificationCenter.default.post(name: .commandName, object: "Play input sound");
            audioPlayer.playInputString(input: inputString);
        }

        func playSoundSpeed(speedMode: String) {
            if isCancelled {
                return
            }
            NotificationCenter.default.post(name: .commandName, object: "Play Sound \(speedMode)");
            switch speedMode {
            case "slow":
                audioPlayer.playSpeedMode(speedMode: .SLOW);
                break
            case "medium":
                audioPlayer.playSpeedMode(speedMode: .NORMAL);
                break
            case "fast":
                audioPlayer.playSpeedMode(speedMode: .FAST);
                break
            default:
                break
            }
        }

        func setSpeed(speedMode: String){
            if isCancelled {
                return
            }
            NotificationCenter.default.post(name: .commandName, object: "Set speed to \(speedMode)");
            switch speedMode {
            case "slow":
                gameController.selectedSpeedMode = .SLOW;
                preferencesManager.setSpeedMode(value: SpeedMode.SLOW.rawValue);
                break
            case "medium":
                gameController.selectedSpeedMode = .NORMAL;
                preferencesManager.setSpeedMode(value: SpeedMode.NORMAL.rawValue);
                break
            case "fast":
                gameController.selectedSpeedMode = .FAST;
                preferencesManager.setSpeedMode(value: SpeedMode.FAST.rawValue);
                break
            default:
                break
            }
        }

        func playSoundMode(driveMode: String) {
            if isCancelled {
                return
            }
            print("playSoundMode \(driveMode)");
            NotificationCenter.default.post(name: .commandName, object: "play Drive Mode \(driveMode)");
            switch driveMode {
            case "dual drive":
                audioPlayer.playDriveMode(driveMode: .DUAL);
                break
            case "joystick control":
                audioPlayer.playDriveMode(driveMode: .JOYSTICK);
                break
            case "gamepad":
                audioPlayer.playDriveMode(driveMode: .GAME);
                break
            default:
                break
            }
        }

        /**
         Function to move motor backward with full speed
         */
        func motorBackward() {
            if isCancelled {
                sendControl(control: Control());
                return
            }
            NotificationCenter.default.post(name: .commandName, object: "Motor Backward ");
            let control = Control(left: -192, right: -192);
            sendControl(control: control);
        }

        /**
         Function to move forward
         */
        func motorForward() {
            if isCancelled {
                sendControl(control: Control());
                return
            }
            NotificationCenter.default.post(name: .commandName, object: "Motor Backward ");
            let control = Control(left: 192, right: 192);
            sendControl(control: control);
        }

        /**
         Function to stop motor
         */
        func motorStop() {
            if isCancelled {
                sendControl(control: Control());
                return
            }
            NotificationCenter.default.post(name: .commandName, object: "Motor stop ");
            let control = Control(left: 0, right: 0);
            sendControl(control: control);
        }

        /**
         handler of led of openBot
         - Parameter factor:
         */
        func setLedBrightness(factor: Int) {
            if isCancelled {
                bluetooth.sendDataFromJs(payloadData: "l" + String(0) + "," + String(0) + "\n")
                return
            }
            print("inside setLedBrightness");
            NotificationCenter.default.post(name: .commandName, object: "Set Led BrightNess \(factor) ");
            let front = (factor * 255) / 100
            let back = ((factor * 255)) / 100
            bluetooth.sendDataFromJs(payloadData: "l" + String(front) + "," + String(back) + "\n");
        }

        /**
         Function to turn on left indicator
         */
        func setLeftIndicatorOn() {
            if isCancelled {
                bluetooth.sendDataFromJs(payloadData: "i0,0\n");
                return
            }
            NotificationCenter.default.post(name: .commandName, object: "Left Indicator On");
            print("inside setLeftIndicatorOn");
            let indicatorValues = "i1,0\n"
            bluetooth.sendDataFromJs(payloadData: indicatorValues)
        }

        /**
         Function to turn on right indicator
         */
        func setRightIndicatorOn() {
            if isCancelled {
                bluetooth.sendDataFromJs(payloadData: "i0,0\n")
                return
            }
            NotificationCenter.default.post(name: .commandName, object: "Right Indicator On");
            print("inside setRightIndicatorOn");
            let indicatorValues = "i0,1\n"
            bluetooth.sendDataFromJs(payloadData: indicatorValues)
        }

        /**
         Function to turn off  indicators
         */
        func indicatorOff() {
            if isCancelled {
                return
            }
            NotificationCenter.default.post(name: .commandName, object: "Indicator Off");
            let indicatorValues = "i0,0\n";
            bluetooth.sendDataFromJs(payloadData: indicatorValues)
        }

        /**
        function to get sonar sensor reading
         - Returns:
         */
        func sonarReading() -> Int {
            if isCancelled {
                return 0
            }
            NotificationCenter.default.post(name: .commandName, object: "Get Sonar Value");
            return bluetooth.getSonar();
        }

        /**
         Function to change drive mode
         - Parameter driveMode:
         */
        func switchDriveMode(driveMode: String) {
            if isCancelled {
                return
            }
            NotificationCenter.default.post(name: .commandName, object: "Switch Drive Mode");
            switch driveMode {
            case "dual":
                gameController.selectedDriveMode = .DUAL;
                preferencesManager.setDriveMode(value: DriveMode.GAME.rawValue);
                break;
            case "joystick":
                gameController.selectedDriveMode = .JOYSTICK;
                preferencesManager.setDriveMode(value: DriveMode.DUAL.rawValue);
                break;
            case "game":
                gameController.selectedDriveMode = .GAME;
                preferencesManager.setDriveMode(value: DriveMode.JOYSTICK.rawValue);
                break;
            default:
                break;
            }
        }

        /**
         function to switch between phone controller and game controller
         - Parameter controller:
         */
        func switchController(controller: String) {
            if isCancelled {
                return
            }
            NotificationCenter.default.post(name: .commandName, object: "Switch Controller");
            switch controller {
            case "phone":
                gameController.selectedControlMode = .PHONE;
                preferencesManager.setControlMode(value: ControlMode.GAMEPAD.rawValue);
                break;
            case "gamepad":
                gameController.selectedControlMode = .GAMEPAD;
                preferencesManager.setControlMode(value: ControlMode.WEB.rawValue);
                break;
            case "web":
                gameController.selectedControlMode = .WEB;
                preferencesManager.setControlMode(value: ControlMode.PHONE.rawValue);
            default:
                break;
            }
        }

        /**
         function to get bumper logs
         */
        func bumperCollision() {
            if isCancelled {
                return
            }
            NotificationCenter.default.post(name: .commandName, object: "Motor Backward ");
            print(bluetooth.bumperData.count)
        }

        /**
         Function retuns speed of vehicle
         - Returns:
         */
        func speedReading() -> Int {
            if isCancelled {
                return 0
            }
            NotificationCenter.default.post(name: .commandName, object: "Get Speed reading");
            return bluetooth.getSpeed();
        }

        /**
         Function returns voltage divider reading
         - Returns:
         */
        func voltageDividerReading() -> Double {
            if isCancelled {
                return 0;
            }
            NotificationCenter.default.post(name: .commandName, object: "get voltage divider");
            return bluetooth.getVoltage();
        }

        /**
         Function returns right front wheel speed
         - Returns:
         */
        func backWheelReading() -> Float {
            if isCancelled {
                return 0;
            }
            let speedometer = bluetooth.speedometer;
            if speedometer != "" {
                let index_1 = speedometer.index(after: speedometer.startIndex)
                let indexOfComma = speedometer.firstIndex(of: ",") ?? index_1
                let index_2 = speedometer.index(before: indexOfComma)
                let rightFont = Float(speedometer[speedometer.index(after: indexOfComma)...])
                return rightFont ?? 0.0;
            }
            return 0;
        }

        /**
         Function returns left front wheel reading
         - Returns:
         */
        func frontWheelReading() -> Float {
            if isCancelled {
                return 0;
            }
            let speedometer = bluetooth.speedometer;
            if speedometer != "" {
                let index_1 = speedometer.index(after: speedometer.startIndex)
                let indexOfComma = speedometer.firstIndex(of: ",") ?? index_1
                let index_2 = speedometer.index(before: indexOfComma)
                let leftFront = Float(speedometer[index_1...index_2])
                return leftFront ?? 0.0;
            }
            return 0;
        }

        func gyroscopeReading(axis: String) -> Double {
            if isCancelled {
                return 0.0;
            }
            sensor.sampleIMU();
            switch axis {
            case "x":
                return sensor.angularRateX;
            case "y":
                return sensor.angularRateY;
            case "z":
                return sensor.angularRateZ;
            default:
                return 0.0
            }
        }

        func accelerationReading(axis: String) -> Double {
            if isCancelled {
                return 0.0;
            }
            sensor.sampleIMU();
            switch axis {
            case "x":
                return sensor.accelerationX;
            case "y":
                return sensor.accelerationY;
            case "z":
                return sensor.accelerationZ;
            default:
                return 0.0
            }
        }

        func magneticReading(axis: String) -> Double {
            if isCancelled {
                return 0.0;
            }
            sensor.sampleIMU();
            switch axis {
            case "x":
                return sensor.magneticFieldX;
            case "y":
                return sensor.magneticFieldY;
            case "z":
                return sensor.magneticFieldZ;
            default:
                return 0.0
            }
        }

        func navigationModel(model: String) {
            if isCancelled {
                return
            }
            print(model);
        }

        /**
         Function for starting point goal navigation via blockly
         - Parameters:
           - forward:
           - left:
         */

        func reachGoal(forward: Double, left: Double) {
            if isCancelled {
                return
            }
            runRobot.enablePointGoalNavigation(forward: forward, left: left);
            NotificationCenter.default.post(name: .pointGoalNav, object: true);
            NotificationCenter.default.post(name: .commandName, object: "reach goal");
            print(forward, left);
        }

        /**
         Function for starting point goal navigation via blockly
         - Parameters:
           - x:
           - y:
         */
        func reachPosition(x: Double, y: Double) {
            if isCancelled {
                return
            }
            print(x, y);
        }

        /**
         Function for staring object detection via blockly
         - Parameters:
           - object:
           - model:
         */
        func follow(object: String, model: String) {
            if isCancelled {
                return
            }
            runRobot.detector?.setSelectedClass(newClass: object)
            NotificationCenter.default.post(name: .commandName, object: "follow");
            print(object, model);
            runRobot.enableObjectTracking(object: object, model: model);
        }

        /**
     A method that call static method enableAutopilot to initialize autopilot
     - Parameter model:
     */
        func enableAutopilot(model: String) {
            if isCancelled {
                return
            }
            runRobot.enableAutopilot(model: model);
        }

        /**
            Function to start multiple detection
         - Parameters:
           - object1:
           - model:
           - object2:
         */
        func enableMultipleDetection(object1: String, model: String, object2: String, task: String) {
            if isCancelled {
                return
            }
            NotificationCenter.default.post(name: .commandObject, object: [object1, object2]);
            NotificationCenter.default.post(name: .commandName, object: "multiple detection");
            runRobot.enableMultipleObjectTracking(object1: object1, model: model, object2: object2);
            NotificationCenter.default.post(name: .createCameraView, object: task);
        }

        /**
         Function to start multiple AI
         - Parameters:
           - autoPilotModelName: name of autopilot model
           - task: task to perform after detecting object
           - object: target object for detector
           - detectorModel: name of object tracking model
         */
        func startMultipleAI(autoPilotModelName: String, task: String, object: String, detectorModel: String) {
            if isCancelled {
                return
            }
            runRobot.detector?.setSelectedClass(newClass: object)
            runRobot.enableMultipleAI(autoPilotModel: autoPilotModelName, task: task, object: object, detectorModel: detectorModel);
            NotificationCenter.default.post(name: .createCameraView, object: task);
        }

        /**
         Function to call static method of runRobot class to stop AI block
         */
        func disableAI() {
            runRobot.disableAI();
        }

        /**
         Function to call static method of runRobot class to enable detection
         - Parameters:
           - object:
           - model:
           - task:
         */
        func onDetect(object: String, model: String, task: String) {
            if isCancelled {
                return
            }
            runRobot.onDetection(object: object, model: model, task: task);
            taskStorage.addAttribute(classType: object, task: task, frames: 0, type: "detect");
            NotificationCenter.default.post(name: .createCameraView, object: "");
        }

        /**

         - Parameters:
           - object:
           - frames:
           - task:
         */
        func onLostFrames(object: String, frames: Int, task: String) {
            if isCancelled {
                return
            }
            taskStorage.addAttribute(classType: object, task: task, frames: frames, type: "unDetect");
        }
        
        func displaySensorData(inputString: String){
            if isCancelled {
                return
            }
            NotificationCenter.default.post(name: .displayItems, object: inputString);
        }
        
        func displayString(inputString: String){
            if isCancelled {
                return
            }
            NotificationCenter.default.post(name: .displayItems, object: inputString);
        }
    }
}

