//
// Created for in-app language switching.
//

import UIKit

/// Branded transition covering the window while `LocalizationManager` rebuilds the root view
/// controller, playing the same role as Android's `activity_language_applying` screen.
final class LanguageApplyingView: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .systemBackground

        let logo = UIImageView(image: Images.openBotLogo)
        logo.contentMode = .scaleAspectFit
        logo.translatesAutoresizingMaskIntoConstraints = false

        let spinner = UIActivityIndicatorView(style: .medium)
        spinner.color = Colors.title
        spinner.startAnimating()
        spinner.translatesAutoresizingMaskIntoConstraints = false

        let message = UILabel()
        message.text = Strings.applyingLanguage
        message.textColor = Colors.border
        message.font = UIFont.systemFont(ofSize: 14)
        message.translatesAutoresizingMaskIntoConstraints = false

        addSubview(logo)
        addSubview(spinner)
        addSubview(message)

        NSLayoutConstraint.activate([
            logo.centerXAnchor.constraint(equalTo: centerXAnchor),
            logo.centerYAnchor.constraint(equalTo: centerYAnchor, constant: -40),
            logo.widthAnchor.constraint(equalToConstant: 96),
            logo.heightAnchor.constraint(equalToConstant: 96),

            spinner.centerXAnchor.constraint(equalTo: centerXAnchor),
            spinner.topAnchor.constraint(equalTo: logo.bottomAnchor, constant: 24),

            message.centerXAnchor.constraint(equalTo: centerXAnchor),
            message.topAnchor.constraint(equalTo: spinner.bottomAnchor, constant: 16),
        ])
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
