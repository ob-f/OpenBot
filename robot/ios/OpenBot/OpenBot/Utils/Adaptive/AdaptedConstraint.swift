//
// Created by Nitish Yadav on 13/09/22.
//

import UIKit

/// A subclass of NSLayoutConstraint that adapts the constant value of a constraint based on the dimension type.
class AdaptedConstraint: NSLayoutConstraint {

    // Properties
    var initialConstant: CGFloat?

    // Lifecycle methods
    override func awakeFromNib() {
        super.awakeFromNib()
        saveConstant()
        adaptConstant()
    }
}

extension AdaptedConstraint {

    /// Adapts the constant value of the constraint based on the dimension type.
    func adaptConstant() {
        if let dimension = getDimension(from: firstAttribute) {
            constant = adapted(dimensionSize: constant, to: dimension)
        }
    }

    /// Returns the dimension type based on the provided constraint attribute.
    ///
    /// - parameter attribute: A NSLayoutConstraint.Attribute value.
    ///
    /// - returns: A Dimension type.
    func getDimension(from attribute: NSLayoutConstraint.Attribute) -> Dimension? {
        switch attribute {
        case .left, .right, .leading, .trailing, .width, .centerX, .leftMargin,
             .rightMargin, .leadingMargin, .trailingMargin, .centerXWithinMargins:
            return .width
        case .top, .bottom, .height, .centerY, .lastBaseline, .firstBaseline,
             .topMargin, .bottomMargin, .centerYWithinMargins:
            return .height
        case .notAnAttribute:
            return nil
        @unknown default:
            return nil
        }
    }

    /// Save initial constraint value
    func saveConstant() {
        initialConstant = constant
    }

    /// Reset constraint value to its initial value
    func resetConstant() {
        if let initialConstant = initialConstant {
            constant = initialConstant
        }
    }
}
