//
//  Created by Sparsh Jain on 29/09/22.
//

import Foundation
import AVFoundation
import UIKit

class AutopilotFragment: CameraController {
    var autopilot: Autopilot?;
    var models: [Model] = [];
    var numberOfThreads: Int = 1
    var expandedAutoPilotView: expandedAutoPilot? = nil
    var autoPilotMode: Bool = false;
    let gameController = GameController.shared
    let bluetooth = bluetoothDataController.shared;
    var vehicleControl: Control = Control();
    var currentModel: ModelItem!
    var currentDevice: RuntimeDevice = RuntimeDevice.CPU
    var bottomAnchorConstraint: NSLayoutConstraint!
    var trailingAnchorConstraint: NSLayoutConstraint!
    private let inferenceQueue = DispatchQueue(label: "openbot.autopilot.inferencequeue")
    private var isInferenceQueueBusy = false
    private var result: Control?
    var autopilotEnabled = false
    let webSocketMsgHandler = WebSocketMessageHandler();
    let fragmentType = FragmentType.shared

    /// Called after the view fragment has loaded.
    override func viewDidLoad() {
        super.viewDidLoad()
        DeviceCurrentOrientation.shared.findDeviceOrientation()
        expandedAutoPilotView = expandedAutoPilot(frame: CGRect(x: 0, y: height - 375, width: width, height: 400))
        expandedAutoPilotView!.backgroundColor = Colors.freeRoamButtonsColor
        expandedAutoPilotView!.layer.cornerRadius = 15
        createCameraView()
        let modelItems = Common.loadAllModelItemsFromBundle()
        if (modelItems.count > 0) {
            models = Model.fromModelItems(list: modelItems);
            currentModel = modelItems[0]
            autopilot = Autopilot(model: models[0], device: RuntimeDevice.CPU, numThreads: numberOfThreads);
        }
        if let threads = preferencesManager.getThreads(){
            numberOfThreads = Int(threads) ?? 1
            autopilot = Autopilot(model: Model.fromModelItem(item: currentModel), device: currentDevice, numThreads: numberOfThreads);
        }
        if let device = preferencesManager.getDevice(){
            currentDevice = RuntimeDevice(rawValue: device) ?? RuntimeDevice.CPU
            autopilot = Autopilot(model: Model.fromModelItem(item: currentModel), device: currentDevice, numThreads: numberOfThreads);
            autopilot?.tfliteOptions.threadCount = numberOfThreads
        }
        if let models = preferencesManager.getAutopilotModel(){
            if Common.isModelItemAvailableInDocument(modelName: models) == true {
                let selectedModelName = models;
                currentModel = Common.returnModelItem(modelName: selectedModelName)
                autopilot = Autopilot(model: Model.fromModelItem(item: currentModel), device: currentDevice, numThreads: numberOfThreads)
            }
        }
        view.addSubview(expandedAutoPilotView!)
        expandedAutoPilotView!.translatesAutoresizingMaskIntoConstraints = false
        expandedAutoPilotView!.widthAnchor.constraint(equalToConstant: width).isActive = true
        expandedAutoPilotView!.heightAnchor.constraint(equalToConstant: width).isActive = true
        bottomAnchorConstraint = expandedAutoPilotView!.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: 5);
        trailingAnchorConstraint = expandedAutoPilotView!.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor, constant: 0)
        trailingAnchorConstraint.isActive = true
        bottomAnchorConstraint.isActive = true
        setupNavigationBarItem()
        NotificationCenter.default.addObserver(self, selector: #selector(switchCamera), name: .switchCamera, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(openBluetoothSettings), name: .ble, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateDevice), name: .updateDevice, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateThread), name: .updateThread, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(toggleAutoMode), name: .autoMode, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateModel), name: .updateModel, object: nil);
        NotificationCenter.default.addObserver(self, selector: #selector(updateDataFromControllerApp), name: .updateStringFromControllerApp, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateLightsCommandFromControllerApp), name: .updateLightsCommandFromControllerApp, object: nil)
        gameController.resetControl = false
        fragmentType.currentFragment = "Autopilot";
        calculateFrame()
        let msg = JSON.toString(FragmentTypeEvent(status: .init(FRAGMENT_TYPE: self.fragmentType.currentFragment)));
        client.send(message: msg);
        //start the server
        var serverListener = ServerListener();
        serverListener.start();
    }

    /// Called when the view controller's view's size is changed by its parent (i.e. for the root view controller when its window rotates or is resized).
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
        calculateFrame()
    }

    /// calculating whether  device orientation is portrait or landscape
    func calculateFrame() {
        if currentOrientation == .portrait || currentOrientation == .portraitUpsideDown {
            trailingAnchorConstraint.constant = 0;
        } else {
            trailingAnchorConstraint.constant = 0;
        }
    }

    /// Creating back navigation button, this method creates default navigation button
    func setupNavigationBarItem() {
        if UIImage(named: "back") != nil {
            let backNavigationIcon = (UIImage(named: "back")?.withRenderingMode(.alwaysOriginal))!
            let newBackButton = UIBarButtonItem(image: backNavigationIcon, title: Strings.autoPilot, target: self, action: #selector(AutopilotFragment.back(sender:)), titleColor: Colors.navigationColor ?? .white)
            navigationItem.leftBarButtonItem = newBackButton
        }
    }

    /// function that remove viewController from navigation
    @objc func back(sender: UIBarButtonItem) {
        _ = navigationController?.popViewController(animated: true)
    }

    /// function to switch camera from front to back and vice versa, called when camera icon is pressed
    @objc func switchCamera() {
        switchCameraView();
    }

    /// function to open bluetooth screen, called when Bluetooth icon is pressed
    @objc func openBluetoothSettings() {
        let nextViewController = (storyboard?.instantiateViewController(withIdentifier: Strings.bluetoothScreen))
        navigationController?.pushViewController(nextViewController!, animated: true)
    }

    /// function to device from CPU to GPU, called after selection of device from device dropdown
    @objc func updateDevice(_ notification: Notification) {
        currentDevice = RuntimeDevice(rawValue: notification.object as! String) ?? RuntimeDevice.CPU
        autopilot = Autopilot(model: Model.fromModelItem(item: currentModel), device: currentDevice, numThreads: numberOfThreads);
        currentDevice.rawValue == "GPU" ? NotificationCenter.default.post(name: .updateThreadLabel, object: "N/A") : NotificationCenter.default.post(name: .updateThreadLabel, object: String(numberOfThreads))
        autopilot?.tfliteOptions.threadCount = numberOfThreads
        preferencesManager.setDevice(value: notification.object as! String);
    }

    ///function to change the autopilot models, called after model from models dropdown is selected.
    @objc func updateModel(_ notification: Notification) {
        let selectedModelName = notification.object as! String
        currentModel = Common.returnModelItem(modelName: selectedModelName)
        autopilot = Autopilot(model: Model.fromModelItem(item: currentModel), device: currentDevice, numThreads: numberOfThreads)
        preferencesManager.setAutopilotModel(value: notification.object as! String);
    }

    /// function to turn on and off autopilot
    @objc func toggleAutoMode() {
        autoPilotMode = !autoPilotMode;
    }

    /// function to change number of threads,being called ofter plus and minus icon is pressed
    @objc func updateThread(_ notification: Notification) {
        let threadCount = notification.object as! String
        numberOfThreads = Int(threadCount) ?? 1
        autopilot = Autopilot(model: Model.fromModelItem(item: currentModel), device: currentDevice, numThreads: numberOfThreads);
        preferencesManager.setThreads(value: notification.object as! String);
    }

    ///function to send output controls to openBot
    ///
    /// - Parameter control:
    func sendControl(control: Control) {
        if (control.getRight() != vehicleControl.getRight() || control.getLeft() != vehicleControl.getLeft()) {
            let left = control.getLeft() * gameController.selectedSpeedMode.rawValue
            let right = control.getRight() * gameController.selectedSpeedMode.rawValue
            vehicleControl = control
            bluetooth.sendData(payload: "c" + String(left) + "," + String(right) + "\n")
            NotificationCenter.default.post(name: .updateSpeedLabel, object: String(Int(left)) + "," + String(Int(right)))
        }
    }

    /// Called after the view was dismissed, covered or otherwise hidden.
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        if autoPilotMode {
            autoPilotMode = false
        }
         
        let msg = JSON.toString(self.fragmentType.closeFragment())
        client.send(message: msg)
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        print("memory is low");
    }

    /// called after camera capture each frame.
    ///
    /// - Parameters:
    ///   - output:
    ///   - sampleBuffer:
    ///   - connection:
    override func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {

        // extract the image buffer from the sample buffer
        let pixelBuffer: CVPixelBuffer? = CMSampleBufferGetImageBuffer(sampleBuffer)

        guard let imagePixelBuffer = pixelBuffer else {
            debugPrint("unable to get image from sample buffer")
            return
        }
        if webRTCClient != nil {
            inferenceQueue.async {
                webRTCClient.captureCurrentFrame(sampleBuffer: sampleBuffer);
                self.isInferenceQueueBusy = false
            }
        }
        guard !isInferenceQueueBusy else {
            return
        }

        inferenceQueue.async {
            if self.autoPilotMode {
                self.isInferenceQueueBusy = true
                let startTime = Date().millisecondsSince1970
                self.result = self.autopilot?.recognizeImage(pixelBuffer: imagePixelBuffer, indicator: 0) ?? Control();
                guard let controlResult = self.result else {
                    return
                }
                let endTime = Date().millisecondsSince1970
                DispatchQueue.main.async {
                    if (endTime - startTime) != 0 {
                        NotificationCenter.default.post(name: .updateAutoPilotFps, object: 1000 / (endTime - startTime));
                    }
                    self.sendControl(control: controlResult)
                }
            } else {
                DispatchQueue.main.async {
                    self.sendControl(control: Control())
                }
            }
            self.isInferenceQueueBusy = false
        }
    }

    /// update the openBot from game controller.
    @objc func updateDataFromControllerApp(_ notification: Notification) {
        if gameController.selectedControlMode == ControlMode.GAMEPAD {
            return
        }

        if notification.object != nil {
            let command = notification.object as! String
            let rightSpeed = command.slice(from: "r:", to: ", ");
            let leftSpeed = command.slice(from: "l:", to: "}}");
            gameController.sendControlFromPhoneController(control: Control(left: Float(Double(leftSpeed ?? "0.0") ?? 0.0), right: Float(Double(rightSpeed ?? "0.0") ?? 0.0)));
        }
    }
    
    /// update screen command data coming from application
    @objc func updateLightsCommandFromControllerApp(_ notification: Notification) {
        if gameController.selectedControlMode == ControlMode.GAMEPAD {
            return
        }
        if notification.object != nil {
            let command = notification.object as! String
            let controllerCommand = command
                .replacingOccurrences(of: "{", with: "")
                .replacingOccurrences(of: "}", with: "")
                .replacingOccurrences(of: "command:", with: "")
                .trimmingCharacters(in: .whitespacesAndNewlines)

            switch controllerCommand {
            case "INDICATOR_LEFT":
                self.webSocketMsgHandler.indicatorLeft()
                break;
            case "INDICATOR_RIGHT":
                self.webSocketMsgHandler.indicatorRight();
                break
            case "INDICATOR_STOP":
                self.webSocketMsgHandler.cancelIndicator();
            case "SPEED_DOWN":
                self.webSocketMsgHandler.speedDown();
                break;
            case "SPEED_UP":
                self.webSocketMsgHandler.speedUp();
                break;
            case "DRIVE_MODE":
                self.webSocketMsgHandler.driveMode()
                break;
            case "NETWORK":
                if let autoModeButton = self.expandedAutoPilotView?.autoModeButton {
                    autoModeButton.setOn(!autoModeButton.isOn, animated: true)
                    self.expandedAutoPilotView?.switchButton(autoModeButton)
                }
                break;
            default:
                break;
            }
        }
    }

}

/**
 Protocol to create autopilot delegate
 */
protocol autopilotDelegate: AnyObject {
    func didPerformAction()
}

