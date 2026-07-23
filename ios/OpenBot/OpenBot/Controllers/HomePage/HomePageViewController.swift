//
// Created by Nitish Yadav on 17/08/22.
//

import UIKit
import CoreBluetooth
import AVFoundation

let height = max(UIScreen.main.bounds.height, UIScreen.main.bounds.width)
let width = min(UIScreen.main.bounds.height, UIScreen.main.bounds.width)
var currentOrientation: UIInterfaceOrientation = UIInterfaceOrientation.portrait
var isBluetoothConnected = false;
var viewControllerName: String?
let gameController = GameController.shared
let fragmentType = FragmentType.shared
var leadingConstraint = NSLayoutConstraint()
var isClientConnected: Bool = false
let bottomSheet = UIView();
let whiteSheet = UIView(frame: UIScreen.main.bounds)

class HeaderView: UICollectionReusableView {
    var label: UILabel!

    override init(frame: CGRect) {
        super.init(frame: frame)

        label = UILabel(frame: CGRect(x: 0, y: 0, width: frame.size.width, height: frame.size.height + 30))
        label.textAlignment = .left
        label.font = UIFont.boldSystemFont(ofSize: 20) // Change the font to bold and size to 20
        self.addSubview(label)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class HomePageViewController: CameraController, UICollectionViewDataSource, UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    @IBOutlet weak var bluetooth: UIButton!
    @IBOutlet weak var settings: UIButton!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet var modesCollectionView: UICollectionView!;
    let sectionHeaders = ["  General", "  AI"]

    @IBOutlet weak var openCodeWebView: UIView!

    /// Called after the view controller has loaded.
    override func viewDidLoad() {
        super.viewDidLoad()

        UITabBar.appearance().tintColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black;
        bluetoothDataController.shared.startScan()
        DeviceCurrentOrientation.shared.findDeviceOrientation()
        setUpTitle();
        let layout = UICollectionViewFlowLayout();
        layout.collectionView?.layer.shadowColor = Colors.gridShadowColor?.cgColor
        layout.collectionView?.layer.shadowOpacity = 1
        if currentOrientation == .portrait {
            layout.itemSize = resized(size: CGSize(width: width * 0.15, height: width * 0.15), basedOn: dimension)
        } else {
            layout.itemSize = resized(size: CGSize(width: width * 0.15, height: width * 0.15), basedOn: dimension)
        }
        layout.collectionView?.backgroundColor = Colors.voltageDividerColor
        layout.minimumInteritemSpacing = 5
        layout.sectionInset = UIEdgeInsets(top: 0, left: 8, bottom: 0, right: 8)
        layout.minimumLineSpacing = 30
        layout.collectionView?.backgroundColor = Colors.voltageDividerColor

        modesCollectionView.delegate = self
        modesCollectionView.dataSource = self
        modesCollectionView.collectionViewLayout = layout;
        modesCollectionView.register(modesCollectionViewCell.self, forCellWithReuseIdentifier: modesCollectionViewCell.identifier)
        modesCollectionView.register(HeaderView.self, forSupplementaryViewOfKind: UICollectionView.elementKindSectionHeader, withReuseIdentifier: "Header")
        self.view.addSubview(modesCollectionView)

        DeviceCurrentOrientation.shared.findDeviceOrientation()
        changeNavigationColor()
        NotificationCenter.default.addObserver(self, selector: #selector(updateControllerValues), name: NSNotification.Name(rawValue: Strings.controllerConnected), object: nil);
        NotificationCenter.default.post(name: NSNotification.Name(rawValue: Strings.controllerConnected), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothConnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothDisconnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(clientConnected), name: .clientConnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(clientDisconnected), name: .clientDisConnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(googleSignIn), name: .googleSignIn, object: nil)
        DataLogger.shared.deleteZipFileFromDocument();
        gameController.resetControl = true
        fragmentType.currentFragment = "Home";
        setupOpenCodeIcon();
        let msg = JSON.toString(FragmentStatus(FRAGMENT_TYPE: fragmentType.currentFragment));
        client.send(message: msg);
    }

    override func initializeCamera() {
        if isClientConnected {
            super.initializeCamera()
        }
    }

    /// override function when image is captured.
    ///
    /// - Parameters:
    ///   - output:
    ///   - sampleBuffer: image buffer.
    ///   - connection:
    override func captureOutput(_ output: AVFoundation.AVCaptureOutput, didOutput sampleBuffer: CoreMedia.CMSampleBuffer, from connection: AVFoundation.AVCaptureConnection) {
        if isClientConnected {
            super.captureOutput(output, didOutput: sampleBuffer, from: connection)
        }
    }

    func changeNavigationColor() {
        if traitCollection.userInterfaceStyle == .light {
            navigationController?.navigationBar.tintColor = Colors.title
        } else {
            navigationController?.navigationBar.tintColor = UIColor.white

        }
    }

    /// Called when the view controller's view's size is changed by its parent (i.e. for the root view controller when its window rotates or is resized).
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        DeviceCurrentOrientation.shared.findDeviceOrientation()
    }

    /// Initialization routine
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated);
        DeviceCurrentOrientation.shared.findDeviceOrientation()
        viewControllerName = classNameFrom(self)
        if (isBluetoothConnected) {
            bluetooth.setImage(Images.bluetoothConnected, for: .normal)
            bluetoothDataController.shared.sendData(payload: "c" + String(0) + "," + String(0) + "\n")

        } else {
            bluetooth.setImage(Images.bluetoothDisconnected, for: .normal)
        }
        gameController.resetControl = true
    }


    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        peripheral.discoverServices(nil)
    }

    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        print("connection was failed")
    }

    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        print("disconnected from :", peripheral)
    }

    func setUpTitle() {
        titleLabel.text = Strings.OpenBot
        titleLabel.textColor = Colors.title;
    }

    @IBAction func onTapSettings() {
        let openDataSettingView = (storyboard?.instantiateViewController(withIdentifier: "settingScreen"))!
        guard (navigationController?.pushViewController(openDataSettingView, animated: true)) != nil else {
            fatalError("guard failure handling has not been implemented")
        }
    }

    /// Main control update function
    @objc func updateControllerValues() {
        gameController.updateControllerValues()
    }

    override func beginAppearanceTransition(_ isAppearing: Bool, animated: Bool) {
        super.beginAppearanceTransition(isAppearing, animated: animated)

    }

    override func endAppearanceTransition() {
        super.endAppearanceTransition()
    }

    @objc func updateConnect(_ notification: Notification) {
        if (isBluetoothConnected) {
            bluetooth.setImage(Images.bluetoothConnected, for: .normal)
            bluetoothDataController.shared.sendData(payload: "c" + String(0) + "," + String(0) + "\n")
        } else {
            bluetooth.setImage(Images.bluetoothDisconnected, for: .normal)
        }
    }

    @objc func clientConnected(_ notification: Notification) {
        isClientConnected = true;
        initializeCamera()
    }

    @objc func clientDisconnected(_ notification: Notification) {
        isClientConnected = false
        stopSession()
    }

    @objc func googleSignIn(_ notification: Notification) {
        whiteSheet.removeFromSuperview()
    }

    // Number of sections
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return sectionHeaders.count
    }

    // Number of items in section
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return Constants.gameModes[section].count
    }

    // Cell for item at index path
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: modesCollectionViewCell.identifier, for: indexPath) as! modesCollectionViewCell;

        cell.configure(with: Constants.gameModes[indexPath.section][indexPath.row]);

        return cell

    }

    // View for header in section
    func collectionView(_ collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, at indexPath: IndexPath) -> UICollectionReusableView {
        switch kind {
        case UICollectionView.elementKindSectionHeader:
            let headerView = collectionView.dequeueReusableSupplementaryView(ofKind: kind, withReuseIdentifier: "Header", for: indexPath) as! HeaderView
            headerView.label.text = sectionHeaders[indexPath.section]
            return headerView
        default:
            assert(false, "Unexpected element kind")
            return UICollectionReusableView()
        }
    }


    func classNameFrom(_ viewController: UIViewController) -> String {
        let currentViewControllerName = NSStringFromClass(viewController.classForCoder).components(separatedBy: ".").last!
        return currentViewControllerName
    }

    // Size for item at index path
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: collectionView.frame.size.width / 5 + 5, height: collectionView.frame.size.width / 5 + 5)
    }

    // Size for header in section
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, referenceSizeForHeaderInSection section: Int) -> CGSize {
        return CGSize(width: collectionView.frame.size.width, height: 60)
    }

    // Space between rows
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 45.0
    }

    // Space between items
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 8.0
    }

    // Inset for section
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        return UIEdgeInsets(top: 20, left: 10, bottom: 10, right: 10)
    }

    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        let viewController = (storyboard?.instantiateViewController(withIdentifier: Constants.gameModes[indexPath.section][indexPath.row].identifier))!
        navigationController?.pushViewController(viewController, animated: true);
    }

    /**
     Function to setup openCodeIcon
     */
    private func setupOpenCodeIcon() {
        let tap = UITapGestureRecognizer(target: self, action: #selector(loadWebView))
        openCodeWebView.addGestureRecognizer(tap)
    }

    /**
     Function to handle and open webview controller on webview tap
     - Parameter sender:
     */
    @objc func loadWebView(_ sender: Any) {
        let storyboard = UIStoryboard(name: "openCode", bundle: nil)
        let viewController = (storyboard.instantiateViewController(withIdentifier: "webView"))
        navigationController?.pushViewController(viewController, animated: true);
    }

}


/// to configure the grid displayed on the homepage.

extension UIBarButtonItem {
    convenience init(image: UIImage, title: String, target: Any?, action: Selector?, titleColor: UIColor) {
        let button = UIButton(type: .custom)
        button.setInsets(forContentPadding: UIEdgeInsets(top: 0.0, left: 0.0, bottom: 0.0, right: 0.0), imageTitlePadding: 10)
        button.setImage(image, for: .normal)
        button.imageView?.layer.transform = CATransform3DMakeScale(1.3, 1.3, 1.3)
        button.setTitle(title, for: .normal)
        button.setTitleColor(titleColor, for: .normal)
        button.frame = CGRect(x: 0, y: 0, width: image.size.width + 10, height: image.size.height + 20)
        if let target = target, let action = action {
            button.addTarget(target, action: action, for: .touchUpInside)
        }
        self.init(customView: button)
    }
}



