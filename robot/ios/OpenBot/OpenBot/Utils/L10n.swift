//
// Created for in-app language switching.
//

import Foundation

enum L10n {
    static func string(_ key: String) -> String {
        LocalizationManager.bundle.localizedString(forKey: key, value: key, table: nil)
    }
}
