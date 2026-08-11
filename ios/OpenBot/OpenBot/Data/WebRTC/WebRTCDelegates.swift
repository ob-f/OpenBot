//
// Created by Nitish Yadav on 09/01/23.
//

import Foundation
import AVFoundation
import Starscream
import WebRTC

var webRTCClient: WebRTCClient!

/// function to create webRTC Delegate
class WebRTCDelegates: WebRTCClientDelegate {

    var useCustomCapturer: Bool = true

    /// callback to check the generate candidate and send to the controller
    func didGenerateCandidate(iceCandidate: RTCIceCandidate) {
        sendCandidate(iceCandidate: iceCandidate)
    }

    /// callback to check whether the ice connection is changed.
    func didIceConnectionStateChanged(iceConnectionState: RTCIceConnectionState) {
        print("ice connection state is : ", iceConnectionState);
    }

    /// callback function to open data channel
    func didOpenDataChannel() {
        print("did open data channel")
    }

    /// callback function to check data is available
    func didReceiveData(data: Data) {
        print(data);
    }
    /// callback function message string received
    func didReceiveMessage(message: String) {
        print("message is : ", message);

    }

    func didConnectWebRTC() {
        print("didConnectWebRTC")
    }

    func didDisconnectWebRTC() {
        print("didDisconnectWebRTC")
    }

    /// initializing function
    init() {
        #if targetEnvironment(simulator)
        useCustomCapturer = true
        #endif
        webRTCClient = WebRTCClient()
        webRTCClient.delegate = self
        webRTCClient.setup(videoTrack: true, audioTrack: true, dataChannel: true, customFrameCapturer: useCustomCapturer)
        NotificationCenter.default.addObserver(self, selector: #selector(websocketDidReceiveMessage), name: .updateDataFromControllerApp, object: nil)
        if useCustomCapturer {
            _ = CameraController.shared.captureSession;
        }
        if !webRTCClient.isConnected {
            webRTCClient.connect(onSuccess: { (offerSDP: RTCSessionDescription) -> Void in
                self.sendSDP(sessionDescription: offerSDP)
            })
        }
    }

    /// function to send ICE candidate object to the controller
    ///
    /// - Parameter iceCandidate:
    func sendCandidate(iceCandidate: RTCIceCandidate) {
        let candidate = Candidate(candidate: iceCandidate.sdp, label: iceCandidate.sdpMLineIndex, id: iceCandidate.sdpMid!, type: "candidate");
        let signalingMessage = JSON.toString(CandidateEvent(status: .init(WEB_RTC_EVENT: candidate)));
        client.send(message: signalingMessage);
    }

    /// function to read the message from the controller connection
    @objc func websocketDidReceiveMessage(_ notification: Notification) {
        guard let text = notification.object as? Data else { return }
        do {
            let signalingMessage = try JSONDecoder().decode(AnswerEvent.self, from: text)
            if signalingMessage.webrtc_event.type == "offer" {
                webRTCClient.receiveOffer(offerSDP: RTCSessionDescription(type: .offer, sdp: (signalingMessage.webrtc_event.sdp)), onCreateAnswer: { (answerSDP: RTCSessionDescription) -> Void in
                    self.sendSDP(sessionDescription: answerSDP)
                })
            } else if signalingMessage.webrtc_event.type == "answer" {
                webRTCClient.receiveAnswer(answerSDP: RTCSessionDescription(type: .answer, sdp: (signalingMessage.webrtc_event.sdp)))
            } else if signalingMessage.webrtc_event.type == "candidate" {
            }
        } catch {
            print(error)

        }
    }

    ///function to send the offer/answer SDP to the controller
    private func sendSDP(sessionDescription: RTCSessionDescription) {
        var type = ""
        if sessionDescription.type == .offer {
            type = "offer"
        } else if sessionDescription.type == .answer {
            type = "answer"
        }
        let signalingMessage = JSON.toString(OfferEvent(status: .init(WEB_RTC_EVENT: .init(type: type, sdp: sessionDescription.sdp))));
        client.send(message: signalingMessage);
    }
}
