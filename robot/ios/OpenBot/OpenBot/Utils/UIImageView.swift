//
// Created by Nitish Yadav on 11/04/23.
//

import Foundation
import UIKit

extension UIImageView {
    func load(url: URL, activityIndicatorStyle: UIActivityIndicatorView.Style = .gray) {
        // Create and start the activity indicator
        let activityIndicator = UIActivityIndicatorView(style: activityIndicatorStyle)
        activityIndicator.translatesAutoresizingMaskIntoConstraints = false
        addSubview(activityIndicator)
        activityIndicator.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        activityIndicator.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true
        activityIndicator.startAnimating()

        DispatchQueue.global().async { [weak self] in
             var data = try? Data(contentsOf: url);
            if data == nil{
                if let profileUrl = Authentication.googleAuthentication.googleSignIn.currentUser?.profile?.imageURL(withDimension: 100){
                    data = try? Data(contentsOf:profileUrl );
                }

            }
                if let image = UIImage(data: data!) {
                    DispatchQueue.main.async {
                        self?.image = image
                        // Hide the activity indicator once the image is loaded
                        activityIndicator.stopAnimating()
                        activityIndicator.removeFromSuperview()
                    }
                }

        }
    }
}
