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
    /// Kept alive only long enough to trigger CoreBluetooth's system permission prompt on first ask.
    var bluetoothPermissionProbe: CBCentralManager?
    let flagLabel = UILabel()
    let sharedPreferences = SharedPreferencesManager()

    /// Called after the view fragment has loaded.
    override func viewDidLoad() {
        super.viewDidLoad()
        locationManager.delegate = self
        createScrollView()
        scrollView.contentSize = CGSize(width: width, height: height)

        scrollView.addSubview(createSectionHeader(text: Strings.general, topAnchor: adapted(dimensionSize: 10, to: .height)))
        createLanguageRow()

        scrollView.addSubview(createSectionHeader(text: Strings.permission, topAnchor: adapted(dimensionSize: 110, to: .height)))
        setupSwitchPositions()
        scrollView.addSubview(createLabel(text: Strings.camera, leadingAnchor: 40, topAnchor: adapted(dimensionSize: 150, to: .height)))
        createCameraSwitch()
        createPermissionIcon(systemName: "camera.fill", topAnchor: adapted(dimensionSize: 150, to: .height))
        scrollView.addSubview(createLabel(text: Strings.location, leadingAnchor: 40, topAnchor: adapted(dimensionSize: 200, to: .height)))
        createLocationSwitch()
        createPermissionIcon(systemName: "location.fill", topAnchor: adapted(dimensionSize: 200, to: .height))
        scrollView.addSubview(createLabel(text: Strings.microphone, leadingAnchor: 40, topAnchor: adapted(dimensionSize: 250, to: .height)))
        createMicrophoneSwitch()
        createPermissionIcon(systemName: "mic.fill", topAnchor: adapted(dimensionSize: 250, to: .height))
        scrollView.addSubview(createLabel(text: Strings.bluetooth, leadingAnchor: 40, topAnchor: adapted(dimensionSize: 300, to: .height)))
        createBluetoothSwitch()
        createPermissionIcon(image: Images.bluetooth, tinted: false, topAnchor: adapted(dimensionSize: 300, to: .height))
        scrollView.addSubview(createLabel(text: Strings.webSignalingServer, leadingAnchor: 40, topAnchor: adapted(dimensionSize: 350, to: .height)))
        createWebSignalingServerField()
        updateSwitchPosition()
        registerKeyboardNotifications()
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
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
        flagLabel.text = LocalizationManager.currentFlagEmoji
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

    /// Creates a bold section header label (e.g. "General", "Permissions").
    func createSectionHeader(text: String, topAnchor: CGFloat) -> UILabel {
        let header = createLabel(text: text, leadingAnchor: 20, topAnchor: topAnchor)
        header.font = UIFont.boldSystemFont(ofSize: 20.0)
        header.frame.size.width = measuredWidth(of: text, font: header.font) + 8
        return header
    }

    /// Creates the tappable "Language" row: title + subtitle on the left, current flag on the right.
    func createLanguageRow() {
        let rowTop = adapted(dimensionSize: 50, to: .height)
        // Leaves room for the flag on the right; wraps instead of relying on a measured single-line
        // width, since translated title/subtitle text varies too much in length to assume one line.
        let textWidth = width - 40 - 90

        let title = UILabel()
        title.text = Strings.language
        title.textColor = Colors.border
        title.font = UIFont.systemFont(ofSize: 17.0)
        title.numberOfLines = 1
        title.frame = CGRect(x: 40, y: rowTop, width: textWidth, height: 22)
        scrollView.addSubview(title)

        let subtitle = UILabel()
        subtitle.text = Strings.languageSubtitle
        subtitle.textColor = Colors.textColor
        subtitle.font = UIFont.systemFont(ofSize: 13.0)
        subtitle.numberOfLines = 2
        subtitle.frame = CGRect(x: 40, y: rowTop + 22, width: textWidth, height: 32)
        scrollView.addSubview(subtitle)

        flagLabel.text = LocalizationManager.currentFlagEmoji
        flagLabel.font = UIFont.systemFont(ofSize: 24.0)
        flagLabel.frame = CGRect(x: width - 70, y: rowTop, width: 40, height: 40)
        scrollView.addSubview(flagLabel)

        let tapArea = UIView(frame: CGRect(x: 0, y: rowTop - 10, width: width, height: 70))
        tapArea.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(openLanguagePicker)))
        scrollView.addSubview(tapArea)
    }

    @objc func openLanguagePicker() {
        let picker = LanguagePickerViewController()
        present(picker, animated: true)
    }

    /// Creates a new label with the given text, leading anchor, and top anchor.
    func createLabel(text: String, leadingAnchor: CGFloat, topAnchor: CGFloat) -> UILabel {
        let label = UILabel()
        label.text = text;
        label.textColor = Colors.border
        label.font = UIFont.systemFont(ofSize: 15.0)
        label.frame.origin = CGPoint(x: leadingAnchor, y: topAnchor)
        label.frame.size = resized(size: CGSize(width: measuredWidth(of: text, font: label.font) + 8, height: 40), basedOn: .height)
        return label;

    }

    /// Creates the SF Symbol icon shown to the left of a permission's switch.
    func createPermissionIcon(systemName: String, topAnchor: CGFloat) {
        createPermissionIcon(image: UIImage(systemName: systemName), tinted: true, topAnchor: topAnchor)
    }

    /// Creates a permission row icon from a bundled image asset
    func createPermissionIcon(image: UIImage?, tinted: Bool, topAnchor: CGFloat) {
        let icon = UIImageView(image: image)
        if tinted {
            icon.tintColor = Colors.border
        }
        icon.contentMode = .scaleAspectFit
        icon.frame = CGRect(x: switchButtonTrailingAnchor - 40, y: topAnchor + 8, width: 24, height: 24)
        scrollView.addSubview(icon)
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
        cameraSwitch.frame.origin = CGPoint(x: width - 80, y: adapted(dimensionSize: 150, to: .height))
        cameraSwitch.addTarget(self, action: #selector(toggleCamera(_:)), for: .valueChanged)
    }

    /// creates a switch for switching location setting
    func createLocationSwitch() {
        locationSwitch.onTintColor = Colors.title
        scrollView.addSubview(locationSwitch)
        locationSwitch.frame.origin = CGPoint(x: width - 80, y: adapted(dimensionSize: 200, to: .height))
        locationSwitch.addTarget(self, action: #selector(toggleLocation(_:)), for: .valueChanged)
    }

    /// creates a switch for switching microphone setting
    func createMicrophoneSwitch() {
        microphoneSwitch.onTintColor = Colors.title
        scrollView.addSubview(microphoneSwitch)
        microphoneSwitch.frame.origin = CGPoint(x: width - 80, y: adapted(dimensionSize: 250, to: .height))
        microphoneSwitch.addTarget(self, action: #selector(toggleMicrophone(_:)), for: .valueChanged)
    }

    /// creates a switch for switching bluetooth setting
    func createBluetoothSwitch() {
        bluetoothSwitch.onTintColor = Colors.title
        scrollView.addSubview(bluetoothSwitch)
        bluetoothSwitch.frame.origin = CGPoint(x: width - 80, y: adapted(dimensionSize: 300, to: .height))
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
        webSignalingServerField.frame = CGRect(x: 40, y: adapted(dimensionSize: 380, to: .height), width: width - 80, height: 40)
        scrollView.addSubview(webSignalingServerField)
    }

    /// persists the web signaling server URL when the user finishes editing
    func textFieldDidEndEditing(_ textField: UITextField) {
        guard textField == webSignalingServerField, let text = textField.text, !text.isEmpty else { return }
        sharedPreferences.setWebSignalingServerUrl(value: text)
    }

    /// dismisses the keyboard when the user taps return
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }

    /// listens for the keyboard so the scroll view can be nudged out of its way
    func registerKeyboardNotifications() {
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(_:)), name: UIResponder.keyboardWillHideNotification, object: nil)
    }

    /// pushes the scroll view's bottom inset up by the keyboard's height and scrolls the field into view
    @objc func keyboardWillShow(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
              let keyboardFrame = (userInfo[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue else { return }
        let keyboardHeight = scrollView.convert(keyboardFrame, from: nil).height
        scrollView.contentInset.bottom = keyboardHeight
        scrollView.scrollIndicatorInsets.bottom = keyboardHeight
        scrollView.scrollRectToVisible(webSignalingServerField.frame.insetBy(dx: 0, dy: -20), animated: true)
    }

    /// restores the scroll view's original insets once the keyboard is dismissed
    @objc func keyboardWillHide(_ notification: Notification) {
        scrollView.contentInset.bottom = 0
        scrollView.scrollIndicatorInsets.bottom = 0
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
            createAllowAlert(alertFor: Strings.camera)
        case .denied:
            createAllowAlert(alertFor: Strings.camera)
        case .notDetermined:
            createPromptForCameraAccess()
        default:
            alertToEncourageCameraAccessInitially()
        }
    }

    /// function to check whether location is allowed or not, requesting it directly the first time it's asked.
    func checkLocation() {
        var authStatus: CLAuthorizationStatus
        if #available(iOS 14.0, *) {
            authStatus = locationManager.authorizationStatus
        } else {
            authStatus = CLLocationManager.authorizationStatus()
        }
        switch authStatus {
        case .notDetermined:
            locationManager.requestWhenInUseAuthorization()
        default:
            createAllowAlert(alertFor: Strings.location)
        }
    }

    /// function to check whether microphone is allowed or not, requesting it directly the first time it's asked.
    func checkMicrophone() {
        switch AVAudioSession.sharedInstance().recordPermission {
        case .undetermined:
            AVAudioSession.sharedInstance().requestRecordPermission { _ in
                DispatchQueue.main.async {
                    self.toggleSwitchButtons()
                }
            }
        default:
            createAllowAlert(alertFor: Strings.microphone)
        }
    }

    ///function to check whether bluetooth is allowed or not, requesting it directly the first time it's asked.
    func checkBluetooth() {
        switch CBCentralManager.authorization {
        case .notDetermined:
            // Instantiating a CBCentralManager triggers CoreBluetooth's system permission prompt.
            bluetoothPermissionProbe = CBCentralManager()
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                self.toggleSwitchButtons()
                self.bluetoothPermissionProbe = nil
            }
        default:
            createAllowAlert(alertFor: Strings.bluetooth)
        }
    }

    /// function to show the camera alert on the screen.
    func alertToEncourageCameraAccessInitially() {
        let alert = UIAlertController(
                title: Strings.important,
                message: Strings.cameraAccessRequiredMessage,
                preferredStyle: UIAlertController.Style.alert
        )
        alert.addAction(UIAlertAction(title: Strings.cancel, style: .default, handler: { action in print(action) }))
        alert.addAction(UIAlertAction(title: String(format: Strings.allowButtonFormat, Strings.camera), style: .cancel, handler: { (alert) -> Void in
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
                title: Strings.important,
                message: String(format: Strings.allowPermissionMessageFormat, alertFor),
                preferredStyle: UIAlertController.Style.alert
        )
        alert.addAction(UIAlertAction(title: Strings.cancel, style: .default, handler: { action in self.toggleSwitchButtons() }))
        alert.addAction(UIAlertAction(title: String(format: Strings.allowButtonFormat, alertFor), style: .cancel, handler: { (alert) -> Void in
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
