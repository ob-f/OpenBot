//
// Created by Nitish Yadav on 13/02/23.
//

import Foundation
import UIKit
import SceneKit
import ARKit
import simd

class PointGoalFragment: UIViewController, ARSCNViewDelegate, UITextFieldDelegate {
    var sceneView: ARSCNView!
    private var startingPoint = SCNNode()
    private var endingPoint: SCNNode!
    private var distanceInput: UITextField!
    private var setGoalRect = UIView()
    private var setGoalText = UILabel()
    private var setGoalHeading = UILabel()
    private var forwardLabel = UILabel()
    private var leftLabel = UILabel()
    private var forwardInput = UITextField()
    private var leftInput = UITextField()
    private var distance: Float = 0
    private var forward: Float = 0
    private var left: Float = 0
    private var isReached: Bool = false
    var marker = SCNNode()
    let infoMessageRect = UIView()
    var navigation: Navigation?
    let numberOfThreads = 1
    private var isInferenceQueueBusy = false
    private let inferenceQueue = DispatchQueue(label: "openbot.navigation.inferencequeue")
    private var isNavQueueBusy = false
    private let navQueue = DispatchQueue(label: "openbot.navigation.navqueue")
    private var result: Control?
    let bluetooth = bluetoothDataController.shared
    var tempPixelBuffer: CVPixelBuffer!
    let blueDress = try! YCbCrImageBufferConverter()
    var vehicleControl = Control()

    /// function called after view of point goal navigation is called
    override func viewDidLoad() {
        super.viewDidLoad()
        sceneView = ARSCNView(frame: view.bounds)
        sceneView.debugOptions = []
        setupNavigationBarItem()
        view.addSubview(sceneView)
        let scene = SCNScene()
        sceneView.scene = scene
        sceneView.delegate = self
        sceneView.showsStatistics = true
        sceneView.debugOptions = [ARSCNDebugOptions.showFeaturePoints, ARSCNDebugOptions.showWorldOrigin]
        let configuration = ARWorldTrackingConfiguration()
        sceneView.session.run(configuration)
        createSetGoalRect()
        createReachMessage()
        let tap = UITapGestureRecognizer(target: self, action: #selector(UIInputViewController.dismissKeyboard))
        view.addGestureRecognizer(tap)
        navigation = Navigation(model: Model.fromModelItem(item: Common.returnNavigationModel()), device: RuntimeDevice.CPU, numThreads: 1)
    }

    /// function will called after point goal navigation will disappear. This method will stop the ARkit
    ///
    /// - Parameter animated:
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        sceneView.session.pause()
        sendControl(control: Control());
    }

    /***
     Function will call when we change the orientation of our phone
     - Parameters:
       - size:
       - coordinator:
     */
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
        if currentOrientation == .portrait {
            sceneView.frame.size = CGSize(width: width, height: height);
            setGoalRect.frame.origin = CGPoint(x: 30, y: height / 2 - 200);
            infoMessageRect.frame.origin = CGPoint(x: 30, y: height / 2 - 100);
        } else {
            sceneView.frame.size = CGSize(width: height, height: width);
            setGoalRect.frame.origin = CGPoint(x: height / 2 - setGoalRect.frame.height / 2, y: width / 2 - setGoalRect.frame.width / 2)
            infoMessageRect.frame.origin = CGPoint(x: height / 2 - width / 2 + 30, y: width / 2 - 100);
        }
    }

    /// function to create the UI of point goal navigation. This function will called all the method that create different UI
    func createSetGoalRect() {
        if currentOrientation == .portrait {
            setGoalRect.frame = CGRect(x: 30, y: height / 2 - 200, width: width - 60, height: 300);
        } else {
            setGoalRect.frame = CGRect(x: height / 2 - width / 2 + 30, y: width / 2 - 150, width: width - 60, height: 300);
        }
        setGoalRect.backgroundColor = traitCollection.userInterfaceStyle == .dark ? Colors.bdColor : .white
        setGoalRect.layer.cornerRadius = 10
        view.addSubview(setGoalRect)
        createSetGoalHeading()
        createSetGoalText()
        createForwardLeftLabels()
        createInputBoxes()
        createButtons()
    }

    /// function to create message rect of point goal navigation
    func createReachMessage() {
        if currentOrientation == .portrait {
            infoMessageRect.frame = CGRect(x: 30, y: height / 2 - 100, width: width - 60, height: 200);
        } else {
            infoMessageRect.frame = CGRect(x: height / 2 - width / 2 + 30, y: width / 2 - 100, width: width - 60, height: 200);
        }
        infoMessageRect.backgroundColor = traitCollection.userInterfaceStyle == .dark ? Colors.bdColor : .white
        let infoText = createLabel(text: Strings.info, fontSize: 18)
        infoMessageRect.addSubview(infoText)
        infoText.translatesAutoresizingMaskIntoConstraints = false
        infoText.leadingAnchor.constraint(equalTo: infoMessageRect.leadingAnchor, constant: 30).isActive = true
        infoText.topAnchor.constraint(equalTo: infoMessageRect.topAnchor, constant: 30).isActive = true
        let goalReachedText = createLabel(text: Strings.goalReachedMessage, fontSize: 16)
        infoMessageRect.addSubview(goalReachedText)
        goalReachedText.translatesAutoresizingMaskIntoConstraints = false
        goalReachedText.leadingAnchor.constraint(equalTo: infoText.leadingAnchor, constant: 0).isActive = true
        goalReachedText.topAnchor.constraint(equalTo: infoText.bottomAnchor, constant: 30).isActive = true

        let stopButton = createLabelButtons(title: Strings.stopLabel, selector: #selector(stop(_:)))
        infoMessageRect.addSubview(stopButton)
        stopButton.translatesAutoresizingMaskIntoConstraints = false
        stopButton.leadingAnchor.constraint(equalTo: infoMessageRect.leadingAnchor, constant: width / 2 - 30).isActive = true
        stopButton.bottomAnchor.constraint(equalTo: infoMessageRect.topAnchor, constant: infoMessageRect.frame.height - 30).isActive = true

        let restartButton = createLabelButtons(title: Strings.restart, selector: #selector(restart(_:)))
        infoMessageRect.addSubview(restartButton)
        restartButton.translatesAutoresizingMaskIntoConstraints = false
        restartButton.trailingAnchor.constraint(equalTo: infoMessageRect.trailingAnchor, constant: -30).isActive = true
        restartButton.bottomAnchor.constraint(equalTo: stopButton.bottomAnchor, constant: 0).isActive = true
    }

    /// function to create text inside the previous rect
    func createSetGoalText() {
        setGoalText = createLabel(text: Strings.setGoalText, fontSize: 16)
        setGoalRect.addSubview(setGoalText)
        setGoalText.numberOfLines = 0
        setGoalText.translatesAutoresizingMaskIntoConstraints = false
        setGoalText.leadingAnchor.constraint(equalTo: setGoalHeading.leadingAnchor, constant: 0).isActive = true
        setGoalText.topAnchor.constraint(equalTo: setGoalHeading.bottomAnchor, constant: 30).isActive = true
    }

    /// function to create Set Goal heading
    func createSetGoalHeading() {
        setGoalHeading = createLabel(text: Strings.setGoal, fontSize: 18)
        setGoalRect.addSubview(setGoalHeading)
        setGoalHeading.translatesAutoresizingMaskIntoConstraints = false
        setGoalHeading.leadingAnchor.constraint(equalTo: setGoalRect.leadingAnchor, constant: 30).isActive = true
        setGoalHeading.topAnchor.constraint(equalTo: setGoalRect.topAnchor, constant: 30).isActive = true
    }

    /// function to create forward and left text on Set goal rect
    func createForwardLeftLabels() {
        forwardLabel = createLabel(text: Strings.forward + Strings.meter, fontSize: 14)
        setGoalRect.addSubview(forwardLabel)
        forwardLabel.translatesAutoresizingMaskIntoConstraints = false
        forwardLabel.leadingAnchor.constraint(equalTo: setGoalRect.leadingAnchor, constant: width / 2 - 100).isActive = true
        forwardLabel.topAnchor.constraint(equalTo: setGoalText.bottomAnchor, constant: 20).isActive = true
        leftLabel = createLabel(text: Strings.left + Strings.meter, fontSize: 14)
        setGoalRect.addSubview(leftLabel)
        leftLabel.translatesAutoresizingMaskIntoConstraints = false
        leftLabel.leadingAnchor.constraint(equalTo: setGoalRect.leadingAnchor, constant: width / 2).isActive = true
        leftLabel.topAnchor.constraint(equalTo: forwardLabel.topAnchor, constant: 0).isActive = true
    }

    /// function to create input boxes for left and forward inputs
    func createInputBoxes() {
        forwardInput = createTextField()
        forwardInput.delegate = self
        setGoalRect.addSubview(forwardInput)
        forwardInput.translatesAutoresizingMaskIntoConstraints = false
        forwardInput.leadingAnchor.constraint(equalTo: setGoalRect.leadingAnchor, constant: width / 2 - 100).isActive = true
        forwardInput.topAnchor.constraint(equalTo: forwardLabel.bottomAnchor, constant: 20).isActive = true
        forwardInput.widthAnchor.constraint(equalToConstant: 75).isActive = true
        forwardInput.heightAnchor.constraint(equalToConstant: 30).isActive = true

        leftInput = createTextField()
        leftInput.delegate = self
        setGoalRect.addSubview(leftInput)
        leftInput.translatesAutoresizingMaskIntoConstraints = false
        leftInput.leadingAnchor.constraint(equalTo: leftLabel.leadingAnchor, constant: 0).isActive = true
        leftInput.topAnchor.constraint(equalTo: leftLabel.bottomAnchor, constant: 20).isActive = true
        leftInput.widthAnchor.constraint(equalToConstant: 70).isActive = true
        leftInput.heightAnchor.constraint(equalToConstant: 30).isActive = true
    }

    /// function to create cancel and start buttons
    func createButtons() {
        let cancelButton = createLabelButtons(title: Strings.canceled, selector: #selector(cancelFun(_:)))
        setGoalRect.addSubview(cancelButton)
        cancelButton.translatesAutoresizingMaskIntoConstraints = false
        cancelButton.leadingAnchor.constraint(equalTo: setGoalText.leadingAnchor, constant: 0).isActive = true
        cancelButton.bottomAnchor.constraint(equalTo: setGoalRect.bottomAnchor, constant: -15).isActive = true
        let startButton = createLabelButtons(title: Strings.startLabel, selector: #selector(doneFun(_:)))
        setGoalRect.addSubview(startButton)
        startButton.translatesAutoresizingMaskIntoConstraints = false
        startButton.bottomAnchor.constraint(equalTo: setGoalRect.bottomAnchor, constant: -15).isActive = true
        startButton.trailingAnchor.constraint(equalTo: setGoalRect.trailingAnchor, constant: -30).isActive = true
    }

    /// function to create text on rect
    ///
    /// - Parameters:
    ///   - text: text
    ///   - fontSize: font size of text
    ///   - textColor: font color of text
    /// - Returns: UILabel
    func createLabel(text: String, fontSize: CGFloat) -> UILabel {
        let label = UILabel()
        label.text = text
        label.textColor = Colors.textColor
        label.font = label.font.withSize(fontSize)
        return label
    }

    /// create Cancel and start button
    ///
    /// - Parameters:
    ///   - title: title of button ie. CANCEL or START
    ///   - selector: function that will invoke after tapping on button
    /// - Returns: UIButton
    func createLabelButtons(title: String, selector: Selector) -> UIButton {
        let button = UIButton()
        button.setTitleColor(Colors.title, for: .normal)
        button.frame.size = CGSize(width: 100, height: 40)
        button.setTitle(title, for: .normal)
        button.addTarget(self, action: selector, for: .touchUpInside)
        return button
    }

    /// function to create input field for forward and left
    ///
    /// - Returns:
    func createTextField() -> UITextField {
        let textField = UITextField()
        textField.frame.size.width = 100
        textField.layer.borderWidth = 1
        textField.layer.cornerRadius = 8
        textField.layer.masksToBounds = true
        textField.keyboardType = .numbersAndPunctuation
        textField.backgroundColor = traitCollection.userInterfaceStyle == .dark ? Colors.bdColor : .white
        return textField
    }

    /// function executed when tapping on DONE button. It will set a reference position and start the point goal navigation.
    ///
    /// - Parameter sender:
    @objc func doneFun(_ sender: UIView) {
        setGoalRect.removeFromSuperview()
        isReached = false
        let forwardDistance = (forwardInput.text == nil ? 0 : Float(forwardInput.text ?? "0")) ?? 0
        let LeftDistance = (leftInput.text == nil ? 0 : Float(leftInput.text ?? "0")) ?? 0
        let camera = sceneView.pointOfView!
        let cameraTransform = camera.transform
        _ = SCNVector3(-cameraTransform.m31, -cameraTransform.m32, -cameraTransform.m33)
        let markerInCameraFrame = SCNVector3(Float(-LeftDistance), 0.0, Float(-forwardDistance))
        let markerInWorldFrame = markerInCameraFrame.transformed(by: cameraTransform)
        marker = SCNNode(geometry: SCNPlane(width: 0.1, height: 0.1))
        marker.position = markerInWorldFrame
        let imageMaterial = SCNMaterial()
        imageMaterial.diffuse.contents = Images.gmapMarker
        marker.geometry?.firstMaterial = imageMaterial
        sceneView.scene.rootNode.addChildNode(marker)
        startingPoint.position = sceneView.pointOfView?.position ?? camera.position
        endingPoint = marker
        calculateRoute()
    }

    /// function called after tapping on STOP button.This function will remove the point goal navigation view and return back to homepage
    ///
    /// - Parameter sender:
    @objc func stop(_ sender: UIView) {
        _ = navigationController?.popViewController(animated: true)
    }

    /// function to restart the point goal navigation
    ///
    /// - Parameter sender:
    @objc func restart(_ sender: UIView) {
        infoMessageRect.removeFromSuperview()
        view.addSubview(setGoalRect)

    }

    /// function to add two SCNVector3 vector
    ///
    /// - Parameters:
    ///   - vector1: first vector
    ///   - vector2: second vector
    /// - Returns: resultant vector of vector1 + vector2
    func addVectors(_ vector1: SCNVector3, _ vector2: SCNVector3) -> SCNVector3 {
        SCNVector3(vector1.x + vector2.x, vector1.y + vector2.y, vector1.z + vector2.z)
    }

    /// delegate revoke when SceneKit node corresponding to a new AR anchor has been added to the scene.
    ///
    /// - Parameters:
    ///   - renderer: SCNSceneRenderer,
    ///   - scene:
    ///   - time:
    func renderer(_ renderer: SCNSceneRenderer, didRenderScene scene: SCNScene, atTime time: TimeInterval) {
        guard let currentFrame = sceneView.session.currentFrame else {
            return
        }
        let pixelBuffer = currentFrame.capturedImage
        if sceneView.pointOfView?.position != nil {
            processPixelBuffer(pixelBuffer, sceneView.pointOfView!)
            tempPixelBuffer = pixelBuffer
        }
    }

    /// function to process the buffer and send buffer to navigation.tflite to give result
    ///
    /// - Parameters:
    ///   - pixelBuffer: pixelBuffer from camera
    ///   - position: position of camera
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
            print("startPose ===== \(startPose.qx) , endPose ======> \(endPose.qx) , yaw ====> \(yaw)")
            let converted = try! blueDress.convertToBGRA(imageBuffer: pixelBuffer)

            let refCon = NSMutableData()
            var timingInfo: CMSampleTimingInfo = .invalid
            var formatDescription: CMVideoFormatDescription? = nil
            CMVideoFormatDescriptionCreateForImageBuffer(allocator: kCFAllocatorDefault, imageBuffer: converted, formatDescriptionOut: &formatDescription)

            var output: CMSampleBuffer? = nil
            let status = CMSampleBufferCreateForImageBuffer(allocator: kCFAllocatorDefault, imageBuffer: converted, dataReady: true, makeDataReadyCallback: nil, refcon: refCon.mutableBytes, formatDescription: formatDescription!, sampleTiming: &timingInfo, sampleBufferOut: &output)

            // extract the image buffer from the sample buffer
            let pixelBufferBGRA: CVPixelBuffer? = CMSampleBufferGetImageBuffer(output!)

            self.result = self.navigation?.recognizeImage(pixelBuffer: pixelBufferBGRA!, goalDistance: distance, goalSin: sin(yaw), goalCos: cos(yaw))
            guard let controlResult = self.result else {
                return
            }

            if self.isReached {
                self.sendControl(control: Control())
            } else {
                self.sendControl(control: controlResult)
            }

            isNavQueueBusy = false
        }
    }

    /// delegate revoke when SceneKit node corresponding to a new AR anchor has been added to the scene.
    ///
    /// - Parameters:
    ///   - renderer:
    ///   - time:
    func renderer(_ renderer: SCNSceneRenderer, updateAtTime time: TimeInterval) {
        guard let camera = sceneView.pointOfView else {
            return
        }
        checkCameraPosition(position: camera.presentation.simdPosition)
    }

    // Define a distance threshold for triggering the event
    let distanceThreshold: Float = 0.15 // adjust this value as needed

    /// function to check openBot is reached at point or not
    ///
    /// - Parameter position: position of camera
    func checkCameraPosition(position: simd_float3) {
        if endingPoint != nil && isReached != true {
            distance = simd_distance(position, endingPoint.simdPosition)
            if distance <= distanceThreshold {
                DispatchQueue.main.async {
                    self.view.addSubview(self.infoMessageRect)
                    self.isReached = true
                    self.marker.removeFromParentNode()
                }
            }
        }
    }

    /// function to cancel the point goal navigation and return to home
    ///
    /// - Parameter sender:
    @objc func cancelFun(_ sender: UIView) {
        _ = navigationController?.popViewController(animated: true)
    }

    /// function to calculate the route to the point
    func calculateRoute() {
        _ = simd_distance(startingPoint.simdPosition, endingPoint.simdPosition)
        _ = simd_normalize(endingPoint.simdPosition - startingPoint.simdPosition)
    }

    /// function called to remove keyboard
    ///
    /// - Parameter textField:
    /// - Returns:
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }

    /// function called to remove keyboard
    @objc func dismissKeyboard() {
        view.endEditing(true)
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

    /// Utility functions for dot and cross products
    func SCNVector3DotProduct(_ a: SCNVector3, _ b: SCNVector3) -> Float {
        a.x * b.x + a.y * b.y + a.z * b.z
    }

    /// function to do cross product between two vectors
    ///
    /// - Parameters:
    ///   - a: first vector
    ///   - b: second vector
    /// - Returns: cross product of a and b ie. a*b
    func SCNVector3CrossProduct(_ a: SCNVector3, _ b: SCNVector3) -> SCNVector3 {
        SCNVector3(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x)
    }

    ///function to send output controls to openBot
    ///
    /// - Parameter control:
    func sendControl(control: Control) {
        if (control.getRight() != vehicleControl.getRight() || control.getLeft() != vehicleControl.getLeft()) {
            let left = control.getLeft() * gameController.selectedSpeedMode.rawValue
            let right = control.getRight() * gameController.selectedSpeedMode.rawValue
            print("left -> \(control.getLeft()) , right ----> \(control.getRight())")
            vehicleControl = control
            bluetooth.sendData(payload: "c" + String(left) + "," + String(right) + "\n")
            NotificationCenter.default.post(name: .updateSpeedLabel, object: String(Int(left)) + "," + String(Int(right)))
            NotificationCenter.default.post(name: .updateRpmLabel, object: String(Int(control.getLeft())) + "," + String(Int(control.getRight())))
        }
    }

    func setupNavigationBarItem() {
        if UIImage(named: "back") != nil {
            let backNavigationIcon = (UIImage(named: "back")?.withRenderingMode(.alwaysOriginal))!
            let newBackButton = UIBarButtonItem(image: backNavigationIcon, title: Strings.navigation, target: self, action: #selector(back(sender:)), titleColor: Colors.navigationColor ?? .white)
            navigationItem.leftBarButtonItem = newBackButton
        }
    }

    @objc func back(sender: UIBarButtonItem) {
        _ = navigationController?.popViewController(animated: true)
    }
}

extension SCNVector3 {
    var simdVector: simd_float3 {
        simd_float3(x, y, z)
    }

    func transformed(by matrix: SCNMatrix4) -> SCNVector3 {
        let x = self.x * matrix.m11 + self.y * matrix.m21 + self.z * matrix.m31 + matrix.m41
        let y = self.x * matrix.m12 + self.y * matrix.m22 + self.z * matrix.m32 + matrix.m42
        let z = self.x * matrix.m13 + self.y * matrix.m23 + self.z * matrix.m33 + matrix.m43

        return SCNVector3(x: x, y: y, z: z)
    }
}

class Pose {
    var tx: Float
    var ty: Float
    var tz: Float
    var qx: Float
    var qy: Float
    var qz: Float
    var qw: Float

    init(tx: Float, ty: Float, tz: Float, qx: Float, qy: Float, qz: Float, qw: Float) {
        self.tx = tx
        self.ty = ty
        self.tz = tz
        self.qx = qx
        self.qy = qy
        self.qz = qz
        self.qw = qw
    }

    /// rotate a vector by this pose's orientation
    func rotateVector(vector: [Float]) -> [Float] {
        let x = vector[0]
        let y = vector[1]
        let z = vector[2]

        let x2 = qw * x + qy * z - qz * y
        let y2 = qw * y - qx * z + qz * x
        let z2 = qw * z + qx * y - qy * x
        let w2 = -qx * x - qy * y - qz * z

        return [
            qy * z2 - qz * y2 + qw * x2 + qx * w2,
            qz * x2 - qx * z2 + qw * y2 + qy * w2,
            qx * y2 - qy * x2 + qw * z2 + qz * w2
        ]
    }
}


