//
//  File.swift
//  
//
//  Created by Tomoya Hirano on 2020/07/21.
//

import CoreVideo
import Metal

extension CVMetalTextureCache {
    static func make(device: MTLDevice) throws -> CVMetalTextureCache {
        var textureCache: CVMetalTextureCache? = nil
        let cvReturn = CVMetalTextureCacheCreate(kCFAllocatorDefault, nil, device, nil, &textureCache)
        if cvReturn == kCVReturnSuccess {
            return textureCache!
        } else {
            throw CVReturnError(code: cvReturn)
        }
    }
}

extension CVMetalTexture {
    static func make(sourceImage: CVImageBuffer, pixelFormat: MTLPixelFormat, textureCache: CVMetalTextureCache) throws -> CVMetalTexture {
        let width = CVPixelBufferGetWidth(sourceImage)
        let height = CVPixelBufferGetHeight(sourceImage)
        var texture: CVMetalTexture? = nil
        let cvReturn = CVMetalTextureCacheCreateTextureFromImage(
            kCFAllocatorDefault,
            textureCache,
            sourceImage,
            nil,
            pixelFormat,
            width,
            height,
            0,
            &texture
        )
        if cvReturn == kCVReturnSuccess {
            return texture!
        } else {
            throw CVReturnError(code: cvReturn)
        }
    }
    
    static func make(sourceImage: CVImageBuffer, planeIndex: Int, pixelFormat: MTLPixelFormat, textureCache: CVMetalTextureCache) throws -> CVMetalTexture {
        let width = CVPixelBufferGetWidthOfPlane(sourceImage, planeIndex)
        let height = CVPixelBufferGetHeightOfPlane(sourceImage, planeIndex)
        var texture: CVMetalTexture? = nil
        let cvReturn = CVMetalTextureCacheCreateTextureFromImage(
            kCFAllocatorDefault,
            textureCache,
            sourceImage,
            nil,
            pixelFormat,
            width,
            height,
            planeIndex,
            &texture
        )
        if cvReturn == kCVReturnSuccess {
            return texture!
        } else {
            throw CVReturnError(code: cvReturn)
        }
    }
    
    var metalTexture: MTLTexture? {
        CVMetalTextureGetTexture(self)
    }
}

