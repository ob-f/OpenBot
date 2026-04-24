//
// Created by Sparsh Jain on 27/10/22.
//

import Foundation
import AVFoundation
import UIKit

class ObjectTrackingFragment: CameraController {
    var objectTrackingSettings: ObjectTrackingSettings?
    var numberOfThreads: Int = 1
    var bottomAnchorConstraint: NSLayoutConstraint!
    var trailingAnchorConstraint: NSLayoutConstraint!
    var detector: Detector?
    var models: [Model] = []
    var autoMode: Bool = false
    let gameController = GameController.shared
    var vehicleControl: Control = Control()
    let bluetooth = bluetoothDataController.shared
    var currentModel: ModelItem!
    var currentDevice: RuntimeDevice = RuntimeDevice.CPU
    var currentObject: String = "person"
    public var MINIMUM_CONFIDENCE_TF_OD_API: Float = 0.5
    private let inferenceQueue = DispatchQueue(label: "openbot.autopilot.inferencequeue")
    private var isInferenceQueueBusy = false
    private var result: Control?
    private var frames: [UIView] = []
    private var bufferHeight = 0
    private var bufferWidth = 0
    private let edgeOffset: CGFloat = 2.0
    private var useDynamicSpeed: Bool = false
    let webSocketMsgHandler = WebSocketMessageHandler();
    let fragmentType = FragmentType.shared

    /// Called after the view fragment has loaded.
    override func viewDidLoad() {
        let modelItems = Common.loadAllModelItemsFromBundle()
        if (modelItems.count > 0) {
            let model = modelItems.first(where: { $0.type == TYPE.DETECTOR.rawValue })
            currentModel = model
            detector = try! Detector.create(model: Model.fromModelItem(item: model ?? modelItems[0]), device: RuntimeDevice.CPU, numThreads: numberOfThreads) as? Detector
        }
        if let threads = preferencesManager.getThreads(){
            numberOfThreads = Int(threads) ?? 4
            detector?.tfliteOptions.threadCount = numberOfThreads
        }
        if let confidence = preferencesManager.getObjectTrackConfidence(){
            MINIMUM_CONFIDENCE_TF_OD_API = Float(confidence as! Int) / 100.0
        }
        if let device = preferencesManager.getDevice(){
            currentDevice = RuntimeDevice(rawValue: device) ?? RuntimeDevice.CPU
            detector = try! Detector.create(model: Model.fromModelItem(item: currentModel), device: currentDevice, numThreads: numberOfThreads) as? Detector
            detector?.tfliteOptions.threadCount = numberOfThreads
        }
        if let lastModel = preferencesManager.getObjectTrackModel(){
            if Common.isModelItemAvailableInDocument(modelName: lastModel) == true {
                currentModel = Common.returnModelItem(modelName: lastModel)
                detector = try! Detector.create(model: Model.fromModelItem(item: currentModel), device: currentDevice, numThreads: numberOfThreads) as? Detector
            }
        }
        if let object = preferencesManager.getObjectTrackingObject(){
            currentObject = object;
            detector?.setSelectedClass(newClass: object);
        }
        objectTrackingSettings = ObjectTrackingSettings(frame: CGRect(x: 0, y: height - 375, width: width, height: 375), detector: detector, model: currentModel)
        objectTrackingSettings!.backgroundColor = Colors.freeRoamButtonsColor
        objectTrackingSettings!.layer.cornerRadius = 5
        createCameraView()
        view.addSubview(objectTrackingSettings!)
        bottomAnchorConstraint = objectTrackingSettings!.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -5)
        trailingAnchorConstraint = objectTrackingSettings!.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor, constant: 0)
        NotificationCenter.default.addObserver(self, selector: #selector(openBluetoothSettings), name: .ble, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(switchCamera), name: .switchCamera, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateDevice), name: .updateDevice, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateThread), name: .updateThread, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateConfidence), name: .updateConfidence, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(toggleAutoMode), name: .autoModeObjectTracking, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateModel), name: .updateModel, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateSelectedObject), name: .updateObject, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateDataFromControllerApp), name: .updateStringFromControllerApp, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateLightsCommandFromControllerApp), name: .updateLightsCommandFromControllerApp, object: nil)
        setupNavigationBarItem()
        gameController.resetControl = false
        fragmentType.currentFragment = "ObjectDetection";
        calculateFrame()
        let msg = JSON.toString(FragmentStatus(FRAGMENT_TYPE: self.fragmentType.currentFragment));
        client.send(message: msg);
        super.viewDidLoad()
    }

    /// Called when the view controller's view's size is changed by its parent (i.e. for the root view controller when its window rotates or is resized).
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
        calculateFrame()
    }

    func calculateFrame() {
        if currentOrientation == .portrait || currentOrientation == .portraitUpsideDown {
            objectTrackingSettings?.frame.origin.x = 0
            objectTrackingSettings?.frame.origin.y = height - 375
        } else {
            objectTrackingSettings?.frame.origin.x = height - 375
            objectTrackingSettings?.frame.origin.y = 30
        }
    }

    @objc func openBluetoothSettings() {
        let nextViewController = (storyboard?.instantiateViewController(withIdentifier: Strings.bluetoothScreen))
        navigationController?.pushViewController(nextViewController!, animated: true)
    }

    /// Initialization routine
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        switch objectTrackingSettings?.autoModeButton.isOn {
        case false:
            autoMode = false
        case true:
            autoMode = false
            toggleAutoMode()
        case .none:
            autoMode = false
        case .some(_):
            autoMode = false
        }
    }

    @objc func switchCamera() {
        switchCameraView()
    }

    /// function to update the detector model when change in device[CPU, GPU, NNAPI] is triggered.
    @objc func updateDevice(_ notification: Notification) throws {
        currentDevice = RuntimeDevice(rawValue: notification.object as! String) ?? RuntimeDevice.CPU
        detector = try! Detector.create(model: Model.fromModelItem(item: currentModel), device: currentDevice, numThreads: numberOfThreads) as? Detector
        currentDevice.rawValue == RuntimeDevice.GPU.rawValue ? NotificationCenter.default.post(name: .updateThreadLabel, object: "N/A") : NotificationCenter.default.post(name: .updateThreadLabel, object: String(numberOfThreads))
        detector?.tfliteOptions.threadCount = numberOfThreads
        preferencesManager.setDevice(value: notification.object as! String);
    }

    /// function to update the number of threads.
    @objc func updateThread(_ notification: Notification) {
        let threadCount = notification.object as! String
        numberOfThreads = Int(threadCount) ?? 4
        detector?.tfliteOptions.threadCount = numberOfThreads
        preferencesManager.setThreads(value: notification.object as! String);
    }

    /// function to update the confidence of the model
    @objc func updateConfidence(_ notification: Notification) {
        let confidence = notification.object as! Int
        MINIMUM_CONFIDENCE_TF_OD_API = Float(confidence) / 100.0
        preferencesManager.setObjectTrackConfidence(value: confidence);
    }

    /// function to toggle the auto mode
    @objc func toggleAutoMode() {
        autoMode = !autoMode
    }

    /// function to send the controls to the device.
    func sendControl(control: Control) {
        if (control.getRight() != vehicleControl.getRight() || control.getLeft() != vehicleControl.getLeft()) {
            let left = control.getLeft() * gameController.selectedSpeedMode.rawValue
            let right = control.getRight() * gameController.selectedSpeedMode.rawValue
            vehicleControl = control
            bluetooth.sendData(payload: "c" + String(left) + "," + String(right) + "\n")
            NotificationCenter.default.post(name: .updateSpeedLabel, object: String(Int(left)) + "," + String(Int(right)))
            NotificationCenter.default.post(name: .updateRpmLabel, object: String(Int(control.getLeft())) + "," + String(Int(control.getRight())))
        }
    }

    /// function to update the model
    @objc func updateModel(_ notification: Notification) throws {
        let selectedModelName = notification.object as! String
        currentModel = Common.returnModelItem(modelName: selectedModelName)
        detector = try! Detector.create(model: Model.fromModelItem(item: currentModel), device: currentDevice, numThreads: numberOfThreads) as? Detector
        NotificationCenter.default.post(name: .updateObjectList, object: detector?.getLabels())
        NotificationCenter.default.post(name: .updateObject, object: currentObject)
    }

    /// function to update the selected object.
    @objc func updateSelectedObject(_ notification: Notification) throws {
        currentObject = notification.object as! String
        detector?.setSelectedClass(newClass: notification.object as! String)
    }

    /// Called after the view was dismissed, covered or otherwise hidden.
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        if autoMode {
            autoMode = false
        }
        
        let msg = JSON.toString(self.fragmentType.closeFragment())
        client.send(message: msg)
        
    }

    func setupNavigationBarItem() {
        if UIImage(named: "back") != nil {
            let backNavigationIcon = (UIImage(named: "back")?.withRenderingMode(.alwaysOriginal))!
            let newBackButton = UIBarButtonItem(image: backNavigationIcon, title: Strings.objectTracking, target: self, action: #selector(ObjectTrackingFragment.back(sender:)), titleColor: Colors.navigationColor ?? .white)
            navigationItem.leftBarButtonItem = newBackButton
        }
    }

    @objc func back(sender: UIBarButtonItem) {
        _ = navigationController?.popViewController(animated: true)
    }

    func updateTarget(_ detection: CGRect) -> Control {

        // Left and right wheels control values
        var left: Float = 0.0
        var right: Float = 0.0

        let frameWidth = UIScreen.main.bounds.size.width
        let frameHeight = UIScreen.main.bounds.size.height

        let dx: CGFloat = CGFloat(detector!.getImageSizeX()) / frameWidth
        let dy: CGFloat = CGFloat(detector!.getImageSizeY()) / frameHeight
        let trackedPos = detection.applying(CGAffineTransform(scaleX: dx, y: dy))

        // Calculate track box area for distance estimate
        let boxArea: Float = Float(trackedPos.height * trackedPos.width)

        // Make sure object center is in frame
        var centerX: Float = Float(trackedPos.midX)
        centerX = max(0, min(centerX, Float(frameWidth)))

        // Scale relative position along x-axis between -1 and 1
        let x_pos_norm: Float = 1.0 - 2.0 * centerX / Float(frameWidth)

        // Generate vehicle controls
        if (x_pos_norm < 0.0) {
            left = 1.0
            right = 1.0 + x_pos_norm
        } else {
            left = 1 - x_pos_norm
            right = 1.0
        }

        // Adjust speed depending on size of detected object bounding box
        if (objectTrackingSettings!.dynamicSpeedCheckbox.isChecked) {  //  Set use of dynamic speed on or off (used in updateTarget())
            var scaleFactor: Float = 1.0 - boxArea / Float(frameWidth * frameHeight)
            scaleFactor = scaleFactor > 0.75 ? 1.0 : scaleFactor // tracked object far, full speed
            // apply scale factor if tracked object is not too near, otherwise stop
            if (scaleFactor > 0.25) {
                left *= scaleFactor
                right *= scaleFactor
            } else {
                left = 0.0
                right = 0.0
            }
        }

        return Control(left: left, right: right)
    }

    /// Set use of dynamic speed on or off (used in updateTarget())
    ///
    /// - Parameter isEnabled
    func setDynamicSpeed(isEnabled: Bool) {
        self.useDynamicSpeed = isEnabled
    }

    /// function to create the frame on the screen for detection on based of image device(front/back) or orientation (portrait/landscape).
    func addFrame(item: Detector.Recognition, color: UIColor) -> UIView {
        let frame = UIView()
        var convertedRect: CGRect = item.getLocation()
        if currentOrientation == .portrait && (captureSession.inputs[0] as! AVCaptureDeviceInput).device.position == .front {
            let detection = item.getLocation()
            let scaleX = width / CGFloat(bufferWidth)
            let scaleY = height / CGFloat(bufferHeight)
            let transform = CGAffineTransform.identity.scaledBy(x: scaleX, y: scaleY)
            let revertTransform = transform.concatenating(__CGAffineTransformMake(-1.0, 0.0, 0.0, 1.0, CGFloat(width), 0.0));
            convertedRect = detection.applying(revertTransform)
        } else if currentOrientation != .portrait && (captureSession.inputs[0] as! AVCaptureDeviceInput).device.position == .front {
            let detection = item.getLocation()
            let scaleX = height / CGFloat(bufferWidth)
            let scaleY = width / CGFloat(bufferHeight)
            let transform = CGAffineTransform.identity.scaledBy(x: scaleX, y: scaleY)
            let revertTransform = transform.concatenating(__CGAffineTransformMake(-1.0, 0.0, 0.0, 1.0, CGFloat(height), 0.0));
            convertedRect = detection.applying(revertTransform)
        } else if currentOrientation == .portrait && (captureSession.inputs[0] as! AVCaptureDeviceInput).device.position == .back {
            let detection = item.getLocation()
            let scaleX = width / CGFloat(bufferWidth)
            let scaleY = height / CGFloat(bufferHeight)
            let transform = CGAffineTransform.identity.scaledBy(x: scaleX, y: scaleY)
            convertedRect = detection.applying(transform)
        } else {
            let detection = item.getLocation()
            let scaleX = height / CGFloat(bufferWidth)
            let scaleY = width / CGFloat(bufferHeight)
            let transform = CGAffineTransform.identity.scaledBy(x: scaleX, y: scaleY)
            convertedRect = detection.applying(transform)
        }
        if convertedRect.origin.x < 0 {
            convertedRect.origin.x = edgeOffset
        }

        if convertedRect.origin.y < 0 {
            convertedRect.origin.y = edgeOffset
        }

        if convertedRect.maxY > UIScreen.main.bounds.maxY {
            convertedRect.size.height = UIScreen.main.bounds.maxY - convertedRect.origin.y - edgeOffset
        }

        if convertedRect.maxX > UIScreen.main.bounds.maxX {
            convertedRect.size.width = UIScreen.main.bounds.maxX - convertedRect.origin.x - edgeOffset
        }
        frame.frame = convertedRect
        frame.layer.borderColor = color.cgColor
        frame.layer.borderWidth = 3.0
        let nameString = UITextView()
        nameString.textColor = UIColor.white
        nameString.font = nameString.font?.withSize(12)
        nameString.backgroundColor = color.withAlphaComponent(0.5)
        nameString.text = item.getTitle() + " " + String(format: "%.2f", item.getConfidence() * 100) + "%"
        nameString.translatesAutoresizingMaskIntoConstraints = true
        nameString.sizeToFit()
        frame.addSubview(nameString)
        return frame
    }

    /// callback function when any image is clicked on object tracking fragment.
    override func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {

        // extract the image buffer from the sample buffer
        let pixelBuffer: CVPixelBuffer? = CMSampleBufferGetImageBuffer(sampleBuffer)

        bufferWidth = CVPixelBufferGetWidth(pixelBuffer!)
        bufferHeight = CVPixelBufferGetHeight(pixelBuffer!)

        guard let imagePixelBuffer = pixelBuffer else {
            debugPrint("unable to get image from sample buffer")
            return
        }
        if webRTCClient != nil {
            inferenceQueue.async {
                webRTCClient.captureCurrentFrame(sampleBuffer: sampleBuffer)
                self.isInferenceQueueBusy = false
            }
        }
        guard !isInferenceQueueBusy else {
            return
        }

        inferenceQueue.async {
            if self.autoMode {
                self.isInferenceQueueBusy = true
                let startTime = Date().millisecondsSince1970
                let res = self.detector?.recognizeImage(pixelBuffer: imagePixelBuffer,detectionType: "single")
                let endTime = Date().millisecondsSince1970
                if (res!.count > 0) {
                    self.result = self.updateTarget(res!.first!.getLocation())
                } else {
                    self.result = Control(left: 0, right: 0)
                }
                guard let controlResult = self.result else {
                    return
                }

                DispatchQueue.main.async {

                    if (self.frames.count > 0) {
                        for frame in self.frames {
                            frame.removeFromSuperview()
                        }
                    }
                    self.frames.removeAll()
                    var i = 0
                    if (res!.count > 0) {
                        for item in res! {
                            if (item.getConfidence() > self.MINIMUM_CONFIDENCE_TF_OD_API) {
                                let frame = self.addFrame(item: item, color: Constants.frameColors[i % 5])
                                self.frames.append(frame)
                                self.cameraView.addSubview(frame)
                                i += 1
                            }
                        }
                    }

                    if (endTime - startTime) != 0 {
                        NotificationCenter.default.post(name: .updateObjectTrackingFps, object: 1000 / (endTime - startTime))
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

    /// function to update the data from the controller.
    @objc func updateDataFromControllerApp(_ notification: Notification) {
        if gameController.selectedControlMode == ControlMode.GAMEPAD {
            return
        }
        if notification.object != nil {
            let command = notification.object as! String
            let rightSpeed = command.slice(from: "r:", to: ", ")
            let leftSpeed = command.slice(from: "l:", to: "}}")
            gameController.sendControlFromPhoneController(control: Control(left: Float(Double(leftSpeed ?? "0.0") ?? 0.0), right: Float(Double(rightSpeed ?? "0.0") ?? 0.0)))
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
                if let autoModeButton = self.objectTrackingSettings?.autoModeButton {
                    autoModeButton.setOn(!autoModeButton.isOn, animated: true)
                    self.objectTrackingSettings?.switchButton(autoModeButton)
                }
                break;
            default:
                break;
            }
        }
    }

}
