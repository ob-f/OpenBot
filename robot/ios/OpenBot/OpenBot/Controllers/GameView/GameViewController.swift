//
// Created by Sparsh Jain on 20/08/22.
//

import UIKit
import GameController

class GameViewController: UIViewController {

    var dateFormatter = DateFormatter()
    private let maximumControllerCount: Int = 1
    var gameController = GameController.shared
    weak var delegate: InputManagerDelegate?
    let controllerImage: UIImageView! = nil
    @IBOutlet weak var controlImage: UIImageView!
    let overlayLeft = DrawRect(frame: CGRect(origin: CGPoint(x: 65, y: 166.5), size: CGSize(width: 25, height: 20)))
    let overlayRight = DrawRect(frame: CGRect(origin: CGPoint(x: 105, y: 166.5), size: CGSize(width: 25, height: 20)))
    let overlayUp = DrawRect(frame: CGRect(origin: CGPoint(x: 86.5, y: 144), size: CGSize(width: 20, height: 25)))
    let overlayDown = DrawRect(frame: CGRect(origin: CGPoint(x: 86.5, y: 185), size: CGSize(width: 20, height: 25)))
    let overlayA = DrawCircle(frame: CGRect(origin: CGPoint(x: 332, y: 193.5), size: CGSize(width: 28, height: 28)))
    let overlayB = DrawCircle(frame: CGRect(origin: CGPoint(x: 360.5, y: 161.5), size: CGSize(width: 28, height: 28)))
    let overlayX = DrawCircle(frame: CGRect(origin: CGPoint(x: 303.5, y: 161.5), size: CGSize(width: 28, height: 28)))
    let overlayY = DrawCircle(frame: CGRect(origin: CGPoint(x: 332, y: 135), size: CGSize(width: 28, height: 28)))
    let overlayOptions = DrawRect(frame: CGRect(origin: CGPoint(x: 156, y: 161.5), size: CGSize(width: 20, height: 12)))
    let overlayMenu = DrawRect(frame: CGRect(origin: CGPoint(x: 265, y: 161.5), size: CGSize(width: 20, height: 12)))
    let overlayL1Shoulder = DrawRect(frame: CGRect(origin: CGPoint(x: 69, y: 58.5), size: CGSize(width: 56, height: 25)))
    let overlayL2Shoulder = DrawRect(frame: CGRect(origin: CGPoint(x: 69, y: 24.5), size: CGSize(width: 56, height: 25)))
    let overlayR1Shoulder = DrawRect(frame: CGRect(origin: CGPoint(x: 314.5, y: 58.5), size: CGSize(width: 56, height: 25)))
    let overlayR2Shoulder = DrawRect(frame: CGRect(origin: CGPoint(x: 314.5, y: 24.5), size: CGSize(width: 56, height: 25)))
    let overlayLeftThumb = DrawCircle(frame: CGRect(origin: CGPoint(x: 134.5, y: 210), size: CGSize(width: 46, height: 46)))
    let overlayRightThumb = DrawCircle(frame: CGRect(origin: CGPoint(x: 259.5, y: 210), size: CGSize(width: 46, height: 46)))
    var overlayLeftThumb_var = DrawCircle(frame: CGRect(origin: CGPoint(x: 140.5, y: 210), size: CGSize(width: 46, height: 46)))
    var overlayRightThumb_var = DrawCircle(frame: CGRect(origin: CGPoint(x: 264.5, y: 210), size: CGSize(width: 46, height: 46)))
    var restrictRotation: UIInterfaceOrientationMask = .portrait

    /// Notifies the view controller that its view is about to be added to a view hierarchy.
    override func viewWillAppear(_ animated: Bool) {
        AppDelegate.AppUtility.lockOrientation(UIInterfaceOrientationMask.portrait, andRotateTo: UIInterfaceOrientation.portrait)
    }

    /// Called after the view controller has loaded.
    override func viewDidLoad() {
        super.viewDidLoad()
        setupNavigationBarItem()
        dateFormatter.dateFormat = "HH:mm:ss.SSSS"
        gameController.resetControl = true
        NotificationCenter.default.addObserver(self, selector: #selector(didConnectController), name: NSNotification.Name(rawValue: Strings.controllerConnected), object: nil)
    }

    /// Initialization routine
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        didConnectController();
    }

    override func viewWillDisappear(_ animated: Bool) {
        DeviceCurrentOrientation.shared.findDeviceOrientation()
        AppDelegate.AppUtility.lockOrientation(UIInterfaceOrientationMask.all)
    }

    /// Called after the view was dismissed, covered or otherwise hidden.
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        DeviceCurrentOrientation.shared.findDeviceOrientation()
    }

    /// function to connect with the controller and trigger all the callback functions
    @objc func didConnectController() {
        if (connectedController == nil) {
            return
        }

        let controller = connectedController;
        _ = String(format: "%.2f", controller!.battery.unsafelyUnwrapped.batteryLevel * 100);

        delegate?.inputManager(self, didConnect: controller!)

        controller!.extendedGamepad?.dpad.left.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("←", pressed, self.overlayLeft)
        }
        controller!.extendedGamepad?.dpad.right.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("→", pressed, self.overlayRight)
        }
        controller!.extendedGamepad?.dpad.up.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("↑", pressed, self.overlayUp)
        }
        controller!.extendedGamepad?.dpad.down.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("↓", pressed, self.overlayDown)
        }
        controller!.extendedGamepad?.buttonA.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("A", pressed, self.overlayA)
        }
        controller!.extendedGamepad?.buttonB.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("B", pressed, self.overlayB)
        }
        controller!.extendedGamepad?.buttonX.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("X", pressed, self.overlayX)
        }
        controller!.extendedGamepad?.buttonY.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("Y", pressed, self.overlayY)
        }
        controller!.extendedGamepad?.buttonOptions?.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("SELECT", pressed, self.overlayOptions)
        }
        controller!.extendedGamepad?.buttonMenu.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("OPTIONS", pressed, self.overlayMenu)
        }
        controller!.extendedGamepad?.leftShoulder.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("L1", pressed, self.overlayL1Shoulder)
        }
        controller!.extendedGamepad?.rightShoulder.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("R1", pressed, self.overlayR1Shoulder)
        }
        controller!.extendedGamepad?.leftTrigger.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("L2", pressed, self.overlayL2Shoulder)
        }
        controller!.extendedGamepad?.leftTrigger.valueChangedHandler = { (button, value, pressed) in
            self.triggerChangedHandler("L2", value, pressed)
        }
        controller!.extendedGamepad?.rightTrigger.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("R2", pressed, self.overlayR2Shoulder)
        }
        controller!.extendedGamepad?.rightTrigger.valueChangedHandler = { (button, value, pressed) in
            self.triggerChangedHandler("R2", value, pressed)
        }
        controller!.extendedGamepad?.leftThumbstick.valueChangedHandler = { (button, xvalue, yvalue) in
            self.thumbstickChangedHandlerLeft("THUMB-LEFT", xvalue, yvalue)
        }
        controller!.extendedGamepad?.rightThumbstick.valueChangedHandler = { (button, xvalue, yvalue) in
            self.thumbstickChangedHandlerRight("THUMB-RIGHT", xvalue, yvalue)
        }
        controller!.extendedGamepad?.leftThumbstickButton?.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("THUMB-LEFT", pressed, self.overlayLeftThumb)
        }
        controller!.extendedGamepad?.rightThumbstickButton?.pressedChangedHandler = { (button, value, pressed) in
            self.buttonChangedHandler("THUMB-RIGHT", pressed, self.overlayRightThumb)
        }
    }

    /// function to handle the change when tapped on a button
    ///
    /// - Parameters:
    ///   - button: button name
    ///   - pressed: is Pressed?
    ///   - overlay: overlay View to display on screen.
    func buttonChangedHandler(_ button: String, _ pressed: Bool, _ overlay: UIView) {
        if pressed {
            controlImage.addSubview(overlay)
        } else {

            overlay.removeFromSuperview()
        }
    }

    /// function to handle the change in L2 R2 buttons
    ///
    /// - Parameters:
    ///   - button: button name
    ///   - value: value
    ///   - pressed: is Pressed?
    func triggerChangedHandler(_ button: String, _ value: Float, _ pressed: Bool) {
        if pressed {
            _ = String(format: "%.2f", value)

        }
    }

    /// function to handle the left thumbstick changes
    ///
    /// - Parameters:
    ///   - button: button
    ///   - xvalue: x axis value
    ///   - yvalue:  y axis value
    func thumbstickChangedHandlerLeft(_ button: String, _ xvalue: Float, _ yvalue: Float) {
        if (xvalue == 0 && yvalue == 0) {
            overlayLeftThumb_var.removeFromSuperview()
        } else {
            overlayLeftThumb_var.removeFromSuperview()
            overlayLeftThumb_var = DrawCircle(frame: CGRect(origin: CGPoint(x: Int(140.5) + Int(xvalue * 12), y: 210 - Int(yvalue * 12)), size: CGSize(width: 46, height: 46)))
            controlImage.addSubview(overlayLeftThumb_var)
        }
    }

    /// function to handle the right thumbstick changes
    ///
    /// - Parameters:
    ///   - button: button
    ///   - xvalue: x axis value
    ///   - yvalue:  y axis value
    func thumbstickChangedHandlerRight(_ button: String, _ xvalue: Float, _ yvalue: Float) {
        if (xvalue == 0 && yvalue == 0) {
            overlayRightThumb_var.removeFromSuperview()
        } else {
            overlayRightThumb_var.removeFromSuperview()
            overlayRightThumb_var = DrawCircle(frame: CGRect(origin: CGPoint(x: Int(264.5) + Int(xvalue * 12), y: 210 - Int(yvalue * 12)), size: CGSize(width: 46, height: 46)))
            controlImage.addSubview(overlayRightThumb_var)
        }
    }

    func setupNavigationBarItem() {
        if UIImage(named: "back") != nil {
            let backNavigationIcon = (UIImage(named: "back")?.withRenderingMode(.alwaysOriginal))!
            let newBackButton = UIBarButtonItem(image: backNavigationIcon, title: Strings.controllerMapping, target: self, action: #selector(back(sender:)), titleColor: Colors.navigationColor ?? .white)
            navigationItem.leftBarButtonItem = newBackButton
        }
    }

    @objc func back(sender: UIBarButtonItem) {
        _ = navigationController?.popViewController(animated: true)
    }

}


protocol InputManagerDelegate: AnyObject {
    func inputManager(_ manager: GameViewController, didConnect controller: GCController)
    func inputManager(_ manager: GameViewController, didDisconnect controller: GCController)
}

class DrawCircle: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = UIColor(white: 1, alpha: 0.0)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func draw(_ rect: CGRect) {
        let bpath = UIBezierPath(arcCenter: CGPoint(x: rect.height / 2, y: rect.width / 2),
                radius: CGFloat(rect.height / 2),
                startAngle: CGFloat(0),
                endAngle: CGFloat(Double.pi * 2),
                clockwise: true)

        let color: UIColor = UIColor.red
        color.set()
        bpath.fill()
    }
}

class DrawRect: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = UIColor(white: 1, alpha: 0.0)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func draw(_ rect: CGRect) {
        let bpath = UIBezierPath(roundedRect: rect, cornerRadius: rect.width / 10)
        let color: UIColor = UIColor.red
        color.set()
        bpath.fill()
    }


}

