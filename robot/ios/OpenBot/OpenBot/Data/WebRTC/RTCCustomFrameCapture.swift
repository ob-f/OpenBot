//
// Created by Nitish Yadav on 03/01/23.
//

import Foundation
import WebRTC

/// Implementation of capturer to capture the video feed from the device to send to WEBRTC
class RTCCustomFrameCapturer: RTCVideoCapturer {

    let kNanosecondsPerSecond: Float64 = 1000000000
    var nanoseconds: Float64 = 0

    /// initialization routine
    override init(delegate: RTCVideoCapturerDelegate) {
        super.init(delegate: delegate)
    }

    /// routine to capture the image/video feed from the device camera
    ///
    /// - Parameter sampleBuffer: image buffer
    public func capture(_ sampleBuffer: CMSampleBuffer) {
        let _pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
        if let pixelBuffer = _pixelBuffer {
            let rtcPixelBuffer = RTCCVPixelBuffer(pixelBuffer: pixelBuffer)
            let timeStampNs = CMTimeGetSeconds(CMSampleBufferGetPresentationTimeStamp(sampleBuffer)) * kNanosecondsPerSecond
            let rtcVideoFrame: RTCVideoFrame!
            switch currentOrientation {
            case .portrait:
                rtcVideoFrame = RTCVideoFrame(buffer: rtcPixelBuffer, rotation: RTCVideoRotation._0, timeStampNs: Int64(timeStampNs))
            case .unknown:
                rtcVideoFrame = RTCVideoFrame(buffer: rtcPixelBuffer, rotation: RTCVideoRotation._0, timeStampNs: Int64(timeStampNs))
            case .portraitUpsideDown:
                rtcVideoFrame = RTCVideoFrame(buffer: rtcPixelBuffer, rotation: RTCVideoRotation._0, timeStampNs: Int64(timeStampNs))

            case .landscapeLeft:
                rtcVideoFrame = RTCVideoFrame(buffer: rtcPixelBuffer, rotation: RTCVideoRotation._0, timeStampNs: Int64(timeStampNs))

            case .landscapeRight:
                rtcVideoFrame = RTCVideoFrame(buffer: rtcPixelBuffer, rotation: RTCVideoRotation._0, timeStampNs: Int64(timeStampNs))

            @unknown default:
                rtcVideoFrame = RTCVideoFrame(buffer: rtcPixelBuffer, rotation: RTCVideoRotation._0, timeStampNs: Int64(timeStampNs))

            }
            delegate?.capturer(self, didCapture: rtcVideoFrame)
        }
    }

    /// function to call the frame function into the library
    ///
    /// - Parameter pixelBuffer: image pixel buffer
    public func capture(_ pixelBuffer: CVPixelBuffer) {
        let rtcPixelBuffer = RTCCVPixelBuffer(pixelBuffer: pixelBuffer)
        let timeStampNs = nanoseconds * kNanosecondsPerSecond
        let rtcVideoFrame = RTCVideoFrame(buffer: rtcPixelBuffer, rotation: RTCVideoRotation._0, timeStampNs: Int64(timeStampNs))
        delegate?.capturer(self, didCapture: rtcVideoFrame)
        nanoseconds += 1
    }
}

