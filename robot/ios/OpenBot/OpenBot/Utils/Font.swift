//
// Created by Nitish Yadav on 13/09/22.
//

import UIKit

/// Select fonts used in the app
enum HelveticaNeue {
    static func regular(size: CGFloat) -> UIFont {
        UIFont(name: "HelveticaNeue", size: size.adaptedFontSize)!
    }

    static func bold(size: CGFloat) -> UIFont {
        UIFont(name: "HelveticaNeue-Bold", size: size.adaptedFontSize)!
    }
}
