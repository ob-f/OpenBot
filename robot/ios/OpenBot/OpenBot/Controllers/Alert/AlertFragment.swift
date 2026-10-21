//
// Created by Nitish Yadav on 09/06/23.
//

import Foundation
import UIKit
import GoogleSignIn
/**
 class  to create alert
 */
class alertFragment : UIViewController {
    @IBOutlet weak var confirmLogoutLabel: UILabel!
    @IBOutlet weak var confirmLogoutMessageLabel: UILabel!
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var logOutButton: UIButton!
    override func viewDidLoad() {
        super.viewDidLoad();
        view.backgroundColor = UIColor.black.withAlphaComponent(0.6);
        confirmLogoutLabel.font = HelveticaNeue.regular(size: 16);
        confirmLogoutLabel.text = Strings.confirmLogoutTitle
        confirmLogoutMessageLabel.text = Strings.confirmLogoutMessage
        cancelButton.configuration?.title = Strings.canceled
        logOutButton.configuration?.title = Strings.logOut
    }
    
    @IBAction func cancelBtn(_ sender: Any) {
        dismiss(animated: true);
    }
    
    @IBAction func logOutBtn(_ sender: Any) {
        dismiss(animated: true);
        GIDSignIn.sharedInstance.signOut()
        UserDefaults.deleteAllProjectsFromUserDefaults();
        NotificationCenter.default.post(name: .googleSignIn, object: nil);
    }
}
