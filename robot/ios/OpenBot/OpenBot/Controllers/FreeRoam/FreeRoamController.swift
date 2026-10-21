//
//  Created by Nitish Yadav on 23/08/22.
//

import UIKit
import AVFoundation
import Network
import FirebaseAuth

/// Implementation of the FreeRoamController
class FreeRoamController: CameraController, UIGestureRecognizerDelegate {
    var sonarLabel = UILabel()
    var voltageLabel = UILabel()
    var outerSonar: UIView!
    var selectedSpeedMode: SpeedMode = SpeedMode.SLOW;
    var selectedControlMode: ControlMode = ControlMode.GAMEPAD;
    var selectedDriveMode: DriveMode = DriveMode.DUAL;
    let bluetooth = bluetoothDataController.shared;
    var gameControllerObj: GameController?;
    var vehicleControl = Control();
    let firstView = UIView()
    let secondView = UIView()
    let deviceOrientation = DeviceCurrentOrientation.shared
    var gameController = GameController.shared
    var bluetoothIcon = UIImageView()
    var isClientConnected: Bool = false
    var audioPlayer = AudioPlayer.shared
    private let mainView = UIView()
    let mSocket = NativeWebSocket.shared;
    let roomId: String = Auth.auth().currentUser?.email ?? ""
    private var serverWebrtcDelegate: ServerWebrtcDelegate?
    let webSocketMsgHandler = WebSocketMessageHandler();
    let fragmentType = FragmentType.shared

    /// Called after the view controller has loaded.
    override func viewDidLoad() {
        super.viewDidLoad()
        print("navigationController is :", navigationController);
        setupNavigationBarItem()
        setupSpeedMode()
        applySafeAreaConstraints()
        apply()
        createFirstView()
        createSecondView()
        createSpeedometer()
        createSonalLabel();
        createVoltageLabel()
        createLabels()
        createVoltageController(h: 0)
        createSonarController(h: 0)
        NotificationCenter.default.addObserver(self, selector: #selector(updateScreen), name: .updateLabel, object: nil);
        createDIcon()
        createDriveIcon()
        createBluetoothIcon()
        updateControlMode()
        updateGameControllerModeType()
        updateSpeedModes()
        drawTick()
        if currentOrientation == UIInterfaceOrientation.landscapeRight || currentOrientation == UIInterfaceOrientation.landscapeLeft {
            applyLandScapeConstraint()
        }
        if let value = preferencesManager.getControlMode() {
            if let mode = ControlMode(rawValue: value){
                if(mode == ControlMode.GAMEPAD){
                    selectedControlMode = ControlMode.PHONE
                }
                else if(mode == ControlMode.PHONE){
                    selectedControlMode = ControlMode.WEB
                }
                else{
                    selectedControlMode = ControlMode.GAMEPAD
                }
                updateControlMode();
            }
        }
        if let value = preferencesManager.getDriveMode() {
            if let mode = DriveMode(rawValue: value){
                if(mode == DriveMode.DUAL){
                    selectedDriveMode = DriveMode.JOYSTICK
                    gameController.selectedDriveMode = DriveMode.JOYSTICK
                }
                else if(mode == DriveMode.GAME){
                    selectedDriveMode = DriveMode.DUAL
                    gameController.selectedDriveMode = DriveMode.DUAL
                }
                else if(mode == DriveMode.JOYSTICK){
                    selectedDriveMode = DriveMode.GAME
                    gameController.selectedDriveMode = DriveMode.GAME
                }
                updateGameControllerModeType()
            }
        }
        syncSpeedFromStoredPreferences()
        
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothConnected, object: nil);
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothDisconnected, object: nil);
        NotificationCenter.default.addObserver(self, selector: #selector(decreaseSpeedMode), name: .decreaseSpeedMode, object: nil);
        NotificationCenter.default.addObserver(self, selector: #selector(increaseSpeedMode), name: .increaseSpeedMode, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateDriveMode), name: .updateDriveMode, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateDataFromControllerApp), name: .updateStringFromControllerApp, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateLightsCommandFromControllerApp), name: .updateLightsCommandFromControllerApp, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(clientConnected), name: .clientConnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(clientDisconnected), name: .clientDisConnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(switchCamera), name: .switchCamera, object: nil)
        gameController.resetControl = false
        fragmentType.currentFragment = "FreeRoam";
        let msg = JSON.toString(FragmentTypeEvent(status: .init(FRAGMENT_TYPE: self.fragmentType.currentFragment)));
        client.send(message: msg);
    }

    /**
     Removing all notifications
     */
    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    /// Called after the view was dismissed, covered or otherwise hidden.
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated);
    }

    func applySafeAreaConstraints() {
        mainView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(mainView)
    }

    override func initializeCamera() {
        if isClientConnected {
            super.initializeCamera()
        }
    }

    /// function to apply the margins and constraints
    func apply() {
        let margins = view.layoutMarginsGuide
        NSLayoutConstraint.activate([
            mainView.leadingAnchor.constraint(equalTo: margins.leadingAnchor),
            mainView.trailingAnchor.constraint(equalTo: margins.trailingAnchor),
            mainView.topAnchor.constraint(equalTo: margins.topAnchor),
            mainView.bottomAnchor.constraint(equalTo: margins.bottomAnchor)
        ])
    }

    /// Called when the view controller's view's size is changed by its parent (i.e. for the root view controller when its window rotates or is resized).
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        DeviceCurrentOrientation.shared.findDeviceOrientation()
        switch currentOrientation {
        case .portrait:
            ApplyPortraitConstraint()
            break
        case .landscapeRight:
            applyLandScapeConstraint()
            break
        case .landscapeLeft:
            applyLandScapeConstraint()
            break
        default:
            ApplyPortraitConstraint();
        }

    }

    /// Notifies the view controller that its view is about to be added to a view hierarchy.
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        syncSpeedFromStoredPreferences()
    }

    override func captureOutput(_ output: AVFoundation.AVCaptureOutput, didOutput sampleBuffer: CoreMedia.CMSampleBuffer, from connection: AVFoundation.AVCaptureConnection) {
        super.captureOutput(output, didOutput: sampleBuffer, from: connection)
    }

    /// Initialization routine
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
    }

    /// function to apply the portrait constraints on objects
    func ApplyPortraitConstraint() {
        removeConstraints()
        let leading = secondView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 0)
        leading.identifier = Strings.secondView
        let top = secondView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: view.frame.width / 2 - 30)
        top.identifier = Strings.secondView
        let width = secondView.widthAnchor.constraint(equalTo: view.widthAnchor, multiplier: 1)
        width.identifier = Strings.secondView
        let height = secondView.heightAnchor.constraint(equalTo: view.heightAnchor, multiplier: 0.5)
        height.identifier = Strings.secondView
        NSLayoutConstraint.activate([
            leading, top, width, height
        ])
    }

    /// function to apply the landscape constraints on objects
    func applyLandScapeConstraint() {
        removeConstraints()
        let leading = secondView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: height / 2 - 12)
        leading.identifier = Strings.secondView
        let top = secondView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: -30)
        top.identifier = Strings.secondView
        let width = secondView.widthAnchor.constraint(equalTo: view.widthAnchor, multiplier: 0.5)
        width.identifier = Strings.secondView
        let height = secondView.heightAnchor.constraint(equalTo: view.heightAnchor, multiplier: 1)
        height.identifier = Strings.secondView
        NSLayoutConstraint.activate([
            leading, top, width, height
        ])
    }

    /// function to remove all the constraints
    func removeConstraints() {
        for constraint in view.constraints {
            if constraint.identifier == Strings.secondView {
                view.removeConstraint(constraint)
            }
        }
    }

    /// function to create the first view
    func createFirstView() {
        firstView.frame = CGRect(x: 0, y: 0, width: view.frame.width, height: view.frame.height / 2)
        mainView.addSubview(firstView)
        firstView.translatesAutoresizingMaskIntoConstraints = false

    }

    /// function to create the second view
    func createSecondView() {
        let h = view.frame.height / 2
        let width = view.frame.width
        let size = CGSize(width: width, height: h)
        let origin = CGPoint(x: 0, y: height / 2 - 30)
        secondView.frame = CGRect(origin: origin, size: resized(size: size, basedOn: dimension))
        mainView.addSubview(secondView)
        secondView.translatesAutoresizingMaskIntoConstraints = false
    }

    /// function to create the speedometer
    func createSpeedometer() {
        let speedometer = GaugeView(frame: CGRect(x: 0, y: -10, width: width, height: 256))
        speedometer.backgroundColor = .clear
        speedometer.value = 0
        speedometer.tag = 100
        firstView.addSubview(speedometer)
    }

    /// function to create the ticks
    func drawTicks(count: Int, radius: Int) {
        var rotationInDegrees: CGFloat = 0
        let pointOfBreak1: Int = count / 4 - 1;
        let pointOfBreak2: Int = 3 * count / 4 + 1;
        for i in 0..<count {
            if i <= pointOfBreak1 || i >= pointOfBreak2 {
                let tick = createTick()
                let x = CGFloat(Float(187.5) + Float(radius) * cosf(2 * Float(i) * Float(Double.pi) / Float(count) - Float(Double.pi) / 2))
                let y = CGFloat(Float(240) + Float(radius) * sinf(2 * Float(i) * Float(Double.pi) / Float(count) - Float(Double.pi) / 2))
                tick.center = CGPoint(x: x - 10, y: y - 70)
                tick.transform = CGAffineTransform.identity.rotated(by: rotationInDegrees * .pi / 180.0)
                firstView.addSubview(tick)
                rotationInDegrees = rotationInDegrees + (360.0 / CGFloat(count))
            }
        }
    }

    /// function to create a single tick
    func createTick() -> UIView {
        let tick = UIView(frame: CGRect(x: 0, y: 0, width: 2.0, height: 1.0))
        tick.backgroundColor = UIColor(red: 0.00, green: 0.44, blue: 0.77, alpha: 1.00)
        return tick
    }

    /// function to draw the ticks
    func drawTick() {
        let ticks = 40
        var radius = 120
        for _ in 0...3 {
            drawTicks(count: ticks, radius: radius)
            radius = radius - 22;
        }
    }

    func createLabels() {
        createLabel(value: Strings.controller, x: 35, y: 20, width: 100, height: 40)
        createLabel(value: Strings.speed, x: 35, y: 207, width: 100, height: 40)
        createLabel(value: Strings.driveMode, x: 35, y: 110, width: 100, height: 40)
    }

    func createVoltageLabel() {
        voltageLabel.frame = CGRect(x: 43, y: 300, width: 50, height: 40)
        voltageLabel.text = "0V"
        voltageLabel.textColor = Colors.textColor;
        voltageLabel.font = voltageLabel.font.withSize(15)
        firstView.addSubview(voltageLabel)
    }

    func createSonalLabel() {
        sonarLabel.frame = CGRect(x: Int(width) - 96, y: 300, width: 60, height: 40)
        sonarLabel.text = "0CM"
        sonarLabel.textColor = Colors.textColor;
        sonarLabel.font = sonarLabel.font.withSize(15)
        firstView.addSubview(sonarLabel)
    }

    func createDIcon() {
        let dIcon = UIButton()
        dIcon.setTitle("D", for: .normal)
        dIcon.layer.borderWidth = 1
        dIcon.layer.borderColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white.cgColor : UIColor.black.cgColor;
        dIcon.setTitleColor(traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black, for: .normal)
        dIcon.frame = CGRect(x: 100, y: 230, width: 40, height: 40)
        dIcon.layer.cornerRadius = 5
        firstView.addSubview(dIcon)
    }

    /// function to create the button for drive mode
    func createDriveIcon() {
        let driveIconRect = createRectangle(x: 160, y: 230, width: 40, height: 40, borderColor: "bdColor", isBorderRequire: true)
        driveIconRect.layer.borderColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white.cgColor : UIColor.black.cgColor;
        let driveIcon = UIImageView(frame: CGRect(x: driveIconRect.frame.size.width / 4, y: driveIconRect.frame.size.height / 4, width: 20, height: 20))
        driveIcon.image = UIImage(named: "drive")
        firstView.addSubview(driveIconRect)
        driveIconRect.addSubview(driveIcon)
    }

    /// function to create the button for bluetooth
    func createBluetoothIcon() {
        let blueToothIconRect = createRectangle(x: 220, y: 230, width: 40, height: 40, borderColor: "bdColor", isBorderRequire: true)
        blueToothIconRect.layer.borderColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white.cgColor : UIColor.black.cgColor;
        bluetoothIcon = UIImageView(frame: CGRect(x: 2 * blueToothIconRect.frame.size.width / 4 - 10, y: blueToothIconRect.frame.size.height / 4, width: 20, height: 20))
        if isBluetoothConnected {
            bluetoothIcon.image = Images.bluetoothConnected_v2
        } else {
            bluetoothIcon.image = Images.bluetoothDisconnected_v2
        }
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(openBluetoothSettings(tapGestureRecognizer:)))
        blueToothIconRect.isUserInteractionEnabled = true
        blueToothIconRect.addGestureRecognizer(tapGesture)
        firstView.addSubview(blueToothIconRect)
        blueToothIconRect.addSubview(bluetoothIcon)
    }

    /// function to create the label for the voltage
    func createVoltageController(h: Double) {
        let outerVoltage = createRectangle(x: 30, y: 190, width: 50, height: 110, borderColor: "bdColor", isBorderRequire: true)
        outerVoltage.layer.borderColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white.cgColor : UIColor.black.cgColor;
        let relativeHeight = Double(h * 9.16)
        let innerVoltage = createRectangle(x: 0, y: 110 - Int(relativeHeight), width: 49, height: Int(relativeHeight) - 1, borderColor: "noColor", isBorderRequire: false)
        innerVoltage.layer.cornerRadius = 5;
        innerVoltage.backgroundColor = Colors.voltageDividerColor
        outerVoltage.addSubview(innerVoltage)
        firstView.addSubview(outerVoltage)
    }

    /// function to create the label for the sonar
    func createSonarController(h: Double) {
        if outerSonar != nil {
            outerSonar.subviews[0].removeFromSuperview()
        }
        let relativeHeight = Double(h * 0.3667)
        outerSonar = createRectangle(x: Int(width) - 100, y: 190, width: 50, height: 110, borderColor: "borderColor", isBorderRequire: true);
        firstView.addSubview(outerSonar)
        outerSonar.layer.borderColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white.cgColor : UIColor.black.cgColor;
        let innerSonar = createRectangle(x: 0, y: 110 - Int(relativeHeight), width: 49, height: Int(relativeHeight) - 1, borderColor: "noColor", isBorderRequire: false)
        innerSonar.layer.cornerRadius = 5;
        innerSonar.backgroundColor = Colors.sonar
        outerSonar.addSubview(innerSonar)
    }

    /// function to create the label for the buttons
    func createLabel(value: String, x: Int, y: Int, width: Int, height: Int) {
        let label = UILabel()
        label.text = value
        label.font = UIFont(name: "medium", size: adapted(dimensionSize: 30, to: .width))
        label.frame = CGRect(x: x, y: y, width: width, height: height)
        label.textColor = Colors.textColor;
        label.font = label.font.withSize(15)
        secondView.addSubview(label)

    }

    /// function to create the rectangle for the button
    func createRectangle(x: Int, y: Int, width: Int, height: Int, borderColor: String, isBorderRequire: Bool) -> UIView {
        let rectangleView = UIView();
        let origin = CGPoint(x: x, y: y)
        let size = CGSize(width: width, height: height)
        rectangleView.frame = CGRect(origin: origin, size: size)
        rectangleView.layer.cornerRadius = 5;
        if isBorderRequire {
            rectangleView.layer.borderWidth = 1;
            return rectangleView;
        }
        return rectangleView;

    }

    /// function to create the control type mode and update values
    func updateControlMode() {
        let gamePadController = createMode(x: 35, y: 55, width: Int(width / 4), label: Strings.gamepad, icon: "gamepad", action: #selector(gamepadMode(_:)))
        let phoneController = createMode(x: 140, y: 55, width: Int(width / 4), label: Strings.phone, icon: "phone", action: #selector(phoneMode(_:)))
        let webController = createMode(x: 245, y: 55, width: Int(width / 4), label: Strings.web, icon: "globe", action: #selector(webMode(_:)))
        if selectedControlMode == ControlMode.GAMEPAD {
            NotificationCenter.default.addObserver(self, selector: #selector(updateControllerValues), name: NSNotification.Name(rawValue: Strings.controllerConnected), object: nil);
            gameControllerObj = gameController
            gameController.selectedControlMode = ControlMode.GAMEPAD
            gamePadController.backgroundColor = Colors.title
            let msg = JSON.toString(ConnectionActiveEvent(status: .init(CONNECTION_ACTIVE: "false")));
            client.send(message: msg);
            if(webRTCClient != nil){
                webRTCClient.disconnect();
            }
        } else if selectedControlMode == ControlMode.PHONE {
            gameControllerObj = nil;
            gameController.selectedControlMode = ControlMode.PHONE
            NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: Strings.controllerConnected), object: nil);
            phoneController.backgroundColor = Colors.title
            client.start();
            let msg = JSON.toString(ConnectionActiveEvent(status: .init(CONNECTION_ACTIVE: "true")));
            client.send(message: msg);
        }
        else {
            gameControllerObj = nil;
            gameController.selectedControlMode = ControlMode.WEB
            webController.backgroundColor = Colors.title
            client.start();
            let msg = JSON.toString(ConnectionActiveEvent(status: .init(CONNECTION_ACTIVE: "false")));
            client.send(message: msg);
            if(webRTCClient != nil){
                webRTCClient.disconnect();
            }
            mSocket.connectWebServer { [weak self] in
                self?.sendMessage()
                self?.serverWebrtcDelegate = ServerWebrtcDelegate()
            }
        }
        updateGameControllerModeType()
        secondView.addSubview(gamePadController)
        secondView.addSubview(phoneController)
        secondView.addSubview(webController)
    }

    /// function to create the game controller mode and update values
    func updateGameControllerModeType() {
        let joystick = createMode(x: 35, y: 145, width: Int(width / 4), label: Strings.joystick, icon: "joystick", action: #selector(joystick(_:)))
        let game = createMode(x: 140, y: 145, width: Int(width / 4), label: Strings.game, icon: "game", action: #selector(gameMode(_:)))
        let dual = createMode(x: 245, y: 145, width: Int(width / 4), label: Strings.dual, icon: "dual", action: #selector(dualMode(_:)))
        if selectedControlMode == ControlMode.PHONE {
            selectedDriveMode = DriveMode.DUAL
            dual.backgroundColor = Colors.titleDeactivated
        }
        else if selectedControlMode == ControlMode.WEB{
            selectedDriveMode = DriveMode.GAME
            game.backgroundColor = Colors.titleDeactivated;
        }
        else {
            if selectedDriveMode == DriveMode.JOYSTICK {
                joystick.backgroundColor = Colors.title
            } else if selectedDriveMode == DriveMode.GAME {
                game.backgroundColor = Colors.title
            } else if selectedDriveMode == DriveMode.DUAL {
                dual.backgroundColor = Colors.title
            }
        }
        gameController.selectedDriveMode = selectedDriveMode
        secondView.addSubview(joystick)
        secondView.addSubview(game)
        secondView.addSubview(dual)
    }

    @objc func joystick(_ sender: UIView) {
        selectedDriveMode = DriveMode.JOYSTICK
        gameController.selectedDriveMode = DriveMode.JOYSTICK
        updateGameControllerModeType()
        preferencesManager.setDriveMode(value: DriveMode.DUAL.rawValue);
    }

    @objc func gameMode(_ sender: UIView) {
        selectedDriveMode = DriveMode.GAME
        gameController.selectedDriveMode = DriveMode.GAME
        updateGameControllerModeType()
        preferencesManager.setDriveMode(value: DriveMode.JOYSTICK.rawValue);
    }

    @objc func dualMode(_ sender: UIView) {
        selectedDriveMode = DriveMode.DUAL
        gameController.selectedDriveMode = DriveMode.DUAL
        updateGameControllerModeType()
        preferencesManager.setDriveMode(value: DriveMode.GAME.rawValue);
    }

    /// function to update the speed modes value and create UI
    func updateSpeedModes() {
        let slowMode = createMode(x: 35, y: 242, width: Int(width / 4), label: Strings.slow, icon: "slow", action: #selector(slow(_:)))
        let mediumMode = createMode(x: 140, y: 242, width: Int(width / 4), label: Strings.medium, icon: "medium", action: #selector(medium(_:)))
        let fastMode = createMode(x: 245, y: 242, width: Int(width / 4), label: Strings.fast, icon: "fast", action: #selector(fast(_:)))
        if selectedSpeedMode == SpeedMode.SLOW {
            slowMode.backgroundColor = Colors.title
        } else if selectedSpeedMode == SpeedMode.NORMAL {
            mediumMode.backgroundColor = Colors.title
        } else {
            fastMode.backgroundColor = Colors.title
        }
        gameController.selectedSpeedMode = selectedSpeedMode;
        secondView.addSubview(slowMode)
        secondView.addSubview(mediumMode)
        secondView.addSubview(fastMode)
    }

    @objc func phoneMode(_ sender: UIView) {
        selectedControlMode = ControlMode.PHONE
        updateControlMode()
        preferencesManager.setControlMode(value: ControlMode.GAMEPAD.rawValue);
    }

    @objc func gamepadMode(_ sender: UIView) {
        selectedControlMode = ControlMode.GAMEPAD;
        updateControlMode()
        preferencesManager.setControlMode(value: ControlMode.WEB.rawValue);
    }
    
    @objc func webMode(_ sender: UIView){
        selectedControlMode = ControlMode.WEB;
        updateControlMode()
        selectedDriveMode = DriveMode.GAME;
        preferencesManager.setControlMode(value: ControlMode.PHONE.rawValue);
    }

    @objc func slow(_ sender: UIView) {
        applySpeedMode(.SLOW)
    }

    @objc func medium(_ sender: UIView) {
        applySpeedMode(.NORMAL)
    }

    @objc func fast(_ sender: UIView) {
        applySpeedMode(.FAST)
    }

    /// UI function to create the mode
    func createMode(x: Int, y: Int, width: Int, label: String, icon: String, action: Selector?) -> UIView {
        let modeRectangle = createRectangle(x: x, y: y, width: width, height: 55, borderColor: "noColor", isBorderRequire: false)
        modeRectangle.backgroundColor = Colors.freeRoamButtonsColor
        let modeRectangleLabel = UILabel(frame: CGRect(x: 10, y: 4, width: 100, height: 50))
        modeRectangleLabel.text = label
        modeRectangleLabel.textColor = .white
        modeRectangleLabel.font = modeRectangleLabel.font.withSize(13)
        modeRectangle.addSubview(modeRectangleLabel)
        let modeIcon = UIImageView(frame: CGRect(x: modeRectangle.frame.width - 12.8 - modeRectangle.frame.width / 10, y: modeRectangle.frame.height / 3, width: 19, height: 19))
        if let image = UIImage(named: icon)?.withRenderingMode(.alwaysTemplate) {
            modeIcon.image = image
            modeIcon.tintColor = .white
        }        
        let tapGesture = UITapGestureRecognizer(target: self, action: action)
        tapGesture.delegate = self
        modeRectangle.addGestureRecognizer(tapGesture)
        modeRectangle.addSubview(modeIcon)
        modeRectangle.layer.borderColor = .none
        return modeRectangle
    }

    /// update the screen values
    @objc func updateScreen() {
        updateSonar()
        updateVoltage()
        updateSpeedometer()
    }

    /// update the sonar values
    func updateSonar() {
        let sonar = bluetooth.sonarData
        if sonar != "" {
            let index = sonar.index(after: sonar.startIndex)
            let actualSonarValue = min(Int(String(sonar[index...])) ?? 0, 300)
            sonarLabel.text = String(actualSonarValue) + " CM"
            createSonarController(h: Double(actualSonarValue))
        }
    }

    /// update the voltage value
    func updateVoltage() {
        let voltage = bluetooth.voltageDivider
        voltageLabel.frame.origin.x = 35;
        if voltage != "" {
            let index = voltage.index(after: voltage.startIndex)
            voltageLabel.text = String(voltage[index...]) + " V"
            createVoltageController(h: Double(String(voltage[index...])) ?? 0)
        }
    }

    /// update the speedometer value
    func updateSpeedometer() {
        let oldTag = view.viewWithTag(100);
        oldTag?.removeFromSuperview();
        let a = GaugeView(frame: CGRect(x: 0, y: 0, width: view.frame.width, height: 256))
        a.tag = 100
        let speedometer = bluetooth.speedometer
        if speedometer != "" {
            let index_1 = speedometer.index(after: speedometer.startIndex)
            let indexOfComma = speedometer.firstIndex(of: ",") ?? index_1
            let index_2 = speedometer.index(before: indexOfComma)
            let leftFront = Float(speedometer[index_1...index_2])
            let rightFont = Float(speedometer[speedometer.index(after: indexOfComma)...])
            let value = Int(((leftFront ?? 0) + (rightFont ?? 0)) / 2)
            a.value = value
            a.backgroundColor = .clear
            firstView.addSubview(a)
        }
    }

    /// Main control update function
    @objc func updateControllerValues() {
        gameController.updateControllerValues()
    }

    /// check the status bar orientation.
    var statusBarOrientation: UIInterfaceOrientation? {
        get {
            guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                  let orientation = windowScene.interfaceOrientation as UIInterfaceOrientation?
            else {
                #if DEBUG
                fatalError("Could not obtain UIInterfaceOrientation from a valid windowScene")
                #else
                return nil
                #endif
            }
            return orientation
        }
    }

    func setupNavigationBarItem() {
        if UIImage(named: "back") != nil {
            let backNavigationIcon = (UIImage(named: "back")?.withRenderingMode(.alwaysOriginal))!
            let newBackButton = UIBarButtonItem(image: backNavigationIcon, title: Strings.freeRoam, target: self, action: #selector(FreeRoamController.back(sender:)), titleColor: Colors.navigationColor ?? .white)
            navigationItem.leftBarButtonItem = newBackButton
        }
    }

    @objc func back(sender: UIBarButtonItem) {
        _ = navigationController?.popViewController(animated: true)
    }

    override func beginAppearanceTransition(_ isAppearing: Bool, animated: Bool) {
        super.beginAppearanceTransition(isAppearing, animated: animated)

    }

    override func endAppearanceTransition() {
        super.endAppearanceTransition()
    }

    /// open the bluetooth settings screen
    @objc func openBluetoothSettings(tapGestureRecognizer: UITapGestureRecognizer) {
        let nextViewController = (storyboard?.instantiateViewController(withIdentifier: Strings.bluetoothScreen))
        navigationController?.pushViewController(nextViewController!, animated: true)
    }

    /// update when decrease mode
    @objc func updateConnect(_ notification: Notification) {
        if (isBluetoothConnected) {
            bluetoothIcon.image = Images.bluetoothConnected_v2
        } else {
            bluetoothIcon.image = Images.bluetoothDisconnected_v2
        }
    }

    /// callback to apply speed mode when speed is decreased
    @objc func decreaseSpeedMode(_ notification: Notification) {
        applySpeedMode(gameController.selectedSpeedMode, playAudio: true)
    }

    /// callback to apply speed mode when speed is increased
    @objc func increaseSpeedMode(_ notification: Notification) {
        applySpeedMode(gameController.selectedSpeedMode, playAudio: true)
    }

    /// update drive mode
    @objc func updateDriveMode(_ notification: Notification) {
        if selectedControlMode == .PHONE {
            selectedDriveMode = .DUAL
        } else {
            switch selectedDriveMode {
            case .JOYSTICK:
                selectedDriveMode = .GAME
                break;
            case .GAME:
                selectedDriveMode = .DUAL
                break;
            case .DUAL:
                selectedDriveMode = .JOYSTICK;
                break;
            }
        }
        updateGameControllerModeType()
        audioPlayer.playDriveMode(driveMode: selectedDriveMode);
    }

    /// update screen data coming from application
    @objc func updateDataFromControllerApp(_ notification: Notification) {
        if gameController.selectedControlMode == ControlMode.GAMEPAD {
            return
        }
        if notification.object != nil {
            let command = notification.object as! String
            let rightSpeed = command.slice(from: "r:", to: ", ");
            let leftSpeed = command.slice(from: "l:", to: "}}");
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
            let controllerCommand = command.slice(from: "command: ", to: " }")
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
            default:
                break;
            }
        }
    }

    /// callback to switch camera when web controller sends switch camera command
    @objc func switchCamera() {
        switchCameraView()
    }

    /// update when client connects
    @objc func clientConnected(_ notification: Notification) {
        isClientConnected = true;
        initializeCamera()
    }

    /// update when client disconnects
    @objc func clientDisconnected(_ notification: Notification) {
        isClientConnected = false
        stopSession()
    }

    /// update speed mode
    func setupSpeedMode() {
        gameController.selectedDriveMode = DriveMode.JOYSTICK;
    }

    /// function to load saved speed mode from preferences
    func syncSpeedFromStoredPreferences() {
        if let value = preferencesManager.getSpeedMode(),
           let mode = SpeedMode(rawValue: value) {
            applySpeedMode(mode)
        }
    }

    /// function to apply speed mode and update the UI
    func applySpeedMode(_ mode: SpeedMode, playAudio: Bool = false) {
        selectedSpeedMode = mode
        gameController.selectedSpeedMode = mode
        preferencesManager.setSpeedMode(value: mode.rawValue)
        updateSpeedModes()
        if playAudio {
            audioPlayer.playSpeedMode(speedMode: mode)
        }
    }
    
    /// function to parse the message for the connection and send it.
    func sendMessage() {
        var msg = JSON.toString(ServerConnectionActiveEvent(status: .init(CONNECTION_ACTIVE: "true"), roomId: roomId));
        print(msg);
        var data = msg.data(using: .utf8);
        mSocket.send(data: data!);
        msg = JSON.toString(ServerVideoProtocolEvent(status: .init(VIDEO_PROTOCOL: "WEBRTC"), roomId: roomId));
        data = msg.data(using: .utf8);
        mSocket.send(data: data!);
        msg = JSON.toString(ServerVideoServerUrlEvent(status: .init(VIDEO_SERVER_URL: ""), roomId: roomId));
        data = msg.data(using: .utf8);
        mSocket.send(data: data!);
        msg = JSON.toString(ServerVideoCommandEvent(status: .init(VIDEO_COMMAND: "START"), roomId: roomId));
        data = msg.data(using: .utf8);
        mSocket.send(data: data!);
        msg = JSON.toString(ServerVehicleStatusEvent(status: .init(LOGS: false, NOISE: false, NETWORK: false, DRIVE_MODE: "GAME", INDICATOR_LEFT: false, INDICATOR_RIGHT: false, INDICATOR_STOP: true), roomId: roomId));
        data = msg.data(using: .utf8);
        mSocket.send(data: data!);
    }

}

extension String {
    func slice(from: String, to: String) -> String? {
        (range(of: from)?.upperBound).flatMap { substringFrom in
            (range(of: to, range: substringFrom..<endIndex)?.lowerBound).map { substringTo in
                String(self[substringFrom..<substringTo])
            }
        }
    }
}



