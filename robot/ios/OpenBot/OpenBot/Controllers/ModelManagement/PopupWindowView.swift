//
// Created by Nitish Yadav on 19/11/22.
//

import Foundation
import UIKit
import DropDown
import GoogleSignIn


class popupWindowView: UIView {
    var typeDropdown = DropDown()
    var typeDropdownView = UIView()
    var modelName: String = ""
    var pathType: String = ""
    var classDropdown = DropDown()
    var classDropdownView = UIView()
    var headingLeadingAnchor: NSLayoutConstraint!
    var typeDropdownLeadingAnchor: NSLayoutConstraint!
    var classDropdownLeadingAnchor: NSLayoutConstraint!
    var firstBox = UIView()
    var secondBox = UIView()
    var model: ModelItem!
    var widthOfModel: String!
    var heightOfModel: String!
    var modelAddress: String = ""


    /// Initializing function
    init(frame: CGRect, _ modelName: String, _ modelAddress: String, _ pathType: String) {
        super.init(frame: frame)
        self.modelName = modelName
        self.modelAddress = modelAddress
        self.pathType = pathType
        let tap = UITapGestureRecognizer(target: self, action: #selector(dismissKeyboard(_:)))
        setupModelItem();
        addGestureRecognizer(tap)
        createHeading();
        _ = createLabel(text: Strings.name, leadingAnchor: 20, topAnchor: 60);
        _ = createTextField(text: modelName, trailingAnchor: 300, topAnchor: 50);
        createtfliteLabel()
        createEditIcon()
        _ = createLabel(text: Strings.type, leadingAnchor: 20, topAnchor: 130);
        createTypeDropDown()
        _ = createLabel(text: Strings.class, leadingAnchor: 20, topAnchor: 200);
        createClassDropdown()
        _ = createLabel(text: Strings.inputOfModel, leadingAnchor: 20, topAnchor: 250);
        firstBox = createBox(isFirst: true);
        addSubview(firstBox)
        _ = createLabel(text: "x", leadingAnchor: width / 2 + 20, topAnchor: 250)
        secondBox = createBox(isFirst: false);
        addSubview(secondBox)
        createFirstInputTextField();
        createSecondInputField()
        createButton(label: Strings.cancel, leadingAnchor: 20, backgroundColor: Colors.freeRoamButtonsColor!, buttonWidth: 100, action: #selector(onCancelBtnTap(_:)))
        createButton(label: Strings.done, leadingAnchor: width / 2 - 50, backgroundColor: Colors.freeRoamButtonsColor!, buttonWidth: 200, action: #selector(onDoneBtnTap(_:)));
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }


    /// function to setup models list.
    func setupModelItem() {
        model = Common.returnModelItem(modelName: modelName)
        widthOfModel = ModelItem.getWidthOfInput(model.inputSize);
        heightOfModel = ModelItem.getHeightOfInput(model.inputSize);
        if pathType == "ASSET" {
            model.pathType = "ASSET"
        } else {
            model.pathType = "URL"
        }
    }

    /// function to create headings
    func createHeading() {
        let heading = UILabel();
        heading.text = Strings.modelDetails;
        heading.tintColor = Colors.title;
        heading.font = heading.font.withSize(22);
        addSubview(heading);
        heading.translatesAutoresizingMaskIntoConstraints = false;
        let headingWidth = measuredWidth(of: Strings.modelDetails, font: heading.font) + 8
        heading.widthAnchor.constraint(equalToConstant: headingWidth).isActive = true;
        heading.heightAnchor.constraint(equalToConstant: 40).isActive = true;
        heading.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: 10).isActive = true;
        headingLeadingAnchor = heading.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: width / 2 - headingWidth / 2)
        headingLeadingAnchor.isActive = true;
    }

    /// function to create labels.
    func createLabel(text: String, leadingAnchor: CGFloat, topAnchor: CGFloat) -> UILabel {
        let label = UILabel();
        label.text = text;
        addSubview(label);
        label.translatesAutoresizingMaskIntoConstraints = false;
        label.widthAnchor.constraint(equalToConstant: measuredWidth(of: text, font: label.font) + 8).isActive = true;
        label.heightAnchor.constraint(equalToConstant: 40).isActive = true;
        label.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: leadingAnchor).isActive = true;
        label.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: topAnchor).isActive = true;
        return label;
    }

    /// function to create text fields.
    func createTextField(text: String, trailingAnchor: CGFloat, topAnchor: CGFloat) -> UITextField {
        let textField = UITextField(frame: CGRect(x: 120, y: 50, width: 170, height: 60));
        textField.layer.borderWidth = 1;
        textField.text = text
        addSubview(textField);
        textField.addTarget(self, action: #selector(nameDidChange(_:)), for: .editingChanged);
        return textField;
    }

    /// function to create edit icons
    func createEditIcon() {
        let editIcon = UIImageView();
        editIcon.image = Images.edit;
        addSubview(editIcon);
        editIcon.translatesAutoresizingMaskIntoConstraints = false;
        editIcon.widthAnchor.constraint(equalToConstant: (Images.edit?.size.width ?? 20 * 1.2)).isActive = true;
        editIcon.heightAnchor.constraint(equalToConstant: (Images.edit?.size.height ?? 20 * 1.2)).isActive = true;
        editIcon.trailingAnchor.constraint(equalTo: safeAreaLayoutGuide.trailingAnchor, constant: -10).isActive = true;
        editIcon.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: 75).isActive = true
    }

    /// function to create tflite label.
    func createtfliteLabel() {
        let label = UILabel();
        label.text = Strings.tfliteLabel;
        addSubview(label)
        label.translatesAutoresizingMaskIntoConstraints = false;
        label.widthAnchor.constraint(equalToConstant: measuredWidth(of: Strings.tfliteLabel, font: label.font) + 8).isActive = true;
        label.heightAnchor.constraint(equalToConstant: 40).isActive = true;
        label.trailingAnchor.constraint(equalTo: safeAreaLayoutGuide.trailingAnchor, constant: 0).isActive = true
        label.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: 60).isActive = true
    }

    /// function to create type drop down.
    func createTypeDropDown() {
        typeDropdown.backgroundColor = Colors.freeRoamButtonsColor
        typeDropdown.textColor = Colors.border ?? .black
        typeDropdown.anchorView = typeDropdownView;
        typeDropdown.dataSource = Constants.types;
        let ddView = UIView();
        addSubview(ddView);
        ddView.translatesAutoresizingMaskIntoConstraints = false;
        ddView.widthAnchor.constraint(equalToConstant: width / 2 + 30).isActive = true;
        ddView.heightAnchor.constraint(equalToConstant: 35).isActive = true;
        typeDropdownLeadingAnchor = ddView.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.trailingAnchor, constant: -width / 2 - 50)
        typeDropdownLeadingAnchor.isActive = true
        ddView.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: 130).isActive = true;
        let tap = UITapGestureRecognizer(target: self, action: #selector(showTypeDropdown(_:)))
        ddView.addGestureRecognizer(tap)
        let modelTypeLabel = UILabel();
        modelTypeLabel.text = Common.returnModelItem(modelName: modelName).type
        ddView.addSubview(modelTypeLabel)
        modelTypeLabel.translatesAutoresizingMaskIntoConstraints = false;
        modelTypeLabel.widthAnchor.constraint(equalToConstant: 100).isActive = true;
        modelTypeLabel.heightAnchor.constraint(equalToConstant: 40).isActive = true;
        modelTypeLabel.leadingAnchor.constraint(equalTo: ddView.leadingAnchor, constant: 0).isActive = true;
        modelTypeLabel.topAnchor.constraint(equalTo: ddView.topAnchor, constant: 0).isActive = true
        modelTypeLabel.textColor = Colors.border
        typeDropdown.selectionAction = { [self] (index: Int, item: String) in
            modelTypeLabel.text = item
            model.type = item;
        }
        let upwardImage = UIImageView()
        upwardImage.frame.size = CGSize(width: 5, height: 5)
        upwardImage.image = Images.downArrow
        ddView.addSubview(upwardImage)
        upwardImage.tintColor = Colors.border
        upwardImage.translatesAutoresizingMaskIntoConstraints = false
        upwardImage.trailingAnchor.constraint(equalTo: ddView.trailingAnchor, constant: -10).isActive = true
        upwardImage.topAnchor.constraint(equalTo: ddView.topAnchor, constant: 11.5).isActive = true
    }

    /// function to create class drop down
    func createClassDropdown() {
        classDropdown.backgroundColor = Colors.freeRoamButtonsColor
        classDropdown.textColor = Colors.border ?? .black
        classDropdown.anchorView = classDropdownView;
        classDropdown.dataSource = ["AUTOPILOT_F", "MOBILENETV1_1_0_Q", "MOBILENETV3_S_Q", "YOLOV4", "NAVIGATION"]
        let ddView = UIView();
        addSubview(ddView);
        ddView.translatesAutoresizingMaskIntoConstraints = false;
        ddView.widthAnchor.constraint(equalToConstant: width / 2 + 30).isActive = true;
        ddView.heightAnchor.constraint(equalToConstant: 35).isActive = true;
        classDropdownLeadingAnchor = ddView.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.trailingAnchor, constant: -width / 2 - 50)
        classDropdownLeadingAnchor.isActive = true;
        ddView.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor, constant: 200).isActive = true;
        let tap = UITapGestureRecognizer(target: self, action: #selector(showClassDropdown(_:)))
        ddView.addGestureRecognizer(tap)
        let classLabel = UILabel();
        classLabel.text = Common.returnModelItem(modelName: modelName).class
        ddView.addSubview(classLabel)
        classLabel.translatesAutoresizingMaskIntoConstraints = false;
        classLabel.widthAnchor.constraint(equalToConstant: 150).isActive = true;
        classLabel.heightAnchor.constraint(equalToConstant: 40).isActive = true;
        classLabel.leadingAnchor.constraint(equalTo: ddView.leadingAnchor, constant: 0).isActive = true;
        classLabel.topAnchor.constraint(equalTo: ddView.topAnchor, constant: 0).isActive = true
        classLabel.textColor = Colors.border
        classDropdown.selectionAction = { [self] (index: Int, item: String) in
            classLabel.text = item
            model.class = item
        }
        let upwardImage = UIImageView()
        upwardImage.frame.size = CGSize(width: 5, height: 5)
        upwardImage.image = Images.downArrow
        ddView.addSubview(upwardImage)
        upwardImage.tintColor = Colors.border
        upwardImage.translatesAutoresizingMaskIntoConstraints = false
        upwardImage.trailingAnchor.constraint(equalTo: ddView.trailingAnchor, constant: -10).isActive = true
        upwardImage.topAnchor.constraint(equalTo: ddView.topAnchor, constant: 11.5).isActive = true
    }

    /// function to create box
    func createBox(isFirst: Bool) -> UIView {
        let box = isFirst ? UIView(frame: CGRect(x: 120, y:  250, width: 50, height: 40)) :
                UIView(frame: CGRect(x: 250, y: 250, width: 50, height: 40))
        box.layer.borderColor = Colors.border?.cgColor;
        box.layer.borderWidth = 1;
        return box
    }

    /// function to create first input text field for number.
    func createFirstInputTextField() {
        let input = UITextField();
        input.keyboardType = .numberPad;
        let modelInputSize = Common.returnModelItem(modelName: modelName).inputSize
        let index = modelInputSize.index(modelInputSize.startIndex, offsetBy: 3)
        input.text = String(modelInputSize[..<index])
        input.addTarget(self, action: #selector(wDidChange(_:)), for: .editingChanged);
        firstBox.addSubview(input);
        input.translatesAutoresizingMaskIntoConstraints = false;
        input.widthAnchor.constraint(equalToConstant: 40).isActive = true;
        input.heightAnchor.constraint(equalToConstant: 38).isActive = true;
        input.leadingAnchor.constraint(equalTo: firstBox.safeAreaLayoutGuide.leadingAnchor, constant: 10).isActive = true;
        input.topAnchor.constraint(equalTo: firstBox.safeAreaLayoutGuide.topAnchor, constant: 0).isActive = true
    }

    /// function to create second input text field for number.
    func createSecondInputField() {
        let input = UITextField();
        input.keyboardType = .numberPad;
        let modelInputSize = Common.returnModelItem(modelName: modelName).inputSize
        let index = modelInputSize.firstIndex(of: "x");
        let nextIndex = modelInputSize.index(after: index!)
        input.text = String(modelInputSize.suffix(from: nextIndex))
        input.addTarget(self, action: #selector(hDidChange(_:)), for: .editingChanged);
        secondBox.addSubview(input);
        input.translatesAutoresizingMaskIntoConstraints = false;
        input.widthAnchor.constraint(equalToConstant: 40).isActive = true;
        input.heightAnchor.constraint(equalToConstant: 38).isActive = true;
        input.leadingAnchor.constraint(equalTo: secondBox.safeAreaLayoutGuide.leadingAnchor, constant: 10).isActive = true;
        input.topAnchor.constraint(equalTo: secondBox.safeAreaLayoutGuide.topAnchor, constant: 0).isActive = true
    }

    /// function to create buttons
    func createButton(label: String, leadingAnchor: CGFloat, backgroundColor: UIColor, buttonWidth: CGFloat, action: Selector?) {
        let button = UIButton()
        button.setTitle(label, for: .normal);
        button.backgroundColor = backgroundColor
        button.layer.cornerRadius = 5;
        addSubview(button);
        button.translatesAutoresizingMaskIntoConstraints = false;
        button.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor, constant: leadingAnchor).isActive = true;
        button.bottomAnchor.constraint(equalTo: safeAreaLayoutGuide.bottomAnchor, constant: -10).isActive = true;
        button.heightAnchor.constraint(equalToConstant: 50).isActive = true;
        button.widthAnchor.constraint(equalToConstant: buttonWidth).isActive = true
        button.addTarget(self, action: action!, for: .touchUpInside)
    }

    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        endEditing(true)
        return true
    }

    @objc func showTypeDropdown(_ sender: UITapGestureRecognizer? = nil) {
        typeDropdown.show()
    }

    @objc func showClassDropdown(_ sender: UITapGestureRecognizer? = nil) {
        classDropdown.show()
    }

    /// handler when name is changed in text field.
    @objc func nameDidChange(_ textField: UITextField) {
        model.name = textField.text ?? model.name
        if !model.name.contains(Strings.tflite) {
            model.name.append(Strings.tflite)
        }
    }

    /// handler when width of model is updated
    @objc func wDidChange(_ textField: UITextField) {
        widthOfModel = textField.text ?? ModelItem.getWidthOfInput(model.inputSize);
    }

    /// handler when height of model is updated
    @objc func hDidChange(_ textField: UITextField) {
        heightOfModel = textField.text ?? ModelItem.getHeightOfInput(model.inputSize);
    }

    /// handler when tapped on the cancel button, removed the popup from the window.
    @objc func onCancelBtnTap(_ sender: UIButton) {
        NotificationCenter.default.post(name: .removeBlankScreen, object: nil);
        removeFromSuperview();
    }

    /// handler when tapped on the save/done button.
    @objc func onDoneBtnTap(_ sender: UIButton) throws {
        try Common.saveConfigFileToDocument(modelItems: Common.modifyModels(modelAddress: modelAddress, model: model, widthOfModel: widthOfModel, heightOfModel: heightOfModel));
        if let index = model.name.firstIndex(of: ".") {
            if GIDSignIn.sharedInstance.currentUser != nil {
                let encoder = JSONEncoder()
                encoder.outputFormatting = [.withoutEscapingSlashes]
                do {
                    let jsonData = try encoder.encode(Common.modifyModels(modelAddress: modelAddress, model: model, widthOfModel: widthOfModel, heightOfModel: heightOfModel));
                    if let jsonString = String(data: jsonData, encoding: .utf8) {
                        if let data = jsonString.data(using: .utf8) {
                            Authentication().updateModelListFile(fileData: data);
                        }
                    }
                }
            } else {
            }
            NotificationCenter.default.post(name: .removeBlankScreen, object: model.name.prefix(upTo: index));
        }
        removeFromSuperview();
    }

    /// to dismiss keyboard from the view.
    @objc func dismissKeyboard(_ sender: UIButton) {
        endEditing(true);
    }


}
