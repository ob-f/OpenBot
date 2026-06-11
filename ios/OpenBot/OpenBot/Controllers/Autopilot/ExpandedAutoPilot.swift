//
// Created by Nitish Yadav on 13/10/22.
//

import Foundation
import UIKit
import DropDown

class expandedAutoPilot: UIView {
    let autoModeButton = UISwitch()
    var serverLabel = UILabel()
    var speedLabel = UILabel()
    var leftSpeedLabel = UILabel()
    var deviceDropDown = DropDown()
    var deviceDropDownLabel = UILabel()
    var modelDropdownLabel = UILabel()
    var imageInputLabel = UILabel()
    var threadLabel = UILabel()
    var deviceDropDownView = UIView()
    var modelDropDownView = UIView()
    var ddView = UIView()
    var modelDropDown = DropDown()
    var serverDropDown = DropDown()
    var dropDownWidth: NSLayoutConstraint!
    var serverDropDownLabel = UILabel()
    var serverDropDownView = UIView()
    var bluetoothIcon = UIImageView()
    var preferencesManager : SharedPreferencesManager = SharedPreferencesManager()

    /// Overriding the init function of UIView
    ///
    /// - Parameter frame: frame of View
    override init(frame: CGRect) {
        super.init(frame: frame)
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
        addSubview(createLabel(text: Strings.server, leadingAnchor: Int(adapted(dimensionSize: 20, to: .height)), topAnchor: Int(adapted(dimensionSize: 50, to: .height))))
        addSubview(createLabel(text: Strings.model, leadingAnchor: Int(adapted(dimensionSize: 20, to: .height)), topAnchor: Int(adapted(dimensionSize: 90, to: .height))))
        addSubview(createLabel(text: Strings.speed, leadingAnchor: Int(adapted(dimensionSize: 20, to: .height)), topAnchor: Int(adapted(dimensionSize: 120, to: .height))))
        setupSpeed()
        addSubview(createLabel(text: Strings.device, leadingAnchor: Int(adapted(dimensionSize: 20, to: .height)), topAnchor: Int(adapted(dimensionSize: 160, to: .height))))
        createDeviceDropDown()
        createServerDropDown()
        serverDropDown.hide()
        createModelDropDown()
        addSubview(createLabel(text: Strings.input, leadingAnchor: 180, topAnchor: Int(adapted(dimensionSize: 120, to: .height))))
        setupInput();
        addSubview(createLabel(text: Strings.threads, leadingAnchor: 180, topAnchor: Int(adapted(dimensionSize: 160, to: .height))))
        setupThreads();
        setupVehicleControls()
        createLeftSpeed()
        if let device = preferencesManager.getDevice(){
            deviceDropDownLabel.text = device;
        }
        if let threads = preferencesManager.getThreads(){
            threadLabel.text = threads;
        }
        if let autopilotModel = preferencesManager.getAutopilotModel(){
            if Common.isModelItemAvailableInDocument(modelName: autopilotModel) == true {
                let selectedModel = autopilotModel;
                modelDropdownLabel.text = selectedModel
                let models = loadAllAutoPilotModels()
                for model in models {
                    guard let index = model.name.firstIndex(of: ".") else {
                        return
                    }
                    if model.name.prefix(upTo: index) == selectedModel {
                        imageInputLabel.text = model.inputSize
                        break
                    }
                }
            }
            else{
                preferencesManager.setAutopilotModel(value: "");
            }
        }
        NotificationCenter.default.addObserver(self, selector: #selector(updateModel), name: .updateModel, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateDevice), name: .updateDevice, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateThreadLabel), name: .updateThreadLabel, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothConnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothDisconnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateSpeedLabel), name: .updateSpeedLabel, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(toggleNetwork), name: .toggleNetworks, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateFps), name: .updateAutoPilotFps, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateServer), name: .server, object: nil);
    }

    /// Initialization routine
    ///
    /// - Parameter aDecoder:
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    /// Method called when we touch up or down of UIView. It will change the origin of UIView
    ///
    /// - Parameter gesture:
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
                        self.frame.origin.y = height - 375;
                    } else {
                        self.frame.origin.y = adapted(dimensionSize: 5, to: .height)
                    }
                }
            default:
                break
            }
        }
    }

    /// creating a blue color horizontal line inside view
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

    /// creating  bluetooth icon to load bluetooth screen
    func createBluetoothIcon() {
        let bluetoothIconView = UIView();
        bluetoothIconView.frame.origin = CGPoint(x: width - adapted(dimensionSize: 85, to: .height), y: adapted(dimensionSize: 20, to: .height));
        bluetoothIconView.frame.size = CGSize(width: 40, height: 50);
        addSubview(bluetoothIconView);
        if (isBluetoothConnected) {
            bluetoothIcon = createIcons(iconImg: Images.bluetoothConnected_v2!, x: 0, y: 5, size: resized(size: Images.bluetoothConnected_v2!.size, basedOn: Dimension.height), backgroundColor: Colors.title ?? .blue)
        } else {
            bluetoothIcon = createIcons(iconImg: Images.bluetoothDisconnected_v2!, x: 0, y: 5, size: resized(size: Images.bluetoothDisconnected_v2!.size, basedOn: Dimension.height), backgroundColor: Colors.title ?? .blue)
        }
        bluetoothIconView.addSubview(bluetoothIcon)
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(ble(_:)))
        bluetoothIconView.isUserInteractionEnabled = true
        bluetoothIconView.addGestureRecognizer(tapGesture)
    }

    /// creating camera icon to switch camera
    func createCameraIcon() {
        if let image = Images.frontCamera {
            let cameraIconView = UIView();
            cameraIconView.frame.origin = CGPoint(x: width - adapted(dimensionSize: 50, to: .width), y: adapted(dimensionSize: 20, to: .height));
            cameraIconView.frame.size = CGSize(width: 50, height: 50);
            addSubview(cameraIconView)
            let camera = createIcons(iconImg: image, x: 5, y: 5, size: resized(size: image.size, basedOn: Dimension.height), backgroundColor: Colors.title ?? .blue)
            cameraIconView.addSubview(camera);
            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(switchCamera(_:)))
            cameraIconView.isUserInteractionEnabled = true
            cameraIconView.addGestureRecognizer(tapGesture)
        }
    }

    /// function to create text
    ///
    /// - Parameters:
    ///   - text: String
    ///   - leadingAnchor: margin from left
    ///   - topAnchor: margin from top
    /// - Returns:
    func createLabel(text: String, leadingAnchor: Int, topAnchor: Int) -> UILabel {
        let label = UILabel()
        label.text = text
        label.textColor = Colors.border
        label.frame.origin = CGPoint(x: leadingAnchor, y: topAnchor)
        label.frame.size = resized(size: CGSize(width: text.count * 10, height: 40), basedOn: .height)
        return label
    }

    /// function to  create icons
    ///
    /// - Parameters:
    ///   - iconImg: image
    ///   - topAnchor: margin top
    ///   - trailingAnchor: margin Bottonm
    ///   - x: x ordinate of icon
    ///   - y: y ordinate of icon
    ///   - size: size of image
    ///   - backgroundColor:
    ///   - action: method that will called after tapping
    /// - Returns:
    func createIcons(iconImg: UIImage, x: CGFloat, y: CGFloat, size: CGSize, backgroundColor: UIColor) -> UIImageView {
        let iconImage = UIImageView(frame: CGRect(x: x, y: y, width: size.width, height: size.height))
        iconImage.image = iconImg
        iconImage.layer.cornerRadius = 30
        return iconImage
    }

    /// function to create toggle button
    func createSwitchButton() {
        autoModeButton.isOn = false
        autoModeButton.setOn(false, animated: true)
        autoModeButton.onTintColor = Colors.title
        autoModeButton.addTarget(self, action: #selector(switchButton(_:)), for: .valueChanged);
        autoModeButton.translatesAutoresizingMaskIntoConstraints = false
        addSubview(autoModeButton)
        autoModeButton.widthAnchor.constraint(equalToConstant: 40).isActive = true
        autoModeButton.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: 120).isActive = true
        autoModeButton.topAnchor.constraint(equalTo: topAnchor, constant: 25).isActive = true
    }

    /// function to create server dropdown
    func createServerDropDown() {
        serverDropDown.backgroundColor = Colors.freeRoamButtonsColor
        serverDropDown.textColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black;

        serverDropDown.anchorView = serverDropDownView
        serverDropDown.dataSource = servers
        serverDropDown.show()
        ddView = createDropdownView(borderColor: "", buttonName: "No Server", leadingAnchor: 180, topAnchor: adapted(dimensionSize: 50, to: .height), action: #selector(showServerDropdown(_:)))
        serverDropDown.selectionAction = { [unowned self] (index: Int, item: String) in
            serverDropDownLabel.text = item
            let server = serverItems.filter { server in
                server.name == item
            }
            if item == "No Server"{
                ServerCommunication.serverEndPoint = nil;
            }
            if let endpoint = server.first?.endpoint {
                serverConnection = ServerCommunication(endpoint: endpoint)
            };
        }
        serverDropDown.width = 150
        serverDropDownView.frame.size = CGSize(width: 200, height: 100);
        serverDropDownView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(serverDropDownView)
        serverDropDownView.topAnchor.constraint(equalTo: topAnchor, constant: adapted(dimensionSize: 40, to: .height)).isActive = true
        serverDropDownView.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: 180).isActive = true;
        let upwardImage = UIImageView()
        upwardImage.frame.size = CGSize(width: 5, height: 5)
        upwardImage.image = Images.downArrow
        ddView.addSubview(upwardImage)
        upwardImage.translatesAutoresizingMaskIntoConstraints = false
        upwardImage.trailingAnchor.constraint(equalTo: ddView.trailingAnchor, constant: -10).isActive = true
        upwardImage.topAnchor.constraint(equalTo: ddView.topAnchor, constant: 11.5).isActive = true
        serverDropDownLabel.text = "No Server"
        serverDropDownLabel.textColor = Colors.border
        serverDropDownLabel.frame = CGRect(x: 10, y: 0, width: 210, height: 40)
        ddView.addSubview(serverDropDownLabel)
    }

    /// function to create autopilot dropdown
    func createModelDropDown() {
        let selectedModels = Common.loadSelectedModels(mode: Constants.autopilotMode);
        modelDropDown.backgroundColor = Colors.freeRoamButtonsColor
        modelDropDown.textColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black;
        modelDropDown.anchorView = modelDropDownView
        modelDropDown.dataSource = selectedModels
        ddView = createDropdownView(borderColor: "", buttonName: "CLI-Mobile", leadingAnchor: 180, topAnchor: adapted(dimensionSize: 90, to: .height), action: #selector(showModelDropdown(_:)))
        let upwardImage = UIImageView()
        upwardImage.frame.size = CGSize(width: 5, height: 5)
        upwardImage.image = Images.downArrow
        upwardImage.backgroundColor = Colors.freeRoamButtonsColor
        ddView.addSubview(upwardImage)
        upwardImage.translatesAutoresizingMaskIntoConstraints = false
        upwardImage.trailingAnchor.constraint(equalTo: ddView.trailingAnchor, constant: -10).isActive = true
        upwardImage.topAnchor.constraint(equalTo: ddView.topAnchor, constant: 11.5).isActive = true
        modelDropdownLabel.text = "CLI-Mobile"
        modelDropdownLabel.textColor = Colors.border
        modelDropdownLabel.frame = CGRect(x: 10, y: 0, width: 210, height: 40)
        ddView.addSubview(modelDropdownLabel)
        modelDropDown.selectionAction = { [self] (index: Int, item: String) in
            modelDropdownLabel.text = item
            NotificationCenter.default.post(name: .updateModel, object: item)

        }
        modelDropDown.width = 150
        modelDropDownView.frame.size = CGSize(width: 200, height: 100);
        modelDropDownView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(modelDropDownView)
        modelDropDownView.topAnchor.constraint(equalTo: upwardImage.topAnchor, constant: 0).isActive = true
        modelDropDownView.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: 180).isActive = true;
    }

    /// function to create input label frame of model
    func setupInput() {
        imageInputLabel.frame = CGRect(x: width - 80, y: adapted(dimensionSize: 120, to: .height), width: 100, height: 40)
        imageInputLabel.text = "256x96";
        addSubview(imageInputLabel)
    }

    /// function to create the ui of speed
    func setupSpeed() {
        speedLabel = createLabel(text: "*** fps", leadingAnchor: 90, topAnchor: Int(adapted(dimensionSize: 120, to: .height)))
        addSubview(speedLabel)
    }

    /// Create a dropdown list
    func createDeviceDropDown() {
        deviceDropDown.backgroundColor = Colors.freeRoamButtonsColor
        deviceDropDown.textColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black;
        deviceDropDown.anchorView = deviceDropDownView
        deviceDropDown.dataSource = Constants.devices
        deviceDropDown.width = 90
        ddView = createDropdownView(borderColor: "", buttonName: "CPU", leadingAnchor: 80, topAnchor: adapted(dimensionSize: 160, to: .height), action: #selector(showDeviceDropdown(_:)))
        let downwardImage = UIImageView()
        downwardImage.frame.size = CGSize(width: 5, height: 5)
        downwardImage.image = Images.downArrow
        ddView.addSubview(downwardImage)
        downwardImage.translatesAutoresizingMaskIntoConstraints = false
        downwardImage.trailingAnchor.constraint(equalTo: ddView.trailingAnchor, constant: -10).isActive = true
        downwardImage.topAnchor.constraint(equalTo: ddView.topAnchor, constant: 11.5).isActive = true
        deviceDropDownLabel.text = "CPU"
        deviceDropDownLabel.textColor = Colors.border
        deviceDropDownLabel.frame = CGRect(x: 10, y: 0, width: 60, height: 40)
        dropDownWidth.constant = 100
        ddView.addSubview(deviceDropDownLabel)
        deviceDropDown.selectionAction = { [unowned self] (index: Int, item: String) in
            deviceDropDownLabel.text = item
            NotificationCenter.default.post(name: .updateDevice, object: item)
        }
        deviceDropDownView.frame.size = CGSize(width: 200, height: 100);
        deviceDropDownView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(deviceDropDownView)
        deviceDropDownView.topAnchor.constraint(equalTo: topAnchor, constant: 190).isActive = true
        deviceDropDownView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 91).isActive = true
    }

    /// function to create dropdown
    ///
    /// - Parameters:
    ///   - borderColor:
    ///   - buttonName:
    ///   - leadingAnchor:
    ///   - topAnchor:
    ///   - action:
    /// - Returns:
    func createDropdownView(borderColor: String, buttonName: String, leadingAnchor: CGFloat, topAnchor: CGFloat, action: Selector?) -> UIView {
        let dd = UIView()
        dd.layer.cornerRadius = 10
        dd.backgroundColor = Colors.freeRoamButtonsColor
        let tap = UITapGestureRecognizer(target: self, action: action)
        dd.addGestureRecognizer(tap)
        addSubview(dd)
        dd.translatesAutoresizingMaskIntoConstraints = false
        dd.topAnchor.constraint(equalTo: self.topAnchor, constant: topAnchor).isActive = true;
        dd.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: leadingAnchor).isActive = true
        dropDownWidth = dd.widthAnchor.constraint(equalToConstant: 180)
        dropDownWidth.isActive = true
        dd.heightAnchor.constraint(equalToConstant: 40).isActive = true
        return dd
    }

    /// function to create thread view
    func setupThreads() {

        // setting plus
        let plusImageView = UIView();
        plusImageView.frame.size = CGSize(width: 30, height: 30);
        plusImageView.frame.origin = CGPoint(x: width - 40, y: adapted(dimensionSize: 160, to: .height));
        addSubview(plusImageView)
        let plusImage = UIImageView();
        plusImage.image = UIImage(systemName: "plus");
        plusImage.frame.size = CGSize(width: 20, height: 20);
        plusImage.isUserInteractionEnabled = true;
        let tapOnPlus = UITapGestureRecognizer(target: self, action: #selector(increaseThreads(_:)))
        plusImageView.addGestureRecognizer(tapOnPlus)
        plusImageView.addSubview(plusImage)
        plusImage.translatesAutoresizingMaskIntoConstraints = false
        plusImage.leadingAnchor.constraint(equalTo: plusImageView.leadingAnchor, constant: 5.5).isActive = true
        plusImage.topAnchor.constraint(equalTo: plusImageView.topAnchor, constant: 10).isActive = true

        //setting minus
        let minusImageView = UIView()
        minusImageView.frame.size = CGSize(width: 30, height: 30);
        minusImageView.frame.origin = CGPoint(x: width - 90, y: adapted(dimensionSize: 160, to: .height))
        addSubview(minusImageView)
        let minusImage = UIImageView()
        minusImage.image = UIImage(systemName: "minus");
        minusImage.frame.size = CGSize(width: 30, height: 30);
        minusImage.isUserInteractionEnabled = true;
        minusImageView.addSubview(minusImage)
        let tapOnMinus = UITapGestureRecognizer(target: self, action: #selector(decreaseThreads(_:)))
        minusImageView.addGestureRecognizer(tapOnMinus)
        minusImage.translatesAutoresizingMaskIntoConstraints = false
        minusImage.leadingAnchor.constraint(equalTo: minusImageView.leadingAnchor, constant: 5.5).isActive = true
        minusImage.topAnchor.constraint(equalTo: minusImageView.topAnchor, constant: 10).isActive = true

        // thread Label
        threadLabel.frame.size = CGSize(width: 10, height: 40);
        addSubview(threadLabel);
        threadLabel.translatesAutoresizingMaskIntoConstraints = false
        threadLabel.text = "1";
        threadLabel.textColor = Colors.border
        threadLabel.translatesAutoresizingMaskIntoConstraints = false
        threadLabel.leadingAnchor.constraint(equalTo: minusImageView.trailingAnchor, constant: 4).isActive = true
        threadLabel.topAnchor.constraint(equalTo: minusImageView.topAnchor, constant: 8).isActive = true
    }

    /// function to create game control mode and speed view using vehicleControl class
    func setupVehicleControls() {
        let vehicleControls = VehicleControl();
        addSubview(vehicleControls)
        vehicleControls.translatesAutoresizingMaskIntoConstraints = false
        vehicleControls.topAnchor.constraint(equalTo: threadLabel.safeAreaLayoutGuide.bottomAnchor, constant: adapted(dimensionSize: 10, to: .height)).isActive = true;
        vehicleControls.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: 7).isActive = true
    }

    /// function to create speed text
    func createLeftSpeed() {
        leftSpeedLabel.frame.size = CGSize(width: 100, height: 40);
        leftSpeedLabel.frame.origin = CGPoint(x: 20, y: adapted(dimensionSize: 200, to: .height))
        leftSpeedLabel.text = "xxx,xxx"
        addSubview(leftSpeedLabel)
        leftSpeedLabel.font = leftSpeedLabel.font.withSize(13.5)
    }

    /// function called after get connected or disconnected from bluetooth
    ///
    /// - Parameter sender:
    @objc func ble(_ sender: UIView) {
        NotificationCenter.default.post(name: .ble, object: nil)
    }

    /// function to switch camera from front to  back and from back to front
    ///
    /// - Parameter sender:
    @objc func switchCamera(_ sender: UIView) {
        NotificationCenter.default.post(name: .switchCamera, object: nil)
    }

    /// functon to on off auto pilot
    ///
    /// - Parameter sender:
    @objc func switchButton(_ sender: UISwitch) {
        NotificationCenter.default.post(name: .autoMode, object: nil)
    }

    /// function to show server dropdown
    ///
    /// - Parameter sender:
    @objc func showServerDropdown(_ sender: UIButton) {
        serverDropDown.show()
    }

    /// function called after touching model to show model dropdown
    ///
    /// - Parameter sender:
    @objc func showModelDropdown(_ sender: UIButton) {
        modelDropDown.show()
    }

    /// fuction called after touching device menu to show device dropdown
    ///
    /// - Parameter sender:
    @objc func showDeviceDropdown(_ sender: UIButton) {
        deviceDropDown.show()
    }

    /// function to increase number of threads
    ///
    /// - Parameter sender:
    @objc func increaseThreads(_ sender: UIImage) {
        if threadLabel.text == "9" || threadLabel.text == "N/A" {
            return
        }
        var value = Int(threadLabel.text ?? "1")
        value = (value ?? 1) + 1;
        threadLabel.text = String(value!)
        NotificationCenter.default.post(name: .updateThread, object: threadLabel.text)
    }

    /// function to decrease number of threads
    ///
    /// - Parameter sender:
    @objc func decreaseThreads(_ sender: UIImage) {
        if threadLabel.text == "1" || threadLabel.text == "N/A" {
            return
        }
        var value = Int(threadLabel.text ?? "1")
        value = (value ?? 1) - 1;
        threadLabel.text = String(value!)
        NotificationCenter.default.post(name: .updateThread, object: threadLabel.text)
    }

    /// function to loads all models from Bundle
    ///
    /// - Returns:
    func loadModels() -> [ModelItem] {
        if let url = Bundle.main.url(forResource: "config", withExtension: "json") {
            do {
                let data = try Data(contentsOf: url)
                let decoder = JSONDecoder()
                let jsonData = try decoder.decode([ModelItem].self, from: data)
                return jsonData;
            } catch {
                print("error:\(error)")
            }
        }
        return [];
    }

    /// function returns all models of autopilot
    ///
    /// - Returns: all models of autopilot
    func loadAllAutoPilotModels() -> [ModelItem] {
        var autoPilot: [ModelItem] = []
        let allModels = loadModels()
        for model in allModels {
            if model.type == Constants.autopilotMode {
                autoPilot.append(model)
            }
        }
        return autoPilot
    }

    /// function called after selecting model from model dropdown
    ///
    /// - Parameter notification:
    @objc func updateModel(_ notification: Notification) {
        let selectedModel = notification.object as! String
        modelDropdownLabel.text = selectedModel
        let models = loadAllAutoPilotModels()
        for model in models {
            guard let index = model.name.firstIndex(of: ".") else {
                return
            }
            if model.name.prefix(upTo: index) == selectedModel {
                imageInputLabel.text = model.inputSize
                break
            }
        }
    }

    /// function called after selecting device from device dropdown to change device from CPU to xnnpack and vice versa
    ///
    /// - Parameter notification:
    @objc func updateDevice(_ notification: Notification) {
        let selectedDevice = notification.object as! String
        deviceDropDownLabel.text = selectedDevice
    }

    /// function to update the thread count on screen
    ///
    /// - Parameter notification:
    @objc func updateThreadLabel(_ notification: Notification) {
        threadLabel.text = (notification.object as! String)

    }

    /// function to change the icon of bluetooth when bluetooth is connected or disconnected
    ///
    /// - Parameter notification:
    @objc func updateConnect(_ notification: Notification) {
        if (isBluetoothConnected) {
            bluetoothIcon.image = Images.bluetoothConnected_v2
        } else {
            bluetoothIcon.image = Images.bluetoothDisconnected_v2
        }
    }

    /// function to change the speed on screen
    ///
    /// - Parameter notification:
    @objc func updateSpeedLabel(_ notification: Notification) {
        leftSpeedLabel.text = (notification.object as! String)
    }

    /// callback to toggle autopilot when network command is received from web controller
    ///
    /// - Parameter notification:
    @objc func toggleNetwork(_ notification: Notification) {
        autoModeButton.setOn(!autoModeButton.isOn, animated: true)
        switchButton(autoModeButton)
    }

    /// function that show current fps
    ///
    /// - Parameter notification:
    @objc func updateFps(_ notification: Notification) {
        speedLabel.text = String(notification.object as! Double)
    }

    @objc func updateServer(_ notification: Notification){
        serverDropDown.dataSource = servers;
    }
}

