//
// Created by Hardik Garg on 03/08/26
//

import UIKit

struct LanguageOption {
    let tag: String?
    let displayName: String
    let flagEmoji: String
}

/// Supported in-app display languages, resolved into a `Bundle` that `L10n.string(_:)` reads.
enum LocalizationManager {
    private static let languageDefaultsKey = "AppLanguageOverride"
    private static let supportedTags = ["en", "de", "es", "fr", "zh-Hans", "ko", "hi"]

    static var options: [LanguageOption] {
        [
            LanguageOption(tag: nil, displayName: Strings.systemDefault, flagEmoji: "🌐"),
            LanguageOption(tag: "en", displayName: "English", flagEmoji: "🇺🇸"),
            LanguageOption(tag: "de", displayName: "Deutsch", flagEmoji: "🇩🇪"),
            LanguageOption(tag: "es", displayName: "Español", flagEmoji: "🇪🇸"),
            LanguageOption(tag: "fr", displayName: "Français", flagEmoji: "🇫🇷"),
            LanguageOption(tag: "zh-Hans", displayName: "中文", flagEmoji: "🇨🇳"),
            LanguageOption(tag: "ko", displayName: "한국어", flagEmoji: "🇰🇷"),
            LanguageOption(tag: "hi", displayName: "हिन्दी", flagEmoji: "🇮🇳"),
        ]
    }

    /// The chosen language tag, or `nil` when following the system language.
    static var currentTag: String? {
        get { UserDefaults.standard.string(forKey: languageDefaultsKey) }
        set {
            if let newValue = newValue {
                UserDefaults.standard.set(newValue, forKey: languageDefaultsKey)
            } else {
                UserDefaults.standard.removeObject(forKey: languageDefaultsKey)
            }
            cachedBundle = nil
        }
    }

    /// The flag emoji for the currently active language (falls back to the system-default option's).
    static var currentFlagEmoji: String {
        options.first(where: { $0.tag == currentTag })?.flagEmoji ?? options[0].flagEmoji
    }

    private static var cachedBundle: Bundle?

    /// The bundle every localized string lookup should use: the user's chosen language if set,
    /// otherwise the best match between the system's preferred languages and what we support.
    /// Cached until `currentTag` changes, since resolving it hits the file system.
    static var bundle: Bundle {
        if let cachedBundle = cachedBundle {
            return cachedBundle
        }
        let tag = currentTag ?? bestSystemMatch()
        let resolved: Bundle
        if let path = Bundle.main.path(forResource: tag, ofType: "lproj"),
           let bundle = Bundle(path: path) {
            resolved = bundle
        } else {
            resolved = .main
        }
        cachedBundle = resolved
        return resolved
    }

    private static func bestSystemMatch() -> String {
        Bundle.preferredLocalizations(from: supportedTags).first ?? "en"
    }

    /// Applies a newly selected language: persists it, then rebuilds the window's whole view
    static func apply(tag: String?, from viewController: UIViewController) {
        currentTag = tag

        guard let window = viewController.view.window else { return }

        let overlay = LanguageApplyingView(frame: window.bounds)
        overlay.alpha = 0
        window.addSubview(overlay)

        UIView.animate(withDuration: 0.2, animations: {
            overlay.alpha = 1
        }, completion: { _ in
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) {
                let newRoot = UIStoryboard(name: "Main", bundle: nil).instantiateInitialViewController()
                if let newRoot = newRoot {
                    refreshTabBarTitles(in: newRoot)
                }
                window.rootViewController = newRoot
                UIView.animate(withDuration: 0.3, animations: {
                    overlay.alpha = 0
                }, completion: { _ in
                    overlay.removeFromSuperview()
                })
            }
        })
    }

    static func refreshTabBarTitles(in root: UIViewController) {
        guard let tabBarController = findTabBarController(in: root) else { return }
        for child in tabBarController.viewControllers ?? [] {
            switch child {
            case is HomePageViewController:
                child.tabBarItem.title = Strings.homeTab
            case is projectFragment:
                child.tabBarItem.title = Strings.projectsTab
            case is profileFragment:
                child.tabBarItem.title = Strings.profileTab
            default:
                break
            }
        }
    }

    private static func findTabBarController(in viewController: UIViewController) -> UITabBarController? {
        if let tabBarController = viewController as? UITabBarController {
            return tabBarController
        }
        if let navigationController = viewController as? UINavigationController {
            return navigationController.viewControllers.first.flatMap(findTabBarController)
        }
        return viewController.children.first.flatMap(findTabBarController)
    }
}
