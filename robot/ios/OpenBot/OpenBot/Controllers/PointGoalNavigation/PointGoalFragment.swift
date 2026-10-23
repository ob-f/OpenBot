//
// Created by Nitish Yadav on 13/02/23.
//

import Foundation
import UIKit
import SceneKit
import ARKit
import simd

/// Point goal navigation: tap a spot on the floor, press Start, and the robot drives there.
class PointGoalFragment: UIViewController, ARSCNViewDelegate {
    var sceneView: ARSCNView!
    var navigation: Navigation?
    let bluetooth = bluetoothDataController.shared
    let blueDress = try! YCbCrImageBufferConverter()

    private let startButton = UIButton(type: .system)
    private let stopButton = UIButton(type: .system)
    private let hintLabel = ToastLabel()
    private var hintHideWork: DispatchWorkItem?
    private var goalNode: SCNNode?

    // Main thread state.
    private var isRunning = false
    private var lastNavUpdate: TimeInterval = 0
    private var safetyTimer: Timer?
    private var runGeneration = 0
    private var lastControl: Control?
    private var cameraHeight: Float = PointGoalFragment.savedCameraHeight()
    private var cameraHeightMeasured = false
    // When ARKit tracking went "limited" during the current drive, or nil while it's normal.
    private var trackingLimitedSince: TimeInterval?

    // Shared with the render thread and the navigation queue.
    private let shared = SharedState()
    private let navQueue = DispatchQueue(label: "openbot.navigation.navqueue")
    private var lastCalibration: TimeInterval = 0

    private static let cameraHeightKey = "pointGoalCameraHeight"
    // ARKit calls back every frame (~16 ms); this long without a navigation step means it's stuck.
    private static let watchdogTimeout: TimeInterval = 0.7
    private static let safetyTick: TimeInterval = 0.25
    // Short tracking hiccups (a bump, a fast turn, a plain floor) only pause the drive; longer ones end it.
    private static let trackingLimitedTimeout: TimeInterval = 2
    // Resend the stop once in case the first command got lost over Bluetooth.
    private static let stopResendDelay: TimeInterval = 0.1

    override func viewDidLoad() {
        super.viewDidLoad()
        sceneView = ARSCNView(frame: view.bounds)
        sceneView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        sceneView.scene = SCNScene()
        sceneView.delegate = self
        view.addSubview(sceneView)
        setupNavigationBarItem()
        setupButtons()
        setupHintLabel()

        let tap = UITapGestureRecognizer(target: self, action: #selector(handleTap(_:)))
        sceneView.addGestureRecognizer(tap)

        // Leaving the app (home button, call, app switcher) must never leave the robot driving.
        NotificationCenter.default.addObserver(self, selector: #selector(appWillResignActive), name: UIApplication.willResignActiveNotification, object: nil)

        navigation = Navigation(model: Model.fromModelItem(item: Common.returnNavigationModel()), device: RuntimeDevice.CPU, numThreads: 1)
        showHint(Strings.pointGoalTapHint)
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        let configuration = ARWorldTrackingConfiguration()
        // Only used to measure the camera height above the floor.
        configuration.planeDetection = [.horizontal]
        sceneView.session.run(configuration)
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        stopDriving()
        sceneView.session.pause()
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    // MARK: - UI

    private func setupButtons() {
        configure(button: startButton, symbol: "play.fill", color: .systemGreen, label: Strings.startLabel)
        startButton.addTarget(self, action: #selector(startPressed), for: .touchUpInside)
        configure(button: stopButton, symbol: "stop.fill", color: .systemRed, label: Strings.stopLabel)
        // Stop on finger down, not on release, so the robot stops the moment the button is touched.
        stopButton.addTarget(self, action: #selector(stopPressed), for: .touchDown)

        for button in [startButton, stopButton] {
            button.isHidden = true
            view.addSubview(button)
            button.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint.activate([
                button.centerXAnchor.constraint(equalTo: view.centerXAnchor),
                button.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -24),
                button.widthAnchor.constraint(equalToConstant: 64),
                button.heightAnchor.constraint(equalToConstant: 64),
            ])
        }
    }

    private func configure(button: UIButton, symbol: String, color: UIColor, label: String) {
        button.setImage(UIImage(systemName: symbol), for: .normal)
        button.tintColor = .white
        button.backgroundColor = color
        button.layer.cornerRadius = 32
        button.accessibilityLabel = label
    }

    private func setupHintLabel() {
        hintLabel.textInsets = UIEdgeInsets(top: 10, left: 14, bottom: 10, right: 14)
        hintLabel.numberOfLines = 0
        hintLabel.textAlignment = .center
        hintLabel.textColor = .white
        hintLabel.font = UIFont.systemFont(ofSize: 15)
        hintLabel.backgroundColor = UIColor.black.withAlphaComponent(0.7)
        hintLabel.layer.cornerRadius = 10
        hintLabel.clipsToBounds = true
        hintLabel.alpha = 0
        view.addSubview(hintLabel)
        hintLabel.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            hintLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            hintLabel.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 16),
            hintLabel.leadingAnchor.constraint(greaterThanOrEqualTo: view.leadingAnchor, constant: 16),
            hintLabel.trailingAnchor.constraint(lessThanOrEqualTo: view.trailingAnchor, constant: -16),
        ])
    }

    private func showHint(_ text: String) {
        hintHideWork?.cancel()
        hintLabel.text = text
        UIView.animate(withDuration: 0.2) { self.hintLabel.alpha = 1 }
        let work = DispatchWorkItem { [weak self] in
            UIView.animate(withDuration: 0.3) { self?.hintLabel.alpha = 0 }
        }
        hintHideWork = work
        DispatchQueue.main.asyncAfter(deadline: .now() + 4, execute: work)
    }

    // MARK: - Setting the goal

    /// The goal is where the tap ray meets the floor, with the floor at the known camera height
    /// below the phone. This only needs the camera's orientation (gravity is exact from the IMU), so
    /// it's correct from the first frame; image depth would be guessed until the robot moves.
    @objc private func handleTap(_ gesture: UITapGestureRecognizer) {
        guard !isRunning, let camera = sceneView.pointOfView else {
            return
        }
        let point = gesture.location(in: sceneView)
        let near = sceneView.unprojectPoint(SCNVector3(Float(point.x), Float(point.y), 0))
        let far = sceneView.unprojectPoint(SCNVector3(Float(point.x), Float(point.y), 1))
        let origin = near.simdVector
        let direction = simd_normalize(far.simdVector - near.simdVector)
        let cameraPosition = camera.presentation.simdWorldPosition

        let goal = PointGoalMath.intersectFloor(origin: origin, direction: direction, floorY: cameraPosition.y - cameraHeight)
        let result = PointGoalMath.validate(camera: cameraPosition, goal: goal)
        print("goal tap \(point): camera=\(cameraPosition) height=\(cameraHeight) (\(cameraHeightMeasured ? "measured" : "saved")) goal=\(String(describing: goal)) -> \(result)")

        switch result {
        case .notFloor:
            showHint(Strings.pointGoalNotFloor)
        case .tooClose:
            showHint(String(format: Strings.pointGoalTooClose, PointGoalMath.minGoalDistance))
        case .ok:
            guard let goal = goal else {
                return
            }
            placeGoal(at: goal)
            let distance = PointGoalMath.horizontalDistance(cameraPosition, goal)
            showHint(String(format: Strings.pointGoalSet, distance))
            startButton.isHidden = false
        }
    }

    private func placeGoal(at position: simd_float3) {
        goalNode?.removeFromParentNode()
        let plane = SCNPlane(width: CGFloat(PointGoalMath.markerSize), height: CGFloat(PointGoalMath.markerSize))
        let material = SCNMaterial()
        material.diffuse.contents = Images.gmapMarker
        material.isDoubleSided = true
        plane.firstMaterial = material
        let node = SCNNode(geometry: plane)
        // Lift the pin so its tip, not its center, sits on the goal spot.
        node.simdWorldPosition = position + simd_float3(0, PointGoalMath.markerSize / 2, 0)
        // Upright in the world and turned toward the camera, however the phone is mounted.
        let billboard = SCNBillboardConstraint()
        billboard.freeAxes = .Y
        node.constraints = [billboard]
        sceneView.scene.rootNode.addChildNode(node)
        goalNode = node
        shared.goal = position
    }

    private func removeGoal() {
        goalNode?.removeFromParentNode()
        goalNode = nil
        shared.goal = nil
    }

    // MARK: - Driving

    @objc private func startPressed() {
        guard !isRunning, shared.goal != nil else {
            return
        }
        guard navigation != nil else {
            showHint(Strings.pointGoalModelFailure)
            return
        }
        isRunning = true
        trackingLimitedSince = nil
        runGeneration += 1
        shared.startRun(generation: runGeneration)
        lastNavUpdate = Date.timeIntervalSinceReferenceDate
        lastControl = nil
        startButton.isHidden = true
        stopButton.isHidden = false

        // Stops the robot if the navigation loop goes quiet (frozen camera or app).
        safetyTimer = Timer.scheduledTimer(withTimeInterval: PointGoalFragment.safetyTick, repeats: true) { [weak self] _ in
            self?.safetyTick()
        }
    }

    @objc private func stopPressed() {
        guard isRunning else {
            return
        }
        print("nav: stopped by user")
        stopDriving()
        showHint(Strings.pointGoalTapHint)
    }

    @objc private func appWillResignActive() {
        // Only a drive is stopped; a goal that is just placed survives the notification shade or a call banner.
        if isRunning {
            stopDriving()
        }
    }

    private func safetyTick() {
        guard isRunning else {
            return
        }
        let silent = Date.timeIntervalSinceReferenceDate - lastNavUpdate
        if silent > PointGoalFragment.watchdogTimeout {
            print("nav: no update for \(silent) s, stopping robot")
            stopDriving()
            showHint(Strings.pointGoalWatchdog)
        }
    }

    /// Stops the motors first, then cleans up. Safe to call when not driving.
    private func stopDriving() {
        let wasRunning = isRunning
        isRunning = false
        shared.stopRun()
        sendControl(Control(), force: true)
        // A write without response is skipped while the Bluetooth buffer is full; this one is queued.
        bluetooth.stopRobot()
        if wasRunning {
            DispatchQueue.main.asyncAfter(deadline: .now() + PointGoalFragment.stopResendDelay) { [weak self] in
                guard let self = self, !self.isRunning else {
                    return
                }
                self.sendControl(Control(), force: true)
            }
        }
        safetyTimer?.invalidate()
        safetyTimer = nil
        stopButton.isHidden = true
        startButton.isHidden = true
        removeGoal()
    }

    private func onNavigationStep(generation: Int, outcome: NavigationOutcome) {
        guard isRunning, generation == runGeneration else {
            return
        }
        let now = Date.timeIntervalSinceReferenceDate
        switch outcome {
        case .drive(let control):
            lastNavUpdate = now
            trackingLimitedSince = nil
            sendControl(control, force: false)
        case .arrived(let distance):
            print("nav: goal reached at distance=\(distance)")
            stopDriving()
            showHint(Strings.goalReachedMessage)
        case .trackingLimited:
            // The pose can't be trusted right now: hold still, keep the goal, and resume once tracking
            // is back. Only a long outage ends the drive.
            lastNavUpdate = now
            sendControl(Control(), force: false)
            let since = trackingLimitedSince ?? now
            trackingLimitedSince = since
            if now - since > PointGoalFragment.trackingLimitedTimeout {
                print("nav: tracking limited for \(now - since) s, stopping")
                stopDriving()
                showHint(Strings.pointGoalTrackingLost)
            }
        case .skipped:
            // No new command was computed, so this doesn't count as a healthy step: if it keeps
            // happening, the watchdog stops the robot instead of it driving on its last command.
            break
        }
    }

    private func sendControl(_ control: Control, force: Bool) {
        if !force, let last = lastControl, last.getLeft() == control.getLeft(), last.getRight() == control.getRight() {
            return
        }
        lastControl = control
        // The policy can ask for a bit more than full power; the firmware doesn't cap it, so cap here.
        let speed = gameController.selectedSpeedMode.rawValue
        let left = Int((max(-1, min(1, control.getLeft())) * speed).rounded())
        let right = Int((max(-1, min(1, control.getRight())) * speed).rounded())
        bluetooth.sendData(payload: "c\(left),\(right)\n")
        NotificationCenter.default.post(name: .updateSpeedLabel, object: "\(left),\(right)")
        NotificationCenter.default.post(name: .updateRpmLabel, object: String(Int(control.getLeft())) + "," + String(Int(control.getRight())))
    }

    // MARK: - ARKit callbacks (render thread)

    func renderer(_ renderer: SCNSceneRenderer, didRenderScene scene: SCNScene, atTime time: TimeInterval) {
        guard let frame = sceneView.session.currentFrame, let camera = sceneView.pointOfView?.presentation else {
            return
        }
        // Skip while the previous frame is still being processed; driving on old frames is late anyway
        // and a backlog would delay the Stop button.
        guard let run = shared.beginStep() else {
            return
        }
        let generation = run.generation
        let goal = run.goal
        if case .normal = frame.camera.trackingState {
        } else {
            shared.endStep()
            DispatchQueue.main.async { [weak self] in
                self?.onNavigationStep(generation: generation, outcome: .trackingLimited)
            }
            return
        }

        let position = camera.simdWorldPosition
        let orientation = camera.simdWorldOrientation
        let pixelBuffer = frame.capturedImage
        navQueue.async { [weak self] in
            guard let self = self else {
                return
            }
            let outcome = self.navigationStep(pixelBuffer: pixelBuffer, position: position, orientation: orientation, goal: goal)
            self.shared.endStep()
            DispatchQueue.main.async {
                self.onNavigationStep(generation: generation, outcome: outcome)
            }
        }
    }

    /// Runs on the navigation queue.
    private func navigationStep(pixelBuffer: CVPixelBuffer, position: simd_float3, orientation: simd_quatf, goal: simd_float3) -> NavigationOutcome {
        let distance = PointGoalMath.horizontalDistance(position, goal)
        let yaw = PointGoalMath.deltaYaw(position: position, orientation: orientation, goal: goal)

        if shared.hasArrived(distance: distance, deltaYaw: yaw) {
            return .arrived(distance: distance)
        }
        guard let bgra = convertToBGRA(pixelBuffer), let control = navigation?.recognizeImage(pixelBuffer: bgra, goalDistance: distance, goalSin: sin(yaw), goalCos: cos(yaw)) else {
            return .skipped
        }
        print("nav: distance=\(distance) deltaYaw=\(yaw) control=(\(control.getLeft()), \(control.getRight()))")
        return .drive(control)
    }

    private func convertToBGRA(_ pixelBuffer: CVPixelBuffer) -> CVPixelBuffer? {
        guard let converted = try? blueDress.convertToBGRA(imageBuffer: pixelBuffer) else {
            return nil
        }
        let refCon = NSMutableData()
        var timingInfo: CMSampleTimingInfo = .invalid
        var formatDescription: CMVideoFormatDescription? = nil
        CMVideoFormatDescriptionCreateForImageBuffer(allocator: kCFAllocatorDefault, imageBuffer: converted, formatDescriptionOut: &formatDescription)
        guard let format = formatDescription else {
            return nil
        }
        var output: CMSampleBuffer? = nil
        CMSampleBufferCreateForImageBuffer(allocator: kCFAllocatorDefault, imageBuffer: converted, dataReady: true, makeDataReadyCallback: nil, refcon: refCon.mutableBytes, formatDescription: format, sampleTiming: &timingInfo, sampleBufferOut: &output)
        guard let sample = output else {
            return nil
        }
        return CMSampleBufferGetImageBuffer(sample)
    }

    func renderer(_ renderer: SCNSceneRenderer, updateAtTime time: TimeInterval) {
        guard time - lastCalibration > 0.5 else {
            return
        }
        lastCalibration = time
        calibrateCameraHeight()
    }

    /// Measures the camera height from ARKit's floor plane, if it sees one.
    private func calibrateCameraHeight() {
        guard let frame = sceneView.session.currentFrame, let camera = sceneView.pointOfView?.presentation else {
            return
        }
        let cameraY = camera.simdWorldPosition.y
        var planes: [(y: Float, area: Float)] = []
        var floors: [(y: Float, area: Float)] = []
        for case let anchor as ARPlaneAnchor in frame.anchors where anchor.alignment == .horizontal {
            let center = anchor.transform * simd_float4(anchor.center, 1)
            let plane = (y: center.y, area: anchor.extent.x * anchor.extent.z)
            planes.append(plane)
            // Phones with LiDAR label planes; trust a plane labelled as floor over size alone.
            if ARPlaneAnchor.isClassificationSupported, case .floor = anchor.classification {
                floors.append(plane)
            }
        }
        guard let floorY = PointGoalMath.pickFloorY(planes: floors.isEmpty ? planes : floors, cameraY: cameraY) else {
            return
        }
        let height = cameraY - floorY
        // Only heights a phone on the robot can have; not while someone holds it up by hand.
        guard height >= PointGoalMath.minMountHeight, height <= PointGoalMath.maxMountHeight else {
            return
        }
        DispatchQueue.main.async { [weak self] in
            guard let self = self else {
                return
            }
            if !self.cameraHeightMeasured || abs(height - self.cameraHeight) > 0.005 {
                print("camera height measured: \(height) m")
                UserDefaults.standard.set(height, forKey: PointGoalFragment.cameraHeightKey)
            }
            self.cameraHeight = height
            self.cameraHeightMeasured = true
        }
    }

    private static func savedCameraHeight() -> Float {
        let saved = UserDefaults.standard.float(forKey: cameraHeightKey)
        return saved > 0 ? saved : PointGoalMath.defaultCameraHeight
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

private enum NavigationOutcome {
    case drive(Control)
    case arrived(distance: Float)
    case trackingLimited
    case skipped
}

/// State read by the render thread and the navigation queue, written by the main thread.
private final class SharedState {
    private let lock = NSLock()
    private var _goal: simd_float3?
    private var running = false
    private var generation = 0
    private var stepInFlight = false
    private var arrival = ArrivalCheck()

    var goal: simd_float3? {
        get { lock.lock(); defer { lock.unlock() }; return _goal }
        set { lock.lock(); _goal = newValue; lock.unlock() }
    }

    func startRun(generation: Int) {
        lock.lock()
        running = true
        self.generation = generation
        arrival = ArrivalCheck()
        lock.unlock()
    }

    func stopRun() {
        lock.lock()
        running = false
        lock.unlock()
    }

    /// Claims the next navigation step, or nil if not driving or a step is still running.
    func beginStep() -> (generation: Int, goal: simd_float3)? {
        lock.lock()
        defer { lock.unlock() }
        guard running, !stepInFlight, let goal = _goal else {
            return nil
        }
        stepInFlight = true
        return (generation, goal)
    }

    func endStep() {
        lock.lock()
        stepInFlight = false
        lock.unlock()
    }

    func hasArrived(distance: Float, deltaYaw: Float) -> Bool {
        lock.lock()
        defer { lock.unlock() }
        return arrival.hasArrived(distance: distance, deltaYaw: deltaYaw)
    }
}

/// Math for turning a tap into a goal on the floor, in ARKit world coordinates (y is up, meters).
/// Kept lenient on purpose: it only rejects taps a floor robot can never drive to.
enum PointGoalMath {
    enum Result: Equatable {
        case ok
        case notFloor
        case tooClose
    }

    /// The floor is always at least this far below the phone on the robot.
    static let minCameraHeight: Float = 0.03
    /// Phone camera height on an OpenBot DIY body, until it has been measured.
    static let defaultCameraHeight: Float = 0.12
    /// Measured heights outside this range mean the phone isn't on the robot right now.
    static let minMountHeight: Float = 0.04
    static let maxMountHeight: Float = 0.40
    /// Closer goals would count as reached right away.
    static let minGoalDistance: Float = 0.30
    /// Further than this the tap ray is so flat that a tiny angle error is a big distance error
    /// (at 12 cm height, 0.1 degree is already ~25 cm at 4 m).
    static let maxRayDistance: Float = 6
    /// Size of the goal pin in meters.
    static let markerSize: Float = 0.1

    static func validate(camera: simd_float3, goal: simd_float3?) -> Result {
        guard let goal = goal else {
            return .notFloor
        }
        // Windows, walls, sky: anything at or above the camera can't be on the floor.
        if goal.y > camera.y - minCameraHeight {
            return .notFloor
        }
        if horizontalDistance(camera, goal) < minGoalDistance {
            return .tooClose
        }
        return .ok
    }

    /// Where the tap ray meets the floor. Taps at or above the horizon (window, wall, sky) never do.
    static func intersectFloor(origin: simd_float3, direction: simd_float3, floorY: Float) -> simd_float3? {
        guard direction.y < -1e-4 else {
            return nil
        }
        let t = (floorY - origin.y) / direction.y
        guard t > 0 else {
            return nil
        }
        let hit = origin + t * direction
        guard horizontalDistance(origin, hit) <= maxRayDistance else {
            return nil
        }
        return simd_float3(hit.x, floorY, hit.z)
    }

    /// Floor height from horizontal planes: the largest one below the camera. The lowest one would
    /// pick up false planes reflected in shiny floors.
    static func pickFloorY(planes: [(y: Float, area: Float)], cameraY: Float) -> Float? {
        var floorY: Float?
        var bestArea: Float = 0
        for plane in planes where plane.y <= cameraY - minCameraHeight && plane.area > bestArea {
            bestArea = plane.area
            floorY = plane.y
        }
        return floorY
    }

    static func horizontalDistance(_ a: simd_float3, _ b: simd_float3) -> Float {
        simd_length(simd_float2(a.x - b.x, a.z - b.z))
    }

    /// Angle from the camera's forward direction to the goal, in (-pi, pi].
    /// Uses simd's quaternion rotation, which is right for any phone roll (landscape mount).
    static func deltaYaw(position: simd_float3, orientation: simd_quatf, goal: simd_float3) -> Float {
        let forward = orientation.act(simd_float3(0, 0, -1))
        var yaw = atan2(forward.z, forward.x) - atan2(goal.z - position.z, goal.x - position.x)
        if yaw > .pi {
            yaw -= 2 * .pi
        } else if yaw <= -.pi {
            yaw += 2 * .pi
        }
        return yaw
    }
}

/// Decides when the robot has arrived. Besides being within the goal radius, a robot that is close and
/// has just passed the goal (it's now beside or behind it, or getting further away again) has arrived
/// too: turning back to hit the exact spot only makes it circle around it.
struct ArrivalCheck {
    /// Within this distance of the goal the robot has arrived.
    static let goalRadius: Float = 0.10
    /// Inside this distance, passing the goal also counts as arriving.
    static let arrivalZone: Float = 0.30
    /// Moving away by more than this from the closest point means the goal was passed.
    static let movingAway: Float = 0.03

    private var closest = Float.greatestFiniteMagnitude

    /// - Parameters:
    ///   - distance: distance to the goal in meters
    ///   - deltaYaw: angle between heading and goal direction, as from PointGoalMath.deltaYaw
    mutating func hasArrived(distance: Float, deltaYaw: Float) -> Bool {
        closest = min(closest, distance)
        if distance < ArrivalCheck.goalRadius {
            return true
        }
        if distance >= ArrivalCheck.arrivalZone {
            return false
        }
        let goalBesideOrBehind = abs(deltaYaw) > Float.pi / 2
        let movingAway = distance > closest + ArrivalCheck.movingAway
        return goalBesideOrBehind || movingAway
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
