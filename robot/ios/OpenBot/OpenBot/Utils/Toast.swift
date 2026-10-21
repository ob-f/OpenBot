//
// Created by Nitish Yadav on 27/04/23.
//

import Foundation
import UIKit

/**
 Class for Toast message for profile update
*/
class ToastLabel: UILabel {
    var textInsets = UIEdgeInsets.zero {
        didSet {
            invalidateIntrinsicContentSize()
        }
    }

    /**
     overriding function to create custom rect for toast message label
     - Parameters:
       - bounds:
       - numberOfLines:
     - Returns:
     */
    override func textRect(forBounds bounds: CGRect, limitedToNumberOfLines numberOfLines: Int) -> CGRect {
        let insetRect = bounds.inset(by: textInsets)
        let textRect = super.textRect(forBounds: insetRect, limitedToNumberOfLines: numberOfLines)
        let invertedInsets = UIEdgeInsets(top: -textInsets.top, left: -textInsets.left, bottom: -textInsets.bottom, right: -textInsets.right)

        return textRect.inset(by: invertedInsets)
    }

    override func drawText(in rect: CGRect) {
        super.drawText(in: rect.inset(by: textInsets))
    }
}

extension UIViewController {
    static let DELAY_SHORT = 1.5
    static let DELAY_LONG = 3.0

    /**
     Function to set the delay and origin of toast message
     - Parameters:
       - text:
       - delay:
     */
    func showToast(_ text: String, delay: TimeInterval = DELAY_LONG, icon: UIImage) {
        let toastView = UIView();
        let checkIcon = UIImageView();
        toastView.backgroundColor = UIColor.black;
        checkIcon.image = icon;
        toastView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(toastView)
        let safeArea = view.safeAreaLayoutGuide
        toastView.widthAnchor.constraint(equalToConstant: 270).isActive = true;
        toastView.heightAnchor.constraint(equalToConstant: 35).isActive = true;
        toastView.leadingAnchor.constraint(equalTo: safeArea.leadingAnchor, constant: width/2 - 135).isActive = true;
        toastView.topAnchor.constraint(equalTo: safeArea.topAnchor, constant: height - 200).isActive = true;
        checkIcon.translatesAutoresizingMaskIntoConstraints = false;
        toastView.addSubview(checkIcon);
        checkIcon.widthAnchor.constraint(equalToConstant: icon.size.width);
        checkIcon.heightAnchor.constraint(equalToConstant: icon.size.height);
        checkIcon.leadingAnchor.constraint(equalTo: toastView.leadingAnchor, constant: 17).isActive = true
        checkIcon.topAnchor.constraint(equalTo: toastView.topAnchor, constant: 6).isActive = true
        let label = ToastLabel()
        label.backgroundColor = UIColor(white: 0, alpha: 0.5)
        label.textColor = .white
        label.font = HelveticaNeue.regular(size: 12);
        label.text = text
        label.translatesAutoresizingMaskIntoConstraints = false
        toastView.addSubview(label)

        let saveArea = view.safeAreaLayoutGuide
        label.widthAnchor.constraint(equalToConstant: width).isActive = true;
        label.heightAnchor.constraint(equalToConstant: 40).isActive = true;
        label.leadingAnchor.constraint(equalTo: checkIcon.trailingAnchor, constant: 17).isActive = true;
        label.topAnchor.constraint(equalTo: toastView.topAnchor,constant: -5).isActive = true;
        UIView.animate(withDuration: 0.5, delay: 0, options: .curveEaseIn, animations: {
            toastView.alpha = 1
        }, completion: { _ in
            UIView.animate(withDuration: 0.5, delay: delay, options: .curveEaseOut, animations: {
                toastView.alpha = 0
            }, completion: { _ in
                toastView.removeFromSuperview();
            })
        })
    }
}
