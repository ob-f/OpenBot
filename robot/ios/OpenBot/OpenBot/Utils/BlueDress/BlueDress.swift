import Metal
import CoreVideo

public class YCbCrImageBufferConverter {
    private let textureCache: CVMetalTextureCache
    private let commandQueue: MTLCommandQueue
    private let pipelineState: MTLRenderPipelineState
    
    private var outputPixelBuffer: CVImageBuffer? = nil
    private var outputTexture: MTLTexture? = nil
    
    public init() throws {
        let device = MTLCreateSystemDefaultDevice()!
        let metalLib = try device.makeModuleLibrary()
        textureCache = try .make(device: device)
        commandQueue = device.makeCommandQueue()!
        pipelineState = try device.makeRenderPipelineState(metalLib: metalLib)
    }
    
    public func convertToBGRA(imageBuffer: CVPixelBuffer) throws -> CVPixelBuffer {
        guard imageBuffer.isYpCbCr else { fatalError() }
        guard imageBuffer.planeCount == 2 else { fatalError() }
        
        try imageBuffer.lock()
        defer { _ = try? imageBuffer.unlock() }
        
        let yTexture = try CVMetalTexture.make(
            sourceImage: imageBuffer,
            planeIndex: PlaneIndex.y.rawValue,
            pixelFormat: .r8Unorm,
            textureCache: textureCache
        ).metalTexture!
        
        let cbcrTexture = try CVMetalTexture.make(
            sourceImage: imageBuffer,
            planeIndex: PlaneIndex.cbcr.rawValue,
            pixelFormat: .rg8Unorm,
            textureCache: textureCache
        ).metalTexture!
        
        if outputPixelBuffer == nil {
            /// ここでリサイズかけられる
            outputPixelBuffer = try CVPixelBuffer.make(width: yTexture.width, height: yTexture.height)
        }
        
        if outputTexture == nil {
            outputTexture = try CVMetalTexture.make(
                sourceImage: outputPixelBuffer!,
                pixelFormat: .bgra8Unorm,
                textureCache: textureCache
            ).metalTexture!
        }
        
        let renderDesc = MTLRenderPassDescriptor()
        renderDesc.colorAttachments[0].texture = outputTexture
        renderDesc.colorAttachments[0].loadAction = .clear
        
        if let commandBuffer = commandQueue.makeCommandBuffer(),
           let renderEncoder = commandBuffer.makeRenderCommandEncoder(descriptor: renderDesc) {
            renderEncoder.setRenderPipelineState(pipelineState)
            renderEncoder.setFragmentTexture(yTexture, index: 0)
            renderEncoder.setFragmentTexture(cbcrTexture, index: 1)
            renderEncoder.drawPrimitives(type: .triangleStrip, vertexStart: 0, vertexCount: 4)
            renderEncoder.endEncoding()
            commandBuffer.commit()
        }
        
        return outputPixelBuffer!
    }
}

