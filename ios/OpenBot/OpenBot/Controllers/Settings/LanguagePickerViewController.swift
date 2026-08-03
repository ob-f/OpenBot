//
// Created for in-app language switching.
//

import UIKit

/// Full-screen language picker presented from the Settings screen, mirroring the Android app's
/// `LanguagePickerDialogFragment`: a rounded card listing every `LanguageOption` with a flag,
/// a checkmark on the current selection, and a Cancel button.
final class LanguagePickerViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {
    private let card = UIView()
    private let titleLabel = UILabel()
    private let tableView = UITableView()
    private let cancelButton = UIButton(type: .system)
    private let cellReuseIdentifier = "LanguageOptionCell"

    private let options = LocalizationManager.options
    private let selectedTag = LocalizationManager.currentTag

    override func viewDidLoad() {
        super.viewDidLoad()
        modalPresentationStyle = .overFullScreen
        modalTransitionStyle = .crossDissolve
        view.backgroundColor = UIColor.black.withAlphaComponent(0.4)

        setupCard()
        setupTitleLabel()
        setupTableView()
        setupCancelButton()
    }

    private func setupCard() {
        card.backgroundColor = Colors.bdColor ?? .white
        card.layer.cornerRadius = 12
        card.clipsToBounds = true
        card.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(card)

        NSLayoutConstraint.activate([
            card.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: 24),
            card.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor, constant: -24),
            card.centerYAnchor.constraint(equalTo: view.centerYAnchor),
        ])
    }

    private func setupTitleLabel() {
        titleLabel.text = Strings.language
        titleLabel.textColor = Colors.border
        titleLabel.font = UIFont.boldSystemFont(ofSize: 18)
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        card.addSubview(titleLabel)

        NSLayoutConstraint.activate([
            titleLabel.topAnchor.constraint(equalTo: card.topAnchor, constant: 16),
            titleLabel.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 16),
            titleLabel.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -16),
        ])
    }

    private func setupTableView() {
        tableView.dataSource = self
        tableView.delegate = self
        tableView.register(UITableViewCell.self, forCellReuseIdentifier: cellReuseIdentifier)
        tableView.rowHeight = 52
        tableView.isScrollEnabled = false
        tableView.translatesAutoresizingMaskIntoConstraints = false
        card.addSubview(tableView)

        // The table has no intrinsic content size on its own, so its height must be pinned
        // explicitly -- otherwise Auto Layout collapses it to zero and no rows are visible.
        let contentHeight = tableView.rowHeight * CGFloat(options.count)
        let maxHeight = UIScreen.main.bounds.height * 0.5
        let tableHeight = min(contentHeight, maxHeight)
        tableView.isScrollEnabled = contentHeight > maxHeight

        NSLayoutConstraint.activate([
            tableView.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 8),
            tableView.leadingAnchor.constraint(equalTo: card.leadingAnchor),
            tableView.trailingAnchor.constraint(equalTo: card.trailingAnchor),
            tableView.bottomAnchor.constraint(equalTo: card.bottomAnchor),
            tableView.heightAnchor.constraint(equalToConstant: tableHeight),
        ])
    }

    private func setupCancelButton() {
        cancelButton.setTitle(Strings.cancel, for: .normal)
        cancelButton.setTitleColor(.white, for: .normal)
        cancelButton.titleLabel?.font = UIFont.boldSystemFont(ofSize: 16)
        cancelButton.backgroundColor = .black
        cancelButton.layer.cornerRadius = 8
        cancelButton.addTarget(self, action: #selector(cancelTapped), for: .touchUpInside)
        cancelButton.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(cancelButton)

        NSLayoutConstraint.activate([
            cancelButton.topAnchor.constraint(equalTo: card.bottomAnchor, constant: 16),
            cancelButton.leadingAnchor.constraint(equalTo: card.leadingAnchor),
            cancelButton.trailingAnchor.constraint(equalTo: card.trailingAnchor),
            cancelButton.heightAnchor.constraint(equalToConstant: 44),
        ])
    }

    @objc private func cancelTapped() {
        dismiss(animated: true)
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        options.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: cellReuseIdentifier, for: indexPath)
        let option = options[indexPath.row]
        cell.textLabel?.text = "\(option.flagEmoji)  \(option.displayName)"
        cell.textLabel?.textColor = Colors.border
        cell.accessoryType = option.tag == selectedTag ? .checkmark : .none
        cell.selectionStyle = .default
        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let option = options[indexPath.row]
        // Let the picker fully dismiss before starting the transition, otherwise the two
        // presentations race each other.
        dismiss(animated: true) {
            if let presenter = UIApplication.shared.connectedScenes
                .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
                .first?.rootViewController {
                LocalizationManager.apply(tag: option.tag, from: presenter)
            }
        }
    }
}
