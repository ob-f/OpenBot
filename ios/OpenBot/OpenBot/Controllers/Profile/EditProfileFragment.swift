//
// Created by Nitish Yadav on 11/04/23.
//

import Foundation
import UIKit
import GoogleSignIn
import FirebaseAuth
import FirebaseStorage
import FirebaseFirestore
/***
 class for fragment of edit profile section
 */
class editProfileFragment: UIViewController, UIImagePickerControllerDelegate, UINavigationControllerDelegate,UIScrollViewDelegate, UITextFieldDelegate {
/**
 Outlets for the user interface elements used in this view controller.
*/
    private var profileIcon: UIImageView!
    private let authentication = Authentication()
    private var firstName: UILabel!
    private var lastName: UILabel!
    let alert = UIAlertController(title: Strings.loadingTitle, message: Strings.loadingProfileMessage, preferredStyle: .alert)
    private var email: UILabel!
    private var dob: UILabel!
    private var firstNameField: UITextField!
    private var lastNameField: UITextField!
    private var dobField : UITextField!
    let imagePickerVC = UIImagePickerController()
    private var scrollView = UIScrollView();
    var saveChangesBtn = CustomButton();
    private var datePicker = UIDatePicker();

/**
    Method calls after view loaded
 */
    override func viewDidLoad() {
        super.viewDidLoad();
//        createOverlayAlert();
        setupNavigationBarItem();
        view.addSubview(scrollView);
        let contentHeight: CGFloat = currentOrientation == .portrait ? height * 0.6 : 1000;
        scrollView.contentSize = CGSize(width: width, height: contentHeight)
        imagePickerVC.delegate = self;
        createUserProfileImageView()
        scrollView.frame = currentOrientation == .portrait ? CGRect(x: 0, y: profileIcon.bottom + adapted(dimensionSize: 40, to: .height), width: width, height: height) : CGRect(x: height - width - safeAreaLayoutValue.bottom, y: profileIcon.top, width: width, height: height);
        createLabels();
        createTextFields();
        firstNameField.delegate = self;
        dobField.delegate = self;
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(dismissKeyboard))
        view.addGestureRecognizer(tapGesture)
        createButtons();
        scrollView.contentInsetAdjustmentBehavior = .never
    }

    /**
     override function calls on rotation of phone changes, this method is used to update the UI in landscape as well as portrait mode
     - Parameters:
       - size:
       - coordinator:
     */
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
        scrollView.setContentOffset(CGPoint(x: 0, y: 0), animated: false)
        if currentOrientation == .portrait {
            scrollView.isScrollEnabled = false
            scrollView.contentSize = CGSize(width: width, height: 800);
            scrollView.frame = CGRect(x: 0, y: profileIcon.bottom + adapted(dimensionSize: 40, to: .height), width: width, height: height)
            scrollView.contentInset.bottom = 0
        } else {
            scrollView.frame = CGRect(x: height - width - safeAreaLayoutValue.bottom, y: profileIcon.top, width: width, height: height);
            scrollView.contentSize = CGSize(width: width, height: 1000);
            scrollView.isScrollEnabled = true
            scrollView.contentInset.bottom = view.safeAreaInsets.bottom
            scrollView.contentOffset.y += view.safeAreaInsets.bottom
        }
    }

    /**
     function creates the user profile image view and related components.
     */
    private func createUserProfileImageView() {
        profileIcon = UIImageView(frame: CGRect(x: (width - 121) / 2, y: safeAreaLayoutValue.top + adapted(dimensionSize: 20, to: .height), width: adapted(dimensionSize: 100, to: .height), height: adapted(dimensionSize: 100, to: .height)));
        profileIcon.layer.cornerRadius = profileIcon.frame.size.width / 2
        profileIcon.clipsToBounds = true
        let imgUrl = (Auth.auth().currentUser?.photoURL ?? authentication.googleSignIn.currentUser?.profile?.imageURL(withDimension: 100))!;
        profileIcon.load(url: imgUrl)
        view.addSubview(profileIcon)
        profileIcon.contentMode = .scaleAspectFill
        let uploadImgIcon = UIImage(named: "uploadImage");
        let uploadImage = UIImageView(frame: CGRect(x: profileIcon.frame.origin.x + profileIcon.frame.width / 2 + adapted(dimensionSize: 10, to: .height), y: profileIcon.frame.origin.y + profileIcon.frame.height - 40.0, width: 40.5, height: 40.5));
        uploadImage.image = uploadImgIcon;
        uploadImage.isUserInteractionEnabled = true;
        let tap = UITapGestureRecognizer(target: self, action: #selector(openImagePicker))
        uploadImage.addGestureRecognizer(tap)
        view.addSubview(uploadImage);
    }


    /**
     This function creates the labels for the text fields.
     */
    private func createLabels() {
        firstName = CustomLabel(text: Strings.firstNameFieldLabel, fontSize: 16, fontColor: traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black, frame: CGRect(x: 26, y: 0, width: 150, height: 40))
        lastName = CustomLabel(text: Strings.lastNameFieldLabel, fontSize: 16, fontColor: traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black, frame: CGRect(x: 26, y: (firstName.frame.origin.y + 100.0), width: 150, height: 40))
        dob = CustomLabel(text: Strings.dateOfBirthFieldLabel, fontSize: 16, fontColor: traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black, frame: CGRect(x: 26, y: (lastName.frame.origin.y + 100.0), width: 150, height: 40))
        email = CustomLabel(text: Strings.emailFieldLabel, fontSize: 16, fontColor: traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black, frame: CGRect(x: 26, y: (dob.frame.origin.y + 100.0), width: 150, height: 40))
        scrollView.addSubview(firstName);
        scrollView.addSubview(lastName);
        scrollView.addSubview(dob);
        scrollView.addSubview(email);
    }

    /**
     This function creates input fields
     */
    private func createTextFields() {
        firstNameField = CustomTextField(frame: CGRect(x: 17, y: firstName.frame.origin.y + adapted(dimensionSize: 30, to: .height), width: width - 34, height: 47));
        view.addSubview(firstNameField);
        setTextField(textField: firstNameField, value: getFirstName(name: Auth.auth().currentUser?.displayName ?? Strings.unknownFallback));
        lastNameField = CustomTextField(frame: CGRect(x: 17, y: lastName.frame.origin.y + adapted(dimensionSize: 30, to: .height), width: width - 34, height: 47));
        setTextField(textField: lastNameField, value: getLastName(name: Auth.auth().currentUser?.displayName ?? ""));
        dobField = CustomTextField(frame: CGRect(x: 17, y: dob.frame.origin.y + adapted(dimensionSize: 30, to: .height), width: width - 34, height: 47));
        self.setTextField(textField: self.dobField, value: Date.getCurrentDate());
        getDOB { dob in
            self.dobField.text = dob;
        }
        let img = UIImageView(frame: CGRect(x: dobField.frame.size.width - 40, y: 15, width: 20, height: 20));
        img.image = UIImage(named: "calendar");
        dobField.addSubview(img)
        let emailField = CustomTextField(frame: CGRect(x: 17, y: email.frame.origin.y + adapted(dimensionSize: 30, to: .height), width: width - 34, height: 47));
        setTextField(textField: emailField, value: authentication.googleSignIn.currentUser?.profile?.email ?? "");
        emailField.isEnabled = false
        emailField.textColor = traitCollection.userInterfaceStyle == .dark ? Colors.lightBlack : UIColor.lightGray;
        scrollView.addSubview(firstNameField);
        scrollView.addSubview(lastNameField);
        scrollView.addSubview(dobField);
        scrollView.addSubview(emailField);
    }

    /**
     Function to setup the textFields their radius background Colors
     - Parameters:
       - textField: the field to be set
       - value: default value to textField
     */
    private func setTextField(textField: UITextField, value: String) {
        textField.layer.masksToBounds = true;
        textField.layer.borderColor = UIColor(red: 0 / 255, green: 113 / 255, blue: 197 / 255, alpha: 0.4).cgColor
        textField.layer.borderWidth = 1.5
        textField.layer.cornerRadius = 8;
        textField.text = value;
        textField.textColor = traitCollection.userInterfaceStyle == .dark ? UIColor.white : UIColor.black;
    }

    /**
     Delegate method called when character in an UITextField changes. Here, this method is checking whether first name
     of user is empty or not
     - Parameters:
       - textField:
       - range:
       - string:
     - Returns:
     */
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        print("I was called")
        let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string)
        if newText?.count == 0 {
            textField.layer.borderColor = UIColor.red.cgColor
            saveChangesBtn.isEnabled = false;
        } else {
            saveChangesBtn.isEnabled = true;
            DispatchQueue.main.async {
                textField.layer.borderColor = UIColor(red: 0 / 255, green: 113 / 255, blue: 197 / 255, alpha: 0.4).cgColor
            }
        }
        return true
    }

    /**
     Function to get first name of user from Full name
     - Parameter name:
     - Returns: first name of user
     */
    private func getFirstName(name: String) -> String {
        let indexOfSpace = name.firstIndex(of: " ");
        if indexOfSpace == nil {
            return name;
        }
        return String(name[..<indexOfSpace!])
    }

    /**
     function to return the last name of user from google sign-in
     - Parameter name:
     - Returns:
     */
    private func getLastName(name: String) -> String {
        let indexOfSpace = name.lastIndex(of: " ");
        if indexOfSpace == nil {
            return "";
        } else {
            let index = name.index(after: indexOfSpace!);
            return String(name[index...])
        }

    }

    /**
     function to remove the keyboard from screen
     */
    @objc func dismissKeyboard() {
        view.endEditing(true)
    }

    /**
     Function to open the image picker on tap of image
     */
    @objc func openImagePicker() {
        imagePickerVC.sourceType = .photoLibrary
        present(imagePickerVC, animated: true)
    }

    /**
     delegate called after successfully selecting an image
     - Parameters:
       - picker:
       - info:
     */
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
        picker.dismiss(animated: true, completion: nil)
        if let image = info[.originalImage] as? UIImage {
            profileIcon.image = image
        }
    }

    /**
     handler of done button of date picker
     - Parameter sender:
     */
    @objc func doneButtonTapped(_ sender: UITextField) {
        // Dismiss the date picker
        sender.resignFirstResponder()
    }

    /**
     Function to create button named cancel and done.
     */
    private func createButtons() {
        let cancelBtn = CustomButton(text: Strings.cancel, frame: CGRect(x: 17, y: email.bottom + adapted(dimensionSize: 60, to: .height), width: 147, height: 47), selector: #selector(cancel))
        scrollView.addSubview(cancelBtn);
        saveChangesBtn = CustomButton(text: Strings.saveChangesButton, frame: CGRect(x: cancelBtn.frame.origin.x + 194.0, y: cancelBtn.frame.origin.y, width: 147, height: 47), selector: #selector(saveChanges))
        scrollView.addSubview(saveChangesBtn)
        saveChangesBtn.backgroundColor = Colors.title
        saveChangesBtn.setTitleColor(UIColor.white, for: .normal);
    }

    /**
     handle of cancel button tap. This will return to profile page
     - Parameter sender:
     */
    @objc func cancel(_ sender: UIButton) {
        navigationController?.popViewController(animated: true);
    }

    /**
     Handler of done button. Calls apis for update the profile
     - Parameter sender:
     */
    @objc func saveChanges(_ sender: UIButton) {
        createOverlayAlert();
        updateDOb(date: dobField.text ?? Date.getCurrentDate());
        let credential = GoogleAuthProvider.credential(withIDToken: (authentication.googleSignIn.currentUser?.idToken!.tokenString)!, accessToken: (authentication.googleSignIn.currentUser?.accessToken.tokenString)!)
        Auth.auth().signIn(with: credential) { result, error in
            if let user = result?.user {
                let changeRequest = user.createProfileChangeRequest()
                let firstName = self.firstNameField.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
                let lastName = self.lastNameField.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
                changeRequest.displayName = firstName + " " + lastName
                self.uploadImage();
                changeRequest.commitChanges { error in
                    if let error = error {
                        print("Error updating profile: \(error.localizedDescription)")
                    } else {
                        self.alert.dismiss(animated: true);
                        self.showToast(Strings.profileUpdatedSuccessToast, icon: UIImage(named: "check")!);
                    }
                }
            }
        }
    }

    /**
     A function for update of user image. Calls after done button tap
     */
    private func uploadImage() {
        // Get a reference to the storage service
        let storage = Storage.storage()

// Create a reference to the image file in Firebase storage using the user's UID as the filename
        let uid = Auth.auth().currentUser?.uid ?? ""
        let imageRef = storage.reference().child("profile_pictures/\(uid).jpg")

// Convert the image to data
        guard let image = profileIcon.image, let imageData = image.jpegData(compressionQuality: 0.5) else {
            print("Error converting image to data")
            return
        }

        let maxSize: CGFloat = 1024 // maximum size of the image
        let actualHeight = image.size.height
        let actualWidth = image.size.width
        var maxHeight = maxSize
        var maxWidth = maxSize

// Check the aspect ratio of the image
        let aspectRatio = actualWidth / actualHeight

        if aspectRatio > 1 {
            // Landscape image
            maxWidth = maxSize
            maxHeight = maxSize / aspectRatio
        } else {
            // Portrait image
            maxHeight = maxSize
            maxWidth = maxSize * aspectRatio
        }

        let rect = CGRect(x: 0.0, y: 0.0, width: maxWidth, height: maxHeight)
        UIGraphicsBeginImageContext(rect.size)
        image.draw(in: rect)
        let compressedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        guard (compressedImage?.jpegData(compressionQuality: 0.5)) != nil else {
            print("Error compressing image")
            return
        }
// Upload the image data to Firebase storage
        let uploadTask = imageRef.putData(imageData, metadata: nil) { metadata, error in
            if let error = error {
                print("Error uploading image: \(error.localizedDescription)")
            } else {
                // Get the download URL of the uploaded image
                imageRef.downloadURL { url, error in
                    if let error = error {
                        print("Error getting download URL: \(error.localizedDescription)")
                    } else {
                        // Update the user's profile with the download URL of the uploaded image
                        let changeRequest = Auth.auth().currentUser?.createProfileChangeRequest()
                        changeRequest?.photoURL = url
                        changeRequest?.commitChanges { error in
                            if let error = error {
                                print("Error updating profile: \(error.localizedDescription)")
                            } else {
                                print("Profile updated successfully")
                            }
                        }
                    }
                }
            }
        }

// Observe the upload progress
        uploadTask.observe(.progress) { snapshot in
            let percentComplete = Double(snapshot.progress!.completedUnitCount) / Double(snapshot.progress!.totalUnitCount) * 100.0
            print("Upload progress: \(percentComplete)%")
        }

    }

    /**
     Method to create animation
     */
    func createOverlayAlert() {
        let loadingIndicator: UIActivityIndicatorView = UIActivityIndicatorView(frame: CGRectMake(10, 5, 50, 50)) as UIActivityIndicatorView
        loadingIndicator.startAnimating();
        loadingIndicator.hidesWhenStopped = true
        loadingIndicator.style = UIActivityIndicatorView.Style.medium
        alert.view.addSubview(loadingIndicator)
        present(alert, animated: true, completion: nil)
    }

    func imageDidLoad() {
        alert.dismiss(animated: true);
    }

    /**
     Toast message on error while loading profile picture
     */
    func imageLoadFailed() {
        showToast(Strings.profilePictureLoadErrorToast, icon: UIImage(named: "check")!);
        alert.dismiss(animated: true);
    }

    /**
     Function to customize navigation bar
     */
    func setupNavigationBarItem() {
        if UIImage(named: "back") != nil {
            let backNavigationIcon = (UIImage(named: "back")?.withRenderingMode(.alwaysOriginal))!
            let newBackButton = UIBarButtonItem(image: backNavigationIcon, title: Strings.editProfile, target: self, action: #selector(back(sender:)), titleColor: Colors.navigationColor ?? .white)
            navigationItem.leftBarButtonItem = newBackButton
        }
    }

    /**
     Function calls on back button pressed
     - Parameter sender:
     */
    @objc func back(sender: UIBarButtonItem) {
        _ = navigationController?.popViewController(animated: true)
    }

    private func updateDOb(date : String) {
        let dateString = date // The string representation of the DOB
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "dd/MM/yyyy"

        if let date = dateFormatter.date(from: dateString) {
            let timestamp = Timestamp(date: date)


            if let uid = Auth.auth().currentUser?.uid {
                let userRef = Firestore.firestore().collection("users").document(uid)
                let data: [String: Any] = [
                    "dob": timestamp
                ]
                userRef.setData(data, merge: true) { error in
                    if let error = error {
                        // Handle the error
                        print("Error updating user profile: \(error.localizedDescription)")
                    } else {
                        // User profile updated successfully
                        print("User profile updated successfully!")
                    }
                }
            }
        }
    }

    func textFieldDidBeginEditing(_ textField: UITextField) {
        if textField == dobField {
           showDatePicker();
        }
    }

    /**
     Function to show date picker
     */
    func showDatePicker() {
        datePicker.datePickerMode = .date
        datePicker.addTarget(self, action: #selector(dateChange), for:
        UIControl.Event.valueChanged);
        datePicker.preferredDatePickerStyle = .wheels
        datePicker.frame.size = CGSize(width: 0, height: 300);
        dobField.inputView = datePicker;
        datePicker.maximumDate = Date()
    }

    /**

     - Parameter datePicker:
     */
    @objc func dateChange(datePicker: UIDatePicker){
        dobField.text = formatDate(date: datePicker.date);
    }

    /**
    function to format date of birth of user in dd/MM/yyyy
     - Parameter date:
     - Returns:
     */
    private func formatDate(date : Date) -> String{
        let formatter = DateFormatter();
        formatter.dateFormat = "dd/MM/yyyy";
        return formatter.string(from: date);
    }

    /**
     Function to get Date of Birth of user
     - Parameter completion:
     */
    private func getDOB(completion: @escaping (String?) -> Void) {
        if let uid = Auth.auth().currentUser?.uid {
            let userRef = Firestore.firestore().collection("users").document(uid)

            userRef.getDocument { (document, error) in
                if let document = document, document.exists {
                    if let dobTimestamp = document.data()?["dob"] as? Timestamp {
                        let dobDate = dobTimestamp.dateValue()

                        let dateFormatter = DateFormatter()
                        dateFormatter.dateFormat = "dd/MM/yyyy"
                        let dobString = dateFormatter.string(from: dobDate)

                        completion(dobString)
                    } else {
                        completion(nil)
                    }
                } else {
                    completion(nil)
                }
            }
        } else {
            completion(nil)
        }
    }
}





