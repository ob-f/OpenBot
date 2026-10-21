//
// Created by Nitish Yadav on 13/09/22.
//

import UIKit

/// Extension to CGFloat data type that returns an adapted font size.
extension CGFloat {
    var adaptedFontSize: CGFloat {
        adapted(dimensionSize: self, to: dimension)
    }
}

/// Extension to Float data type that returns a byte array representation.
extension Float {
    var bytes: [UInt8] {
        withUnsafeBytes(of: self, Array.init)
    }
}
