//
// Created by Nitish Yadav on 06/04/23.
//

import Foundation
import UIKit
class CustomLabel : UILabel {
    override init(frame: CGRect) {
        super.init(frame: frame)
    }

    convenience init(text : String,fontSize : CGFloat, fontColor : UIColor,frame : CGRect){
        self.init(frame: frame)
        createLabel(text: text, fontSize: fontSize, fontColor: fontColor, frame: frame);
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    func createLabel(text: String, fontSize: CGFloat, fontColor: UIColor, frame: CGRect) {
        self.text = text
        self.frame = frame
        font = font.withSize(fontSize)
        textColor = fontColor
    }
}