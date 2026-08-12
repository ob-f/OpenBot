//
// Created by Nitish Yadav on 06/04/23.
//

import Foundation
import UIKit
/**
 a custom class to create google sign-in button
 */
class GoogleSignInBtn : UIView {

    /**
     initializer of class
     - Parameter frame:
     */
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSignInBtn(frame: frame)
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    /**
     Function to create button
     - Parameter frame:
     */
    func createSignInBtn(frame: CGRect) {
        self.backgroundColor = UIColor(named: "signInButtonColor");
        layer.cornerRadius = 10;
        // Icon + text are centered as a group based on the text's real measured width, since a
        // fixed offset/width tuned for the English string leaves other languages off-center or
        // truncated (translations vary a lot in length).
        let font = UIFont.systemFont(ofSize: 18)
        let textWidth = measuredWidth(of: Strings.signInWithGoogle, font: font)
        let iconWidth: CGFloat = 20
        let spacing: CGFloat = 12
        let groupWidth = iconWidth + spacing + textWidth
        let groupStartX = (self.frame.width - groupWidth) / 2
        addSubview(createGoogleIcon(frame: CGRect(x: groupStartX, y: 16, width: iconWidth, height: iconWidth)));
        addSubview(createSingInText(frame: CGRect(x: groupStartX + iconWidth + spacing, y: 5, width: textWidth + 4, height: 40), font: font))
    }

    /**
     Function to add google icon inside the button
     - Parameter frame:
     - Returns:
     */
    private func createGoogleIcon(frame: CGRect) -> UIImageView {
        let googleIcon = UIImageView(frame: frame);
        googleIcon.image = UIImage(named: "googleIcon");
        return googleIcon;
    }

    /**
     Function to add text inside the function
     - Parameter frame:
     - Returns:
     */
    private func createSingInText(frame: CGRect, font: UIFont) -> UILabel {
        let signInText = UILabel(frame: frame);
        signInText.text = Strings.signInWithGoogle;
        signInText.textColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black
        signInText.font = font;
        return signInText;
    }

}