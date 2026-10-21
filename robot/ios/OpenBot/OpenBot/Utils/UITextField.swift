//
// Created by Nitish Yadav on 11/04/23.
//

import Foundation
import UIKit


class CustomTextField: UITextField {

    override func awakeFromNib() {
        super.awakeFromNib()
        textColor = UIColor(named: "gamepad")
        // Set the border color and width
        layer.masksToBounds = true
        layer.borderColor = UIColor(red: 0/255, green: 113/255, blue: 197/255, alpha: 0.4).cgColor
        layer.borderWidth = 2.0

        // Set the corner radius
        layer.cornerRadius = 10.0

        // Set the left and right padding
        let paddingView = UIView(frame: CGRect(x: 0, y: 0, width: 10, height: frame.size.height))
        leftView = paddingView
        leftViewMode = .always

        let paddingView2 = UIView(frame: CGRect(x: 0, y: 0, width: 10, height: frame.size.height))
        rightView = paddingView2
        rightViewMode = .always
    }

    let padding = UIEdgeInsets(top: 0, left: 15, bottom: 0, right: 10)

    override open func textRect(forBounds bounds: CGRect) -> CGRect {
        return bounds.inset(by: padding)
    }

    override open func placeholderRect(forBounds bounds: CGRect) -> CGRect {
        return bounds.inset(by: padding)
    }

    override open func editingRect(forBounds bounds: CGRect) -> CGRect {
        return bounds.inset(by: padding)
    }

    override func borderRect(forBounds bounds: CGRect) -> CGRect {
        return bounds.inset(by: padding)
    }
}
