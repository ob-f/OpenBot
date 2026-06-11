//
// Created by Sparsh Jain on 28/10/22.
//

import Foundation
import UIKit
import DropDown

/// Implementation of Object tracking settings.
class ObjectTrackingSettings: UIView {
    let autoModeButton = UISwitch()
    var speedLabel = UILabel()
    var modelDropdownLabel = UILabel()
    var imageInputLabel = UILabel()
    var threadLabel = UILabel()
    var confidenceLabel = UILabel()
    var deviceDropDownLabel = UILabel()
    var objectDropDownLabel = UILabel()
    var detector: Detector?;
    var selectedModel: ModelItem?;
    var bluetoothIcon = UIImageView()
    var leftSpeedLabel = UILabel()
    var deviceDropDown = DropDown()
    var deviceDropDownView = UIView()
    var modelDropDown = DropDown()
    var objectDropDown = DropDown();
    var objectDropDownView = UIView()
    var dynamicSpeedLabel = UILabel()
    var dynamicSpeedCheckbox = Checkbox()
    var preferencesManager : SharedPreferencesManager = SharedPreferencesManager()

    /// Initialization routine.
    ///
    /// - Parameters:
    ///     - frame:
    ///     - detector:
    ///     - model:
    init(frame: CGRect, detector: Detector?, model: ModelItem) {

        // Setup UI
        self.detector = detector;
        selectedModel = model
        super.init(frame: frame);
        let swipeDown = UISwipeGestureRecognizer(target: self, action: #selector(respondToSwipeGesture))
        swipeDown.direction = .down
        addGestureRecognizer(swipeDown)
        let swipeUp = UISwipeGestureRecognizer(target: self, action: #selector(respondToSwipeGesture))
        swipeUp.direction = .up
        addGestureRecognizer(swipeUp)
        createBar()
        addSubview(createLabel(text: Strings.autoMode, leadingAnchor: Int(adapted(dimensionSize: 20, to: .height)), topAnchor: Int(adapted(dimensionSize: 15, to: .height))));
        createBluetoothIcon()
        createCameraIcon()
        createSwitchButton()
        addSubview(createLabel(text: Strings.dynamicSpeed, leadingAnchor: Int(adapted(dimensionSize: 150, to: .height)), topAnchor: Int(adapted(dimensionSize: 40, to: .height))))
        setDynamicSpeed()
        addSubview(createLabel(text: Strings.model, leadingAnchor: Int(adapted(dimensionSize: 20, to: .height)), topAnchor: Int(adapted(dimensionSize: 60, to: .height))))
        createModelDropDown()
        addSubview(createLabel(text: Strings.speed, leadingAnchor: Int(adapted(dimensionSize: 20, to: .height)), topAnchor: Int(adapted(dimensionSize: 90, to: .height))))
        setupSpeed()
        addSubview(createLabel(text: Strings.input, leadingAnchor: 180, topAnchor: Int(adapted(dimensionSize: 90, to: .height))))
        setupInput();
        addSubview(createLabel(text: Strings.object, leadingAnchor: Int(adapted(dimensionSize: 20, to: .height)), topAnchor: Int(adapted(dimensionSize: 120, to: .height))))
        setupObjectDropDown()
        addSubview(createLabel(text: Strings.confidence, leadingAnchor: 180, topAnchor: Int(adapted(dimensionSize: 120, to: .height))))
        setupConfidence()
        addSubview(createLabel(text: Strings.device, leadingAnchor: Int(adapted(dimensionSize: 20, to: .height)), topAnchor: Int(adapted(dimensionSize: 150, to: Dimension.height))))
        createDeviceDropDown()
        addSubview(createLabel(text: Strings.threads, leadingAnchor: 180, topAnchor: Int(adapted(dimensionSize: 150, to: .height))))
        setupThreads();
        setupVehicleControls();
        createLeftSpeed()
        if let model = preferencesManager.getObjectTrackModel(){
            if Common.isModelItemAvailableInDocument(modelName: model) == true {
                modelDropdownLabel.text = model
                let returnModel = Common.returnModelItem(modelName: model)
                self.selectedModel = returnModel
                imageInputLabel.text = returnModel.inputSize
            }
            else{
                preferencesManager.setObjectTrackModel(value: "");
            }
        }
        
        if let object = preferencesManager.getObjectTrackingObject(){
            objectDropDownLabel.text = object
        }

        if let confidence = preferencesManager.getObjectTrackConfidence(){
            confidenceLabel.text = String(confidence as! Int) + "%"
        }
        
        if let device = preferencesManager.getDevice(){
            deviceDropDownLabel.text = device;
        }
        
        if let threads = preferencesManager.getThreads(){
            threadLabel.text = threads;
        }
        
        if let dynamicSpeed = preferencesManager.getDynamicSpeed(){
            if dynamicSpeed as! Bool == true {
                dynamicSpeedCheckbox.isChecked = true;
            }
        }
        // Setup callbacks
        NotificationCenter.default.addObserver(self, selector: #selector(updateModel), name: .updateModel, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateObject), name: .updateObject, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateDevice), name: .updateDevice, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateThreadLabel), name: .updateThreadLabel, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothConnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothDisconnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateSpeedLabel), name: .updateSpeedLabel, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(toggleNetwork), name: .toggleNetworks, object: nil);
        NotificationCenter.default.addObserver(self, selector: #selector(updateSpeed), name: .updateObjectTrackingFps, object: nil);
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    /// function to create switch buttons.
    func createSwitchButton() {
        autoModeButton.isOn = false
        autoModeButton.setOn(false, animated: true)
        autoModeButton.onTintColor = Colors.title
        autoModeButton.addTarget(self, action: #selector(switchButton(_:)), for: .valueChanged)
        autoModeButton.translatesAutoresizingMaskIntoConstraints = false
        addSubview(autoModeButton)
        autoModeButton.widthAnchor.constraint(equalToConstant: 40).isActive = true
        autoModeButton.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: 120).isActive = true
        autoModeButton.topAnchor.constraint(equalTo: topAnchor, constant: 25).isActive = true
    }

    /// function to create labels.
    func createLabel(text: String, leadingAnchor: Int, topAnchor: Int) -> UILabel {
        let label = UILabel()
        label.text = text
        label.textColor = Colors.border
        label.frame.origin = CGPoint(x: leadingAnchor, y: topAnchor)
        label.frame.size = resized(size: CGSize(width: text.count * 10, height: 40), basedOn: .height)
        return label
    }

    /// function to handle the updating of auto mode.
    @objc func switchButton(_ sender: UISwitch) {
        NotificationCenter.default.post(name: .autoModeObjectTracking, object: nil)
        if sender.isOn {
            if selectedModel?.name == "MobileNetV1-300" + Strings.tflite {
                speedLabel.text = "30 fps"
            } else {
                speedLabel.text = "2 fps"
            }
        } else {
            speedLabel.text = "xxx fps"
        }
    }

    /// function to handle the swipe gesture to hide or display the settings.
    @objc func respondToSwipeGesture(gesture: UIGestureRecognizer) {
        if let swipeGesture = gesture as? UISwipeGestureRecognizer {
            switch swipeGesture.direction {
            case .down:
                UIView.animate(withDuration: 0.25) {
                    if currentOrientation == .portrait {
                        self.frame.origin.y = height - adapted(dimensionSize: 50, to: .height)
                    } else {
                        self.frame.origin.y = width - adapted(dimensionSize: 50, to: .height)
                    }
                }
            case .up:
                UIView.animate(withDuration: 0.25) {
                    if currentOrientation == .portrait {
                        self.frame.origin.y = height - width;
                    } else {
                        self.frame.origin.y = adapted(dimensionSize: 20, to: .height)
                    }
                }
            default:
                break
            }
        }
    }

    /// function to create bars for the object tracking.
    func createBar() {
        let bar = UIView()
        bar.backgroundColor = Colors.title
        addSubview(bar)
        bar.translatesAutoresizingMaskIntoConstraints = false
        bar.widthAnchor.constraint(equalToConstant: adapted(dimensionSize: 50, to: .height)).isActive = true
        bar.heightAnchor.constraint(equalToConstant: 5).isActive = true
        bar.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: width / 2 - 30).isActive = true
        bar.topAnchor.constraint(equalTo: topAnchor, constant: 10).isActive = true
        bar.layer.cornerRadius = 2
    }

    /// function to create the bluetooth button
    func createBluetoothIcon() {
        let bluetoothIconView = UIView();
        bluetoothIconView.frame.origin = CGPoint(x: width - adapted(dimensionSize: 85, to: .height), y: adapted(dimensionSize: 10, to: .height));
        bluetoothIconView.frame.size = CGSize(width: 40, height: 50);
        addSubview(bluetoothIconView);
        if (isBluetoothConnected) {
            bluetoothIcon = createIcons(iconImg: Images.bluetoothConnected_v2!, x: 5, y: 15, size: resized(size: Images.bluetoothConnected_v2!.size, basedOn: Dimension.width), backgroundColor: Colors.title ?? .blue, action: #selector(ble(_:)))
        } else {
            bluetoothIcon = createIcons(iconImg: Images.bluetoothDisconnected_v2!, x: 5, y: 15, size: resized(size: Images.bluetoothDisconnected_v2!.size, basedOn: Dimension.width), backgroundColor: Colors.title ?? .blue, action: #selector(ble(_:)))
        }
        bluetoothIconView.addSubview(bluetoothIcon);
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(ble(_:)))
        bluetoothIconView.isUserInteractionEnabled = true
        bluetoothIconView.addGestureRecognizer(tapGesture)

    }

    /// function to create the camera button
    func createCameraIcon() {
        if let image = Images.frontCamera {
            let cameraIconView = UIView();
            cameraIconView.frame.origin = CGPoint(x: width - adapted(dimensionSize: 50, to: .width), y: adapted(dimensionSize: 20, to: .height));
            cameraIconView.frame.size = CGSize(width: 50, height: 50);
            addSubview(cameraIconView)
            let camera = createIcons(iconImg: image, x: 5, y: 0, size: resized(size: image.size, basedOn: Dimension.height), backgroundColor: Colors.title ?? .blue, action: #selector(switchCamera(_:)))
            cameraIconView.addSubview(camera);
            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(switchCamera(_:)))
            cameraIconView.isUserInteractionEnabled = true
            cameraIconView.addGestureRecognizer(tapGesture)
        }
    }

    @objc func ble(_ sender: UIView) {
        NotificationCenter.default.post(name: .ble, object: nil)
    }

    /// function to trigger the switch camera.
    @objc func switchCamera(_ sender: UIView) {
        NotificationCenter.default.post(name: .switchCamera, object: nil)
    }

    /// function to create the icons UI
    func createIcons(iconImg: UIImage, x: CGFloat, y: CGFloat, size: CGSize, backgroundColor: UIColor, action: Selector?) -> UIImageView {
        let iconImage = UIImageView(frame: CGRect(x: x, y: y, width: size.width, height: size.height))
        iconImage.image = iconImg
        iconImage.layer.cornerRadius = 30
        return iconImage
    }

    /// function to setup image size.
    func setupInput() {
        imageInputLabel.frame = CGRect(x: width - 80, y: adapted(dimensionSize: 90, to: .height), width: 100, height: 40)
        imageInputLabel.text = selectedModel?.inputSize
        addSubview(imageInputLabel)
    }

    /// function to create speed label to display fps
    func setupSpeed() {
        speedLabel = createLabel(text: "*** fps", leadingAnchor: 90, topAnchor: Int(adapted(dimensionSize: 90, to: .height)))
        addSubview(speedLabel)
    }
    
    /// function to create checkbox to enable or disable dynamic speed
    func setDynamicSpeed(){
        dynamicSpeedCheckbox = createCheckbox(leadingAnchor: 181.5, topAnchor: 68,action: #selector(updateDynamicSpeed(_:)))
        addSubview(dynamicSpeedCheckbox);
    }
    
    /// function to create checkbox for objects
    func createCheckbox(leadingAnchor: CGFloat, topAnchor: CGFloat, action: Selector?) -> Checkbox {
        let checkbox = Checkbox(frame: CGRect(x: leadingAnchor, y: topAnchor, width: 20, height: 20))
        checkbox.checkedBorderColor = traitCollection.userInterfaceStyle == .dark ? .white : .black;
        checkbox.uncheckedBorderColor = traitCollection.userInterfaceStyle == .dark ? .white : .black;
        checkbox.checkmarkColor = traitCollection.userInterfaceStyle == .dark ? .white : .black;
        checkbox.checkedBorderColor = traitCollection.userInterfaceStyle == .dark ? .white : .black;
        checkbox.checkmarkStyle = .tick;
        checkbox.isEnabled = true;
        checkbox.addTarget(self, action: action!, for: .valueChanged);
        return checkbox
    }
    
    @objc func updateDynamicSpeed(_ sender: UIButton) {
        preferencesManager.setDynamicSpeed(value: dynamicSpeedCheckbox.isChecked);
    }
        
    /// function to crete the dropdown for objects
    func setupObjectDropDown() {
        objectDropDown.backgroundColor = Colors.freeRoamButtonsColor;
        objectDropDown.textColor = Colors.textColor ?? .black
        objectDropDown.anchorView = objectDropDownView;
        objectDropDown.dataSource = detector?.getLabels() ?? [" "];
        objectDropDown.width = 150;
        objectDropDown.selectionAction = { [self] (index: Int, item: String) in
            objectDropDownLabel.text = item
            NotificationCenter.default.post(name: .updateObject, object: item)
        }
        let dd = UIView()
        dd.backgroundColor = Colors.freeRoamButtonsColor
        objectDropDownLabel.text = "person"
        objectDropDownLabel.textColor = Colors.border
        let tap = UITapGestureRecognizer(target: self, action: #selector(showObjectDropdown(_:)))
        objectDropDownLabel.addGestureRecognizer(tap)
        dd.addGestureRecognizer(tap)
        let upwardImage = UIImageView()
        upwardImage.frame.size = CGSize(width: 5, height: 5)
        upwardImage.image = Images.downArrow
        dd.addSubview(upwardImage)
        upwardImage.translatesAutoresizingMaskIntoConstraints = false
        upwardImage.trailingAnchor.constraint(equalTo: dd.trailingAnchor, constant: -20).isActive = true
        upwardImage.topAnchor.constraint(equalTo: dd.topAnchor, constant: 11.5).isActive = true
        addSubview(dd)
        dd.translatesAutoresizingMaskIntoConstraints = false
        dd.topAnchor.constraint(equalTo: topAnchor, constant: adapted(dimensionSize: 120, to: .height)).isActive = true;
        dd.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: 80).isActive = true
        dd.widthAnchor.constraint(equalToConstant: 100).isActive = true
        dd.heightAnchor.constraint(equalToConstant: 30).isActive = true
        objectDropDownLabel.frame = CGRect(x: 10, y: 0, width: 80, height: 40)
        dd.addSubview(objectDropDownLabel)
        addSubview(objectDropDownView)
        objectDropDownView.frame.size = CGSize(width: 200, height: 100);
        objectDropDownView.translatesAutoresizingMaskIntoConstraints = false
        objectDropDownView.topAnchor.constraint(equalTo: topAnchor, constant: -150).isActive = true
        objectDropDownView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 91).isActive = true
    }

    /// Set the detection minimum confidence level
    func setupConfidence() {
        // Setting plus
        let plusImageView = UIView();
        plusImageView.frame.size = CGSize(width: 30, height: 30);
        plusImageView.frame.origin = CGPoint(x: width - 40, y: adapted(dimensionSize: 120, to: .height));
        addSubview(plusImageView)
        let plusImage = UIImageView();
        plusImage.image = Images.plus
        plusImage.frame.size = CGSize(width: 20, height: 20);
        plusImage.isUserInteractionEnabled = true;
        let tapOnPlus = UITapGestureRecognizer(target: self, action: #selector(increaseConfidence(_:)))
        plusImageView.addGestureRecognizer(tapOnPlus)
        plusImageView.addSubview(plusImage)
        plusImage.translatesAutoresizingMaskIntoConstraints = false
        plusImage.leadingAnchor.constraint(equalTo: plusImageView.leadingAnchor, constant: 5.5).isActive = true
        plusImage.topAnchor.constraint(equalTo: plusImageView.topAnchor, constant: 10).isActive = true

        // Setting minus
        let minusImageView = UIView()
        minusImageView.frame.size = CGSize(width: 30, height: 30);
        minusImageView.frame.origin = CGPoint(x: width - 100, y: adapted(dimensionSize: 120, to: .height))
        addSubview(minusImageView)
        let minusImage = UIImageView()
        minusImage.image = Images.minus
        minusImage.frame.size = CGSize(width: 30, height: 30);
        minusImage.isUserInteractionEnabled = true;
        minusImageView.addSubview(minusImage)
        let tapOnMinus = UITapGestureRecognizer(target: self, action: #selector(decreaseConfidence(_:)))
        minusImageView.addGestureRecognizer(tapOnMinus)
        minusImage.translatesAutoresizingMaskIntoConstraints = false
        minusImage.leadingAnchor.constraint(equalTo: minusImageView.leadingAnchor, constant: 5.5).isActive = true
        minusImage.topAnchor.constraint(equalTo: minusImageView.topAnchor, constant: 10).isActive = true

        // Thread Label
        confidenceLabel.frame.size = CGSize(width: 10, height: 40);
        addSubview(confidenceLabel);
        confidenceLabel.translatesAutoresizingMaskIntoConstraints = false
        confidenceLabel.text = "50%";
        confidenceLabel.textColor = Colors.border
        confidenceLabel.translatesAutoresizingMaskIntoConstraints = false
        confidenceLabel.leadingAnchor.constraint(equalTo: minusImageView.trailingAnchor, constant: 0).isActive = true
        confidenceLabel.topAnchor.constraint(equalTo: minusImageView.topAnchor, constant: 8).isActive = true
    }

    /// Set the number of threads
    func setupThreads() {
        // Setting plus
        let plusImageView = UIView();
        plusImageView.frame.size = CGSize(width: 30, height: 30);
        plusImageView.frame.origin = CGPoint(x: width - 40, y: adapted(dimensionSize: 150, to: .height));
        addSubview(plusImageView)
        let plusImage = UIImageView();
        plusImage.image = Images.plus
        plusImage.frame.size = CGSize(width: 20, height: 20);
        plusImage.isUserInteractionEnabled = true;
        let tapOnPlus = UITapGestureRecognizer(target: self, action: #selector(increaseThreads(_:)))
        plusImageView.addGestureRecognizer(tapOnPlus)
        plusImageView.addSubview(plusImage)
        plusImage.translatesAutoresizingMaskIntoConstraints = false
        plusImage.leadingAnchor.constraint(equalTo: plusImageView.leadingAnchor, constant: 5.5).isActive = true
        plusImage.topAnchor.constraint(equalTo: plusImageView.topAnchor, constant: 10).isActive = true

        // Setting minus
        let minusImageView = UIView()
        minusImageView.frame.size = CGSize(width: 30, height: 30);
        minusImageView.frame.origin = CGPoint(x: width - 100, y: adapted(dimensionSize: 150, to: .height))
        addSubview(minusImageView)
        let minusImage = UIImageView()
        minusImage.image = Images.minus
        minusImage.frame.size = CGSize(width: 30, height: 30);
        minusImage.isUserInteractionEnabled = true;
        minusImageView.addSubview(minusImage)
        let tapOnMinus = UITapGestureRecognizer(target: self, action: #selector(decreaseThreads(_:)))
        minusImageView.addGestureRecognizer(tapOnMinus)
        minusImage.translatesAutoresizingMaskIntoConstraints = false
        minusImage.leadingAnchor.constraint(equalTo: minusImageView.leadingAnchor, constant: 5.5).isActive = true
        minusImage.topAnchor.constraint(equalTo: minusImageView.topAnchor, constant: 10).isActive = true

        // Thread Label
        threadLabel.frame.size = CGSize(width: 10, height: 40);
        addSubview(threadLabel);
        threadLabel.translatesAutoresizingMaskIntoConstraints = false
        threadLabel.text = "1";
        threadLabel.textColor = Colors.border
        threadLabel.translatesAutoresizingMaskIntoConstraints = false
        threadLabel.leadingAnchor.constraint(equalTo: minusImageView.trailingAnchor, constant: 8).isActive = true
        threadLabel.topAnchor.constraint(equalTo: minusImageView.topAnchor, constant: 8).isActive = true
    }

    /// function to increase the confidence
    @objc func increaseConfidence(_ sender: UIImage) {
        if confidenceLabel.text == "100%" {
            return
        }
        var value = Int(confidenceLabel.text?.prefix((confidenceLabel.text?.count ?? 1) - 1) ?? "50")
        value = (value ?? 5) + 5;
        confidenceLabel.text = String(value!) + "%"
        NotificationCenter.default.post(name: .updateConfidence, object: value)
    }

    /// function to decrease the confidence
    @objc func decreaseConfidence(_ sender: UIImage) {
        if confidenceLabel.text == "0%" {
            return
        }
        var value = Int(confidenceLabel.text?.prefix((confidenceLabel.text?.count ?? 1) - 1) ?? "50")
        value = (value ?? 5) - 5;
        confidenceLabel.text = String(value!) + "%"
        NotificationCenter.default.post(name: .updateConfidence, object: value)
    }

    /// function to increase the threads
    @objc func increaseThreads(_ sender: UIImage) {
        if threadLabel.text == "9" || threadLabel.text == "N/A" {
            return
        }
        var value = Int(threadLabel.text ?? "1")
        value = (value ?? 1) + 1;
        threadLabel.text = String(value!)
        NotificationCenter.default.post(name: .updateThread, object: threadLabel.text)
    }

    /// function to decrease the threads
    @objc func decreaseThreads(_ sender: UIImage) {
        if threadLabel.text == "1" || threadLabel.text == "N/A" {
            return
        }
        var value = Int(threadLabel.text ?? "1")
        value = (value ?? 1) - 1;
        threadLabel.text = String(value!)
        NotificationCenter.default.post(name: .updateThread, object: threadLabel.text)
    }

    /// function to setup the controls of the vehicle
    func setupVehicleControls() {
        let vehicleControls = VehicleControl();
        addSubview(vehicleControls)
        vehicleControls.translatesAutoresizingMaskIntoConstraints = false
        vehicleControls.topAnchor.constraint(equalTo: threadLabel.safeAreaLayoutGuide.bottomAnchor, constant: adapted(dimensionSize: 20, to: .height)).isActive = true;
        vehicleControls.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: 7).isActive = true
    }

    /// UI function
    func createDeviceDropDown() {
        deviceDropDown.backgroundColor = Colors.freeRoamButtonsColor
        deviceDropDown.textColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black;
        deviceDropDown.dataSource = Constants.devices
        deviceDropDown.width = 90
        let dd = UIView()
        deviceDropDown.anchorView = dd;
        deviceDropDownLabel.text = RuntimeDevice.CPU.rawValue
        deviceDropDownLabel.textColor = Colors.border
        deviceDropDown.selectionAction = { [unowned self] (index: Int, item: String) in
            deviceDropDownLabel.text = item
            NotificationCenter.default.post(name: .updateDevice, object: item)
        }
        dd.layer.cornerRadius = 10
        dd.backgroundColor = Colors.freeRoamButtonsColor
        deviceDropDownLabel.text = RuntimeDevice.CPU.rawValue
        deviceDropDownLabel.textColor = Colors.border
        let tap = UITapGestureRecognizer(target: self, action: #selector(showDeviceDropdown(_:)))
        dd.addGestureRecognizer(tap)
        let upwardImage = UIImageView()
        upwardImage.frame.size = CGSize(width: 5, height: 5)
        upwardImage.image = Images.downArrow
        dd.addSubview(upwardImage)
        upwardImage.translatesAutoresizingMaskIntoConstraints = false
        upwardImage.trailingAnchor.constraint(equalTo: dd.trailingAnchor, constant: -20).isActive = true
        upwardImage.topAnchor.constraint(equalTo: dd.topAnchor, constant: 11.5).isActive = true
        addSubview(dd)
        dd.translatesAutoresizingMaskIntoConstraints = false
        dd.topAnchor.constraint(equalTo: topAnchor, constant: adapted(dimensionSize: 150, to: .height)).isActive = true;
        dd.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: 80).isActive = true
        dd.widthAnchor.constraint(equalToConstant: 100).isActive = true
        dd.heightAnchor.constraint(equalToConstant: 30).isActive = true
        deviceDropDownLabel.frame = CGRect(x: 10, y: 0, width: 60, height: 40)
        dd.addSubview(deviceDropDownLabel)
    }

    /// UI function
    func createModelDropDown() {
        let selectedModels = Common.loadSelectedModels(mode: Constants.objectTrackingMode);
        modelDropDown.backgroundColor = Colors.freeRoamButtonsColor;
        modelDropDown.textColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black;
        let dd = UIView()
        let modelDropDownAnchor = UIView(frame: CGRect(x: 180, y: adapted(dimensionSize: 60, to: .height), width: 100, height: 200));
        modelDropDown.anchorView = modelDropDownAnchor;
        addSubview(modelDropDownAnchor);
        modelDropDown.dataSource = selectedModels
        modelDropDown.selectionAction = { [self] (index: Int, item: String) in
            modelDropdownLabel.text = item
            NotificationCenter.default.post(name: .updateModel, object: item)
        }
        dd.layer.cornerRadius = 10
        dd.backgroundColor = Colors.freeRoamButtonsColor
        modelDropdownLabel.text = selectedModels.first
        modelDropdownLabel.textColor = Colors.border
        let tap = UITapGestureRecognizer(target: self, action: #selector(showModelDropdown(_:)))
        dd.addGestureRecognizer(tap)
        let upwardImage = UIImageView()
        upwardImage.frame.size = CGSize(width: 5, height: 5)
        upwardImage.image = Images.downArrow
        dd.addSubview(upwardImage)
        upwardImage.translatesAutoresizingMaskIntoConstraints = false
        upwardImage.trailingAnchor.constraint(equalTo: dd.trailingAnchor, constant: -10).isActive = true
        upwardImage.topAnchor.constraint(equalTo: dd.topAnchor, constant: 11.5).isActive = true
        addSubview(dd)
        dd.translatesAutoresizingMaskIntoConstraints = false
        dd.topAnchor.constraint(equalTo: topAnchor, constant: adapted(dimensionSize: 60, to: .height)).isActive = true;
        dd.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: 180).isActive = true
        dd.widthAnchor.constraint(equalToConstant: 180).isActive = true
        dd.heightAnchor.constraint(equalToConstant: 40).isActive = true
        modelDropdownLabel.frame = CGRect(x: 0, y: 0, width: 210, height: 40)
        dd.addSubview(modelDropdownLabel)
    }

    /// Display speed on the UI
    func createLeftSpeed() {
        leftSpeedLabel.frame.size = CGSize(width: 100, height: 40);
        leftSpeedLabel.frame.origin = CGPoint(x: 20, y: adapted(dimensionSize: 200, to: .height))
        leftSpeedLabel.text = "xxx,xxx"
        addSubview(leftSpeedLabel)
        leftSpeedLabel.font = leftSpeedLabel.font.withSize(13.5)
    }

    /// UI callback function
    @objc func showModelDropdown(_ sender: UIButton) {
        modelDropDown.show()
    }

    /// UI callback function
    @objc func showDeviceDropdown(_ sender: UIButton) {
        deviceDropDown.show()
    }

    /// UI callback function
    @objc func showObjectDropdown(_ sender: UIButton) {
        objectDropDown.show()
    }

    /// Callback function to update device (CPU, GPU, ...)
    @objc func updateDevice(_ notification: Notification) {
        let selectedDevice = notification.object as! String
        deviceDropDownLabel.text = selectedDevice
    }

    /// Callback function to update the number of threads
    @objc func updateThreadLabel(_ notification: Notification) {
        threadLabel.text = (notification.object as! String)
    }

    /// Callback function to update the detector
    @objc func updateModel(_ notification: Notification) {
        let selectedModel = notification.object as! String
        modelDropdownLabel.text = selectedModel
        let model = Common.returnModelItem(modelName: selectedModel)
        self.selectedModel = model
        imageInputLabel.text = model.inputSize
        preferencesManager.setObjectTrackModel(value: selectedModel);
    }

    /// Callback function to update the UI
    @objc func updateObject(_ notification: Notification) {
        let selectedObject = notification.object as! String
        objectDropDownLabel.text = selectedObject
        preferencesManager.setObjectTrackingObject(value: selectedObject);
    }

    /// Callback function to update the BLE connection status
    @objc func updateConnect(_ notification: Notification) {
        if (isBluetoothConnected) {
            bluetoothIcon.image = Images.bluetoothConnected_v2
        } else {
            bluetoothIcon.image = Images.bluetoothDisconnected_v2
        }
    }

    /// Callback function to update the speed label
    @objc func updateSpeedLabel(_ notification: Notification) {
        leftSpeedLabel.text = notification.object as? String
    }

    /// callback to toggle autopilot when network command is received from web controller
    @objc func toggleNetwork(_ notification: Notification) {
        autoModeButton.setOn(!autoModeButton.isOn, animated: true)
        switchButton(autoModeButton)
    }

    /// Callback function to update the speed label
    @objc func updateSpeed(_ notification: Notification) {
        speedLabel.text = String(notification.object as! Double);
    }
}
