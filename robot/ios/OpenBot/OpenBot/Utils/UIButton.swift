//
// Created by Nitish Yadav on 11/04/23.
//

import Foundation
import UIKit

extension UIButton {
    func setInsets(
            forContentPadding contentPadding: UIEdgeInsets,
            imageTitlePadding: CGFloat
    ) {
        var config = UIButton.Configuration.filled()
        config.contentInsets = NSDirectionalEdgeInsets(top: contentPadding.top, leading: contentPadding.left, bottom: contentPadding.bottom, trailing: contentPadding.right + imageTitlePadding)
        config.titlePadding = imageTitlePadding

        let button = UIButton(frame: CGRect(x: 0, y: 0, width: 100, height: 50))
        button.configuration = config
        button.setTitle("Button Title", for: .normal)
    }
}

class CustomButton: UIButton {
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    convenience init(text: String, frame: CGRect,selector :Selector) {
        self.init(frame: frame)
        setTitle(text, for: .normal);
        layer.cornerRadius = 10;
        layer.borderColor = Colors.title?.cgColor
        layer.borderWidth = 2;
        setTitleColor(traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black, for: .normal);
        addTarget(nil, action: selector, for: .touchUpInside);
    }
}
