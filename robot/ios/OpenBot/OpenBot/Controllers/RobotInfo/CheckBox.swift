//
// Created by Nitish Yadav on 22/12/22.
//

import Foundation
import UIKit

/// Checkbox is a simple, animation free checkbox and UISwitch alternative designed
/// to be performant and easy to implement.
public class Checkbox: UIControl {

    // MARK: - Enums

    /// Shape of the center checkmark that appears when `Checkbox.isChecked == true`.
    public enum CheckmarkStyle {
        /// ■
        case square
        /// ●
        case circle
        /// ╳
        case cross
        /// ✓
        case tick
    }

    /// Shape of the outside box containing the checkmarks contents.
    ///
    /// Used as a visual indication of where the user can tap.
    public enum BorderStyle {
        /// ▢
        case square
        /// ◯
        case circle
    }

    // MARK: - Properties

    /// Shape of the center checkmark that appears when `Checkbox.isChecked == true`.
    ///
    /// **Default:** `CheckmarkStyle.square`
    public var checkmarkStyle: CheckmarkStyle = .square

    /// Shape of the outside border containing the checkmarks contents.
    ///
    /// Used as a visual indication of where the user can tap.
    ///
    /// **Default:** `BorderStyle.square`
    public var borderStyle: BorderStyle = .square

    /// Width of the borders stroke.
    ///
    /// **NOTE**
    ///
    /// Diagonal/rounded lines tend to appear thicker, so border styles
    /// that use these (.circle) have had their border widths halved to compensate
    /// in order appear similar next to other border styles.
    ///
    /// **Default:** `2`
    public var borderLineWidth: CGFloat = 2

    /// Size of the center checkmark element.
    ///
    /// Drawn as a percentage of the size of the Checkbox's draw rect.
    ///
    /// **Default:** `0.5`
    public var checkmarkSize: CGFloat = 0.5

    /// The checboxes border color in its unchecked state.
    ///
    /// **Default:** The current tintColor.
    public var uncheckedBorderColor: UIColor!

    /// The checboxes border color in its checked state.
    ///
    /// **Default:** The current tintColor.
    public var checkedBorderColor: UIColor!

    /// **Default:** The current tintColor.
    public var checkmarkColor: UIColor!

    /// **Default:** White.
    public var checkboxBackgroundColor: UIColor! = .white

    /// The checkboxes fill color.
    ///
    /// **Default:** `UIColoe.Clear`
    public var checkboxFillColor: UIColor = .clear

    /// Sets the corner radius for the checkbox border.
    ///
    ///**Default:** `0.0`
    /// - Note: Only applies to checkboxes with `BorderStyle.square`
    public var borderCornerRadius: CGFloat = 0.0

    /// Increases the controls touch area.
    ///
    /// Checkbox's tend to be smaller than regular UIButton elements
    /// and in some cases making them difficult to interact with.
    /// This property helps with that.
    ///
    /// **Default:** `5`
    public var increasedTouchRadius: CGFloat = 5

    /// A function can be passed in here and will be called
    /// when the `isChecked` value changes due to a tap gesture
    /// triggered by the user.
    ///
    /// An alternative to use the TargetAction method.
    public var valueChanged: ((_ isChecked: Bool) -> Void)?

    /// Indicates whether the checkbox is currently in a state of being
    /// checked or not.
    public var isChecked: Bool = false {
        didSet {
            setNeedsDisplay()
        }
    }

    /// Determines if tapping the checkbox generates haptic feedback to the user.
    ///
    /// **Default:** `true`
    public var useHapticFeedback: Bool = true

    private var feedbackGenerator: UIImpactFeedbackGenerator?

    // MARK: - Lifecycle

    public override init(frame: CGRect) {
        super.init(frame: frame)
        setupDefaults()
    }

    public required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setupDefaults()
    }

    private func setupDefaults() {
        backgroundColor = UIColor.init(white: 1, alpha: 0)
        uncheckedBorderColor = tintColor
        checkedBorderColor = tintColor
        checkmarkColor = tintColor

        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleTapGesture(recognizer:)))
        addGestureRecognizer(tapGesture)

        if useHapticFeedback {
            feedbackGenerator = UIImpactFeedbackGenerator(style: .light)
            feedbackGenerator?.prepare()
        }
    }

    override public func draw(_ rect: CGRect) {
        drawBorder(shape: borderStyle, in: rect)
        if isChecked {
            drawCheckmark(style: checkmarkStyle, in: rect)
        }
    }

    // MARK: - Borders

    private func drawBorder(shape: BorderStyle, in rect: CGRect) {
        let adjustedRect = CGRect(x: borderLineWidth / 2,
                y: borderLineWidth / 2,
                width: rect.width - borderLineWidth,
                height: rect.height - borderLineWidth)

        switch shape {
        case .circle:
            circleBorder(rect: adjustedRect)
        case .square:
            squareBorder(rect: adjustedRect)
        }
    }

    private func squareBorder(rect: CGRect) {
        let rectanglePath = UIBezierPath(roundedRect: rect, cornerRadius: borderCornerRadius)

        if isChecked {
            checkedBorderColor.setStroke()
        } else {
            uncheckedBorderColor.setStroke()
        }

        rectanglePath.lineWidth = borderLineWidth
        rectanglePath.stroke()
        checkboxFillColor.setFill()
        rectanglePath.fill()
    }

    private func circleBorder(rect: CGRect) {
        let ovalPath = UIBezierPath(ovalIn: rect)

        if isChecked {
            checkedBorderColor.setStroke()
        } else {
            uncheckedBorderColor.setStroke()
        }

        ovalPath.lineWidth = borderLineWidth / 2
        ovalPath.stroke()
        checkboxFillColor.setFill()
        ovalPath.fill()
    }

    // MARK: - Checkmarks

    private func drawCheckmark(style: CheckmarkStyle, in rect: CGRect) {
        let adjustedRect = checkmarkRect(in: rect)
        switch checkmarkStyle {
        case .square:
            squareCheckmark(rect: adjustedRect)
        case .circle:
            circleCheckmark(rect: adjustedRect)
        case .cross:
            crossCheckmark(rect: adjustedRect)
        case .tick:
            tickCheckmark(rect: adjustedRect)
        }
    }

    private func circleCheckmark(rect: CGRect) {
        let ovalPath = UIBezierPath(ovalIn: rect)
        checkmarkColor.setFill()
        ovalPath.fill()
    }

    private func squareCheckmark(rect: CGRect) {
        let path = UIBezierPath(rect: rect)
        checkmarkColor.setFill()
        path.fill()
    }

    private func crossCheckmark(rect: CGRect) {
        let bezier4Path = UIBezierPath()
        bezier4Path.move(to: CGPoint(x: rect.minX + 0.06250 * rect.width, y: rect.minY + 0.06452 * rect.height))
        bezier4Path.addLine(to: CGPoint(x: rect.minX + 0.93750 * rect.width, y: rect.minY + 0.93548 * rect.height))
        bezier4Path.move(to: CGPoint(x: rect.minX + 0.93750 * rect.width, y: rect.minY + 0.06452 * rect.height))
        bezier4Path.addLine(to: CGPoint(x: rect.minX + 0.06250 * rect.width, y: rect.minY + 0.93548 * rect.height))
        checkmarkColor.setStroke()
        bezier4Path.lineWidth = checkmarkSize * 2
        bezier4Path.stroke()
    }

    private func tickCheckmark(rect: CGRect) {
        let bezierPath = UIBezierPath()
        bezierPath.move(to: CGPoint(x: rect.minX + 0.04688 * rect.width, y: rect.minY + 0.63548 * rect.height))
        bezierPath.addLine(to: CGPoint(x: rect.minX + 0.34896 * rect.width, y: rect.minY + 0.95161 * rect.height))
        bezierPath.addLine(to: CGPoint(x: rect.minX + 0.95312 * rect.width, y: rect.minY + 0.04839 * rect.height))
        checkmarkColor.setStroke()
        bezierPath.lineWidth = checkmarkSize * 2
        bezierPath.stroke()
    }

    // MARK: - Size Calculations

    private func checkmarkRect(in rect: CGRect) -> CGRect {
        let width = rect.maxX * checkmarkSize
        let height = rect.maxY * checkmarkSize
        let adjustedRect = CGRect(x: (rect.maxX - width) / 2,
                y: (rect.maxY - height) / 2,
                width: width,
                height: height)
        return adjustedRect
    }

    // MARK: - Touch

    @objc private func handleTapGesture(recognizer: UITapGestureRecognizer) {
        isChecked = !isChecked
        valueChanged?(isChecked)
        sendActions(for: .valueChanged)

        if useHapticFeedback {
            // Trigger impact feedback.
            feedbackGenerator?.impactOccurred()

            // Keep the generator in a prepared state.
            feedbackGenerator?.prepare()
        }
    }

    override public func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        let relativeFrame = bounds
        let hitTestEdgeInsets = UIEdgeInsets(top: -increasedTouchRadius, left: -increasedTouchRadius, bottom: -increasedTouchRadius, right: -increasedTouchRadius)
        let hitFrame = relativeFrame.inset(by: hitTestEdgeInsets)
        return hitFrame.contains(point)
    }
}
