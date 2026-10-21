//
// Created by Nitish Yadav on 09/09/22.
//

import Foundation
import UIKit

/// To recognise and store the orientation of the device.
class DeviceCurrentOrientation {
    static let shared: DeviceCurrentOrientation = DeviceCurrentOrientation()

    /// function to find the current orientation of the device and store it in currentOrientation variable.
    func findDeviceOrientation() {
        let isPortrait = UIDevice.current.orientation.isPortrait
        let isLandscape = UIDevice.current.orientation.isLandscape
        if (isPortrait == false && isLandscape == false) {
            return // improper orientation (i.e. camera facing the ground or the sky)
        }
        switch UIDevice.current.orientation {
        case .landscapeLeft:
            currentOrientation = .landscapeLeft
            break
        case .landscapeRight:
            currentOrientation = .landscapeRight
            break
        case .portrait:
            currentOrientation = .portrait
            break
        case .portraitUpsideDown:
            currentOrientation = .portraitUpsideDown
            break
        default:
            currentOrientation = .unknown
            break
        }
    }
}
