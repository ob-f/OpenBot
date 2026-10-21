//
// Created by Sparsh Jain and Quentin Leboutet on 18/11/22.
//

import Foundation
import Accelerate
import CoreImage

extension CVPixelBuffer {
    
    /// Resizes a pixel buffer to a specific size. Finds the biggest square in the pixel buffer and advances rows based on it.
    ///
    /// - Parameters:
    ///     - size: The size to resize the pixel buffer to.
    ///     - with: pool of CVPixelBuffer to avoid memory allocation
    /// - Returns: A resized pixel buffer or nil if the resize operation failed.
    func resized(to size: CGSize, preserveAspectRatio: Bool, with pool: CVPixelBufferPool) -> CVPixelBuffer? {
        
            // Get the size and pixel format of the input pixel buffer.
            let imageWidth = CVPixelBufferGetWidth(self)
            let imageHeight = CVPixelBufferGetHeight(self)
            let aspectRatio = CGFloat(imageWidth) / CGFloat(imageHeight)
            let pixelBufferType = CVPixelBufferGetPixelFormatType(self)
            let imageChannels = 4
            
            // Check that the pixel format is supported.
            assert(pixelBufferType == kCVPixelFormatType_32ARGB ||
                   pixelBufferType == kCVPixelFormatType_32BGRA ||
                   pixelBufferType == kCVPixelFormatType_32RGBA)
            
            // Create a pixel buffer adapter to handle the conversion and scaling
            var pixelBufferAdapter: CVBuffer?
            
            let status = CVPixelBufferPoolCreatePixelBuffer(nil, pool, &pixelBufferAdapter)
            guard status == kCVReturnSuccess else {
                return nil
            }
            
            guard let outputPixelBuffer = pixelBufferAdapter else {
                return nil
            }
            
            // Lock the base address of the source pixel buffer to access its pixel data
            guard CVPixelBufferLockBaseAddress(self, CVPixelBufferLockFlags(rawValue: 0)) == kCVReturnSuccess else {
                return nil
            }
            
            // Lock the base address of the destination pixel buffer to access its pixel data
            guard CVPixelBufferLockBaseAddress(outputPixelBuffer, CVPixelBufferLockFlags(rawValue: 0)) == kCVReturnSuccess else {
                return nil
            }
            
            // Get the pixel data from the source pixel buffer
            let sourceData = CVPixelBufferGetBaseAddress(self)
            
            // Get the pixel data from the destination pixel buffer
            let destinationData = CVPixelBufferGetBaseAddress(outputPixelBuffer)
            
            // Create a vImage buffer from the input pixel buffer.
            var inputVImageBuffer = vImage_Buffer(data: sourceData, height: UInt(imageHeight), width: UInt(imageWidth), rowBytes: CVPixelBufferGetBytesPerRow(self))
        
        if (preserveAspectRatio) {
            
            // Get the distance between the crop mask and the input image
            let scaleX = size.width / CGFloat(imageWidth)
            let scaleY = size.height / CGFloat(imageHeight)
            var scaledHeight: UInt = 0
            var scaledWidth: UInt = 0
            var scaledImageRowBytes: Int = 0
            
            if (scaleY<=scaleX) { // scaling operation should bring the vertical borders of the scaled image in contact with the vertical borders of the crop mask
                scaledImageRowBytes = Int(size.width) * imageChannels
                scaledHeight = UInt(size.width/aspectRatio)
                scaledWidth = UInt(size.width)
            } else { // scaling operation should bring the horizontal borders of the scaled image in contact with the horizontal borders of the crop mask
                scaledImageRowBytes = Int(size.height*aspectRatio) * imageChannels
                scaledHeight = UInt(size.height)
                scaledWidth = UInt(size.height*aspectRatio)
            }
            
            // Create a vImage buffer for the scaled image.
            var scaledVImageBuffer = vImage_Buffer(data: destinationData, height: scaledHeight, width: scaledWidth, rowBytes: scaledImageRowBytes)
            
            // Perform the scale operation on the input vImage buffer and store the result in the scaled vImage buffer.
            let scaleError = vImageScale_ARGB8888(&inputVImageBuffer, &scaledVImageBuffer, nil, vImage_Flags(0))
            
            // Get the cropping rectangle coordinates in the original buffer
            let cropX = Int((CGFloat(scaledVImageBuffer.width) - size.width) / 2)
            let cropY = Int((CGFloat(scaledVImageBuffer.height) - size.height) / 2)
            
            // Make sure the cropping rectangle is entirely within the original buffer
            guard cropX >= 0 && cropY >= 0 && cropX + Int(size.width) <= scaledVImageBuffer.width && cropY + Int(size.height) <= scaledVImageBuffer.height else {
                print("ERROR !!")
                return nil
            }
            
            // Initialize a new vImage_Buffer for the cropped region
            let croppedVImageBuffer = vImage_Buffer(data: scaledVImageBuffer.data.advanced(by: cropY * scaledVImageBuffer.rowBytes + cropX * imageChannels),
                                                    height: vImagePixelCount(size.height),
                                                    width: vImagePixelCount(size.width),
                                                    rowBytes: scaledVImageBuffer.rowBytes)
            
            // Unlock the base address of the source pixel buffer
            guard CVPixelBufferUnlockBaseAddress(self, CVPixelBufferLockFlags(rawValue: 0)) == kCVReturnSuccess else {
                return nil
            }
            
            // Unlock the base address of the destination pixel buffer
            guard CVPixelBufferUnlockBaseAddress(outputPixelBuffer, CVPixelBufferLockFlags(rawValue: 0)) == kCVReturnSuccess else {
                return nil
            }
            
            // Check that the scale operation succeeded.
            guard scaleError == kvImageNoError else {
                return nil
            }
            
            // Create a CVPixelBuffer from the scaled vImage buffer.
            var croppedPixelBuffer: CVPixelBuffer?
            
            // Converts the scaled vImage buffer to CVPixelBuffer
            let conversionStatus = CVPixelBufferCreateWithBytes(nil, Int(size.width), Int(size.height), pixelBufferType, croppedVImageBuffer.data, croppedVImageBuffer.rowBytes, nil, nil, nil, &croppedPixelBuffer)
            
            // Check that the CVPixelBuffer creation succeeded.
            guard conversionStatus == kCVReturnSuccess else {
                return nil
            }
            return croppedPixelBuffer
            
        } else {
            
            // Create a vImage buffer for the scaled image.
            let scaledImageRowBytes = Int(size.width) * imageChannels
            var scaledVImageBuffer = vImage_Buffer(data: destinationData, height: UInt(size.height), width: UInt(size.width), rowBytes: scaledImageRowBytes)
            
            // Performs the scale operation on input image buffer and stores it in scaled image buffer.
            let scaleError = vImageScale_ARGB8888(&inputVImageBuffer, &scaledVImageBuffer, nil, vImage_Flags(0))
            
            // Unlock the base address of the source pixel buffer
            guard CVPixelBufferUnlockBaseAddress(self, CVPixelBufferLockFlags(rawValue: 0)) == kCVReturnSuccess else {
                return nil
            }
            
            // Unlock the base address of the destination pixel buffer
            guard CVPixelBufferUnlockBaseAddress(outputPixelBuffer, CVPixelBufferLockFlags(rawValue: 0)) == kCVReturnSuccess else {
                return nil
            }
            
            // Check that the scale operation succeeded.
            guard scaleError == kvImageNoError else {
                return nil
            }
            
            // Create a CVPixelBuffer from the scaled vImage buffer.
            var scaledPixelBuffer: CVPixelBuffer?
            
            // Converts the scaled vImage buffer to CVPixelBuffer
            let conversionStatus = CVPixelBufferCreateWithBytes(nil, Int(size.width), Int(size.height), pixelBufferType, scaledVImageBuffer.data, scaledVImageBuffer.rowBytes, nil, nil, nil, &scaledPixelBuffer)
            
            // Check that the CVPixelBuffer creation succeeded.
            guard conversionStatus == kCVReturnSuccess else {
                return nil
            }
            return scaledPixelBuffer

        }
    }
    
    /// Resizes a pixel buffer to a specific size and then crop a desired number of top pixels.
    ///
    /// - Parameters
    ///     - size: The size to resize the pixel buffer to.
    ///     - top: Number of pixels to crop on top of the resized picture
    ///     - with: pool of CVPixelBuffer to avoid memory allocation
    /// - Returns: A resized pixel buffer or nil if the resize operation failed.
    func resizeAndCropTop(to size: CGSize, top: Int, preserveAspectRatio: Bool, with pool: CVPixelBufferPool) -> CVPixelBuffer? {
        
        // Get the size and pixel format of the input pixel buffer.
        let imageWidth = CVPixelBufferGetWidth(self)
        let imageHeight = CVPixelBufferGetHeight(self)
        let aspectRatio = CGFloat(imageWidth) / CGFloat(imageHeight)
        let pixelBufferType = CVPixelBufferGetPixelFormatType(self)
        let imageChannels = 4
        
        // Check that the pixel format is supported.
        assert(pixelBufferType == kCVPixelFormatType_32ARGB ||
               pixelBufferType == kCVPixelFormatType_32BGRA ||
               pixelBufferType == kCVPixelFormatType_32RGBA)
        
        // Create a pixel buffer adapter to handle the conversion and scaling
        var pixelBufferAdapter: CVBuffer?
        
        let status = CVPixelBufferPoolCreatePixelBuffer(nil, pool, &pixelBufferAdapter)
        guard status == kCVReturnSuccess else {
            return nil
        }
        
        guard let outputPixelBuffer = pixelBufferAdapter else {
            return nil
        }
        
        // Lock the base address of the source pixel buffer to access its pixel data
        guard CVPixelBufferLockBaseAddress(self, CVPixelBufferLockFlags(rawValue: 0)) == kCVReturnSuccess else {
            return nil
        }
        
        // Lock the base address of the destination pixel buffer to access its pixel data
        guard CVPixelBufferLockBaseAddress(outputPixelBuffer, CVPixelBufferLockFlags(rawValue: 0)) == kCVReturnSuccess else {
            return nil
        }
        
        // Get the pixel data from the source pixel buffer
        let sourceData = CVPixelBufferGetBaseAddress(self)
        
        // Get the pixel data from the destination pixel buffer
        let destinationData = CVPixelBufferGetBaseAddress(outputPixelBuffer)
        
        // Create a vImage buffer from the input pixel buffer.
        var inputVImageBuffer = vImage_Buffer(data: sourceData, height: UInt(imageHeight), width: UInt(imageWidth), rowBytes: CVPixelBufferGetBytesPerRow(self))
        
        if (preserveAspectRatio) {

            // Get the distance between the crop mask and the input image
            let scaleX = size.width / CGFloat(imageWidth)
            let scaleY = size.height / CGFloat(imageHeight)
            var scaledHeight: UInt = 0
            var scaledWidth: UInt = 0
            var scaledImageRowBytes: Int = 0
            
            if (scaleY<=scaleX) { // scaling operation should bring the vertical borders of the scaled image in contact with the vertical borders of the crop mask
                scaledImageRowBytes = Int(size.width) * imageChannels
                scaledHeight = UInt(size.width/aspectRatio)
                scaledWidth = UInt(size.width)
            } else { // scaling operation should bring the horizontal borders of the scaled image in contact with the horizontal borders of the crop mask
                scaledImageRowBytes = Int(size.height*aspectRatio) * imageChannels
                scaledHeight = UInt(size.height)
                scaledWidth = UInt(size.height*aspectRatio)
            }
            
            // Create a vImage buffer for the scaled image.
            var scaledVImageBuffer = vImage_Buffer(data: destinationData, height: scaledHeight, width: scaledWidth, rowBytes: scaledImageRowBytes)
            
            // Perform the scale operation on the input vImage buffer and store the result in the scaled vImage buffer.
            let scaleError = vImageScale_ARGB8888(&inputVImageBuffer, &scaledVImageBuffer, nil, vImage_Flags(0))
            
            // Get the cropping rectangle coordinates in the original buffer
            let cropX = Int((CGFloat(scaledVImageBuffer.width) - size.width) / 2)
            let cropY = Int((CGFloat(scaledVImageBuffer.height) - size.height) / 2) + top
            
            // Initialize a new vImage_Buffer for the cropped region
            let croppedVImageBuffer = vImage_Buffer(data: scaledVImageBuffer.data.advanced(by: cropY * scaledVImageBuffer.rowBytes + cropX * imageChannels),
                                                    height: vImagePixelCount(size.height - CGFloat(top)),
                                                    width: vImagePixelCount(size.width),
                                                    rowBytes: scaledVImageBuffer.rowBytes)
            
            // Unlock the base address of the source pixel buffer
            guard CVPixelBufferUnlockBaseAddress(self, CVPixelBufferLockFlags(rawValue: 0)) == kCVReturnSuccess else {
                return nil
            }
            
            // Unlock the base address of the destination pixel buffer
            guard CVPixelBufferUnlockBaseAddress(outputPixelBuffer, CVPixelBufferLockFlags(rawValue: 0)) == kCVReturnSuccess else {
                return nil
            }
            
            // Check that the scale operation succeeded.
            guard scaleError == kvImageNoError else {
                return nil
            }
            
            // Create a CVPixelBuffer from the scaled vImage buffer.
            var croppedPixelBuffer: CVPixelBuffer?
            
            // Converts the scaled vImage buffer to CVPixelBuffer
            let conversionStatus = CVPixelBufferCreateWithBytes(nil, Int(size.width), Int(size.height) - top, pixelBufferType, croppedVImageBuffer.data, croppedVImageBuffer.rowBytes, nil, nil, nil, &croppedPixelBuffer)
            
            // Check that the CVPixelBuffer creation succeeded.
            guard conversionStatus == kCVReturnSuccess else {
                return nil
            }
            return croppedPixelBuffer
            
        } else {
            
            // Create a vImage buffer for the scaled image.
            let scaledImageRowBytes = Int(size.width) * imageChannels
            var scaledVImageBuffer = vImage_Buffer(data: destinationData, height: UInt(size.height), width: UInt(size.width), rowBytes: scaledImageRowBytes)
            
            // Performs the scale operation on input image buffer and stores it in scaled image buffer.
            let scaleError = vImageScale_ARGB8888(&inputVImageBuffer, &scaledVImageBuffer, nil, vImage_Flags(0))
            
            // Get the cropping rectangle coordinates in the original buffer
            let cropX = Int((CGFloat(scaledVImageBuffer.width) - size.width) / 2)
            let cropY = Int((CGFloat(scaledVImageBuffer.height) - size.height) / 2) + top
            
            // Initialize a new vImage_Buffer for the cropped region
            let croppedVImageBuffer = vImage_Buffer(data: scaledVImageBuffer.data.advanced(by: cropY * scaledVImageBuffer.rowBytes + cropX * imageChannels),
                                                    height: vImagePixelCount(size.height - CGFloat(top)),
                                                    width: vImagePixelCount(size.width),
                                                    rowBytes: scaledVImageBuffer.rowBytes)
            
            // Unlock the base address of the source pixel buffer
            guard CVPixelBufferUnlockBaseAddress(self, CVPixelBufferLockFlags(rawValue: 0)) == kCVReturnSuccess else {
                return nil
            }
            
            // Unlock the base address of the destination pixel buffer
            guard CVPixelBufferUnlockBaseAddress(outputPixelBuffer, CVPixelBufferLockFlags(rawValue: 0)) == kCVReturnSuccess else {
                return nil
            }
            
            // Check that the scale operation succeeded.
            guard scaleError == kvImageNoError else {
                return nil
            }
            
            // Create a CVPixelBuffer from the scaled vImage buffer.
            var croppedPixelBuffer: CVPixelBuffer?
            
            // Converts the scaled vImage buffer to CVPixelBuffer
            let conversionStatus = CVPixelBufferCreateWithBytes(nil, Int(size.width), Int(size.height) - top, pixelBufferType, croppedVImageBuffer.data, croppedVImageBuffer.rowBytes, nil, nil, nil, &croppedPixelBuffer)
            
            // Check that the CVPixelBuffer creation succeeded.
            guard conversionStatus == kCVReturnSuccess else {
                return nil
            }
            return croppedPixelBuffer
        }
    }
}
