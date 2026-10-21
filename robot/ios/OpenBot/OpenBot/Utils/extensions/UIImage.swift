//
// Created by Sparsh Jain on 28/11/22.
//

import Foundation
import UIKit

extension UIImage {

    /// This function flips the image horizontally.
    func flipHorizontally() -> UIImage {
        UIGraphicsBeginImageContextWithOptions(size, false, scale)
        let context = UIGraphicsGetCurrentContext()!
        context.translateBy(x: size.width / 2, y: size.height / 2)
        context.scaleBy(x: -1.0, y: 1.0)
        context.translateBy(x: -size.width / 2, y: -size.height / 2)

        self.draw(in: CGRect(x: 0, y: 0, width: size.width, height: size.height))

        let newImage: UIImage = UIGraphicsGetImageFromCurrentImageContext()!
        UIGraphicsEndImageContext()

        return newImage
    }

    /// This function resizes the image to a specified size with a specified scale.
    func resized(to newSize: CGSize, scale: CGFloat = 1) -> UIImage {
        let format = UIGraphicsImageRendererFormat.default()
        format.scale = scale
        let renderer = UIGraphicsImageRenderer(size: newSize, format: format)
        let image = renderer.image { _ in
            draw(in: CGRect(origin: .zero, size: newSize))
        }
        return image
    }
}
