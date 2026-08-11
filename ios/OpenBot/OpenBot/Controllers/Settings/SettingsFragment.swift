//
// Created by Nitish Yadav on 02/11/22.
//

import Foundation
import UIKit
import AVFoundation
import CoreLocation
import CoreLocationUI
import CoreBluetooth

class SettingsFragment: UIViewController, CLLocationManagerDelegate, UITextFieldDelegate {
    var scrollView: UIScrollView!
    var cameraSwitch = UISwitch()
    let locationSwitch = UISwitch()
    let microphoneSwitch = UISwitch()
    let bluetoothSwitch = UISwitch()
    let webSignalingServerField = UITextField()
    var switchButtonTrailingAnchor = width - 80;
    let locationManager = CLLocationManager()
    let sharedPreferences = SharedPreferencesManager()

    /// Called after the view fragment has loaded.
    override func viewDidLoad() {
        super.viewDidLoad()
        createScrollView()
        createPermissionLabel()
        setupSwitchPositions()
        scrollView.contentSize = CGSize(width: width, height: height)
        scrollView.addSubview(createLabel(text: Strings.camera, leadingAnchor: 40, topAnchor: adapted(dimensionSize: 50, to: .height)))
        createCameraSwitch()
        scrollView.addSubview(createLabel(text: Strings.location, leadingAnchor: 40, topAnchor: adapted(dimensionSize: 100, to: .height)))
        createLocationSwitch()
        scrollView.addSubview(createLabel(text: Strings.microphone, leadingAnchor: 40, topAnchor: adapted(dimensionSize: 150, to: .height)))
        createMicrophoneSwitch()
        scrollView.addSubview(createLabel(text: Strings.bluetooth, leadingAnchor: 40, topAnchor: adapted(dimensionSize: 200, to: .height)))
        createBluetoothSwitch()
        scrollView.addSubview(createLabel(text: Strings.webSignalingServer, leadingAnchor: 40, topAnchor: adapted(dimensionSize: 250, to: .height)))
        createWebSignalingServerField()
        updateSwitchPosition()
    }

    /// Called when the view controller's view's size is changed by its parent (i.e. for the root view controller when its window rotates or is resized).
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
        setupSwitchPositions()
        updateScrollView()
        updateSwitchPosition()
    }

    /// Initialization routine
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        toggleSwitchButtons()

    }

    /// Creates a new scroll view with dimensions equal to the main screen size.
    func createScrollView() {
        scrollView = UIScrollView(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: UIScreen.main.bounds.height));
        scrollView.contentSize = CGSize(width: view.frame.width, height: view.frame.height)
        view.addSubview(scrollView)
    }

    /// Updates the dimensions of the scroll view based on the current orientation of the device.
    func updateScrollView() {
        if currentOrientation == .portrait {
            scrollView.frame.size.width = width
            scrollView.frame.size.height = height
        } else {
            scrollView.frame.size.width = height
            scrollView.frame.size.height = width
        }
    }

    /// Creates a label for permissions Heading.
    func createPermissionLabel() {
        let permission = createLabel(text: Strings.permission, leadingAnchor: 40, topAnchor: adapted(dimensionSize: 10, to: .height));
        permission.font = UIFont.systemFont(ofSize: 17.0)
        scrollView.addSubview(permission);
    }

    /// Creates a new label with the given text, leading anchor, and top anchor.
    func createLabel(text: String, leadingAnchor: CGFloat, topAnchor: CGFloat) -> UILabel {
        let label = UILabel()
        label.text = text;
        label.textColor = Colors.border
        label.font = UIFont.systemFont(ofSize: 15.0)
        label.frame.origin = CGPoint(x: leadingAnchor, y: topAnchor)
        label.frame.size = resized(size: CGSize(width: text.count * 12, height: 40), basedOn: .height)
        return label;

    }

    /// Creates a new switch button.
    func createSwitchButton() -> UISwitch {
        let switchButton = UISwitch()
        return switchButton;
    }

    /// Creates a new switch button for the camera and adds it to the scroll
    func createCameraSwitch() {
        cameraSwitch.onTintColor = Colors.title
        scrollView.addSubview(cameraSwitch)
        cameraSwitch.frame.origin = CGPoint(x: width - 80, y: adapted(dimensionSize: 50, to: .height))
        cameraSwitch.addTarget(self, action: #selector(toggleCamera(_:)), for: .valueChanged)
    }

    /// creates a switch for switching location setting
    func createLocationSwitch() {
        locationSwitch.onTintColor = Colors.title
        scrollView.addSubview(locationSwitch)
        locationSwitch.frame.origin = CGPoint(x: width - 80, y: adapted(dimensionSize: 100, to: .height))
        locationSwitch.addTarget(self, action: #selector(toggleLocation(_:)), for: .valueChanged)
    }

    /// creates a switch for switching microphone setting
    func createMicrophoneSwitch() {
        microphoneSwitch.onTintColor = Colors.title
        scrollView.addSubview(microphoneSwitch)
        microphoneSwitch.frame.origin = CGPoint(x: width - 80, y: adapted(dimensionSize: 150, to: .height))
        microphoneSwitch.addTarget(self, action: #selector(toggleMicrophone(_:)), for: .valueChanged)
    }

    /// creates a switch for switching bluetooth setting
    func createBluetoothSwitch() {
        bluetoothSwitch.onTintColor = Colors.title
        scrollView.addSubview(bluetoothSwitch)
        bluetoothSwitch.frame.origin = CGPoint(x: width - 80, y: adapted(dimensionSize: 200, to: .height))
        bluetoothSwitch.addTarget(self, action: #selector(toggleBluetooth(_:)), for: .valueChanged)
    }

    /// creates a text field for the web signaling server URL, prefilled from stored settings
    func createWebSignalingServerField() {
        webSignalingServerField.text = sharedPreferences.getWebSignalingServerUrl()
        webSignalingServerField.placeholder = Strings.webSignalingServerPlaceholder
        webSignalingServerField.borderStyle = .roundedRect
        webSignalingServerField.autocapitalizationType = .none
        webSignalingServerField.autocorrectionType = .no
        webSignalingServerField.delegate = self
        webSignalingServerField.frame = CGRect(x: 40, y: adapted(dimensionSize: 280, to: .height), width: width - 80, height: 40)
        scrollView.addSubview(webSignalingServerField)
    }

    /// persists the web signaling server URL when the user finishes editing
    func textFieldDidEndEditing(_ textField: UITextField) {
        guard textField == webSignalingServerField, let text = textField.text, !text.isEmpty else { return }
        sharedPreferences.setWebSignalingServerUrl(value: text)
    }

    /// function to set the positions of the buttons.
    func setupSwitchPositions() {
        switch (currentOrientation) {
        case .unknown:
            switchButtonTrailingAnchor = width - 80;
        case .portrait:
            switchButtonTrailingAnchor = width - 80;
        case .portraitUpsideDown:
            switchButtonTrailingAnchor = width - 80;
        case .landscapeLeft:
            switchButtonTrailingAnchor = height - 80;
        case .landscapeRight:
            switchButtonTrailingAnchor = height - 80;
        @unknown default:
            switchButtonTrailingAnchor = width - 80;
        }
    }

    /// function to update the positions of the switches
    func updateSwitchPosition() {
        cameraSwitch.frame.origin.x = switchButtonTrailingAnchor
        locationSwitch.frame.origin.x = switchButtonTrailingAnchor
        microphoneSwitch.frame.origin.x = switchButtonTrailingAnchor
        bluetoothSwitch.frame.origin.x = switchButtonTrailingAnchor
    }

    /// Toggles the camera switch button.
    @objc func toggleCamera(_ sender: UISwitch) {
        checkCamera()
    }

    /// Toggles the location switch button.
    @objc func toggleLocation(_ sender: UISwitch) {
        checkLocation()
    }

    /// Toggles the location switch button.
    @objc func toggleMicrophone(_ sender: UISwitch) {
        checkMicrophone()
    }

    /// Toggles the Bluetooth switch button.
    @objc func toggleBluetooth(_ sender: UISwitch) {
        checkBluetooth()
    }

    /// function to check whether the camera is authorized or not.
    func checkCamera() {
        let authStatus = AVCaptureDevice.authorizationStatus(for: AVMediaType.video)
        switch authStatus {
        case .authorized:
            createAllowAlert(alertFor: "Camera")
        case .denied:
            createAllowAlert(alertFor: "Camera")
        case .notDetermined:
            createPromptForCameraAccess()
        default:
            alertToEncourageCameraAccessInitially()
        }
    }

    /// function to check whether location is allowed or not.
    func checkLocation() {
        createAllowAlert(alertFor: "Location")
    }

    /// function to check whether microphone is allowed or not
    func checkMicrophone() {
        createAllowAlert(alertFor: "Microphone")
    }

    ///function to check whether bluetooth is allowed or not
    func checkBluetooth() {
        createAllowAlert(alertFor: "Bluetooth")
    }

    /// function to show the camera alert on the screen.
    func alertToEncourageCameraAccessInitially() {
        let alert = UIAlertController(
                title: "IMPORTANT",
                message: "Camera access required for OpenBot",
                preferredStyle: UIAlertController.Style.alert
        )
        alert.addAction(UIAlertAction(title: "Cancel", style: .default, handler: { action in print(action) }))
        alert.addAction(UIAlertAction(title: "Allow Camera", style: .cancel, handler: { (alert) -> Void in
            guard let url = URL(string: UIApplication.openSettingsURLString), !url.absoluteString.isEmpty else {
                return
            }
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
        }))
        present(alert, animated: true, completion: nil)

    }

    /// function to prompt camera popup
    func createPromptForCameraAccess() {
        let discoverySession = AVCaptureDevice.DiscoverySession(deviceTypes: [.builtInWideAngleCamera], mediaType: .video, position: .unspecified)
        if discoverySession.devices.count > 0 {
            AVCaptureDevice.requestAccess(for: AVMediaType.video) { granted in
                DispatchQueue.main.async {
                }
            }
        }
    }

    /// function to create prompts for settings
    func createAllowAlert(alertFor: String) {
        let alert = UIAlertController(
                title: "IMPORTANT",
                message: "Please allow " + alertFor + " access for OpenBot",
                preferredStyle: UIAlertController.Style.alert
        )
        alert.addAction(UIAlertAction(title: "Cancel", style: .default, handler: { action in self.toggleSwitchButtons() }))
        alert.addAction(UIAlertAction(title: "Allow " + alertFor, style: .cancel, handler: { (alert) -> Void in
            guard let url = URL(string: UIApplication.openSettingsURLString), !url.absoluteString.isEmpty else {
                return
            }
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
        }))
        present(alert, animated: true, completion: nil)
    }

    /// function to change the status of the switch buttons after checking the status of different settings.
    func toggleSwitchButtons() {

        // Camera
        let cameraAuthStatus = AVCaptureDevice.authorizationStatus(for: AVMediaType.video)
        switch cameraAuthStatus {
        case .authorized:
            cameraSwitch.isOn = true
        case .denied:
            cameraSwitch.isOn = false
        case .notDetermined:
            cameraSwitch.isOn = false
        default:
            cameraSwitch.isOn = false
        }

        // Location
        let locationManager = CLLocationManager()
        var locationAuthStatus: CLAuthorizationStatus
        if #available(iOS 14.0, *) {
            locationAuthStatus = locationManager.authorizationStatus
        } else {
            locationAuthStatus = CLLocationManager.authorizationStatus()
        }
        switch locationAuthStatus {
        case .notDetermined:
            locationSwitch.isOn = false
        case .restricted:
            locationSwitch.isOn = false
        case .denied:
            locationSwitch.isOn = false
        case .authorizedAlways:
            locationSwitch.isOn = true
        case .authorizedWhenInUse:
            locationSwitch.isOn = true
        @unknown default:
            locationSwitch.isOn = false
        }

        // Microphone
        switch AVAudioSession.sharedInstance().recordPermission {

        case .granted:
            microphoneSwitch.isOn = true
        case .denied:
            microphoneSwitch.isOn = false
        case .undetermined:
            microphoneSwitch.isOn = true
            AVAudioSession.sharedInstance().requestRecordPermission({ granted in
                self.toggleSwitchButtons()
                self.microphoneSwitch.isOn = true
            })
        @unknown default:
            microphoneSwitch.isOn = false
        }

        // Bluetooth
        switch CBCentralManager.authorization {
        case .notDetermined:
            bluetoothSwitch.isOn = false
        case .restricted:
            bluetoothSwitch.isOn = false
        case .denied:
            bluetoothSwitch.isOn = false
        case .allowedAlways:
            bluetoothSwitch.isOn = true
        @unknown default:
            bluetoothSwitch.isOn = false
        }
    }
}
