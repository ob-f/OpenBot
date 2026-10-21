//
//  File.swift
//  
//
//  Created by Tomoya Hirano on 2020/07/21.
//

import CoreVideo

extension CVPixelBuffer {
    static func make(width: Int, height: Int) throws -> CVPixelBuffer {
        var pixelBuffer: CVPixelBuffer? = nil
        /// kCVPixelBufferMetalCompatibilityKey忘れると-6660になる
        let cvReturn = CVPixelBufferCreate(kCFAllocatorDefault, width, height, kCVPixelFormatType_32BGRA, [kCVPixelBufferMetalCompatibilityKey : true] as CFDictionary, &pixelBuffer)
        if cvReturn == kCVReturnSuccess {
            return pixelBuffer!
        } else {
            throw CVReturnError(code: cvReturn)
        }
    }
    
    func lock() throws {
        let cvReturn = CVPixelBufferLockBaseAddress(self, .readOnly)
        if cvReturn != kCVReturnSuccess {
            throw CVReturnError(code: cvReturn)
        }
    }
    
    func unlock() throws {
        let cvReturn = CVPixelBufferUnlockBaseAddress(self, .readOnly)
        if cvReturn != kCVReturnSuccess {
            throw CVReturnError(code: cvReturn)
        }
    }
    
    var isYpCbCr: Bool {
        switch CVPixelBufferGetPixelFormatType(self) {
        case kCVPixelFormatType_420YpCbCr8BiPlanarFullRange:
            return true
        case kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange:
            return true
        default: /// Unsupported
            return false
        }
    }
    
    var planeCount: Int {
        CVPixelBufferGetPlaneCount(self)
    }
}

