//
// Created by Nitish Yadav on 06/04/23.
//

import Foundation
import UIKit
import AVFoundation

/**
 class for qrScanner
 */
class scannerFragment: CameraController,autopilotDelegate {
    var previewLayer = AVCaptureVideoPreviewLayer();
    var whiteSheet = openCodeRunBottomSheet(frame: UIScreen.main.bounds)
    @IBOutlet weak var cancelBtn: UIView!
    let alert = UIAlertController(title: Strings.loadingTitle, message: Strings.loadingDataMessage, preferredStyle: .alert)
    var userToken: String = ""
    private var commands: String = ""
    private var projectFileId: String = "";
    var qrResult: String = "";
    var msg = CustomLabel(frame: .zero);
    var heading = CustomLabel(frame: .zero);
    var border = UIImageView();
    let firstHalfView = UIView();
    var projectName: String = "";
    weak var jsEval : jsEvaluator?

/**
    Method calls after view will load and initialize the UI and camera
 */
    override func viewDidLoad() {
        super.viewDidLoad();
        setupNavigationBarItem();
        createScanHeading()
        createScanMessage()
        createScannerBorder()
        firstHalfView.frame = currentOrientation == .portrait ? CGRect(x: 0, y: safeAreaLayoutValue.top + 100, width: width, height: height / 2 - 100) : CGRect(x: safeAreaLayoutValue.top, y: 130, width: width, height: height / 2 - 100);
        view.addSubview(firstHalfView);
        cameraView.frame = currentOrientation == .portrait ? CGRect(x: width / 2 - 115, y: 320, width: 230, height: 230) : CGRect(x: width + safeAreaLayoutValue.top + 22, y: 95, width: 230, height: 230)
        initializeCamera()
        view.addSubview(cameraView);
        cancelBtn.isUserInteractionEnabled = true
        let tap = UITapGestureRecognizer(target: self, action: #selector(removeFragment(_:)))
        cancelBtn.addGestureRecognizer(tap)
        NotificationCenter.default.addObserver(self, selector: #selector(reInitializeCamera), name: .reInitializeCamera, object: nil);
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated);
        alert.dismiss(animated: true);
        NotificationCenter.default.removeObserver(self);
    }

    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator);
        firstHalfView.frame = currentOrientation == .portrait ? CGRect(x: 0, y: safeAreaLayoutValue.top + 100, width: width, height: height / 2 - 100) : CGRect(x: safeAreaLayoutValue.top, y: 130, width: width, height: height / 2 - 100);
        border.frame = currentOrientation == .portrait ? CGRect(x: width / 2 - 134, y: 300, width: 268, height: 273) : CGRect(x: firstHalfView.right, y: 70, width: 268, height: 273)
        cameraView.frame = currentOrientation == .portrait ? CGRect(x: width / 2 - 115, y: 320, width: 230, height: 230) : CGRect(x: border.left + 22, y: 90, width: 230, height: 230)
        var orientation = AVCaptureVideoOrientation(rawValue: 1);
        switch currentOrientation {
        case .portrait:
            orientation = AVCaptureVideoOrientation.portrait
        case .landscapeLeft:
            orientation = AVCaptureVideoOrientation.landscapeRight
        case .landscapeRight:
            orientation = AVCaptureVideoOrientation.landscapeLeft;
        default:
            orientation = AVCaptureVideoOrientation.portrait
        }
        previewLayer.connection?.videoOrientation = orientation ?? .portrait
    }

    /**
     Method called after before view load
     - Parameter animated:
     */
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }

    /**
     Remove current Fragment
     - Parameter sender:
     */


    /**
     Create Heading of Fragment
     */
    private func createScanHeading() {
        heading = CustomLabel.init(text: Strings.scanQrCodeHeading, fontSize: 22, fontColor: Colors.textColor ?? .black, frame: CGRect(x: width / 2 - 65, y: 20, width: 145, height: 40))
        heading.textAlignment = .center;
        firstHalfView.addSubview(heading);
    }

    /**
     Create Message of Fragment
     */
    private func createScanMessage() {
        let message = Strings.scanInstructions
        let font = UIFont(name: "HelveticaNeue", size: 15)!
        let messageSize = (message as NSString).boundingRect(with: CGSize(width: 320, height: CGFloat.greatestFiniteMagnitude), options: .usesLineFragmentOrigin, attributes: [.font: font], context: nil)
        let messageHeight = ceil(messageSize.height)

        msg = CustomLabel(text: message, fontSize: 15, fontColor: Colors.textColor ?? .black, frame:
        CGRect(x: width / 2 - 160, y: heading.bottom + 15, width: 320, height: messageHeight))
        msg.numberOfLines = 0
        msg.lineBreakMode = .byWordWrapping
        msg.textAlignment = .center
        msg.font = font
        firstHalfView.addSubview(msg)
    }


    /**
     Create border for Scanner view
     */
    private func createScannerBorder() {
        border.image = UIImage(named: "scanner-boundary");
        border.frame = currentOrientation == .portrait ? CGRect(x: width / 2 - 134, y: 300, width: 268, height: 273) : CGRect(x: width + safeAreaLayoutValue.top, y: 70, width: 268, height: 273)
        view.addSubview(border);
    }

    var hasSuccessfulScan = false
    var scanCount : Int = 0;

    /**
     Delegate Method called for each frame. If Qr scan successful it will stops.
     - Parameters:
       - output: QR code data
       - metadataObjects:
       - connection:
     */
    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        if hasSuccessfulScan {
            // Ignore any further metadata objects if a successful scan has occurred
            return
        }
        for metadataObject in metadataObjects {
            guard let qrCodeObject = metadataObject as? AVMetadataMachineReadableCodeObject,
                  qrCodeObject.type == AVMetadataObject.ObjectType.qr
            else {
                continue
            }
            if let qrCodeString = qrCodeObject.stringValue {
                // Handle the QR code data here
                do {
                    guard let jsonData = qrCodeString.data(using: .utf8) else {
                        return
                    }
                    let qrCodeData = try JSONDecoder().decode(QrData.self, from: jsonData)
                    qrResult = qrCodeData.driveLink
                    projectName = qrCodeData.projectName
                    captureSession.stopRunning();
                    hasSuccessfulScan = true
                    scanCount = scanCount + 1;
                    createOverlayAlert();
                    Authentication.download(file: qrResult) { data, error in
                        self.alert.dismiss(animated: true);
                        if error != nil {
                            self.createErrorUI()
                            return;
                        }
                        if let data = data {
                            print(self.qrResult);
                            self.createSuccessUI();
                            print("data is ", String(data: data, encoding: .utf8)?.trimmingCharacters(in: .whitespacesAndNewlines).replacingOccurrences(of: "\n", with: "") as Any)
                            self.commands = String(data: data, encoding: .utf8)?.trimmingCharacters(in: .whitespacesAndNewlines).replacingOccurrences(of: "\n", with: "") ?? ""
                            return;
                        }
                    }
                } catch {
                    print("Error decoding JSON: \(error)")
                    createErrorUI();
                    hasSuccessfulScan = true
                    return;
                }
            }
        }
    }

    /**
     Override method to initialize camera
     */
    override func initializeCamera() {
        // Configure the input device (camera)
        hasSuccessfulScan = false;
        scanCount = 0;
        guard let captureDevice = AVCaptureDevice.default(for: .video) else {
            return
        }
        guard let input = try? AVCaptureDeviceInput(device: captureDevice) else {
            return
        }
        captureSession = AVCaptureSession();
        captureSession.addInput(input);
        // Configure the output device (metadata)
        let output = AVCaptureMetadataOutput();
        captureSession.addOutput(output);
        output.setMetadataObjectsDelegate(self, queue: DispatchQueue.main);
        output.metadataObjectTypes = [.qr] // specify the types of metadata to capture (in this case, only QR codes)
        previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        previewLayer.videoGravity = .resizeAspectFill
        previewLayer.frame = cameraView.layer.bounds
        cameraView.layer.addSublayer(previewLayer)
        if currentOrientation.rawValue == 3{
            previewLayer.connection?.videoOrientation = AVCaptureVideoOrientation(rawValue: 4) ?? .portrait;
        }
        else if currentOrientation.rawValue == 4{
            previewLayer.connection?.videoOrientation = AVCaptureVideoOrientation(rawValue: 3) ?? .portrait;
        }
        else {
            previewLayer.connection?.videoOrientation = AVCaptureVideoOrientation(rawValue: currentOrientation.rawValue) ?? .portrait;
        }
        captureSession.startRunning()
    }

    /**
     Creating Bottom sheet for QR scan Successful
     */
    private func createSuccessUI() {
        if scanCount > 1 {
            return;
        }
        if whiteSheet.isDescendant(of: view) {
            return
        }
        whiteSheet = openCodeRunBottomSheet(frame: UIScreen.main.bounds, fileName: projectName, isScannerFragment: true);
        whiteSheet.startBtn.addTarget(self, action: #selector(start), for: .touchUpInside);
        whiteSheet.cancelBtn.addTarget(self, action: #selector(cancel), for: .touchUpInside);
        view.addSubview(whiteSheet);
    }

    /**
     Creating Bottom sheet for QR scan Error
     */
    private func createErrorUI() {
        if scanCount > 1 {
            return;
        }
        if whiteSheet.isDescendant(of: view) {
            return
        }
        whiteSheet = openCodeRunBottomSheet(frame: UIScreen.main.bounds, fileName: String(), isScannerFragment: true);
        whiteSheet.cancelButton.addTarget(self, action: #selector(scan), for: .touchUpInside);
        view.addSubview(whiteSheet);
    }


    /**
     function to start and run the project
     */
    @objc private func start() {
        let storyboard = UIStoryboard(name: "openCode", bundle: nil)
        let viewController = (storyboard.instantiateViewController(withIdentifier: "runOpenBot"));
        navigationController?.pushViewController(viewController, animated: true);
        whiteSheet.removeFromSuperview();
        jsEval = jsEvaluator(jsCode: commands);
        preferencesManager.setBlocklyCode(value: commands);
    }

    /**
     Function to remove bottom sheet and restart scanner
     */
    @objc private func cancel() {
        whiteSheet.animateBottomSheet();
        initializeCamera();
        hasSuccessfulScan = false;
        scanCount = 0;
    }

    /**
     Function to create Error Heading  mukerian
     */
    private func createErrorHeading() {
        let errorHeading = CustomLabel(text: Strings.errorHeading,
                fontSize: 18, fontColor: Colors.textColor ?? .black, frame: CGRect(x: 19, y: 23, width: 60, height: 40));
        errorHeading.font = HelveticaNeue.bold(size: 15);
        bottomSheet.addSubview(errorHeading);
    }

    /**
     Function to create Error Message
     */
    private func createErrorMsg() {
        let msg = CustomLabel(text: Strings.genericErrorMessage, fontSize: 15, fontColor: Colors.textColor ?? .black, frame: CGRect(x: 19, y: 57, width: 330, height: 40));
        bottomSheet.addSubview(msg);
    }

    /**
     Function to create Scan Qr Button
     */
    private func createScanQrBtn() {
        let scanQr = UIButton(frame: CGRect(x: 19, y: 120, width: 120, height: 40));
        scanQr.backgroundColor = Colors.title;
        scanQr.setTitle(Strings.scanQrButton, for: .normal);
        scanQr.addTarget(nil, action: #selector(scan), for: .touchUpInside);
        scanQr.layer.cornerRadius = 10;
        bottomSheet.addSubview(scanQr);
    }

    /**
     Function to initialize camera for rescan
     */
    @objc private func scan() {
        whiteSheet.animateBottomSheet();
        hasSuccessfulScan = false;
        scanCount = 0;
        initializeCamera();

    }

    @objc func removeFragment(_ sender: UIView) {
        navigationController?.popViewController(animated: true);
    }

    /**
     Function to create loader
     */
    func createOverlayAlert() {
        if scanCount > 1 {
            return;
        }
        if let presentedViewController = presentedViewController {
            // An alert or view controller is already being presented
            // Handle this case if needed
            return
        }
        let loadingIndicator: UIActivityIndicatorView = UIActivityIndicatorView(frame: CGRectMake(10, 5, 50, 50)) as UIActivityIndicatorView
        loadingIndicator.startAnimating();
        loadingIndicator.hidesWhenStopped = true
        loadingIndicator.style = UIActivityIndicatorView.Style.medium
        alert.view.addSubview(loadingIndicator)
        DispatchQueue.main.async{
            self.present(self.alert, animated: true, completion: nil)
        }
    }

    @objc func reInitializeCamera() {
        initializeCamera()
    }

    func setupNavigationBarItem() {
        if UIImage(named: "back") != nil {
            let backNavigationIcon = (UIImage(named: "back")?.withRenderingMode(.alwaysOriginal))!
            let newBackButton = UIBarButtonItem(image: backNavigationIcon, title: Strings.qrScannerNavTitle, target: self, action: #selector(back(sender:)), titleColor: Colors.navigationColor ?? .white)
            navigationItem.leftBarButtonItem = newBackButton
        }
    }

    @objc func back(sender: UIBarButtonItem) {
        _ = navigationController?.popViewController(animated: true)
    }

    func didPerformAction() {
        NotificationCenter.default.post(name: .createCameraView, object: nil);
    }


}
