//
// Created by Nitish Yadav on 13/09/22.
//

import UIKit

// Horizontal axis, in screen frame
var dimension: Dimension {
    UIDevice.current.orientation.isPortrait ? .width : .height
}

/// This function allows finetuning the height and width of an input objet to match the height and width of the user interface screen
///
/// - Parameters:
///     - size: height and width of an object to finetune
///     - dimension: reference screen dimension used to adjust the sized
func resized(size: CGSize, basedOn dimension: Dimension) -> CGSize {
    let uiScreenWidth = UIScreen.main.bounds.size.width // Width of the user interface screen
    let uiScreenHeight = UIScreen.main.bounds.size.height // Height of the user interface screen
    let device = Device.init(rawValue: UIScreen.main.bounds.size) ?? Device.baseScreenSize
    let rawScreenWidth = device.rawValue.width
    let rawScreenHeight = device.rawValue.height
    var ratio: CGFloat = 0.0
    var width: CGFloat = 0.0
    var height: CGFloat = 0.0

    switch dimension {
    case .width:
        ratio = size.height / size.width
        width = uiScreenWidth * (size.width / rawScreenWidth)
        height = width * ratio
    case .height:
        ratio = size.width / size.height
        height = uiScreenHeight * (size.height / rawScreenHeight)
        width = height * ratio
    }
    // Return the finetuned object size
    return CGSize(width: width, height: height)
}

/// Measures the real rendered width of `text` at `font`
func measuredWidth(of text: String, font: UIFont) -> CGFloat {
    ceil((text as NSString).size(withAttributes: [.font: font]).width)
}

/// This function allows finetuning a reference dimension to match the user interface screen
///
/// - Parameters:
///     - dimensionSize: dimension to map to the UI
///     - dimension: reference screen dimension used to adjust the size
func adapted(dimensionSize: CGFloat, to dimension: Dimension) -> CGFloat {
    let screenWidth = width
    let screenHeight = height
    var ratio: CGFloat = 0.0
    var resultDimensionSize: CGFloat = 0.0

    switch dimension {
    case .width:
        ratio = dimensionSize / Device.baseScreenSize.rawValue.width
        resultDimensionSize = screenWidth * ratio
    case .height:
        ratio = dimensionSize / Device.baseScreenSize.rawValue.height
        resultDimensionSize = screenHeight * ratio
    }
    return resultDimensionSize
}
