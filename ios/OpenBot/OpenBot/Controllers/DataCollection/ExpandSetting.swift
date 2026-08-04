//
// Created by Nitish Yadav on 16/09/22.
//

import Foundation
import UIKit
import DropDown

/// Implementation of all the settings for Data Collection
class expandSetting: UIView, UITextFieldDelegate, UIScrollViewDelegate {
    let logData = UISwitch()
    let bluetoothIcon = UIImageView()
    let delayTextField = UITextField()
    let cameraIcon = UIImageView()
    var previewResolution = UILabel()
    var low = UIButton()
    var medium = UIButton()
    var high = UIButton()
    var modelResolution = UILabel()
    var server = UIView();
    var serverDropDownLabel = UILabel()
    var serverDropDown = DropDown()
    var serverDropDownView = UIView()
    var secondView = UIScrollView()
    var leadingConstraint: NSLayoutConstraint!
    var topConstraint: NSLayoutConstraint!
    var widthConstraint: NSLayoutConstraint!
    var heightConstraint: NSLayoutConstraint!
    var preview = UIButton()
    var training = UIButton()
    var vehicle = UIButton()
    var gps = UIButton()
    var acceleration = UIButton()
    var magnetic = UIButton()
    var gyroscope = UIButton()
    var selectedResolution: Resolutions = Resolutions.MEDIUM
    var sensorButtons = [UIButton]()
    var dropDownView = UIView()
    var ddView = UIView()
    var serverDdView = UIView();
    let dropDown = DropDown()
    var dropdownLabel = UILabel()
    var dropdownTopAnchor: NSLayoutConstraint!
    var modelsName = [String]()
    var resolution = [String]()
    var models: [Model] = [];
    var leftSpeedLabel = UILabel()
    var samplingPeriod: Double = 0.2
    let serverUpwardImage = UIImageView()
    let saveAsUpwardImage = UIImageView();
    let saveAsDropdown = DropDown();
    let saveAsDropdownLabel = UILabel()
    var saveAsDropdownView = UIView();
    var preferencesManager : SharedPreferencesManager = SharedPreferencesManager()

    override init(frame: CGRect) {
        super.init(frame: frame)
        DeviceCurrentOrientation.shared.findDeviceOrientation()
        loadModelsNameAndResolution()
        applyBlurEffect()
        createLogDataButton()
        createBluetoothIcon()
        createCameraIcon()
        resolutionTitle()
        createResolutions()
        createModelResolutionTitle()
        createDropdown()
        createLabels(value: Strings.server, leadingAnchor: 10, topAnchor: 200, labelWidth: 240, labelHeight: 40).font = UIFont.boldSystemFont(ofSize: 16.0)
        createServer()
        createServerDropDown();
        serverDropDown.hide();
        saveAsDropdown.hide();
        createSecondView()
        createSecondViewLabel(value: "Save As", leadingAnchor: 10, topAnchor: 0, labelWidth: 100, labelHeight: 40).font = UIFont.boldSystemFont(ofSize: 16.0)
        createSaveAsDropDown();
        createSecondViewLabel(value: "Images", leadingAnchor: 10, topAnchor: 80, labelWidth: 100, labelHeight: 40).font = UIFont.boldSystemFont(ofSize: 16.0)
        createImagesButton()
        createSecondViewLabel(value: Strings.sensorData, leadingAnchor: 10, topAnchor: 130, labelWidth: 200, labelHeight: 40).font = UIFont.boldSystemFont(ofSize: 16.0)
        createSensorButtons()
        _ = createSecondViewLabel(value: Strings.delay, leadingAnchor: 230, topAnchor: 230, labelWidth: 60, labelHeight: 40)
        createDelayField()
        let vehicleControls = VehicleControl();
        createLeftSpeed()
        addSubview(vehicleControls)
        vehicleControls.translatesAutoresizingMaskIntoConstraints = false
        vehicleControls.topAnchor.constraint(equalTo: magnetic.safeAreaLayoutGuide.bottomAnchor, constant: adapted(dimensionSize: 13, to: .height)).isActive = true;
        vehicleControls.leadingAnchor.constraint(equalTo: secondView.safeAreaLayoutGuide.leadingAnchor, constant: 0).isActive = true
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothConnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothDisconnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide), name: UIResponder.keyboardWillHideNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateSpeedLabel), name: .updateSpeedLabel, object: nil);
        NotificationCenter.default.addObserver(self, selector: #selector(updateServer), name: .server, object: nil);
        let tap = UITapGestureRecognizer(target: self, action: #selector(dismissKeyboard(_:)))
        addGestureRecognizer(tap)
    }


    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }


    /// UI Function to create button for logs
    func createLogDataButton() {
        let label = createLabels(value: Strings.logData, leadingAnchor: 10, topAnchor: 13, labelWidth: 100, labelHeight: 40)
        logData.isOn = false
        logData.setOn(false, animated: true)
        logData.onTintColor = Colors.title
        logData.addTarget(self, action: #selector(switchLogButton(_:)), for: .valueChanged)
        logData.translatesAutoresizingMaskIntoConstraints = false
        addSubview(logData)
        logData.leadingAnchor.constraint(greaterThanOrEqualTo: label.trailingAnchor, constant: 12).isActive = true
        let preferredPosition = logData.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: 90)
        preferredPosition.priority = .defaultLow
        preferredPosition.isActive = true
        logData.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: 10).isActive = true
    }

    /// UI Function to create button for ble
    func createBluetoothIcon() {
        bluetoothIcon.frame.size = CGSize(width: 30, height: 30)
        bluetoothIcon.translatesAutoresizingMaskIntoConstraints = false
        if isBluetoothConnected {
            bluetoothIcon.image = Images.bluetoothConnected_v2
        } else {
            bluetoothIcon.image = Images.bluetoothDisconnected_v2
        }
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(ble(_:)))
        bluetoothIcon.isUserInteractionEnabled = true
        bluetoothIcon.addGestureRecognizer(tapGesture)
        addSubview(bluetoothIcon)
        bluetoothIcon.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: 10).isActive = true
        bluetoothIcon.leadingAnchor.constraint(equalTo: logData.trailingAnchor, constant: 20).isActive = true
    }

    /// UI Function to create button for camera
    func createCameraIcon() {
        cameraIcon.frame.size = CGSize(width: 30, height: 30)
        cameraIcon.translatesAutoresizingMaskIntoConstraints = false
        cameraIcon.image = Images.frontCamera
        cameraIcon.isUserInteractionEnabled = true
        let tap = UITapGestureRecognizer(target: self, action: #selector(reverseCamera(_:)))
        cameraIcon.addGestureRecognizer(tap)
        addSubview(cameraIcon)
        cameraIcon.leadingAnchor.constraint(equalTo: bluetoothIcon.trailingAnchor, constant: 20).isActive = true
        cameraIcon.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: 10).isActive = true
    }

    /// UI Function to create headings
    func resolutionTitle() {
        previewResolution = createLabels(value: Strings.previewResolutionMedium, leadingAnchor: 10, topAnchor: 50, labelWidth: 240, labelHeight: 30)
        previewResolution.font = UIFont.boldSystemFont(ofSize: 16.0)
    }

    /// UI Function to create button for resolutions
    func createResolutions() {
        low = createButton(borderColor: "red", buttonName: Strings.low, leadingAnchor: 10, topAnchor: 80, action: #selector(applyLowResolution(_:)))
        medium = createButton(borderColor: "red", buttonName: Strings.medium, leadingAnchor: 100, topAnchor: 80, action: #selector(applyMediumResolution(_:)))
        high = createButton(borderColor: "red", buttonName: Strings.high, leadingAnchor: 190, topAnchor: 80, action: #selector(applyHighResolution(_:)))
        updateResolution()
    }

    /// UI Function to create label for model resolution
    func createModelResolutionTitle() {
        modelResolution = createLabels(value: Strings.modelResolution + "256x96", leadingAnchor: 10, topAnchor: 130, labelWidth: 240, labelHeight: 30)
        modelResolution.font = UIFont.boldSystemFont(ofSize: 16.0);
    }

    /// UI Function to create dropdowns
    func createDropdown() {
        modelsName = Common.loadAllModelsName()
        dropDown.backgroundColor = Colors.freeRoamButtonsColor
        dropDown.textColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black;
        dropDown.anchorView = dropDownView
        dropDown.dataSource = modelsName
        ddView = createDropdownView(borderColor: "", buttonName: "CLI-Mobile", leadingAnchor: 10, topAnchor: 155, action: #selector(showDropdown(_:)))
        ddView.backgroundColor = Colors.title
        let upwardImage = UIImageView(image: Images.upwardArrow);
        ddView.addSubview(upwardImage);
        upwardImage.frame.size = CGSize(width: 20, height: 20);
        upwardImage.translatesAutoresizingMaskIntoConstraints = false;
        upwardImage.trailingAnchor.constraint(equalTo: ddView.trailingAnchor, constant: -10).isActive = true;
        upwardImage.topAnchor.constraint(equalTo: ddView.safeAreaLayoutGuide.topAnchor, constant: 15).isActive = true;
        dropDown.selectionAction = { [unowned self] (index: Int, item: String) in
            dropdownLabel.text = item
            modelResolution.text = Strings.modelResolution + resolution[index]
            NotificationCenter.default.post(name: .updateModelResolution, object: resolution[index])
        }
        dropDown.width = 210
        dropDown.cornerRadius = 5
        dropDownView.frame.size = CGSize(width: 200, height: 100);
        dropDownView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(dropDownView)
        dropdownTopAnchor = dropDownView.topAnchor.constraint(equalTo: ddView.safeAreaLayoutGuide.bottomAnchor, constant: 0)
        dropdownTopAnchor.isActive = true
        dropDownView.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: 10).isActive = true;
    }

    func createServerDropDown() {
        serverDropDown.backgroundColor = Colors.freeRoamButtonsColor
        serverDropDown.textColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black;
        serverDropDown.anchorView = serverDropDownView
        serverDropDown.width = 210
        serverDropDownView.frame.size = CGSize(width: 210, height: 100);
        addSubview(serverDropDownView);
        serverDropDownView.translatesAutoresizingMaskIntoConstraints = false
        print("nsdService.servers =======", servers);
        serverDropDown.dataSource = servers;
        serverDropDown.show()
        print("serverItems ========== \(serverItems)");
        serverDropDownView.leadingAnchor.constraint(equalTo: server.leadingAnchor, constant: 0).isActive = true;
        serverDropDownView.topAnchor.constraint(equalTo: server.bottomAnchor, constant: 10).isActive = true;
        serverDropDown.selectionAction = { [unowned self] (index: Int, item: String) in
            serverDropDownLabel.text = item;
            let server = serverItems.filter { server in
                server.name == item
            }
            if item == "No Server" {
                ServerCommunication.serverEndPoint = nil;
            }
            if let endpoint = server.first?.endpoint {
                serverConnection = ServerCommunication(endpoint: endpoint)
            };
        }
        serverDropDown.cancelAction = { [unowned self] in
            UIView.animate(withDuration: 0) {
                // Rotate back to the original position
                self.serverUpwardImage.transform = CGAffineTransform.identity
                // Toggle the rotation state
            }
        }

    }

    /// UI Function to create server label
    func createServer() {
        addSubview(server);
        server.backgroundColor = Colors.title;
        server.layer.cornerRadius = 10;
        server.translatesAutoresizingMaskIntoConstraints = false;
        server.widthAnchor.constraint(equalToConstant: 210).isActive = true;
        server.heightAnchor.constraint(equalToConstant: 40).isActive = true;
        server.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: 10).isActive = true;
        server.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: 230).isActive = true;
        serverDropDownLabel.text = Strings.noServer
        serverDropDownLabel.textColor = Colors.border
        serverDropDownLabel.frame = CGRect(x: 10, y: 0, width: 210, height: 40)
        server.addSubview(serverDropDownLabel);
        server.isUserInteractionEnabled = true;
        let tap = UITapGestureRecognizer(target: self, action: #selector(serverHandler))
        server.addGestureRecognizer(tap)
        serverUpwardImage.frame.size = CGSize(width: 5, height: 5)
        serverUpwardImage.image = Images.upwardArrow
        server.addSubview(serverUpwardImage)
        serverUpwardImage.translatesAutoresizingMaskIntoConstraints = false
        serverUpwardImage.trailingAnchor.constraint(equalTo: server.trailingAnchor, constant: -10).isActive = true
        serverUpwardImage.topAnchor.constraint(equalTo: server.topAnchor, constant: 15.5).isActive = true
    }

    /// UI Function to create subview on based of orientation
    func createSecondView() {
        secondView.isScrollEnabled = true;
        secondView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(secondView)
        if currentOrientation == .portrait {
            leadingConstraint = secondView.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: 0)
            topConstraint = secondView.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: 290);
            widthConstraint = secondView.widthAnchor.constraint(equalToConstant: width)
            heightConstraint = secondView.heightAnchor.constraint(equalToConstant: height / 2 + 100)
        } else {
            leadingConstraint = secondView.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: height / 2 - 50)
            leadingConstraint.identifier = Strings.expendSetting;
            topConstraint = secondView.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: 0);
            topConstraint.identifier = Strings.expendSetting
            widthConstraint = secondView.widthAnchor.constraint(equalToConstant: height / 2)
            widthConstraint.identifier = Strings.expendSetting
            heightConstraint = secondView.heightAnchor.constraint(equalToConstant: width)
            heightConstraint.identifier = Strings.expendSetting
        }
        leadingConstraint.identifier = Strings.expendSetting;
        topConstraint.identifier = Strings.expendSetting
        widthConstraint.identifier = Strings.expendSetting
        heightConstraint.identifier = Strings.expendSetting
        NSLayoutConstraint.activate([leadingConstraint, topConstraint, widthConstraint, heightConstraint])
    }

    /// UI Function to to update the constraints
    func refreshConstraints() {
        if currentOrientation == .portrait {
            frame.size.width = width
            leadingConstraint.constant = 0
            topConstraint.constant = 290
            widthConstraint.constant = width
            heightConstraint.constant = height / 2
            dropdownTopAnchor.constant = 0;
        } else {
            frame.size.width = height
            leadingConstraint.constant = height / 2 - 50
            topConstraint.constant = 30
            widthConstraint.constant = height / 2
            heightConstraint.constant = width;
            dropdownTopAnchor.constant = 150;
        }
    }

    /// UI Function to create button for images
    func createImagesButton() {
        preview = createSecondViewButton(buttonName: Strings.preview, leadingAnchor: 10, topAnchor: 80, buttonWidth: 120, action: #selector(applyPreview(_:)), borderColor: Colors.freeRoamButtonsColor!.cgColor)
        preview.tag = 0
        training = createSecondViewButton(buttonName: Strings.training, leadingAnchor: 140, topAnchor: 80, buttonWidth: 120, action: #selector(applyTraining(_:)), borderColor: Colors.title!.cgColor)
        training.tag = 1
        setupImageMode()
    }

    /// Function to check whether sensors are enabled or not
    func checkButtonBorderColor(sensor:String) -> Bool? {
        if let value = preferencesManager.getSensorData(sensor: sensor){
            return value as! Bool;
        }
        return true;
    }
    
    /// UI Function to create button for sensors
    func createSensorButtons() {
        vehicle = createSecondViewButton(buttonName: Strings.vehicle, leadingAnchor: 10, topAnchor: 160, buttonWidth: 80, action: #selector(updateSensor(_:)), borderColor: checkButtonBorderColor(sensor: "isVehicleLogSelected") == true ? Colors.title!.cgColor : Colors.freeRoamButtonsColor!.cgColor)
        vehicle.tag = 1;
        sensorButtons.append(vehicle)
        gps = createSecondViewButton(buttonName: Strings.gps, leadingAnchor: 100, topAnchor: 160, buttonWidth: 80, action: #selector(updateSensor(_:)), borderColor: checkButtonBorderColor(sensor: "isGpsLogSelected") == true ? Colors.title!.cgColor : Colors.freeRoamButtonsColor!.cgColor)
        gps.tag = 2;
        sensorButtons.append(gps)
        acceleration = createSecondViewButton(buttonName: Strings.accelerometer, leadingAnchor: 190, topAnchor: 160, buttonWidth: 140, action: #selector(updateSensor(_:)), borderColor: checkButtonBorderColor(sensor: "isAccelerationLogSelected") == true ? Colors.title!.cgColor : Colors.freeRoamButtonsColor!.cgColor)
        acceleration.tag = 3
        sensorButtons.append(acceleration)
        magnetic = createSecondViewButton(buttonName: Strings.magnetic, leadingAnchor: 10, topAnchor: 210, buttonWidth: 100, action: #selector(updateSensor(_:)), borderColor: checkButtonBorderColor(sensor: "isMagneticLogSelected") == true ? Colors.title!.cgColor : Colors.freeRoamButtonsColor!.cgColor)
        magnetic.tag = 4
        sensorButtons.append(magnetic)
        gyroscope = createSecondViewButton(buttonName: Strings.gyroscope, leadingAnchor: 120, topAnchor: 210, buttonWidth: 100, action: #selector(updateSensor(_:)), borderColor: checkButtonBorderColor(sensor: "isGyroscopeLogSelected") == true ? Colors.title!.cgColor : Colors.freeRoamButtonsColor!.cgColor)
        gyroscope.tag = 5
        sensorButtons.append(gyroscope)
    }

    /// UI Function to create text field for delay
    func createDelayField() {
        let delayTextField = UITextField(frame: CGRect(x: 310, y: 217, width: 50, height: 40))
        delayTextField.placeholder = "200"
        delayTextField.font = UIFont.systemFont(ofSize: 15)
        delayTextField.borderStyle = UITextField.BorderStyle.roundedRect
        delayTextField.autocorrectionType = UITextAutocorrectionType.no
        delayTextField.keyboardType = UIKeyboardType.phonePad
        delayTextField.returnKeyType = UIReturnKeyType.default
        delayTextField.clearButtonMode = UITextField.ViewMode.whileEditing
        delayTextField.contentVerticalAlignment = UIControl.ContentVerticalAlignment.center
        delayTextField.delegate = self
        delayTextField.addTarget(self, action: #selector(delayFieldDidChange), for: .editingChanged)
        let bottomLine = CALayer()
        bottomLine.frame = CGRect(x: 0.0, y: delayTextField.frame.height - 10, width: delayTextField.frame.width, height: 1.0)
        bottomLine.backgroundColor = UIColor.white.cgColor
        delayTextField.borderStyle = UITextField.BorderStyle.none
        delayTextField.layer.addSublayer(bottomLine)
        secondView.addSubview(delayTextField)
    }

    /// UI Function to create Button View
    func createButton(borderColor: String, buttonName: String, leadingAnchor: CGFloat, topAnchor: CGFloat, action: Selector?) -> UIButton {
        let btn = UIButton()
        btn.layer.cornerRadius = 10
        btn.backgroundColor = Colors.freeRoamButtonsColor
        let text = UILabel()
        text.text = buttonName
        text.textColor = Colors.border
        if let action = action {
            btn.addTarget(self, action: action, for: .touchUpInside)
        }
        addSubview(btn)
        btn.translatesAutoresizingMaskIntoConstraints = false
        btn.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: topAnchor).isActive = true;
        btn.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: leadingAnchor).isActive = true
        btn.widthAnchor.constraint(equalToConstant: 80).isActive = true
        btn.heightAnchor.constraint(equalToConstant: 40).isActive = true
        text.frame = CGRect(x: 10, y: 0, width: 70, height: 40)
        btn.addSubview(text)
        return btn
    }

    /// UI Function to create Dropdown View
    func createDropdownView(borderColor: String, buttonName: String, leadingAnchor: CGFloat, topAnchor: CGFloat, action: Selector?) -> UIView {
        let dd = UIView()
        dd.layer.cornerRadius = 10
        dd.backgroundColor = Colors.freeRoamButtonsColor
        dropdownLabel.text = buttonName
        dropdownLabel.textColor = Colors.border
        let tap = UITapGestureRecognizer(target: self, action: action)
        dd.addGestureRecognizer(tap)
        addSubview(dd)
        dd.translatesAutoresizingMaskIntoConstraints = false
        dd.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: topAnchor).isActive = true;
        dd.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: leadingAnchor).isActive = true
        dd.widthAnchor.constraint(equalToConstant: 210).isActive = true
        dd.heightAnchor.constraint(equalToConstant: 40).isActive = true
        dropdownLabel.frame = CGRect(x: 10, y: 0, width: 210, height: 40)
        dd.addSubview(dropdownLabel)
        return dd
    }

    /// UI Function to create Label View
    func createLabels(value: String, leadingAnchor: CGFloat, topAnchor: CGFloat, labelWidth: CGFloat, labelHeight: CGFloat) -> UILabel {
        let label = UILabel()
        label.text = value
        label.font = UIFont(name: "medium", size: adapted(dimensionSize: 30, to: .width))
        label.frame.size = CGSize(width: labelWidth, height: labelHeight)
        label.translatesAutoresizingMaskIntoConstraints = false
        label.textColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black;
        label.font = label.font.withSize(15)
        addSubview(label)
        label.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: topAnchor).isActive = true
        label.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: leadingAnchor).isActive = true
        return label
    }

    /// UI Function to create labels
    func createSecondViewLabel(value: String, leadingAnchor: CGFloat, topAnchor: CGFloat, labelWidth: CGFloat, labelHeight: CGFloat) -> UILabel {
        let label = UILabel()
        label.text = value
        label.font = UIFont(name: "medium", size: adapted(dimensionSize: 30, to: .width))
        label.frame.size = CGSize(width: labelWidth, height: labelHeight)
        label.translatesAutoresizingMaskIntoConstraints = false
        label.textColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black;
        label.font = label.font.withSize(15)
        secondView.addSubview(label)
        label.topAnchor.constraint(equalTo: secondView.topAnchor, constant: topAnchor).isActive = true
        label.leadingAnchor.constraint(equalTo: secondView.leadingAnchor, constant: leadingAnchor).isActive = true
        return label
    }

    /// UI Function to create buttons
    func createSecondViewButton(buttonName: String, leadingAnchor: CGFloat, topAnchor: CGFloat, buttonWidth: CGFloat, action: Selector?, borderColor: CGColor) -> UIButton {
        let btn = UIButton()
        btn.layer.cornerRadius = 10
        btn.backgroundColor = Colors.freeRoamButtonsColor
        let text = UILabel()
        text.text = buttonName
        text.textColor = Colors.border
        if let action = action {
            btn.addTarget(self, action: action, for: .touchUpInside)
        }
        secondView.addSubview(btn)
        btn.layer.borderWidth = 2
        btn.layer.borderColor = borderColor
        btn.translatesAutoresizingMaskIntoConstraints = false
        btn.topAnchor.constraint(equalTo: secondView.topAnchor, constant: topAnchor).isActive = true;
        btn.leadingAnchor.constraint(equalTo: secondView.leadingAnchor, constant: leadingAnchor).isActive = true
        btn.widthAnchor.constraint(equalToConstant: buttonWidth).isActive = true
        btn.heightAnchor.constraint(equalToConstant: 40).isActive = true
        text.frame = CGRect(x: 10, y: 0, width: buttonWidth, height: 40)
        btn.addSubview(text)
        return btn
    }

    /// UI Function to create speed rpm labels
    func createLeftSpeed() {
        leftSpeedLabel.frame.size = CGSize(width: 100, height: 40);
        leftSpeedLabel.frame.origin = CGPoint(x: 10, y: 280)
        leftSpeedLabel.text = "xxx,xxx"
        secondView.addSubview(leftSpeedLabel)
        leftSpeedLabel.font = leftSpeedLabel.font.withSize(13.5)
    }

    /// function to switch camera
    @objc func reverseCamera(_ sender: UITapGestureRecognizer? = nil) {
        NotificationCenter.default.post(name: .switchCamera, object: nil)
    }

    /// UI Function to create blur effect on view
    func applyBlurEffect() {
        let blurEffectView = UIView(frame: bounds);
        blurEffectView.backgroundColor = UIColor(named: "dataCollectionBg");
        blurEffectView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        addSubview(blurEffectView)
    }

    /// function to switch log button
    @objc func switchLogButton(_ sender: UISwitch) {
        NotificationCenter.default.post(name: .logData, object: logData.isOn);
        if sender.isOn {
            for sensor in sensorButtons {
                sensor.isEnabled = false
            }
        } else {
            for sensor in sensorButtons {
                sensor.isEnabled = true
            }
        }
    }

    /// Function to apply low resolution
    @objc func applyLowResolution(_ sender: UIView) {
        selectedResolution = Resolutions.LOW
        NotificationCenter.default.post(name: .updateResolution, object: selectedResolution)
        updateResolution()
        previewResolution.text = Strings.previewResolutionLow
    }

    /// Function to apply medium resolution
    @objc func applyMediumResolution(_ sender: UIView) {
        selectedResolution = Resolutions.MEDIUM
        NotificationCenter.default.post(name: .updateResolution, object: selectedResolution)
        updateResolution()
        previewResolution.text = Strings.previewResolutionMedium
    }

    /// Function to apply high resolution
    @objc func applyHighResolution(_ sender: UIView) {
        selectedResolution = Resolutions.HIGH
        NotificationCenter.default.post(name: .updateResolution, object: selectedResolution)
        updateResolution()
        previewResolution.text = Strings.previewResolutionHigh
    }

    @objc func serverHandler(_ sender: UIView) {
        serverDropDown.show();
        UIView.animate(withDuration: 0) {
            self.serverUpwardImage.transform = CGAffineTransform(rotationAngle: .pi)
        }
    }

    /// Function to update resolutions
    @objc func updateResolution() {

        switch (selectedResolution) {
        case .LOW:
            low.backgroundColor = Colors.title
            medium.backgroundColor = Colors.freeRoamButtonsColor
            high.backgroundColor = Colors.freeRoamButtonsColor
        case .MEDIUM:
            low.backgroundColor = Colors.freeRoamButtonsColor
            medium.backgroundColor = Colors.title
            high.backgroundColor = Colors.freeRoamButtonsColor
        case .HIGH:
            low.backgroundColor = Colors.freeRoamButtonsColor
            medium.backgroundColor = Colors.freeRoamButtonsColor
            high.backgroundColor = Colors.title
        }
    }

    /// function to turn on previews images
    @objc func applyPreview(_ sender: UIView) {
        NotificationCenter.default.post(name: .updatePreview, object: nil)
        let borderColor = (sender.layer.borderColor == Colors.title?.cgColor) ? Colors.freeRoamButtonsColor?.cgColor : Colors.title?.cgColor
        sender.layer.borderColor = borderColor
    }

    /// function to turn on training images
    @objc func applyTraining(_ sender: UIView) {
        NotificationCenter.default.post(name: .updateTraining, object: nil)
        let borderColor = (training.layer.borderColor == Colors.freeRoamButtonsColor?.cgColor) ? Colors.title?.cgColor : Colors.freeRoamButtonsColor?.cgColor
        sender.layer.borderColor = borderColor
    }

    func setupImageMode() {
    }

    /// function to update the sensor settings
    @objc func updateSensor(_ sender: UIButton) {
        NotificationCenter.default.post(name: .updateSensorsForLog, object: sender)
        updateBorderColor(sender: sender);
    }

    /// function to update the border colors
    func updateBorderColor(sender: UIButton) {
        sender.layer.borderColor = (sender.layer.borderColor == Colors.freeRoamButtonsColor?.cgColor) ? Colors.title?.cgColor : Colors.freeRoamButtonsColor?.cgColor
    }

    /// Called when the "Delay (ms)" field in the Data collection fragment changes.
    @objc func delayFieldDidChange(_ sender: UITextField) {
        if sender.text == "" {
            samplingPeriod = 0.2
        } else {
            samplingPeriod = Double(sender.text!)! / 1000.0
        }
    }

    /// function to show the dropdown
    @objc func showDropdown(_ sender: UIButton) {
        dropDown.show()
    }

    ///function to load the model names and their resolutions
    func loadModelsNameAndResolution() {
        modelsName = Common.loadAllModelsName();
        for model in Common.loadAllModelItems() {
            resolution.append(model.inputSize);
        }
    }

    @objc func ble(_ sender: UIView) {
        NotificationCenter.default.post(name: .ble, object: nil)
    }

    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        delayTextField.resignFirstResponder()  //if desired
        return true
    }

    @objc func updateConnect(_ notification: Notification) {
        if (isBluetoothConnected) {
            bluetoothIcon.image = Images.bluetoothConnected
        } else {
            bluetoothIcon.image = Images.bluetoothDisconnected
        }
    }

    @objc func dismissKeyboard(_ sender: UIButton) {
        endEditing(true);
    }

    @objc func keyboardWillShow(_ notification: Notification) {
        if currentOrientation == .portrait {
            topConstraint.constant = 260
        } else {
            topConstraint.constant = 10
        }
    }

    @objc func keyboardWillHide(_ notification: Notification) {
        if currentOrientation == .portrait {
            topConstraint.constant = 290
        } else {
            topConstraint.constant = 30
        }
    }

    @objc func updateSpeedLabel(_ notification: Notification) {
        leftSpeedLabel.text = notification.object as? String
    }

    /// function to show server dropdown
    ///
    /// - Parameter sender:
    @objc func showServerDropdown(_ sender: UIButton) {
        serverDropDown.show()
    }

    @objc func updateServer(_ notification: Notification) {
        serverDropDown.dataSource = servers;
    }

    /// UI Function to create Dropdown View
    func createSaveAsView(borderColor: String, buttonName: String, leadingAnchor: CGFloat, topAnchor: CGFloat, action: Selector?) -> UIView {
        saveAsDropdown.backgroundColor = Colors.freeRoamButtonsColor
        saveAsDropdown.textColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black;
        saveAsDropdown.width = 210
        let dd = UIView()
        dd.layer.cornerRadius = 10;
        dd.backgroundColor = Colors.title;
        saveAsDropdownLabel.text = buttonName;
        saveAsDropdownLabel.textColor = Colors.border
        let tap = UITapGestureRecognizer(target: self, action: action)
        dd.addGestureRecognizer(tap)
        secondView.addSubview(dd);
        dd.translatesAutoresizingMaskIntoConstraints = false
        dd.topAnchor.constraint(equalTo: secondView.topAnchor, constant: topAnchor).isActive = true;
        dd.leadingAnchor.constraint(equalTo: secondView.leadingAnchor, constant: leadingAnchor).isActive = true
        dd.widthAnchor.constraint(equalToConstant: 210).isActive = true
        dd.heightAnchor.constraint(equalToConstant: 40).isActive = true
        saveAsDropdownLabel.frame = CGRect(x: 10, y: 0, width: 180, height: 40)
        dd.addSubview(saveAsDropdownLabel);
        saveAsDropdown.dataSource = ["Server", "Drive", "Local"]
        saveAsDropdown.selectionAction = { [unowned self] (index: Int, item: String) in
            saveAsDropdownLabel.text = item;
            NotificationCenter.default.post(name: .saveAs, object: item);
        }
        saveAsUpwardImage.frame.size = CGSize(width: 5, height: 5)
        saveAsUpwardImage.image = Images.upwardArrow;
        dd.addSubview(saveAsUpwardImage);
        saveAsUpwardImage.translatesAutoresizingMaskIntoConstraints = false;
        saveAsUpwardImage.trailingAnchor.constraint(equalTo: dd.trailingAnchor, constant: -10).isActive = true;
        saveAsUpwardImage.topAnchor.constraint(equalTo: dd.topAnchor, constant: 15.5).isActive = true;
        saveAsDropdown.anchorView = dd
        return dd
    }


    func createSaveAsDropDown() {
        saveAsDropdownView = createSaveAsView(borderColor: "black", buttonName: "Local", leadingAnchor: 10, topAnchor: 30, action: #selector(showSaveAsDropDown))
        secondView.addSubview(saveAsDropdownView);
    }

    @objc func showSaveAsDropDown(_ sender: UIButton) {
        saveAsDropdown.show();
    }

}
