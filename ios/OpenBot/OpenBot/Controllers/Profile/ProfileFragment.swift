//
// Created by Nitish Yadav on 06/04/23.
//

import Foundation
import UIKit
import GoogleSignIn

/**
 Class for profile fragment extending ViewController.
 */
class profileFragment: UIViewController {
    let shadowSheet = UIView(frame: UIScreen.main.bounds);
    var logoutView = UIView()
    let alert = UIAlertController(title: "Loading", message: "Please wait while we load the profile...", preferredStyle: .alert)
    var firstLabel = UILabel()
    var secondLabel = UILabel()
    var signInBtn = GoogleSignInBtn()
    var editProfileLabel = UILabel()
    var logoutLabel = UILabel()
    var userImgView = UIImageView()
    var logoutImgView = UIImageView()
    var signInView = UIView()
    var guestView = UIView()

    /**
     Calls after view of profile will load. This will create UI of the View
     */
    override func viewDidLoad() {
        super.viewDidLoad()
        tabBarItem.title = Strings.profileTab
        createMyProfileLabel();
        navigationController?.navigationBar.setBackgroundImage(UIImage(), for: .default)
        navigationController?.navigationBar.shadowImage = UIImage()
        signInView = CustomView(frame: currentOrientation == .portrait ? CGRect(origin: CGPoint(x: 0, y: 150), size: CGSize(width: width, height: height - 150)) :
                CGRect(origin: CGPoint(x: 0, y: 100), size: CGSize(width: height, height: width - 100))
                , backgroundColor: traitCollection.userInterfaceStyle == .dark ? UIColor.black : UIColor.white);
        guestView.frame = currentOrientation == .portrait ? CGRect(x: 0, y: height / 2 - 100, width: width, height: height / 2)
                : CGRect(x: height / 2 - width / 2, y: 0, width: width, height: height / 2);
        view.addSubview(signInView);
        view.addSubview(guestView);
        createEditProfileAndLogoutLabel();
        createEditProfileAndLogoutIcons();
        createPleaseSignInLabel();
        createSignInBtn();
        updateViewsVisibility()
        NotificationCenter.default.addObserver(self, selector: #selector(googleSignIn), name: .googleSignIn, object: nil)
    }

    /**
     Override function calls when orientation of phone changes
     - Parameters:
       - size:
       - coordinator:
     */
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
        if currentOrientation == .portrait {
            signInView.frame = CGRect(origin: CGPoint(x: 0, y: 140), size: CGSize(width: width, height: height - 140))
            guestView.frame.origin = CGPoint(x: 0, y: height / 2 - 100);
            userImgView.frame.origin.x = safeAreaLayoutValue.left + 38;
            logoutImgView.frame.origin.x = safeAreaLayoutValue.left + 38;
            editProfileLabel.frame.origin.x = userImgView.right + 20;
            logoutLabel.frame.origin.x = editProfileLabel.left;
        } else {
            signInView.frame = CGRect(origin: CGPoint(x: 0, y: 100), size: CGSize(width: height, height: width - 100))
            guestView.frame.origin = CGPoint(x: height / 2 - 200, y: width / 2 - 100);
            userImgView.frame.origin.x = safeAreaLayoutValue.top + 38;
            logoutImgView.frame.origin.x = safeAreaLayoutValue.top + 38;
            editProfileLabel.frame.origin.x = userImgView.right + 20;
            logoutLabel.frame.origin.x = editProfileLabel.left;
        }
        updateUIConstraints();
    }

    /**
     Function to create My profile Label on top
     */
    private func createMyProfileLabel() {
        let label = CustomLabel(text: "My Profile", fontSize: 15, fontColor: Colors.textColor ?? .black, frame: CGRect(origin: .zero, size: CGSize(width: 200, height: 40)));
        label.font = HelveticaNeue.regular(size: 15);
        label.translatesAutoresizingMaskIntoConstraints = false;
        view.addSubview(label)
        label.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: 20).isActive = true;
        label.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 10).isActive = true;
        label.layoutIfNeeded();
    }

    /**
     Function to create the message for sign-in
     */
    private func createPleaseSignInLabel() {
        firstLabel = CustomLabel(text: "Set up your profile by signing in with", fontSize: 16, fontColor: Colors.textColor ?? .black, frame: CGRect(x: width / 2 - 135, y: 20, width: 270, height: 40));
        secondLabel = CustomLabel(text: "your Google account.", fontSize: 16, fontColor: Colors.textColor ?? .black, frame: CGRect(x: width / 2 - 80, y: firstLabel.bottom - 5, width: 160, height: 40));
        guestView.addSubview(firstLabel);
        guestView.addSubview(secondLabel);
    }

    /**
     Function to create google sign-in button.
     */
    private func createSignInBtn() {
        let signInBtn = GoogleSignInBtn(frame: CGRect(x: adapted(dimensionSize: 17, to: .width), y: 100, width: width - adapted(dimensionSize: 34, to: .width), height: 52))
        let tap = UITapGestureRecognizer(target: self, action: #selector(signIn))
        signInBtn.addGestureRecognizer(tap);
        guestView.addSubview(signInBtn);
    }

    /**
     Function to create labels for edit profile and logout
     */
    private func createEditProfileAndLogoutLabel() {
        editProfileLabel = CustomLabel(text: "Edit Profile", fontSize: 16, fontColor: Colors.textColor ?? .black, frame: CGRect(x: userImgView.right + 72, y: 0, width: 120, height: 50))
        signInView.addSubview(editProfileLabel);
        editProfileLabel.isUserInteractionEnabled = true;
        let tapOnEditProfile = UITapGestureRecognizer(target: self, action: #selector(editProfileHandler))
        editProfileLabel.addGestureRecognizer(tapOnEditProfile);
        logoutLabel = CustomLabel(text: "Logout", fontSize: 16, fontColor: Colors.textColor ?? .black, frame: CGRect(x: userImgView.right + 72, y: editProfileLabel.bottom + 25, width: 120, height: 50))
        signInView.addSubview(logoutLabel);
        logoutLabel.isUserInteractionEnabled = true;
        let tapOnLogout = UITapGestureRecognizer(target: self, action: #selector(logoutHandler))
        logoutLabel.addGestureRecognizer(tapOnLogout);
    }

    /**
     Function to create edit profile and logout icons
     */
    private func createEditProfileAndLogoutIcons() {
        let userIcon = UIImage(named: "user_icon");
        let logoutIcon = UIImage(named: "logout");
        userImgView = UIImageView(frame: CGRect(x: safeAreaLayoutValue.left + 38, y: 17, width: userIcon?.size.width ?? 18, height: userIcon?.size.height ?? 18));
        logoutImgView = UIImageView(frame: CGRect(x: safeAreaLayoutValue.left + 38, y: userImgView.bottom + 52, width: logoutIcon?.size.width ?? 24, height: logoutIcon?.size.height ?? 18));
        userImgView.image = userIcon;
        logoutImgView.image = logoutIcon;
        signInView.addSubview(userImgView);
        signInView.addSubview(logoutImgView);
    }

    /**
     Function to create shadow sheet which will load on logout popup.
     */
    private func createShadowSheet() {
        createLogoutPopup()
    }

    /**
     Function to create logout popup
     */
    private func createLogoutPopup() {
        let storyboard = UIStoryboard(name: "openCode", bundle: nil)
        let viewController = (storyboard.instantiateViewController(withIdentifier: "alert"));
        viewController.modalPresentationStyle = UIModalPresentationStyle.overCurrentContext
        viewController.modalTransitionStyle = UIModalTransitionStyle.crossDissolve
        present(viewController, animated: true, completion: nil)
    }

    /**
     Function that will add edit profile view controller to navigation controller
     */
    @objc func editProfileHandler() {
        let storyboard = UIStoryboard(name: "openCode", bundle: nil)
        let viewController = storyboard.instantiateViewController(withIdentifier: "editProfile")
        self.navigationController?.pushViewController(viewController, animated: true)
    }

    /**
     Function for logout from google
     */
    @objc func logoutHandler() {
        createShadowSheet();
    }

    /**
     handler function on cancel button of log out popup pressed
     - Parameter sender:
     */
    @objc func cancel(_ sender: UIButton) {
        shadowSheet.removeFromSuperview();
        logoutView.removeFromSuperview();
    }

    /**
     Function for logout from google
     */
    @objc func logout(_ sender: UIButton) {
        shadowSheet.removeFromSuperview();
        logoutView.removeFromSuperview();
        GIDSignIn.sharedInstance.signOut()
        UserDefaults.deleteAllProjectsFromUserDefaults();
        updateViewsVisibility()
    }

    /**
     Refresh the UI based on google sign-in and guest sign-in
     */
    private func updateViewsVisibility() {
        if GIDSignIn.sharedInstance.currentUser != nil {
            // Show firstLabel, secondLabel, and SignInBtn, and hide other views
            signInView.isHidden = false;
            guestView.isHidden = true;
        } else {
            // Show editProfileLabel, logoutLabel, userImgView, and logoutImgView, and hide other views
            signInView.isHidden = true;
            guestView.isHidden = false;
        }
    }

    /**
     Function to call Authentication class constructor.
     */
    @objc func signIn() {
        Authentication();
    }

    /**
     notification handler on google sign in
     - Parameter notification:
     */
    @objc func googleSignIn(_ notification: Notification) {
        updateViewsVisibility()
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


    /**
     function to update the UI positions in landscape and portrait mode
     */
    private func updateUIConstraints() {
        if currentOrientation == .portrait {
            shadowSheet.frame = UIScreen.main.bounds
            logoutView.frame = CGRect(x: (width - width * 0.90) / 2, y: height / 2 - 84, width: width * 0.90, height: 168)
        } else {
            shadowSheet.frame = CGRect(x: 0, y: 0, width: height, height: width);
            logoutView.frame = CGRect(x: height / 2 - 160, y: width / 2 - 84, width: width * 0.90, height: 168);
        }
    }


}