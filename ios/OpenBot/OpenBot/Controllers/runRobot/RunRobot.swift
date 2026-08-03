//
// Created by Nitish Yadav on 07/04/23.
//

import Foundation
import UIKit
import AVFoundation
import SceneKit
import ARKit
import simd

class runRobot: CameraController, ARSCNViewDelegate, UITextFieldDelegate {
    @IBOutlet weak var stopRobot: UIButton!
    @IBOutlet weak var commandMessage: UILabel!
    let bluetooth = bluetoothDataController.shared
    private var bufferHeight = 0
    private var bufferWidth = 0
    private var result: Control?
    static var detector: Detector?
    static var autopilot: Autopilot?;
    private let inferenceQueue = DispatchQueue(label: "openbot.runRobot.inferencequeue");
    private var isInferenceQueueBusy = false
    var vehicleControl: Control = Control()
    public var MINIMUM_CONFIDENCE_TF_OD_API: Float = 0.5
    private var useDynamicSpeed: Bool = false
    static var isObjectTracking: Bool = false
    static var isAutopilot: Bool = false;
    static var isMultipleObjectTracking: Bool = false
    static var isMultipleAi: Bool = false
    static var isPointGoalNavigation: Bool = false
    static var isDetection: Bool = false
    var tempPixelBuffer: CVPixelBuffer!
    var sceneView: ARSCNView!
    let blueDress = try! YCbCrImageBufferConverter()
    private var isNavQueueBusy = false
    private let navQueue = DispatchQueue(label: "openbot.navigation.navqueue")
    private var distance: Float = 0
    static var navigation: Navigation?
    private var isReached: Bool = false
    var marker = SCNNode()
    private var startingPoint = SCNNode()
    private var endingPoint: SCNNode!
    static var forward: Double = 0
    static var left: Double = 0
    var configuration = ARWorldTrackingConfiguration()
    var task: String = ""
    private var framesArray: [Bool] = [];
    private var totalFrames: Int = 0;
    var counter: Int = 0;
    var object: String = "";
    @IBOutlet weak var robotImage: UIImageView!
    @IBOutlet weak var resetRobot: UIButton!
    var topAnchorConstraint: NSLayoutConstraint!;
    var centerXConstraint: NSLayoutConstraint!;
    var topLabelConstraint: NSLayoutConstraint!;
    var centerlabelXConstraint: NSLayoutConstraint!;
    var topStopButtonlConstraint: NSLayoutConstraint!;
    var centerStopButtonXConstraint: NSLayoutConstraint!;
    var topResetButtonlConstraint: NSLayoutConstraint!;
    var centerResetButtonXConstraint: NSLayoutConstraint!;
    private let sensor = sensorDataRetrieve.shared

    /**
      override function calls when view of controller loaded
     */
    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI();
        stopRobot.backgroundColor = Colors.title;
        stopRobot.setTitle("Stop Robot", for: .normal);
        stopRobot.addTarget(self, action: #selector(cancel), for: .touchUpInside);
        stopRobot.layer.cornerRadius = 10;
        resetRobot.backgroundColor = Colors.title
        resetRobot.setTitle("Reset Robot", for: .normal);
        resetRobot.addTarget(self, action: #selector(resetRobotFunction), for: .touchUpInside);
        resetRobot.layer.cornerRadius = 10;
        NotificationCenter.default.addObserver(self, selector: #selector(updateCommandMsg), name: .commandName, object: nil);
        NotificationCenter.default.addObserver(self, selector: #selector(updateCommandObject), name: .commandObject, object: nil);
        NotificationCenter.default.addObserver(self, selector: #selector(createCamera), name: .createCameraView, object: nil);
        NotificationCenter.default.addObserver(self, selector: #selector(setPosForPointGoalnavigation), name: .pointGoalNav, object: nil);
        NotificationCenter.default.addObserver(self, selector: #selector(display), name: .displayItems, object: nil);
        setupNavigationBarItem();
        let modelItems = Common.loadAllModelItemsFromBundle()
        if (modelItems.count > 0) {
            let detectorModel = modelItems.first(where: { $0.type == TYPE.DETECTOR.rawValue })
            runRobot.detector = try! Detector.create(model: Model.fromModelItem(item: detectorModel ?? modelItems[0]), device: RuntimeDevice.CPU, numThreads: 1) as? Detector
            let autopilotModel = modelItems.first(where: { $0.type == TYPE.AUTOPILOT.rawValue });
            runRobot.autopilot = Autopilot(model: Model.fromModelItem(item: autopilotModel ?? modelItems[0]), device: RuntimeDevice.CPU, numThreads: 1);
        }
        runRobot.navigation = Navigation(model: Model.fromModelItem(item: Common.returnNavigationModel()), device: RuntimeDevice.CPU, numThreads: 1)
    }

    var temp = 0;


    /**
     override function to create camera view behind stopRobot
     */
    override func createCameraView() {
        if temp > 0 {
            return;
        }
        DispatchQueue.main.async {
            super.createCameraView();
            self.view.sendSubviewToBack(super.cameraView);
        }
        temp = temp + 1;
    }

    /**
     function to create the UI for the run robot
     */
    func setUpUI(){
        setUpImage();
        setUpLabel();
        setUpStopButton();
        setUpResetButton();
    }

    /**
     function to create stop button
     */
    func setUpStopButton(){
        stopRobot.translatesAutoresizingMaskIntoConstraints = false;
        stopRobot.widthAnchor.constraint(equalToConstant: 210).isActive = true
        stopRobot.heightAnchor.constraint(equalToConstant: 45).isActive = true
        if currentOrientation == .portrait {
            topStopButtonlConstraint = stopRobot.topAnchor.constraint(equalTo: view.topAnchor, constant: 500)
            centerStopButtonXConstraint = stopRobot.centerXAnchor.constraint(equalTo: view.centerXAnchor);
        } else {
            topStopButtonlConstraint?.isActive = false;
            centerStopButtonXConstraint?.isActive = false
            topStopButtonlConstraint = stopRobot.topAnchor.constraint(equalTo: view.topAnchor, constant: 120)
            centerStopButtonXConstraint = stopRobot.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 430)
        }
        NSLayoutConstraint.activate([topStopButtonlConstraint, centerStopButtonXConstraint])
    }

    /**
     function to create reset button
     */
    func setUpResetButton(){
        resetRobot.translatesAutoresizingMaskIntoConstraints = false;
        resetRobot.widthAnchor.constraint(equalToConstant: 210).isActive = true
        resetRobot.heightAnchor.constraint(equalToConstant: 45).isActive = true
        if currentOrientation == .portrait {
            topResetButtonlConstraint = resetRobot.topAnchor.constraint(equalTo: view.topAnchor, constant: 570)
            centerResetButtonXConstraint = resetRobot.centerXAnchor.constraint(equalTo: view.centerXAnchor);
        } else {
            topResetButtonlConstraint?.isActive = false;
            centerResetButtonXConstraint?.isActive = false;
            topResetButtonlConstraint = resetRobot.topAnchor.constraint(equalTo: view.topAnchor, constant: 210)
            centerResetButtonXConstraint = resetRobot.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 430)
        }
        NSLayoutConstraint.activate([topResetButtonlConstraint, centerResetButtonXConstraint])
    }

    /**
     function to create label
     */
    func setUpLabel(){
        commandMessage.text = Strings.codeExecuting;
        commandMessage.translatesAutoresizingMaskIntoConstraints = false;
        if currentOrientation == .portrait {
            topLabelConstraint = commandMessage.topAnchor.constraint(equalTo: view.topAnchor, constant: 450)
            centerlabelXConstraint = commandMessage.centerXAnchor.constraint(equalTo: view.centerXAnchor)
        } else {
            topLabelConstraint?.isActive = false;
            centerlabelXConstraint?.isActive = false;
            topLabelConstraint = commandMessage.topAnchor.constraint(equalTo: view.topAnchor, constant: 320)
            centerlabelXConstraint = commandMessage.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 115)
        }
        NSLayoutConstraint.activate([topLabelConstraint, centerlabelXConstraint])
    }

    /**
     function to create openbot image
     */
    func setUpImage() {
        robotImage.translatesAutoresizingMaskIntoConstraints = false;
        robotImage.widthAnchor.constraint(equalToConstant: 230).isActive = true
        robotImage.heightAnchor.constraint(equalToConstant: 300).isActive = true
        if currentOrientation == .portrait {
            topAnchorConstraint = robotImage.topAnchor.constraint(equalTo: view.topAnchor, constant: 160)
            centerXConstraint = robotImage.centerXAnchor.constraint(equalTo: view.centerXAnchor, constant: 0)
        } else {
            topAnchorConstraint?.isActive = false;
            centerXConstraint?.isActive = false
            topAnchorConstraint = robotImage.topAnchor.constraint(equalTo: view.topAnchor, constant: 40)
            centerXConstraint = robotImage.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 90)
        }
        NSLayoutConstraint.activate([centerXConstraint, topAnchorConstraint])
    }

    /**
     override function calls after controller view is presented
     - Parameter animated:
     */
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
    }

    /**
     function to restart point goal navigation session when detected error
     - Parameters:
       - session:
       - error:
     */
    func session(_ session: ARSession, didFailWithError error: Error) {
        // Present an error message to the user
        print("Session failed. Changing worldAlignment property.")
        print(error.localizedDescription)
        if let arError = error as? ARError {
            switch arError.errorCode {
            case 102:
                configuration.worldAlignment = .gravity
                restartSessionWithoutDelete()
            default:
                restartSessionWithoutDelete()
            }
        }
    }

    // Restart session with a different worldAlignment - prevents bug from crashing app
    func restartSessionWithoutDelete() {
        sceneView.session.pause()
        sceneView.session.run(configuration, options: [.resetTracking, .removeExistingAnchors])
    }

    // Restart session when interrupted in between threads - prevents bug from crashing app
    func sessionWasInterrupted(_ session: ARSession) {
        self.sceneView.session.pause();
        self.sceneView.session.run(configuration, options: [.resetTracking, .removeExistingAnchors])
    }

    /**
     override function calls after current controller view disappear
     Here we stopping the car, removing all notifications

     - Parameter animated:
     */
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        NotificationCenter.default.post(name: .cancelThread, object: nil)
        sceneView?.session.pause()
    }

    /**
     override function when view of controller disappears
     - Parameter animated:
     */
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        NotificationCenter.default.removeObserver(self);
    }

    /**
     updating current commands
     - Parameter notification:
     */
    var count: Int = 0;

    /**
     function to update commands on screen
     - Parameter notification:
     */
    @objc func updateCommandMsg(_ notification: Notification) {
        let message = notification.object as! String;
        // Update UI periodically to show all the received messages
        if self.count > 1 {
            return;
        }
        self.count = self.count + 1;
        DispatchQueue.main.async {
            self.commandMessage.text = message;
        }
    }

    /**
     function to update the input objects for classes
     - Parameter notification:
     */
    @objc func updateCommandObject(_ notification: Notification) {
        DispatchQueue.main.async {
            runRobot.detector?.setMultipleSelectedClass(newClasses: notification.object as! [String])
        }
    }

    @objc func createCamera(_ notification: Notification) {
        if notification.object != nil {
            task = notification.object as! String
        }
        createCameraView();
    }

    /**
     function to create AR view for point goal navigation
     - Parameter completion:
     */
    func pointGoalCameraView(completion: @escaping () -> Void) {
        DispatchQueue.main.async {
            self.sceneView = ARSCNView(frame: self.view.bounds)
            self.sceneView.debugOptions = []
            self.view.insertSubview(self.sceneView, belowSubview: self.stopRobot);
            self.view.insertSubview(self.sceneView, belowSubview: self.resetRobot);
            let scene = SCNScene()
            self.sceneView.scene = scene
            self.sceneView.delegate = self
            self.sceneView.showsStatistics = true
            self.sceneView.debugOptions = [ARSCNDebugOptions.showFeaturePoints, ARSCNDebugOptions.showWorldOrigin]
            let configuration = ARWorldTrackingConfiguration()
            self.sceneView.session.run(configuration)
            completion()
        }
    }

    /**
    function handles point goal navigation in an AR scene, placing a marker at a specified location.
     */
    @objc func setPosForPointGoalnavigation(_ notification: Notification){
            pointGoalCameraView {
                let camera = self.sceneView.pointOfView!
                let cameraTransform = camera.transform
                _ = SCNVector3(-cameraTransform.m31, -cameraTransform.m32, -cameraTransform.m33)
                let markerInCameraFrame = SCNVector3(Float(-runRobot.left), 0.0, Float(-runRobot.forward))
                let markerInWorldFrame = markerInCameraFrame.transformed(by: cameraTransform)
                self.marker = SCNNode(geometry: SCNPlane(width: 0.1, height: 0.1))
                self.marker.position = markerInWorldFrame
                let imageMaterial = SCNMaterial()
                imageMaterial.diffuse.contents = Images.gmapMarker
                self.marker.geometry?.firstMaterial = imageMaterial
                self.sceneView.scene.rootNode.addChildNode(self.marker)
                self.startingPoint.position = self.sceneView.pointOfView?.position ?? camera.position
                self.endingPoint = self.marker
                self.calculateRoute()
            }
    }

    /**
     function to calculate the route to the point
     */
    func calculateRoute() {
        _ = simd_distance(startingPoint.simdPosition, endingPoint.simdPosition)
        _ = simd_normalize(endingPoint.simdPosition - startingPoint.simdPosition)
    }

    let factor = 0.8;

    /**
     override function calls when the current orientation changed
     - Parameters:
       - size:
       - coordinator:
     */
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
        updateConstraints();
        if (runRobot.isPointGoalNavigation) {
            if currentOrientation == .portrait {
                sceneView.frame.size = CGSize(width: width, height: height);
            } else {
                sceneView.frame.size = CGSize(width: height, height: width);
            }
        }
    }

    /**
     Function calls on cancel button of screen tapped. This stop the execution of blockly code
     */
    @objc func cancel() {
        NotificationCenter.default.post(name: .cancelThread, object: nil);
        NotificationCenter.default.post(name: .commandName, object: "\(Strings.cancel)ed");
        stopCar();
        self.stopRobot.setTitle("Back", for: .normal);
        self.stopRobot.removeTarget(nil, action: nil, for: .touchUpInside)
        self.stopRobot.addTarget(self, action: #selector(self.backItem(sender:)), for: .touchUpInside);
    }

    /**
     function to reset blockly commands for robot
     */
    @objc  func resetRobotFunction() {
            self.stopCar()
            _ = jsEvaluator(jsCode: self.preferencesManager.getBlocklyCode()!);
            self.stopRobot.setTitle("Stop Car", for: .normal);
            self.stopRobot.removeTarget(nil, action: nil, for: .touchUpInside)
            self.stopRobot.addTarget(self, action: #selector(self.cancel), for: .touchUpInside);
    }

    /**
     Function to setup the navigation bar
     */
    func setupNavigationBarItem() {
        if UIImage(named: "back") != nil {
            let backNavigationIcon = (UIImage(named: "back")?.withRenderingMode(.alwaysOriginal))!
            let newBackButton = UIBarButtonItem(image: backNavigationIcon, title: "Playground", target: self, action: #selector(back(sender:)), titleColor: Colors.navigationColor ?? .white)
            navigationItem.leftBarButtonItem = newBackButton
        }
    }

    /**
     Function to remove current viewController from navigation stack
     - Parameter sender:
     */
    @objc func back(sender: UIBarButtonItem) {
        _ = navigationController?.popViewController(animated: true)
        stopCar();
    }

    /**
     Function to remove current viewController from navigation stack on clicking back
     */
    @objc func backItem(sender: UIButton){
        _ = navigationController?.popViewController(animated: true)
        stopCar();
    }

    /**
     Function to update the constraints of image
     */
    fileprivate func updateConstraints() {
        topAnchorConstraint.isActive = false;
        centerXConstraint.isActive = false
        topLabelConstraint.isActive = false;
        centerlabelXConstraint.isActive = false;
        topStopButtonlConstraint.isActive = false;
        centerStopButtonXConstraint.isActive = false;
        topResetButtonlConstraint.isActive = false;
        centerResetButtonXConstraint.isActive = false;
        if currentOrientation == .portrait {
            topAnchorConstraint = robotImage.topAnchor.constraint(equalTo: view.topAnchor, constant: 160)
            centerXConstraint = robotImage.centerXAnchor.constraint(equalTo: view.centerXAnchor, constant: 0)
            topLabelConstraint = commandMessage.topAnchor.constraint(equalTo: view.topAnchor, constant: 450)
            centerlabelXConstraint = commandMessage.centerXAnchor.constraint(equalTo: view.centerXAnchor)
            topStopButtonlConstraint = stopRobot.topAnchor.constraint(equalTo: view.topAnchor, constant: 500)
            centerStopButtonXConstraint = stopRobot.centerXAnchor.constraint(equalTo: view.centerXAnchor);
            topResetButtonlConstraint = resetRobot.topAnchor.constraint(equalTo: view.topAnchor, constant: 570)
            centerResetButtonXConstraint = resetRobot.centerXAnchor.constraint(equalTo: view.centerXAnchor);
        } else {
            topAnchorConstraint = robotImage.topAnchor.constraint(equalTo: view.topAnchor, constant: 40)
            centerXConstraint = robotImage.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 90)
            topLabelConstraint = commandMessage.topAnchor.constraint(equalTo: view.topAnchor, constant: 320)
            centerlabelXConstraint = commandMessage.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 115)
            topStopButtonlConstraint = stopRobot.topAnchor.constraint(equalTo: view.topAnchor, constant: 120)
            centerStopButtonXConstraint = stopRobot.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 430)
            topResetButtonlConstraint = resetRobot.topAnchor.constraint(equalTo: view.topAnchor, constant: 210)
            centerResetButtonXConstraint = resetRobot.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 430)
        }
        NSLayoutConstraint.activate([centerXConstraint, topAnchorConstraint])
        NSLayoutConstraint.activate([topLabelConstraint, centerlabelXConstraint])
        NSLayoutConstraint.activate([topStopButtonlConstraint, centerStopButtonXConstraint])
        NSLayoutConstraint.activate([topResetButtonlConstraint, centerResetButtonXConstraint])
    }

    /**
     function to stop ongoing robot commands
     */
    func stopCar() {
        runRobot.isDetection = false
        runRobot.isObjectTracking = false
        runRobot.isAutopilot = false
        runRobot.isMultipleObjectTracking = false
        runRobot.isPointGoalNavigation = false
        runRobot.isMultipleAi = false
        taskStorage.taskArray = [];
        bluetooth.sendDataFromJs(payloadData: "c" + String(0) + "," + String(0) + "\n");
        bluetooth.sendDataFromJs(payloadData: "l" + String(0) + "," + String(0) + "\n");
        let indicatorValues = "i0,0\n";
        bluetooth.sendDataFromJs(payloadData: indicatorValues);
    }

    /**
     function to send output controls to openBot
     - Parameter control:
     */
    func sendControl(control: Control) {
        if runRobot.isAutopilot || runRobot.isObjectTracking || runRobot.isMultipleObjectTracking || runRobot.isDetection {
            createCameraView();
        }
        if (control.getRight() != vehicleControl.getRight() || control.getLeft() != vehicleControl.getLeft()) {
            let left = control.getLeft() * gameController.selectedSpeedMode.rawValue
            let right = control.getRight() * gameController.selectedSpeedMode.rawValue
            vehicleControl = control
            print("send data to bluetooth", control.getLeft(), " ", control.getRight());
            bluetooth.sendData(payload: "c" + String(left) + "," + String(right) + "\n")
            NotificationCenter.default.post(name: .updateSpeedLabel, object: String(Int(left)) + "," + String(Int(right)))
            NotificationCenter.default.post(name: .updateRpmLabel, object: String(Int(control.getLeft())) + "," + String(Int(control.getRight())))
        }
    }

    /**
      function to update target class when object moves
     - Parameter detection:
     - Returns:
     */
    func updateTarget(_ detection: CGRect) -> Control {

        // Left and right wheels control values
        var left: Float = 0.0
        var right: Float = 0.0

        let frameWidth = UIScreen.main.bounds.size.width
        let frameHeight = UIScreen.main.bounds.size.height

        let dx: CGFloat = CGFloat(runRobot.detector!.getImageSizeX()) / frameWidth
        let dy: CGFloat = CGFloat(runRobot.detector!.getImageSizeY()) / frameHeight
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
        if (useDynamicSpeed) {
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

    /**
     called after camera capture each frame
     - Parameters:
       - output:
       - sampleBuffer:
       - connection:
     */
    override func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        if (runRobot.isObjectTracking || runRobot.isAutopilot || runRobot.isMultipleObjectTracking || runRobot.isMultipleAi || runRobot.isDetection) {
            let pixelBuffer: CVPixelBuffer? = CMSampleBufferGetImageBuffer(sampleBuffer)
            bufferWidth = CVPixelBufferGetWidth(pixelBuffer!)
            bufferHeight = CVPixelBufferGetHeight(pixelBuffer!)
            guard let imagePixelBuffer = pixelBuffer else {
                debugPrint("unable to get image from sample buffer")
                return
            }
            guard !isInferenceQueueBusy else {
                return
            }

            inferenceQueue.async {
                if runRobot.isObjectTracking {
                    self.runObjectTracking(imagePixelBuffer: imagePixelBuffer);
                } else if runRobot.isMultipleObjectTracking {
                    self.runMultipleObjectTracking(imagePixelBuffer: imagePixelBuffer);
                } else if runRobot.isAutopilot {
                    self.runAutopilot(imagePixelBuffer: imagePixelBuffer);
                } else if runRobot.isMultipleAi {
                    self.runAutopilot(imagePixelBuffer: imagePixelBuffer);
                    self.runObjectTrackingForMultipleAI(imagePixelBuffer: imagePixelBuffer);
                } else {
                    self.runDetection(imagePixelBuffer: imagePixelBuffer);
                }
            }

        }
    }

    /**
     function to enable object tracking
     - Parameters:
       - object:
       - model:
     */
    static func enableObjectTracking(object: String, model: String) {
        DispatchQueue.main.async {
            runRobot.isObjectTracking = true;
        }
        let currentModel = Common.returnModelItem(modelName: model)
        detector = try! Detector.create(model: Model.fromModelItem(item: currentModel), device: .CPU, numThreads: 1) as? Detector
    }

    /**
     function to enable autopilot
     - Parameter model:
     */
    static func enableAutopilot(model: String) {
        DispatchQueue.main.async {
            runRobot.isAutopilot = true;
        }
        let currentModel = Common.returnModelItem(modelName: model)
        autopilot = Autopilot(model: Model.fromModelItem(item: currentModel), device: RuntimeDevice.CPU, numThreads: 1);
    }

    /**
     function to enable multiple object tracking
     - Parameters:
       - object1:
       - model:
       - object2:
     */
    static func enableMultipleObjectTracking(object1: String, model: String, object2: String) {
        DispatchQueue.main.async {
            runRobot.isMultipleObjectTracking = true;
        }
        let currentModel = Common.returnModelItem(modelName: model)
        detector = try! Detector.create(model: Model.fromModelItem(item: currentModel), device: .CPU, numThreads: 1) as? Detector
    }

    /**
     function to enable point goal navigation
     - Parameters:
       - forward:
       - left:
     */
    static func enablePointGoalNavigation(forward: Double, left: Double) {
        DispatchQueue.main.async {
            runRobot.isPointGoalNavigation = true;
        }
        runRobot.forward = forward
        runRobot.left = left
        navigation = Navigation(model: Model.fromModelItem(item: Common.returnNavigationModel()), device: RuntimeDevice.CPU, numThreads: 1)
    }

    /**
     function to enable detection
     - Parameters:
       - forward:
       - left:
     */
    static func onDetection(object: String, model: String, task: String) {
        let currentModel = Common.returnModelItem(modelName: model)
        detector = try! Detector.create(model: Model.fromModelItem(item: currentModel), device: .CPU, numThreads: 1) as? Detector
        DispatchQueue.main.async {
            runRobot.isDetection = true;
        }
    }

    @objc func display(_ notification: Notification){
        var message = notification.object as! String;
        DispatchQueue.main.async {
            self.sensor.sampleIMU();
            switch(message.trimmingCharacters(in: .whitespaces)){
            case "sonarReading()" :
                self.commandMessage.text = "sonar : \(self.bluetooth.getSonar())";
                break;
            case "speedReading()" :
                self.commandMessage.text = "speed : \(self.bluetooth.getSpeed())";
                break;
            case "voltageDividerReading()" :
                self.commandMessage.text = "voltage : \(self.bluetooth.getVoltage())";
                break;
            case "gyroscopeReadingX()" :
                self.commandMessage.text = "gyroscopeX : \(self.sensor.angularRateX)";
                break;
            case "gyroscopeReadingY()" :
                self.commandMessage.text = "gyroscopeY : \(self.sensor.angularRateY)";
                break;
            case "gyroscopeReadingZ()" :
                self.commandMessage.text = "gyroscopeZ : \(self.sensor.angularRateZ)";
                break;
            case "accelerationReadingX()" :
                self.commandMessage.text = "accelerationX : \(self.sensor.accelerationX)";
                break;
            case "accelerationReadingY()" :
                self.commandMessage.text = "accelerationY : \(self.sensor.accelerationY)";
                break;
            case "accelerationReadingZ()" :
                self.commandMessage.text = "accelerationZ : \(self.sensor.accelerationZ)";
                break;
            case "magneticReadingX()" :
                self.commandMessage.text = "magneticFieldX : \(self.sensor.magneticFieldX)";
                break;
            case "magneticReadingY()" :
                self.commandMessage.text = "magneticFieldY : \(self.sensor.magneticFieldY)";
                break;
            case "magneticReadingZ()" :
                self.commandMessage.text = "magneticFieldZ : \(self.sensor.magneticFieldZ)";
                break;
            case "frontWheelReading()" :
                let speedometer = self.bluetooth.speedometer;
                if speedometer != "" {
                    let index_1 = speedometer.index(after: speedometer.startIndex)
                    let indexOfComma = speedometer.firstIndex(of: ",") ?? index_1
                    let index_2 = speedometer.index(before: indexOfComma)
                    if let leftFront = Float(speedometer[index_1...index_2]) {
                        // 'leftFront' is now a non-optional Float
                        self.commandMessage.text = "frontWheel : \(String(leftFront))";
                    } else {
                        self.commandMessage.text = "";
                    }
                }
                break;
            case "backWheelReading()" :
                let speedometer = self.bluetooth.speedometer;
                if speedometer != "" {
                    let index_1 = speedometer.index(after: speedometer.startIndex)
                    let indexOfComma = speedometer.firstIndex(of: ",") ?? index_1
                    let index_2 = speedometer.index(before: indexOfComma)
                    if let back = Float(speedometer[speedometer.index(after: indexOfComma)...]) {
                        // 'leftFront' is now a non-optional Float
                        self.commandMessage.text = "frontWheel : \(String(back))";
                    } else {
                        self.commandMessage.text = "";
                    }
                }
                break;
            default:
                break;
            }
        }
    }

    /**
     delegate revoke when SceneKit node corresponding to a new AR anchor has been added to the scene
     - Parameters:
       - renderer:
       - scene:
       - time:
     */
    func renderer(_ renderer: SCNSceneRenderer, didRenderScene scene: SCNScene, atTime time: TimeInterval) {
        if (runRobot.isPointGoalNavigation) {
            guard let currentFrame = sceneView.session.currentFrame else {
                return
            }
            let pixelBuffer = currentFrame.capturedImage
            if sceneView.pointOfView?.position != nil {
                processPixelBuffer(pixelBuffer, sceneView.pointOfView!)
                tempPixelBuffer = pixelBuffer
            }
        }
    }

    /**
     function to process the buffer and send buffer to navigation.tflite to give result
     - Parameters:
       - pixelBuffer:
       - currentPosition:
     */
    func processPixelBuffer(_ pixelBuffer: CVPixelBuffer, _ currentPosition: SCNNode) {
        guard !isNavQueueBusy else {
            return
        }
        if endingPoint == nil {
            return
        }
        navQueue.async { [self] in
            isNavQueueBusy = true
            let startPose = Pose(tx: currentPosition.position.x, ty: currentPosition.position.y, tz: currentPosition.position.z, qx: currentPosition.orientation.x, qy: currentPosition.orientation.y, qz: currentPosition.orientation.z, qw: currentPosition.orientation.w)
            let endPose = Pose(tx: endingPoint.position.x, ty: endingPoint.position.y, tz: endingPoint.position.z, qx: endingPoint.orientation.x, qy: endingPoint.orientation.y, qz: endingPoint.orientation.z, qw: endingPoint.orientation.w)
            let yaw = computeDeltaYaw(pose: startPose, goalPose: endPose)
            let converted = try! blueDress.convertToBGRA(imageBuffer: pixelBuffer)
            let refCon = NSMutableData()
            var timingInfo: CMSampleTimingInfo = .invalid
            var formatDescription: CMVideoFormatDescription? = nil
            CMVideoFormatDescriptionCreateForImageBuffer(allocator: kCFAllocatorDefault, imageBuffer: converted, formatDescriptionOut: &formatDescription)
            var output: CMSampleBuffer? = nil
            _ = CMSampleBufferCreateForImageBuffer(allocator: kCFAllocatorDefault, imageBuffer: converted, dataReady: true, makeDataReadyCallback: nil, refcon: refCon.mutableBytes, formatDescription: formatDescription!, sampleTiming: &timingInfo, sampleBufferOut: &output)
            // extract the image buffer from the sample buffer
            let pixelBufferBGRA: CVPixelBuffer? = CMSampleBufferGetImageBuffer(output!)
            result = runRobot.navigation?.recognizeImage(pixelBuffer: pixelBufferBGRA!, goalDistance: distance, goalSin: sin(yaw), goalCos: cos(yaw))
            guard let controlResult = result else {
                return
            }
            if isReached {
                sendControl(control: Control())
            } else {
                sendControl(control: controlResult)
            }

            isNavQueueBusy = false
        }
    }

    /**
     delegate revoke when SceneKit node corresponding to a new AR anchor has been added to the scene
     - Parameters:
       - renderer:
       - time:
     */
    func renderer(_ renderer: SCNSceneRenderer, updateAtTime time: TimeInterval) {
        if (runRobot.isPointGoalNavigation) {
            guard let camera = sceneView.pointOfView else {
                return
            }
            checkCameraPosition(position: camera.presentation.simdPosition)
        }
    }

    // Define a distance threshold for triggering the event
    let distanceThreshold: Float = 0.15 // adjust this value as needed

    /**
     function to check openBot is reached at point or not
     - Parameter position:
     */
    func checkCameraPosition(position: simd_float3) {
        if endingPoint != nil && isReached != true {
            distance = simd_distance(position, endingPoint.simdPosition)
            if distance <= distanceThreshold {
                DispatchQueue.main.async {
                    self.isReached = true
                    self.marker.removeFromParentNode()
                }
            }
        }
    }

    /**
     function to send control to robot according to object tracking
     - Parameter imagePixelBuffer:
     */
    func runObjectTracking(imagePixelBuffer: CVPixelBuffer) {
        self.isInferenceQueueBusy = true
        let res = runRobot.detector?.recognizeImage(pixelBuffer: imagePixelBuffer, detectionType: "single");
        if res != nil {
            if (res!.count > 0) {
                self.result = self.updateTarget(res!.first!.getLocation())
            } else {
                self.result = Control(left: 0, right: 0)
            }
            guard let controlResult = self.result else {
                return
            }
            DispatchQueue.main.async {
                if (res!.count > 0) {
                    for item in res! {
                        if (item.getConfidence() > self.MINIMUM_CONFIDENCE_TF_OD_API) {
                            print("confidence is :=============", item.getConfidence());
                            self.sendControl(control: controlResult)
                        } else {
                            self.sendControl(control: Control())
                        }
                    }
                }
            }
        }
        self.isInferenceQueueBusy = false
    }

    /**
     function to send control to robot according to multiple object tracking
     - Parameter imagePixelBuffer:
     */
    func runMultipleObjectTracking(imagePixelBuffer: CVPixelBuffer) {
        self.isInferenceQueueBusy = true
        let res = runRobot.detector?.recognizeImage(pixelBuffer: imagePixelBuffer, detectionType: "multiple")
        if res != nil {
            if (res!.count > 0) {
                self.result = self.updateTarget(res!.first!.getLocation())
            } else {
                self.result = Control(left: 0, right: 0)
            }
            guard let controlResult = self.result else {
                return
            }
            DispatchQueue.main.async {
                if (res!.count > 0) {
                    if let objects = runRobot.detector?.returnMultipleSelectedClass() {
                        let filteredObjects = res!.filter { i in
                            if i.getConfidence() > self.MINIMUM_CONFIDENCE_TF_OD_API {
                                return i.getTitle() == objects[1] || i.getTitle() == objects[0]
                            } else {
                                return false
                            }
                        }
                        let stopObject = filteredObjects.filter {
                            $0.getTitle() == objects[1]
                        }
                        let runObject = filteredObjects.filter {
                            $0.getTitle() == objects[0]
                        }
                        if (stopObject.count == 0 && runObject.count > 0) {
                            self.sendControl(control: controlResult)
                        } else if (stopObject.count > 0 && runObject.count > 0) {
                            for i in stopObject {
                                for j in runObject {
                                    let getStopObjectPos = sqrt(pow(i.getLocation().midX - i.getLocation().minY, 2))
                                    let getStartObjectPos = sqrt(pow(j.getLocation().midX - j.getLocation().minY, 2))
                                    if (getStopObjectPos > getStartObjectPos) {
                                        self.sendControl(control: controlResult)
                                    } else {
                                        self.stopCar();
                                        _ = jsEvaluator(jsCode: self.task);
                                    }
                                }
                            }
                        } else if (stopObject.count > 0 && runObject.count == 0) {
                            self.stopCar();
                            _ = jsEvaluator(jsCode: self.task);
                        } else {
                            self.sendControl(control: Control())
                        }
                    }
                }
            }
        }
        self.isInferenceQueueBusy = false
    }

    /**
     function to send control to robot according to autopilot
     - Parameter imagePixelBuffer:
     */
    func runAutopilot(imagePixelBuffer: CVPixelBuffer) {
        let startTime = returnCurrentTimeStampSince1970()
        self.isInferenceQueueBusy = true
        self.result = runRobot.autopilot?.recognizeImage(pixelBuffer: imagePixelBuffer, indicator: 0) ?? Control();
        guard let controlResult = self.result else {
            return
        }
        DispatchQueue.main.async {
            self.sendControl(control: controlResult)
        }
        sendControl(control: controlResult)
        isInferenceQueueBusy = false
        print("Time for autopilot is -----> \(1000 / (returnCurrentTimeStampSince1970() - startTime))")
    }

    /// function to calculate the delta yaw for device position and goal position
    ///
    /// - Parameters:
    ///   - pose: device position
    ///   - goalPose: destination position
    /// - Returns:
    func computeDeltaYaw(pose: Pose, goalPose: Pose) -> Float {
        // compute robot forward axis (global coordinate system)
        let forward: [Float] = [0.0, 0.0, -1.0]
        let forwardRotated = pose.rotateVector(vector: forward)

        // distance vector to goal (global coordinate system)
        let dx = goalPose.tx - pose.tx
        let dz = goalPose.tz - pose.tz

        let yaw = atan2(forwardRotated[2], forwardRotated[0]) - atan2(dz, dx)

        // fit to range (-pi, pi]
        var resultYaw = yaw
        if (resultYaw > Float.pi) {
            resultYaw -= 2 * Float.pi
        } else if (resultYaw <= -Float.pi) {
            resultYaw += 2 * Float.pi
        }
        return resultYaw
    }

    /**
     A static method used to initialize the object tracking and autopilot
     - Parameters:
       - autoPilotModel:
       - task:
       - object:
       - detectorModel:
     */
    static func enableMultipleAI(autoPilotModel: String, task: String, object: String, detectorModel: String) {
        DispatchQueue.main.async {
            runRobot.isMultipleAi = true;
        }
        let objectTrackingModel = Common.returnModelItem(modelName: detectorModel);
        let auto = Common.returnModelItem(modelName: autoPilotModel);
        detector = try! Detector.create(model: Model.fromModelItem(item: objectTrackingModel), device: .CPU, numThreads: 1) as? Detector
        autopilot = Autopilot(model: Model.fromModelItem(item: auto), device: RuntimeDevice.CPU, numThreads: 1);
    }


    /**
     Function to run autopilot and Object tracking simultaneously
     - Parameter imagePixelBuffer:
     */
    func runObjectTrackingForMultipleAI(imagePixelBuffer: CVPixelBuffer) {
        let startTime = returnCurrentTimeStampSince1970();
        self.isInferenceQueueBusy = true
        let res = runRobot.detector?.recognizeImage(pixelBuffer: imagePixelBuffer, detectionType: "single");
        if res != nil {
            DispatchQueue.main.async {
                if (res!.count > 0) {
                    for item in res! {
                        if (item.getConfidence() > self.MINIMUM_CONFIDENCE_TF_OD_API) {
                            self.stopCar();
                            _ = jsEvaluator(jsCode: self.task);
                        } else {
                            return;
                        }
                    }
                }
            }
        }
        self.isInferenceQueueBusy = false
        print("Time for objectTracking is -----> \(1000 / (returnCurrentTimeStampSince1970() - startTime))")
    }

    /**
     function to send control to robot according to object tracking
     - Parameter imagePixelBuffer:
     */
    func runDetection(imagePixelBuffer: CVPixelBuffer) {
        isInferenceQueueBusy = true
        let res = runRobot.detector?.recognizeImage(pixelBuffer: imagePixelBuffer, detectionType: "all");
        if res != nil {
            if (res!.count > 0) {
                result = updateTarget(res!.first!.getLocation())
            } else {
                result = Control(left: 0, right: 0)
            }
            DispatchQueue.main.async {
                if (res!.count > 0) {
                    for item in res! {
                        if (item.getConfidence() > self.MINIMUM_CONFIDENCE_TF_OD_API) {
                            self.counter = 0;
                            if (taskStorage.returnAttributeArray().contains { i in
                                i.keys.contains(item.getTitle())
                            }) {
                                self.object = item.getTitle();
                                _ = jsEvaluator(jsCode: taskStorage.getValueOfAttribute(classType: item.getTitle(), type: "detect") as! String);
                                self.totalFrames = taskStorage.getValueOfAttribute(classType: item.getTitle(), type: "frames") as! Int
                            }
                        } else {
                            self.counter = self.counter + 1;
                            if (self.counter >= self.totalFrames && self.object != "") {
                                let task = taskStorage.getValueOfAttribute(classType: self.object, type: "unDetect");
                                if task != nil {
                                    _ = jsEvaluator(jsCode: task as! String);
                                }
                            }
                        }
                    }
                }
            }
        }
        self.isInferenceQueueBusy = false
    }

    /**
     Function to stop the AI Blocks
     */
    static func disableAI() {
        isObjectTracking = false
        isAutopilot = false;
        isMultipleObjectTracking = false
        isMultipleAi = false
        isDetection = false
    }
}
