//
// Created by Nitish Yadav on 22/12/22.
//

import Foundation
import UIKit

class RobotInfoFrame: UIViewController {

    var topPadding: CGFloat = 0
    var robotType = UILabel()
    var robotName = UILabel()
    var bluetoothIconButton: UIButton!
    var refreshIcon: UIImageView!
    var sensorHeading = UILabel()
    var voltageDividerCheckBox = Checkbox()
    var sonarCheckBox = Checkbox()
    var bumperCheckBox = Checkbox()
    var voltageDividerLabel = UILabel()
    var sonarLabel = UILabel()
    var bumperLabel = UILabel()
    var wheelOdometerHeadingHeading = UILabel()
    var frontCheckBox = Checkbox()
    var frontCheckBoxLabel = UILabel()
    var backCheckBox = Checkbox()
    var backLabel = UILabel()
    var ledHeading = UILabel()
    var indicatorCheckbox = Checkbox()
    var indicatorLabel = UILabel()
    var frontLedCheckbox = Checkbox()
    var frontLedLabel = UILabel()
    var backLedCheckbox = Checkbox()
    var backLedLabel = UILabel()
    var statusCheckbox = Checkbox()
    var statusLabel = UILabel()
    var readingHeading = UILabel()
    var battery = UILabel()
    var speed = UILabel()
    var sonar = UILabel()
    var sendCommandsHeading = UILabel()
    var motorLabel = UILabel()
    var forwardButton = UIButton()
    var backwardButton = UIButton()
    var stopButton = UIButton()
    var lightLabel = UILabel()
    let bluetooth = bluetoothDataController.shared
    var robotInfo: String = ""
    var lightSlider: UISlider!
    @IBOutlet weak var openBotIcon: UIImageView!
    var topAnchorConstraint: NSLayoutConstraint!;
    var centerXConstraint: NSLayoutConstraint!;

    /// Called after the view InfoFrame has loaded.
    override func viewDidLoad() {
        super.viewDidLoad()
        setupNavigationBarItem()
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let window = windowScene.windows.first {
            topPadding = window.safeAreaInsets.top
        } else {
            topPadding = 20
        }

        createUI()
        setupLogo();
        updateConstraints()
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothConnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothDisconnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateScreen), name: .updateLabel, object: nil)
        robotInfo = bluetooth.robotInfo
        bluetooth.sendData(payload: "f\n")
        updateRobotInfo()
    }

    ///
    ///function to setup openBot icon 
    func setupLogo() {
        openBotIcon.translatesAutoresizingMaskIntoConstraints = false;
        openBotIcon.heightAnchor.constraint(equalToConstant: 150).isActive = true;
        topAnchorConstraint = openBotIcon.topAnchor.constraint(equalTo: robotType.bottomAnchor, constant: 10)
        if currentOrientation == .portrait {
            centerXConstraint = openBotIcon.centerXAnchor.constraint(equalTo: view.centerXAnchor, constant: 0)

        } else {
            centerXConstraint = openBotIcon.centerXAnchor.constraint(equalTo: view.centerXAnchor, constant: -width / 2)
        }
        NSLayoutConstraint.activate([centerXConstraint, topAnchorConstraint]);
    }

    /// Called when the view controller's view's size is changed by its parent (i.e. for the root view controller when its window rotates or is resized).
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
        updateConstraints()
    }

    /// Notifies the view controller that its view is about to be added to a view hierarchy.
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        getRobotInfo()
        updateRobotInfo()
        if !isBluetoothConnected {
            updateScreenOnBluetoothDisconnection()
        }

    }


    /// function to create the UI for the robot info
    func createUI() {
        createRobotTypeHeading()
        robotName = createLabels(text: "N/A", topAnchor: topPadding + adapted(dimensionSize: 30, to: .height), leadingAnchor: width / 2)
        bluetoothIconButton = createBluetoothIcon()
        createRefreshIcon()
        createSensorHeading()
        createSensorCheckBoxes()
        createWheelOdometerHeading()
        createWheelOdometerCheckBoxes()
        createLedHeading()
        createLedCheckBoxes()
        createReadingHeading()
        createReadings()
        createSendCommandHeading()
        createSendCommand()
        createLight()
        showLight(isHidden: true)
    }

    /// creates heading label
    func createRobotTypeHeading() {
        robotType = createHeadings(text: "Robot Type")
        robotType.frame.origin = CGPoint(x: 10, y: topPadding + adapted(dimensionSize: 30, to: .height))
    }

    /// creates refresh button
    func createRefreshIcon() {
        if let image = UIImage(named: "refresh") {
            refreshIcon = createIcons(iconImage: image, leadingAnchor: width - 100, topAnchor: topPadding + 20)
        }

        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(refresh(tapGestureRecognizer:)))
        refreshIcon.isUserInteractionEnabled = true
        refreshIcon.addGestureRecognizer(tapGesture)
    }

    /// creates bluetooth icon
    func createBluetoothIcon() -> UIButton {
        let bleButton = UIButton()
        if isBluetoothConnected {
            bleButton.setImage(Images.bluetoothConnected_v2!, for: .normal)
        } else {
            bleButton.setImage(Images.bluetoothDisconnected_v2!, for: .normal)
        }
        bleButton.frame = CGRect(x: width - 60, y: topPadding + adapted(dimensionSize: 40, to: .height), width: 45, height: 45)
        view.addSubview(bleButton)
        bleButton.addTarget(self, action: #selector(openBluetoothSettings(_:)), for: .touchDown)
        return bleButton
    }

    /// creates sensors heading
    func createSensorHeading() {
        sensorHeading = createHeadings(text: "Sensors")
    }

    /// creates checkboxes for sensors
    func createSensorCheckBoxes() {
        voltageDividerCheckBox = createCheckbox(leadingAnchor: 20, topAnchor: 300, action: #selector(updateSonarCheckBox(_:)))
        voltageDividerLabel = createLabels(text: Strings.voltageDivider, topAnchor: 292, leadingAnchor: 55)
        sonarCheckBox = createCheckbox(leadingAnchor: 170, topAnchor: 300, action: #selector(updateSonarCheckBox(_:)))
        sonarLabel = createLabels(text: Strings.sonarText, topAnchor: 292, leadingAnchor: 205)
        bumperCheckBox = createCheckbox(leadingAnchor: 275, topAnchor: 300, action: #selector(updateBumperCheckBox(_:)))
        bumperLabel = createLabels(text: Strings.bumperText, topAnchor: 292, leadingAnchor: 310)
    }

    /// creates heading for wheel odometer
    func createWheelOdometerHeading() {
        wheelOdometerHeadingHeading = createHeadings(text: Strings.wheelOdometer)
    }

    /// creates checkboxes for odometer
    func createWheelOdometerCheckBoxes() {
        frontCheckBox = createCheckbox(leadingAnchor: 20, topAnchor: 375, action: #selector(updateSonarCheckBox(_:)))
        frontCheckBoxLabel = createLabels(text: Strings.front, topAnchor: 365, leadingAnchor: 65)
        backCheckBox = createCheckbox(leadingAnchor: 125, topAnchor: 375, action: #selector(updateSonarCheckBox(_:)))
        backLabel = createLabels(text: Strings.back, topAnchor: 365, leadingAnchor: 165)
    }

    /// creates heading for LED
    func createLedHeading() {
        ledHeading = createHeadings(text: Strings.led)
    }

    /// creates checkboxes for LEDs
    func createLedCheckBoxes() {
        indicatorCheckbox = createCheckbox(leadingAnchor: 20, topAnchor: 470, action: #selector(updateSonarCheckBox(_:)))
        indicatorLabel = createLabels(text: Strings.indicatorText, topAnchor: 460, leadingAnchor: 55)
        frontLedCheckbox = createCheckbox(leadingAnchor: 135, topAnchor: 470, action: #selector(updateSonarCheckBox(_:)))
        frontLedLabel = createLabels(text: Strings.front, topAnchor: 460, leadingAnchor: 165)
        backLedCheckbox = createCheckbox(leadingAnchor: 215, topAnchor: 470, action: #selector(updateSonarCheckBox(_:)))
        backLedLabel = createLabels(text: Strings.back, topAnchor: 460, leadingAnchor: 245)
        statusCheckbox = createCheckbox(leadingAnchor: 290, topAnchor: 470, action: #selector(updateSonarCheckBox(_:)))
        statusLabel = createLabels(text: Strings.status, topAnchor: 460, leadingAnchor: 320)
    }

    /// creates heading for readings
    func createReadingHeading() {
        readingHeading = createHeadings(text: Strings.readings)
    }

    /// creates labels for readings
    func createReadings() {
        battery = createLabels(text: Strings.battery, topAnchor: 530, leadingAnchor: 20)
        speed = createLabels(text: Strings.speedText, topAnchor: 530, leadingAnchor: 110)
        speed.font = speed.font.withSize(12)
        sonar = createLabels(text: Strings.sonarLabel, topAnchor: 530, leadingAnchor: 255)
    }

    /// creates commands heading
    func createSendCommandHeading() {
        sendCommandsHeading = createHeadings(text: Strings.sendCommand)
    }

    /// creates send command buttons.
    func createSendCommand() {
        motorLabel = createLabels(text: Strings.motors, topAnchor: 610, leadingAnchor: 20)
        forwardButton = createButton(width: 80, height: 40, title: Strings.forward, leadingAnchor: 70, topAnchor: 610, action: #selector(forwardButton(_:)))
        backwardButton = createButton(width: 90, height: 40, title: Strings.backward, leadingAnchor: 180, topAnchor: 610, action: #selector(backwardButton(_:)))
        stopButton = createButton(width: 60, height: 40, title: Strings.stop, leadingAnchor: 290, topAnchor: 610, action: #selector(stopButton(_:)))
    }

/// creates heading and slider for lights
    func createLight() {
        lightLabel = createLabels(text: Strings.lightLabel, topAnchor: 610, leadingAnchor: 10)
        lightSlider = createSlider()
    }

    /// creates icons with following
    ///
    /// - Parameters:
    ///   - iconImage: image of the icon
    ///   - leadingAnchor: space from left.
    ///   - topAnchor: space from top.
    /// - Returns: returns image view
    func createIcons(iconImage: UIImage, leadingAnchor: CGFloat, topAnchor: CGFloat) -> UIImageView {
        let icon = UIImageView()
        icon.image = iconImage
        icon.frame.size = CGSize(width: 30, height: 30)
        view.addSubview(icon)
        icon.frame.origin.x = leadingAnchor
        icon.frame.origin.y = topAnchor
        return icon
    }

    /// function to create the headings with text.
    ///
    /// - Parameter text: heading
    /// - Returns: UILabel
    func createHeadings(text: String) -> UILabel {
        let label = UILabel()
        label.text = text
        label.textColor = Colors.border
        label.frame.size.width = CGFloat(text.count * 15)
        label.frame.size.height = 40
        view.addSubview(label)
        label.font = HelveticaNeue.bold(size: 12)
        return label
    }

    /// function to create the labels UI
    func createLabels(text: String, topAnchor: CGFloat, leadingAnchor: CGFloat) -> UILabel {
        let label = UILabel()
        label.text = text
        label.textColor = Colors.border
        label.frame.size.width = CGFloat(text.count * 10)
        label.font = label.font.withSize(12)
        label.frame.size.height = 40
        view.addSubview(label)
        label.frame.origin.x = leadingAnchor
        label.frame.origin.y = topAnchor
        return label
    }

///function to create the checkbox UI
    func createCheckbox(leadingAnchor: CGFloat, topAnchor: CGFloat, action: Selector?) -> Checkbox {
        let checkbox = Checkbox(frame: CGRect(x: leadingAnchor, y: topAnchor, width: 25, height: 25))
        checkbox.checkedBorderColor = traitCollection.userInterfaceStyle == .dark ? .white : .black;
        checkbox.uncheckedBorderColor = traitCollection.userInterfaceStyle == .dark ? .white : .black;
        checkbox.checkmarkColor = traitCollection.userInterfaceStyle == .dark ? .white : .black;
        checkbox.checkedBorderColor = traitCollection.userInterfaceStyle == .dark ? .white : .black;
        checkbox.checkmarkStyle = .tick
        checkbox.isEnabled = false
        view.addSubview(checkbox)
        checkbox.addTarget(self, action: action!, for: .valueChanged)
        return checkbox
    }

    ///function to create the buttons UI
    func createButton(width: Double, height: Double, title: String, leadingAnchor: CGFloat, topAnchor: CGFloat, action: Selector) -> UIButton {
        let button = UIButton()
        button.frame.size = CGSize(width: width, height: height)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 15)
        button.setTitle(title, for: .normal)
        button.setTitleColor(UIColor.init(red: 1, green: 1, blue: 1, alpha: 1), for: .normal)
        button.frame.origin = CGPoint(x: leadingAnchor, y: topAnchor)
        button.backgroundColor = Colors.title
        button.layer.cornerRadius = 5
        button.addTarget(nil, action: action, for: .touchDown)
        view.addSubview(button)
        return button
    }

    /// function to create the slider UI
    func createSlider() -> UISlider {
        let slider = UISlider()
        slider.frame = CGRect(x: 100, y: 610, width: 200, height: 50)
        view.addSubview(slider)
        slider.thumbTintColor = Colors.title
        slider.minimumTrackTintColor = Colors.title
        slider.addTarget(self, action: #selector(lightValueChange(_:)), for: .valueChanged)
        return slider
    }

    /// function to create the constrains of the buttons,labels,headings and images.
    func updateConstraints() {
        if currentOrientation == .portrait {

            robotType.frame.origin.y = topPadding + adapted(dimensionSize: 25, to: .height)
            robotName.frame.origin.y = topPadding + adapted(dimensionSize: 25, to: .height)
            robotName.frame.origin.x = robotType.frame.origin.x + CGFloat((robotType.text?.count ?? 0) * 10) + adapted(dimensionSize: 35, to: .height)
            bluetoothIconButton.frame.origin = CGPoint(x: width - 40, y: topPadding + adapted(dimensionSize: 20, to: .height))
            refreshIcon.frame.origin.y = topPadding + adapted(dimensionSize: 27, to: .height)
            refreshIcon.frame.origin.x = bluetoothIconButton.frame.origin.x - 40
            sensorHeading.frame.origin = CGPoint(x: 10, y: 250)
            voltageDividerCheckBox.frame.origin = CGPoint(x: 20, y: sensorHeading.frame.origin.y + adapted(dimensionSize: 35, to: .height))
            voltageDividerLabel.frame.origin = CGPoint(x: 55, y: sensorHeading.frame.origin.y + adapted(dimensionSize: 28, to: .height))
            sonarCheckBox.frame.origin = CGPoint(x: 170, y: sensorHeading.frame.origin.y + adapted(dimensionSize: 35, to: .height))
            sonarLabel.frame.origin = CGPoint(x: 205, y: sensorHeading.frame.origin.y + adapted(dimensionSize: 28, to: .height))
            bumperCheckBox.frame.origin = CGPoint(x: 265, y: sensorHeading.frame.origin.y + adapted(dimensionSize: 35, to: .height))
            bumperLabel.frame.origin = CGPoint(x: 300, y: sensorHeading.frame.origin.y + adapted(dimensionSize: 28, to: .height))
            wheelOdometerHeadingHeading.frame.origin = CGPoint(x: 10, y: voltageDividerLabel.frame.origin.y + adapted(dimensionSize: 40, to: .height))
            frontCheckBox.frame.origin = CGPoint(x: 20, y: wheelOdometerHeadingHeading.frame.origin.y + adapted(dimensionSize: 35, to: .height))
            frontCheckBoxLabel.frame.origin = CGPoint(x: 65, y: wheelOdometerHeadingHeading.frame.origin.y + adapted(dimensionSize: 28, to: .height))
            backCheckBox.frame.origin = CGPoint(x: 125, y: wheelOdometerHeadingHeading.frame.origin.y + adapted(dimensionSize: 35, to: .height))
            backLabel.frame.origin = CGPoint(x: 165, y: wheelOdometerHeadingHeading.frame.origin.y + adapted(dimensionSize: 28, to: .height))
            ledHeading.frame.origin = CGPoint(x: 10, y: wheelOdometerHeadingHeading.frame.origin.y + adapted(dimensionSize: 55, to: .height))
            indicatorCheckbox.frame.origin = CGPoint(x: 20, y: ledHeading.frame.origin.y + adapted(dimensionSize: 35, to: .height))
            indicatorLabel.frame.origin = CGPoint(x: 55, y: ledHeading.frame.origin.y + adapted(dimensionSize: 28, to: .height))
            frontLedCheckbox.frame.origin = CGPoint(x: 135, y: ledHeading.frame.origin.y + adapted(dimensionSize: 35, to: .height))
            frontLedLabel.frame.origin = CGPoint(x: 165, y: ledHeading.frame.origin.y + adapted(dimensionSize: 28, to: .height))
            backLedCheckbox.frame.origin = CGPoint(x: 215, y: ledHeading.frame.origin.y + adapted(dimensionSize: 35, to: .height))
            backLedLabel.frame.origin = CGPoint(x: 245, y: ledHeading.frame.origin.y + adapted(dimensionSize: 28, to: .height))
            statusCheckbox.frame.origin = CGPoint(x: 290, y: ledHeading.frame.origin.y + adapted(dimensionSize: 35, to: .height))
            statusLabel.frame.origin = CGPoint(x: 320, y: ledHeading.frame.origin.y + adapted(dimensionSize: 28, to: .height))
            readingHeading.frame.origin = CGPoint(x: 10, y: ledHeading.frame.origin.y + adapted(dimensionSize: 55, to: .height))
            battery.frame.origin = CGPoint(x: 20, y: readingHeading.frame.origin.y + adapted(dimensionSize: 25, to: .height))
            speed.frame.origin = CGPoint(x: 120, y: readingHeading.frame.origin.y + adapted(dimensionSize: 25, to: .height))
            sonar.frame.origin = CGPoint(x: 275, y: readingHeading.frame.origin.y + adapted(dimensionSize: 25, to: .height))
            sendCommandsHeading.frame.origin = CGPoint(x: 10, y: readingHeading.frame.origin.y + adapted(dimensionSize: 55, to: .height))
            motorLabel.frame.origin = CGPoint(x: 20, y: sendCommandsHeading.frame.origin.y + adapted(dimensionSize: 35, to: .height))
            motorLabel.isHidden = false
            forwardButton.frame.origin = CGPoint(x: 70, y: sendCommandsHeading.frame.origin.y + adapted(dimensionSize: 35, to: .height))
            backwardButton.frame.origin = CGPoint(x: 160, y: sendCommandsHeading.frame.origin.y + adapted(dimensionSize: 35, to: .height))
            stopButton.frame.origin = CGPoint(x: 260, y: sendCommandsHeading.frame.origin.y + adapted(dimensionSize: 35, to: .height))
            lightLabel.frame.origin = CGPoint(x: 20, y: motorLabel.frame.origin.y + 50)
            lightSlider.frame = CGRect(x: 70, y: (motorLabel.frame.origin.y + adapted(dimensionSize: 45, to: .height)), width: width - 90, height: 40)
            centerXConstraint.constant = 0;
        } else {
            robotName.frame.origin.y = 20
            robotType.frame.origin = CGPoint(x: 10, y: 20)
            robotName.frame.origin.x = robotType.frame.origin.x + CGFloat((robotType.text?.count ?? 0) * 10) + adapted(dimensionSize: 20, to: .height)
            bluetoothIconButton.frame.origin = CGPoint(x: width - 100, y: 20)
            refreshIcon.frame.origin = CGPoint(x: width - 140, y: 28)
            refreshIcon.frame.origin.x = bluetoothIconButton.frame.origin.x - 40
            sensorHeading.frame.origin = CGPoint(x: height / 2, y: 15)
            voltageDividerCheckBox.frame.origin = CGPoint(x: height / 2 + 5, y: 50)
            voltageDividerLabel.frame.origin = CGPoint(x: height / 2 + 40, y: 40)
            sonarCheckBox.frame.origin = CGPoint(x: height / 2 + 145, y: 50)
            sonarLabel.frame.origin = CGPoint(x: height / 2 + 180, y: 40)
            bumperCheckBox.frame.origin = CGPoint(x: height / 2 + 240, y: 50)
            bumperLabel.frame.origin = CGPoint(x: height / 2 + 270, y: 40)
            wheelOdometerHeadingHeading.frame.origin = CGPoint(x: height / 2, y: 80)
            frontCheckBox.frame.origin = CGPoint(x: height / 2 + 5, y: 120)
            frontCheckBoxLabel.frame.origin = CGPoint(x: height / 2 + 40, y: 110)
            backCheckBox.frame.origin = CGPoint(x: height / 2 + 100, y: 120)
            backLabel.frame.origin = CGPoint(x: height / 2 + 135, y: 110)
            ledHeading.frame.origin = CGPoint(x: height / 2, y: 155)
            indicatorCheckbox.frame.origin = CGPoint(x: height / 2 + 5, y: 195)
            indicatorLabel.frame.origin = CGPoint(x: height / 2 + 40, y: 185)
            frontLedCheckbox.frame.origin = CGPoint(x: height / 2 + 110, y: 195)
            frontLedLabel.frame.origin = CGPoint(x: height / 2 + 140, y: 185)
            backLedCheckbox.frame.origin = CGPoint(x: height / 2 + 185, y: 195)
            backLedLabel.frame.origin = CGPoint(x: height / 2 + 215, y: 185)
            statusCheckbox.frame.origin = CGPoint(x: height / 2 + 260, y: 195)
            statusLabel.frame.origin = CGPoint(x: height / 2 + 290, y: 185)
            readingHeading.frame.origin = CGPoint(x: height / 2, y: 235)
            battery.frame.origin = CGPoint(x: height / 2 + 5, y: 260)
            speed.frame.origin = CGPoint(x: height / 2 + 100, y: 260)
            sonar.frame.origin = CGPoint(x: height / 2 + 250, y: 260)
            sendCommandsHeading.frame.origin = CGPoint(x: 10, y: readingHeading.frame.origin.y)
            motorLabel.frame.origin = CGPoint(x: 10, y: sendCommandsHeading.frame.origin.y + adapted(dimensionSize: 30, to: .height))
            motorLabel.isHidden = false
            forwardButton.frame.origin = CGPoint(x: 60, y: motorLabel.frame.origin.y)
            backwardButton.frame.origin = CGPoint(x: 150, y: motorLabel.frame.origin.y)
            stopButton.frame.origin = CGPoint(x: 250, y: motorLabel.frame.origin.y)
            lightLabel.frame.origin = CGPoint(x: 10, y: motorLabel.frame.origin.y + 50)
            lightSlider.frame = CGRect(x: 60, y: (motorLabel.frame.origin.y + adapted(dimensionSize: 45, to: .height)), width: width - 100, height: 40)
            centerXConstraint.constant = -width / 2;
        }
    }

    /// function to get the info from the robot
    func getRobotInfo() {
        if isBluetoothConnected {
            //            bluetoothIcon.image = UIImage(named: "bluetoothConnected")
            bluetooth.sendData(payload: "f\n")
            robotInfo = bluetooth.robotInfo
        } else {
            //            bluetoothIcon.image = UIImage(named: "bluetoothDisconnected")
        }
    }

    /// function to update the robot status
    func updateRobotInfo() {
        getRobotInfo()
        robotInfo = bluetooth.robotInfo
        if robotInfo != "" {
            setRobotName()
            setVoltageCheck()
            setSonarCheck()
            setBumperCheck()
            setFrontWheelOdometryCheck()
            setBackWheelOdometryCheck()
            setFrontLedCheck()
            setBackLedCheck()
            setIndicatorCheck()
            setFrontLedCheck()
            setBackLedCheck()
            setStatusLedCheck()
            showLight(isHidden: false)
            updateLogo(robotName: getRobotName())
        }
    }

    /// function to update the image of the robot on basis of the robot name
    func updateLogo(robotName: String) {
        var asset: NSDataAsset? = nil

        switch robotName {
        case "N/A":
            openBotIcon.image = UIImage(named: "openBotLogo")
            break
        case "DIY":
            asset = NSDataAsset(name: "diy")
            break
        case "MTV":
            asset = NSDataAsset(name: "mtv")
            break
        case "RC_CAR":
            asset = NSDataAsset(name: "rc_car")
            break
        case "RTR_520":
            asset = NSDataAsset(name: "rtr_520")
            break
        case "RTR_TT":
            asset = NSDataAsset(name: "rtr_tt")
            break
        case "RTR_TT2":
            asset = NSDataAsset(name: "rtr_tt")
            break
        default:
            openBotIcon.image = UIImage(named: "openBotLogo")
            return
        }
        let data = asset?.data
        if let data {
            openBotIcon.image = UIImage.gifImageWithData(data)
        }
    }

    /// function to get the robot name from robot info.
    func getRobotName() -> String {
        let firstIndex = robotInfo.index(after: robotInfo.firstIndex(of: "f")!)
        let lastIndex = robotInfo.firstIndex(of: ":")!
        return String(robotInfo[firstIndex..<lastIndex])
    }

    /// function to update the robot name.
    func setRobotName() {
        robotName.text = getRobotName()
        robotName.frame.size.width = CGFloat(getRobotName().count * 10)
    }

    /// function to check whether voltage is available on the robot.
    func setVoltageCheck() {
        if robotInfo.contains(":v") {
            voltageDividerCheckBox.isChecked = true
        }
    }

    /// function to check whether sonar is available on the robot.
    func setSonarCheck() {
        if robotInfo.contains(":s") {
            sonarCheckBox.isChecked = true
        }
    }

    /// function to check whether bumper is available on the robot.
    func setBumperCheck() {
        if robotInfo.contains(":b") {
            bumperCheckBox.isChecked = true
        }
    }

    /// function to check whether front wheel odometer is available on the robot.
    func setFrontWheelOdometryCheck() {
        if robotInfo.contains(":wf") {
            frontCheckBox.isChecked = true
        }
    }

    /// function to check whether back wheel odometer is available on the robot.
    func setBackWheelOdometryCheck() {
        if robotInfo.contains(":wb") {
            backCheckBox.isChecked = true
        }
    }

    /// function to check whether indicator is available on the robot.
    func setIndicatorCheck() {
        if robotInfo.contains("i:") {
            indicatorCheckbox.isChecked = true
        }
    }

    /// function to check whether front leds is available on the robot.
    func setFrontLedCheck() {
        if robotInfo.contains("lf:") {
            frontLedCheckbox.isChecked = true
        }
    }

    /// function to check whether back leds is available on the robot.
    func setBackLedCheck() {
        if robotInfo.contains("lb:") {
            backLedCheckbox.isChecked = true
        }
    }

    /// function to check whether led status is available on the robot.
    func setStatusLedCheck() {
        if robotInfo.contains("ls:") {
            statusCheckbox.isChecked = true
        }
    }

    /// function to check whether should show lights
    func showLight(isHidden: Bool) {
        lightLabel.isHidden = isHidden
        lightSlider.isHidden = isHidden
    }

    @objc func updateSonarCheckBox(_ sender: UIButton) {
    }

    @objc func updateBumperCheckBox(_ sender: UIButton) {
    }

    /// function to update the status after connecting with the robot.
    @objc func updateConnect(_ notification: Notification) {
        if (isBluetoothConnected) {
            bluetoothIconButton.setImage(Images.bluetoothConnected_v2, for: .normal)
            updateRobotInfo()
        } else {
            bluetoothIconButton.setImage(Images.bluetoothDisconnected_v2, for: .normal)
            updateScreenOnBluetoothDisconnection()
        }
    }

    /// moving robot in forward direction after pressing forward button,
    @objc func forwardButton(_ sender: UIButton) {
        bluetooth.sendData(payload: "c" + String(192) + "," + String(192) + "\n")
    }

    /// moving robot in backward direction after pressing forward button,
    @objc func backwardButton(_ sender: UIButton) {
        bluetooth.sendData(payload: "c" + String(-192) + "," + String(-192) + "\n")
    }

    /// stopping robot movement
    @objc func stopButton(_ sender: UIButton) {
        bluetooth.sendData(payload: "c" + String(0) + "," + String(0) + "\n")
    }

    /// updating the screen.
    @objc func updateScreen() {
        updateSonar()
        updateVoltage()
        updateSpeedometer()
        if robotInfo == "" {
            getRobotInfo()
            updateRobotInfo()
        }

    }

    /// updating sonar values from ble data
    func updateSonar() {
        let sonarData = bluetooth.sonarData
        if sonarData != "" {
            let index = sonarData.index(after: sonarData.startIndex)
            let actualSonarValue = min(Int(String(sonarData[index...])) ?? 0, 300)
            sonar.text = "Sonar " + String(actualSonarValue) + " cm"
        }
    }

    /// updating voltage values from ble data
    func updateVoltage() {
        let voltageData = bluetooth.voltageDivider
        if voltageData != "" {
            let index = voltageData.index(after: voltageData.startIndex)
            battery.text = "Battery " + String(voltageData[index...]) + " V"
        }
    }

    /// update speed label from shared BLE RPM formatter
    func updateSpeedometer() {
        speed.text = bluetooth.getFormattedRpmRobotInfo()
    }

    /// updating screen after disconnection
    func updateScreenOnBluetoothDisconnection() {
        robotName.text = "N/A"
        sonarCheckBox.isChecked = false
        bumperCheckBox.isChecked = false
        frontCheckBox.isChecked = false
        backCheckBox.isChecked = false
        indicatorCheckbox.isChecked = false
        frontLedCheckbox.isChecked = false
        backLedCheckbox.isChecked = false
        statusCheckbox.isChecked = false
        voltageDividerCheckBox.isChecked = false
        showLight(isHidden: true)
        updateLogo(robotName: "N/A")
    }

    /// open ble settings
    @objc func openBluetoothSettings(_ sender: UIButton) {
        let nextViewController = (storyboard?.instantiateViewController(withIdentifier: Strings.bluetoothScreen))
        navigationController?.pushViewController(nextViewController!, animated: true)
    }

    /// refresh button redirect calls.
    @objc func refresh(tapGestureRecognizer: UITapGestureRecognizer) {
        if isBluetoothConnected {
            bluetooth.sendData(payload: "f\n")
            updateRobotInfo()
        } else {
            updateScreenOnBluetoothDisconnection()
        }
    }

    /// updating light values on the robot
    @objc func lightValueChange(_ sender: UISlider) {
        let front = round((sender.value * 255) * 100) / 100
        let back = round((sender.value * 255) * 100) / 100
        bluetooth.sendData(payload: "l" + String(front) + "," + String(back) + "\n")
    }

    /// creating back button
    func setupNavigationBarItem() {
        if UIImage(named: "back") != nil {
            let backNavigationIcon = (UIImage(named: "back")?.withRenderingMode(.alwaysOriginal))!
            let newBackButton = UIBarButtonItem(image: backNavigationIcon, title: Strings.robotInfo, target: self, action: #selector(RobotInfoFrame.back(sender:)), titleColor: Colors.navigationColor ?? .white)
            navigationItem.leftBarButtonItem = newBackButton
        }
    }

    /// on tap back button.
    @objc func back(sender: UIBarButtonItem) {
        _ = navigationController?.popViewController(animated: true)
//        MyNavigationController.shared.popViewController(animated: false);
//        let homepageController = (storyboard?.instantiateViewController(withIdentifier: "homePage"))!
//        let profileController = (storyboard?.instantiateViewController(withIdentifier: "profileController"))!
//        let projectController = (storyboard?.instantiateViewController(withIdentifier: "projectsController"))!
//        tabBarController?.viewControllers = [homepageController,profileController,projectController]
    }
}

extension String {
    func index(from: Int) -> Index {
        self.index(startIndex, offsetBy: from)
    }

    func substring(from: Int) -> String {
        let fromIndex = index(from: from)
        return String(self[fromIndex...])
    }

    func substring(to: Int) -> String {
        let toIndex = index(from: to)
        return String(self[..<toIndex])
    }

    func substring(with r: Range<Int>) -> String {
        let startIndex = index(from: r.lowerBound)
        let endIndex = index(from: r.upperBound)
        return String(self[startIndex..<endIndex])
    }
}
