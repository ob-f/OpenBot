//
// Created by Nitish Yadav on 13/09/22.
//

import UIKit

/// The type of devices considered in the app
enum Device {
    case iPhoneSE
    case iPhone7
    case iPhone8
    case iPhone8Plus
    case iPhone11Pro
    case iPhone11ProMax
    case iPhone11ProLandscape
    case iphone8Landscape
    case iPhone10R
    case iPhone10RLandscape
    case iPhone12
    case iPhone12Landscape
    case iPhone13
    case iPhone13Landscape
    static let baseScreenSize: Device = .iPhoneSE
}

/// Device identification
extension Device: RawRepresentable {
    typealias RawValue = CGSize

    init?(rawValue: CGSize) {
        switch rawValue {
        case CGSize(width: 320, height: 568):
            self = .iPhoneSE
        case CGSize(width: 375, height: 667):
            self = .iPhone8
        case CGSize(width: 667, height: 375):
            self = .iphone8Landscape
        case CGSize(width: 414, height: 736):
            self = .iPhone8Plus
        case CGSize(width: 375, height: 812):
            self = .iPhone11Pro
        case CGSize(width: 812, height: 375):
            self = .iPhone11ProLandscape
        case CGSize(width: 414, height: 896):
            self = .iPhone11ProMax
        case CGSize(width: 375, height: 812):
            self = .iPhone10R
        case CGSize(width: 414, height: 896):
            self = .iPhone10RLandscape
        case CGSize(width: 896, height: 414):
            self = .iPhone12
        case CGSize(width: 390, height: 844):
            self = .iPhone13
        case CGSize(width: 844, height: 390):
            self = .iPhone12Landscape
        case CGSize(width: 844, height: 390):
            self = .iPhone13Landscape
        default:
            return nil
        }
    }

    var rawValue: CGSize {
        switch self {
        case .iPhoneSE:
            return CGSize(width: 320, height: 568)
        case .iPhone8:
            return CGSize(width: 375, height: 667)
        case .iPhone8Plus:
            return CGSize(width: 414, height: 736)
        case .iPhone10R:
            return CGSize(width: 414, height: 896)
        case .iPhone10RLandscape:
            return CGSize(width: 896, height: 414)
        case .iPhone11Pro:
            return CGSize(width: 375, height: 812)
        case .iPhone11ProMax:
            return CGSize(width: 414, height: 896)
        case .iPhone11ProLandscape:
            return CGSize(width: 812, height: 414)
        case .iphone8Landscape:
            return CGSize(width: 667, height: 375)
        case .iPhone7:
            return CGSize(width: 320, height: 568)
        case .iPhone12:
            return CGSize(width: 390, height: 844)
        case .iPhone13:
            return CGSize(width: 390, height: 844)
        case .iPhone12Landscape:
            return CGSize(width: 844, height: 390)
        case .iPhone13Landscape:
            return CGSize(width: 844, height: 390)
        }
    }
}
