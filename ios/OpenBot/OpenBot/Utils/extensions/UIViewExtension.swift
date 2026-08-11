//
// Created by Nitish Yadav on 13/09/22.
//

import UIKit

/// This extension adds a method to UIView to update any "adapted constraints" on the view.
extension UIView {
    func updateAdaptedConstraints() {
        let adaptedConstraints = constraints.filter { (constraint) -> Bool in
            constraint is AdaptedConstraint
        } as! [AdaptedConstraint]

        for constraint in adaptedConstraints {
            constraint.resetConstant()
            constraint.awakeFromNib()
        }
    }

}

/// This extension adds computed properties to UIView to get/set the top, bottom, right, and left edges of the view's frame.
extension UIView {

    var top: CGFloat {
        get {
            self.frame.origin.y
        }
        set {
            var frame = self.frame
            frame.origin.y = newValue
            self.frame = frame
        }
    }

    var bottom: CGFloat {
        get {
            frame.origin.y + frame.size.height
        }
        set {
            var frame = self.frame
            frame.origin.y = newValue - self.frame.size.height
            self.frame = frame
        }
    }

    var right: CGFloat {
        get {
            self.frame.origin.x + self.frame.size.width
        }
        set {
            var frame = self.frame
            frame.origin.x = newValue - self.frame.size.width
            self.frame = frame
        }
    }

    var left: CGFloat {
        get {
            self.frame.origin.x
        }
        set {
            var frame = self.frame
            frame.origin.x = newValue
            self.frame = frame
        }
    }
}

class BottomSheetView: UIView {

    override init(frame: CGRect) {
        let newFrame = currentOrientation == .portrait ? CGRect(x: safeAreaLayoutValue.left, y: height - 250 , width: width - safeAreaLayoutValue.left, height: 250) :
                currentOrientation == .landscapeLeft ? CGRect(x: safeAreaLayoutValue.top, y: width - 250, width: height - safeAreaLayoutValue.top-safeAreaLayoutValue.bottom, height: 250) :
                CGRect(x: safeAreaLayoutValue.bottom, y: width - 250, width: height - safeAreaLayoutValue.top - safeAreaLayoutValue.bottom, height: 250);
        super.init(frame: newFrame)
        setup()
        NotificationCenter.default.addObserver(self, selector: #selector(orientationDidChange), name: UIDevice.orientationDidChangeNotification, object: nil)
    }

    deinit {
        NotificationCenter.default.removeObserver(self, name: UIDevice.orientationDidChangeNotification, object: nil)
    }


    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }


    private func setup() {
        backgroundColor = Colors.bdColor;
        layer.cornerRadius = 30;
        layer.maskedCorners = [.layerMaxXMinYCorner, .layerMinXMinYCorner]
    }


    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        if previousTraitCollection != nil && previousTraitCollection!.verticalSizeClass != traitCollection.verticalSizeClass {
            if traitCollection.verticalSizeClass == .compact {
                frame = currentOrientation == .landscapeLeft ? CGRect(x: safeAreaLayoutValue.top, y: width - 250, width: height - safeAreaLayoutValue.top - safeAreaLayoutValue.bottom, height: 250) :
                        CGRect(x: safeAreaLayoutValue.bottom, y: width - 250, width: height - safeAreaLayoutValue.top - safeAreaLayoutValue.bottom, height: 250);
            } else {
                frame = CGRect(x: safeAreaLayoutValue.left, y: height - 250, width: width - safeAreaLayoutValue.left, height: 250)
            }
        }
    }

    var orientationDidChanged: UIDeviceOrientation = .portrait {
        didSet {
            if oldValue == .landscapeLeft && orientationDidChanged == .landscapeRight {
                // The device orientation has changed from landscape left to landscape right
                // Do something here
                frame = CGRect(x: safeAreaLayoutValue.bottom, y: width - 250, width: height - safeAreaLayoutValue.top - safeAreaLayoutValue.bottom, height: 250)
            }
            if oldValue == .landscapeRight && orientationDidChanged == .landscapeLeft {
                // The device orientation has changed from landscape right to landscape left
                frame = CGRect(x: safeAreaLayoutValue.top, y: width - 250, width: height - safeAreaLayoutValue.top, height: 250)

            }
        }
    }


    @objc func orientationDidChange() {
        switch UIDevice.current.orientation {
        case .portrait, .portraitUpsideDown, .landscapeLeft, .landscapeRight:
            orientationDidChanged = UIDevice.current.orientation
        default:
            break
        }
    }


}

class CustomView: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    convenience init(frame: CGRect, backgroundColor: UIColor) {
        self.init(frame: frame);
        setup(backgroundColor: backgroundColor)
    }


    private func setup(backgroundColor: UIColor) {
        self.backgroundColor = backgroundColor;
    }
}

class openCodeRunBottomSheet: UIView {
    let startBtn = UIButton(frame: CGRect(x: 19, y: 150, width: 120, height: 40));
    let bottomSheet = BottomSheetView(frame: .zero);
    let cancelBtn = UIButton(frame: CGRect(x: 160, y: 150, width: 120, height: 40));
    let cancelButton = UIButton(frame: CGRect(x: 19, y: 150, width: 120, height: 40));

    var fileName: String = "";

    override init(frame: CGRect) {
        super.init(frame: frame);
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    convenience init(frame: CGRect, fileName: String,isScannerFragment : Bool) {
        self.init(frame: frame);
        self.fileName = fileName;
        let swipeDown = UISwipeGestureRecognizer(target: self, action: #selector(respondToSwipeGesture))
        swipeDown.direction = .down
        addGestureRecognizer(swipeDown)
        let swipeUp = UISwipeGestureRecognizer(target: self, action: #selector(respondToSwipeGesture))
        swipeUp.direction = .up
        addGestureRecognizer(swipeUp);
        setup();
        createBottomSheet()
        switch fileName {
        case "" :
            createErrorUI(fileName : fileName)
            break;
        case "error" :
            createErrorUI(fileName : fileName)
            break;
        default :
            createSuccessUI(isScannerFragment: isScannerFragment)
            break;
        }
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)

        if previousTraitCollection != nil && previousTraitCollection!.verticalSizeClass != traitCollection.verticalSizeClass {
            // The vertical size class has changed (i.e. the device orientation has changed)
            if traitCollection.verticalSizeClass == .compact {
                // The device is in landscape orientation
                // Do something here
                frame = CGRect(x: 0, y: 0, width: height, height: width);
            } else {
                // The device is in portrait orientation
                // Do something here
                frame = CGRect(x: 0, y: 0, width: width, height: height);
            }
        }
    }

    private func setup() {
        backgroundColor = UIColor.black.withAlphaComponent(0.6)
    }

    private func createBottomSheet() {
        addSubview(bottomSheet)
    }

    /**
     Creating Bottom sheet for QR scan Error
     */
    private func createErrorUI(fileName : String) {
        DispatchQueue.main.async {
            self.createErrorHeading()
            self.createErrorMsg()
            fileName == "" ?  self.createCancel(btnName: Strings.scanQrButton) : self.createCancel(btnName: Strings.cancel)

        }

    }

    /**
     Creating Bottom sheet for QR scan Successful
     */
    private func createSuccessUI(isScannerFragment : Bool) {
        DispatchQueue.main.async {
            if isScannerFragment {
                self.createBottomSheetHeading()
            }
            self.createBottomSheetMsg(fileName: self.fileName,isScannerFragment : isScannerFragment);
            self.createStartBtn()
            self.createCancelBtn();


        }

    }

    private func createBottomSheetHeading() {
        let heading = CustomLabel(text: Strings.qrScanSuccessHeading,
                fontSize: 18, fontColor: Colors.textColor ?? .black, frame: CGRect(x: 19, y: 33, width: 290, height: 40));
        heading.font = HelveticaNeue.bold(size: 15);
        bottomSheet.addSubview(heading);
    }

    private func createBottomSheetMsg(fileName: String,isScannerFragment : Bool) {
        let message = String(format: Strings.qrFileDetectedMessageFormat, fileName)
        let font = UIFont(name: "HelveticaNeue", size: 16)!
        _ = (message as NSString).boundingRect(with: CGSize(width: 320, height: CGFloat.greatestFiniteMagnitude), options: .usesLineFragmentOrigin, attributes: [.font: font], context: nil)
        let msg =   isScannerFragment == true ? CustomLabel(text: message, fontSize: 15,
                fontColor: Colors.textColor ?? .black,
                frame:CGRect(x: 19, y: 87, width: 320, height: 40)) :
                CustomLabel(text: message, fontSize: 15,
                        fontColor: Colors.textColor ?? .black,
                        frame:CGRect(x: 19, y: 33, width: 320, height: 40));
        bottomSheet.addSubview(msg);
        msg.numberOfLines = 2
        msg.textAlignment = .left
        msg.font = font
    }

    /**
    Method to create Start button which run the project
    */
    private func createStartBtn() {
        startBtn.backgroundColor = Colors.title;
        startBtn.setTitle(Strings.startButton, for: .normal);
        startBtn.addTarget(nil, action: #selector(start), for: .touchUpInside);
        startBtn.layer.cornerRadius = 10;
        bottomSheet.addSubview(startBtn);
    }

    /**
     Method to create Cancel button and reload the Scanner
     */
    private func createCancelBtn() {
        cancelBtn.setTitle(Strings.cancel, for: .normal);
        cancelBtn.layer.borderWidth = 1;
        cancelBtn.layer.borderColor = Colors.title?.cgColor;
        cancelBtn.clipsToBounds = true;
        cancelBtn.setTitleColor(traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black, for: .normal)
        cancelButton.setTitleColor(traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black, for: .normal)
        cancelBtn.addTarget(nil, action: #selector(cancel), for: .touchUpInside)
        cancelBtn.layer.cornerRadius = 10;
        bottomSheet.addSubview(cancelBtn);
    }

    @objc private func start() {
    }

    /**
     Function to remove bottom sheet and restart scanner
     */
    @objc private func cancel() {

    }


    /// Method called when we touch up or down of UIView. It will change the origin of UIView
    ///
    /// - Parameter gesture:
    @objc func respondToSwipeGesture(gesture: UIGestureRecognizer) {
        if let swipeGesture = gesture as? UISwipeGestureRecognizer {
            switch swipeGesture.direction {
            case .down:
                UIView.animate(withDuration: 0.25, animations: {
                    if currentOrientation == .portrait {
                        // self.frame.origin.y = height - adapted(dimensionSize: 50, to: .height)
                        self.bottomSheet.frame.origin.y = height;
                    } else {
                        // self.frame.origin.y = width - adapted(dimensionSize: 50, to: .height)
                    }
                }, completion: { _ in
                    // Remove the view from its superview
                    self.removeFromSuperview();
                    NotificationCenter.default.post(name: .reInitializeCamera, object: nil);

                })
            default:
                break
            }
        }
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

    private func createCancel(btnName : String) {
        cancelButton.setTitle(btnName, for: .normal);
        cancelButton.backgroundColor = Colors.title;
        cancelButton.addTarget(nil, action: #selector(scan), for: .touchUpInside);
        cancelButton.layer.cornerRadius = 10;
        bottomSheet.addSubview(cancelButton);
    }

    @objc private func scan() {

    }

    func animateBottomSheet() {
        let animationDuration: TimeInterval = 0.3
// Animate the bottom sheet off the screen
        UIView.animate(withDuration: animationDuration, animations: {
            self.bottomSheet.frame.origin.y = height
        }) { (finished) in
            // Remove the bottom sheet from the superview
            self.removeFromSuperview();
        }
    }


}
