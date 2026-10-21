//
//  MTLDevice+.swift
//  
//
//  Created by Tomoya Hirano on 2020/07/21.
//

import Foundation
import Metal
import CoreVideo

extension MTLDevice {
    func makeModuleLibrary() throws -> MTLLibrary {
        let url = Bundle.main.url(forResource: "default", withExtension: "metallib")!
        return try self.makeLibrary(URL: url)
    }
    
    func makeTextureCache() throws -> CVMetalTextureCache {
      var textureCache: CVMetalTextureCache?
      let result = CVMetalTextureCacheCreate(
        kCFAllocatorDefault,
        nil,
        self,
        nil,
        &textureCache
      )
      if let textureCache = textureCache {
        return textureCache
      } else {
          throw CVReturnError(code: result)
      }
    }
    
    func makeTexureCoordBuffer() -> MTLBuffer {
      let texCoordinateData: [Float] = [
        0, 1,
        1, 1,
        0, 0,
        1, 0
      ]
      let texCoordinateDataSize = MemoryLayout<Float>.size * texCoordinateData.count
      return makeBuffer(bytes: texCoordinateData, length: texCoordinateDataSize)!
    }
    
    func makeVertexBuffer() -> MTLBuffer {
      let vertexData: [Float] = [
        -1.0, -1.0, 0, 1,
        1.0, -1.0, 0, 1,
        -1.0, 1.0, 0, 1,
        1.0, 1.0, 0, 1,
      ]
      let size = vertexData.count * MemoryLayout<Float>.size
      return makeBuffer(bytes: vertexData, length: size)!
    }
    
    func makeRenderPipelineState(metalLib: MTLLibrary,
                                          pixelFormat: MTLPixelFormat = .bgra8Unorm,
                                          vertexFunctionName: String = "vertexShader",
                                          fragmentFunctionName: String = "fragmentShader") throws -> MTLRenderPipelineState {
      let pipelineDesc = MTLRenderPipelineDescriptor()
      pipelineDesc.vertexFunction = metalLib.makeFunction(name: vertexFunctionName)
      pipelineDesc.fragmentFunction = metalLib.makeFunction(name: fragmentFunctionName)
      pipelineDesc.colorAttachments[0].pixelFormat = pixelFormat
      
      return try makeRenderPipelineState(descriptor: pipelineDesc)
    }

}

