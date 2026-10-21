//
// Created by Nitish Yadav on 28/11/22.
//

import Foundation
import UIKit
import UniformTypeIdentifiers

class BottomSheet: UIViewController, UITableViewDataSource, UITableViewDelegate, UIDocumentPickerDelegate, UITextFieldDelegate {
    let table = UITableView()
    var widthOfTable: NSLayoutConstraint!;
    var heightOfTable: NSLayoutConstraint!
    var topAnchorOfTable: NSLayoutConstraint!;
    var leadingAnchorOFTable: NSLayoutConstraint!;
    var bottomSheetArray = [Strings.file, Strings.url]
    var header = UIView()
    var headerWidth: NSLayoutConstraint!;
    let label = UILabel();
    let urlInputBox = UITextField()

    /// Called after the view controller has loaded.
    override func viewDidLoad() {
        super.viewDidLoad()
        setupTable()
        setupHeader()
        createButton(title: Strings.cancel, leadingAnchor: 20, action: #selector(cancel(_:)), color: .red, isTrailingAnchor: false);
        createButton(title: Strings.done, leadingAnchor: -20, action: #selector(done(_:)), color: .blue, isTrailingAnchor: true);
        createLabel()
        urlInputBox.delegate = self
        createUrlTextField()
    }

    func setupTable() {
        table.dataSource = self
        table.delegate = self
        table.register(UITableViewCell.self, forCellReuseIdentifier: "cell");
        view.addSubview(table)
        table.translatesAutoresizingMaskIntoConstraints = false
        topAnchorOfTable = table.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 50);
        leadingAnchorOFTable = table.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: 0);
        NSLayoutConstraint.activate([topAnchorOfTable, leadingAnchorOFTable])
        if currentOrientation == .portrait {
            widthOfTable = table.widthAnchor.constraint(equalToConstant: width)
            heightOfTable = table.heightAnchor.constraint(equalToConstant: height)


        } else {
            widthOfTable = table.widthAnchor.constraint(equalToConstant: height)
            heightOfTable = table.heightAnchor.constraint(equalToConstant: width)
        }
        NSLayoutConstraint.activate([widthOfTable, heightOfTable]);
    }

    func setupHeader() {
        view.addSubview(header);
        header.translatesAutoresizingMaskIntoConstraints = false;
        if currentOrientation == .portrait {
            headerWidth = header.widthAnchor.constraint(equalToConstant: width);
        } else {
            headerWidth = header.widthAnchor.constraint(equalToConstant: height);
        }
        NSLayoutConstraint.activate([headerWidth]);
        header.heightAnchor.constraint(equalToConstant: 50).isActive = true;
        header.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: 0).isActive = true;
        header.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 0).isActive = true;
    }

    func createButton(title: String, leadingAnchor: CGFloat, action: Selector, color: UIColor, isTrailingAnchor: Bool) {
        let btn = UIButton();
        header.addSubview(btn);
        btn.translatesAutoresizingMaskIntoConstraints = false;
        switch isTrailingAnchor {
        case true:
            btn.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor, constant: leadingAnchor).isActive = true;
        case false:
            btn.leadingAnchor.constraint(equalTo: header.leadingAnchor, constant: leadingAnchor).isActive = true;
        }
        btn.topAnchor.constraint(equalTo: header.safeAreaLayoutGuide.topAnchor, constant: 10).isActive = true;
        btn.widthAnchor.constraint(equalToConstant: 80).isActive = true;
        btn.backgroundColor = color
        btn.layer.cornerRadius = 5
        btn.setTitle(title, for: .normal);
        btn.titleLabel?.textColor = Colors.border;
        btn.addTarget(self, action: action, for: .touchUpInside);
    }

    func createLabel() {

        label.text = Strings.addNewModel
        label.frame = CGRect(x: width / 2 - 50, y: 0, width: 120, height: 50);
        label.textColor = Colors.border
        label.font = label.font.withSize(15)
        view.addSubview(label)
    }

    func createUrlTextField() {
        urlInputBox.text = ""
        view.addSubview(urlInputBox);
        urlInputBox.isHidden = true;
        urlInputBox.addTarget(self, action: #selector(nameDidChange(_:)), for: .editingChanged);
        urlInputBox.frame = currentOrientation == .portrait ? CGRect(x: 70 + safeAreaLayoutValue.left, y: 100, width: width - 90, height: 50) :
                CGRect(x: 70 + safeAreaLayoutValue.top, y: 100, width: height - 170, height: 50);
        urlInputBox.layer.borderColor = Colors.border?.cgColor
        urlInputBox.layer.borderWidth = 1;
        urlInputBox.layer.cornerRadius = 5;
    }

    /// Called when the view controller's view's size is changed by its parent (i.e. for the root view controller when its window rotates or is resized).
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
        if currentOrientation == .portrait {
            widthOfTable.constant = width;
            heightOfTable.constant = height / 2
            label.frame.origin = CGPoint(x: width / 2 - 50, y: 0)
            urlInputBox.frame.size.width = width - 90;
            urlInputBox.frame.origin.x = 70 + safeAreaLayoutValue.left
        } else {
            widthOfTable.constant = height;
            heightOfTable.constant = width / 2
            label.frame.origin = CGPoint(x: height / 2 - 50, y: 0)
            urlInputBox.frame.size.width = height - 170;
            urlInputBox.frame.origin.x = 70 + safeAreaLayoutValue.top
        }
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        50
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        2
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell")!
        cell.textLabel?.text = bottomSheetArray[indexPath.row]
        cell.textLabel?.textColor = Colors.border
        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        switch indexPath.row {
        case 0:
            openDocumentPicker()
        case 1:
            urlInputBox.isHidden = false
            let vc = self
            if let sheet = vc.sheetPresentationController {
                sheet.detents = [.large()]

            }
        default:
            break;
        }

    }

    /// function to open bottom sheet to pick tflite models.
    func openDocumentPicker() {
        let tfliteFile = UTType("com.openbot.tflite")!
        let documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: [tfliteFile])

        documentPicker.delegate = self
        documentPicker.modalPresentationStyle = .overFullScreen
        present(documentPicker, animated: true)
    }

    /// function to pick documents from the file explorer.
    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentAt url: URL) {
        guard url.startAccessingSecurityScopedResource() else {
            return
        }
        defer {
            url.stopAccessingSecurityScopedResource()
        }
        var newURL = FileManager.getDocumentsDirectory()
        newURL.appendPathComponent(url.lastPathComponent)
        do {
            if FileManager.default.fileExists(atPath: newURL.path) {
                try FileManager.default.removeItem(atPath: newURL.path)
            }
            try FileManager.default.copyItem(atPath: url.path, toPath: newURL.path)
            print("The new URL: \(newURL)")
        } catch {
            print(error.localizedDescription)
        }

        var error: NSError? = nil
        NSFileCoordinator().coordinate(readingItemAt: url, error: &error) { (url) in

            let keys: [URLResourceKey] = [.nameKey, .isDirectoryKey]
            guard let fileList = FileManager.default.enumerator(at: url, includingPropertiesForKeys: keys)
            else {
                Swift.debugPrint("*** Unable to access the contents of \(url.path) ***\n")
                return
            }
            for case _ as URL in fileList {
                guard url.startAccessingSecurityScopedResource() else {
                    continue
                }
                url.stopAccessingSecurityScopedResource()
            }
        }
        let vc = self
        if let sheet = vc.sheetPresentationController {
            sheet.detents = [.large()]
        }
        let index = url.lastPathComponent.firstIndex(of: ".");
        var modelName: String = ""
        if index != nil {
            modelName = String(url.lastPathComponent.prefix(upTo: index!))
        }
        let popWindowView = popupWindowView(frame: CGRect(x: 10, y: 0, width: width - 20, height: 400), modelName, "", "ASSET")
        print("hello ", url.lastPathComponent)
        view.addSubview(popWindowView);
        popWindowView.backgroundColor = Colors.freeRoamButtonsColor
    }

    func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        print("inside documentPickerWasCancelled")
    }


    @objc func cancel(_ sender: UISwitch) {
        dismiss(animated: true)
    }

    @objc func done(_ sender: UISwitch) {
        urlInputBox.isHidden = true
        let url = URL(string: urlInputBox.text ?? "")
        if let url = url {
            FileDownloader.loadFileAsync(url: url, completion: { s, error in
                print("File downloaded to : \(s!)")
            }, fileName: nil)
        }
        let vc = self
        if let sheet = vc.sheetPresentationController {
            sheet.detents = [.large()]
        }
        let popWindowView = popupWindowView(frame: CGRect(x: 10, y: 0, width: width - 20, height: 400), "", urlInputBox.text ?? "", "URL")
        view.addSubview(popWindowView);
        popWindowView.backgroundColor = Colors.freeRoamButtonsColor
    }

    @objc func nameDidChange(_ textField: UITextField) {

    }

    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        urlInputBox.resignFirstResponder()  //if desired
        return true
    }

}

extension FileManager {

    static func getDocumentsDirectory() -> URL {
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        let documentsDirectory = paths[0]
        return documentsDirectory
    }
}
