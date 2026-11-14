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
    case iPhoneSELandscape
    case iPhone8PlusLandscape
    // Newer iPhones by screen size, one entry covers every model with that screen:
    // iPhone14Pro = 14 Pro/15/15 Pro/16, iPhone14ProMax = 14 Pro Max/15 Plus/15 Pro Max/16 Plus
    // iPhone16Pro = 16 Pro/17/17 Pro/18 Pro, iPhone16ProMax = 16 Pro Max/17 Pro Max/18 Pro Max
    // iPhone14Plus = 12 Pro Max/13 Pro Max/14 Plus, iPhone12Mini = 12 mini/13 mini
    case iPhone12Mini
    case iPhone12MiniLandscape
    case iPhone14Pro
    case iPhone14ProLandscape
    case iPhone14Plus
    case iPhone14PlusLandscape
    case iPhone14ProMax
    case iPhone14ProMaxLandscape
    case iPhoneAir
    case iPhoneAirLandscape
    case iPhone16Pro
    case iPhone16ProLandscape
    case iPhone16ProMax
    case iPhone16ProMaxLandscape
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
            self = .iPhone10RLandscape
        case CGSize(width: 390, height: 844):
            self = .iPhone13
        case CGSize(width: 844, height: 390):
            self = .iPhone12Landscape
        case CGSize(width: 844, height: 390):
            self = .iPhone13Landscape
        // Newer iPhones
        case CGSize(width: 360, height: 780):
            self = .iPhone12Mini
        case CGSize(width: 780, height: 360):
            self = .iPhone12MiniLandscape
        case CGSize(width: 393, height: 852):
            self = .iPhone14Pro
        case CGSize(width: 852, height: 393):
            self = .iPhone14ProLandscape
        case CGSize(width: 428, height: 926):
            self = .iPhone14Plus
        case CGSize(width: 926, height: 428):
            self = .iPhone14PlusLandscape
        case CGSize(width: 430, height: 932):
            self = .iPhone14ProMax
        case CGSize(width: 932, height: 430):
            self = .iPhone14ProMaxLandscape
        case CGSize(width: 420, height: 912):
            self = .iPhoneAir
        case CGSize(width: 912, height: 420):
            self = .iPhoneAirLandscape
        case CGSize(width: 402, height: 874):
            self = .iPhone16Pro
        case CGSize(width: 874, height: 402):
            self = .iPhone16ProLandscape
        case CGSize(width: 440, height: 956):
            self = .iPhone16ProMax
        case CGSize(width: 956, height: 440):
            self = .iPhone16ProMaxLandscape
        case CGSize(width: 568, height: 320):
            self = .iPhoneSELandscape
        case CGSize(width: 736, height: 414):
            self = .iPhone8PlusLandscape
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
            return CGSize(width: 812, height: 375)
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
        case .iPhoneSELandscape:
            return CGSize(width: 568, height: 320)
        case .iPhone8PlusLandscape:
            return CGSize(width: 736, height: 414)
        case .iPhone12Mini:
            return CGSize(width: 360, height: 780)
        case .iPhone12MiniLandscape:
            return CGSize(width: 780, height: 360)
        case .iPhone14Pro:
            return CGSize(width: 393, height: 852)
        case .iPhone14ProLandscape:
            return CGSize(width: 852, height: 393)
        case .iPhone14Plus:
            return CGSize(width: 428, height: 926)
        case .iPhone14PlusLandscape:
            return CGSize(width: 926, height: 428)
        case .iPhone14ProMax:
            return CGSize(width: 430, height: 932)
        case .iPhone14ProMaxLandscape:
            return CGSize(width: 932, height: 430)
        case .iPhoneAir:
            return CGSize(width: 420, height: 912)
        case .iPhoneAirLandscape:
            return CGSize(width: 912, height: 420)
        case .iPhone16Pro:
            return CGSize(width: 402, height: 874)
        case .iPhone16ProLandscape:
            return CGSize(width: 874, height: 402)
        case .iPhone16ProMax:
            return CGSize(width: 440, height: 956)
        case .iPhone16ProMaxLandscape:
            return CGSize(width: 956, height: 440)
        }
    }
}
