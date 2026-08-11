//
// Created by Nitish Yadav on 25/07/23.
//

import Foundation
import AVFoundation
import Starscream
import WebRTC
import FirebaseAuth
/// function to create webRTC Delegate
class ServerWebrtcDelegate: WebRTCClientDelegate {
    /// tracks the active web controller delegate to avoid duplicate message handling
    static weak var activeDelegate: ServerWebrtcDelegate?
    var mSocket = NativeWebSocket.shared;
    var useCustomCapturer: Bool = true
    let webSocketMsgHandler = WebSocketMessageHandler();
    let roomId =  Auth.auth().currentUser?.email ?? ""

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
        let msg = String(data: data, encoding: .utf8)
        if msg == nil{
            return
        }
        let message = msg!.replacingOccurrences(of: "\\", with: "")
        print("data received", message);
        NotificationCenter.default.post(name: .updateDataFromControllerApp, object: message);
        let jsonDecoder = JSONDecoder();
        let text: Data = message.data(using: .utf8)!;
        if message.contains("driveCmd") {
            let cmd = try! jsonDecoder.decode(serverMessage.self, from: text);
            print(cmd.driveCmd.l);
            self.webSocketMsgHandler.driveCommand(control: Control(left: cmd.driveCmd.l, right: cmd.driveCmd.r));
        } else if message.contains("command") {
            guard let cmd = try? jsonDecoder.decode(serverCmd.self, from: text) else { return }
            print(cmd.command);
            switch cmd.command {
            case "INDICATOR_LEFT":
                self.webSocketMsgHandler.indicatorLeft()
                break;
            case "INDICATOR_RIGHT":
                self.webSocketMsgHandler.indicatorRight();
                break
            case "INDICATOR_STOP":
                self.webSocketMsgHandler.cancelIndicator();
            case "SPEED_DOWN":
                self.webSocketMsgHandler.speedDown();
                break;
            case "SPEED_UP":
                self.webSocketMsgHandler.speedUp();
                break;
            case "DRIVE_MODE":
                self.webSocketMsgHandler.driveMode()
                break;
            case "NETWORK":
                self.webSocketMsgHandler.toggleNetwork()
                break;
            case "LOGS":
                self.webSocketMsgHandler.toggleLogging()
                break
            case "SWITCH_CAMERA":
                NotificationCenter.default.post(name: .switchCamera, object: nil);
                break;
            default:
                break;
            }
        } else {
            do {
                let signalingMessage = try JSONDecoder().decode(AnswerEvent.self, from: text)
                if signalingMessage.webrtc_event.type == "offer" {
                    print("offer")
                    webRTCClient.receiveOffer(offerSDP: RTCSessionDescription(type: .offer, sdp: (signalingMessage.webrtc_event.sdp)), onCreateAnswer: { (answerSDP: RTCSessionDescription) -> Void in
                        self.sendSDP(sessionDescription: answerSDP)
                    })
                } else if signalingMessage.webrtc_event.type == "answer" {
                    print("answer")
                    webRTCClient.receiveAnswer(answerSDP: RTCSessionDescription(type: .answer, sdp: (signalingMessage.webrtc_event.sdp)));
                } else if signalingMessage.webrtc_event.type == "candidate" {
                }

            } catch {
                print(error)
            }
        }
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
        ServerWebrtcDelegate.activeDelegate = self
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
        let signalingMessage = JSON.toString(ServerCandidateEvent(status: .init(WEB_RTC_EVENT: candidate), roomId: roomId));
        let data = signalingMessage.data(using: .utf8);
        print("sending to server ->",signalingMessage);
        mSocket.send(data: data!);
    }

    /// function to read the message from the controller connection
    @objc func websocketDidReceiveMessage(_ notification: Notification) {
        guard ServerWebrtcDelegate.activeDelegate === self else { return }
        let msg = notification.object as! String
        let jsonDecoder = JSONDecoder();
        let text: Data = msg.data(using: .utf8)!;
        print("inside websocketDidReceiveMessage",msg)

        if msg.contains("driveCmd") {
            let cmd = try! jsonDecoder.decode(serverMessage.self, from: text);
            print(cmd.driveCmd.l);
            self.webSocketMsgHandler.driveCommand(control: Control(left: cmd.driveCmd.l, right: cmd.driveCmd.r));
        } else if msg.contains("command") {
            guard let cmd = try? jsonDecoder.decode(serverCmd.self, from: text) else { return }
            print(cmd.command);
            switch cmd.command {
            case "INDICATOR_LEFT":
                self.webSocketMsgHandler.indicatorLeft()
                break;
            case "INDICATOR_RIGHT":
                self.webSocketMsgHandler.indicatorRight();
                break
            case "INDICATOR_STOP":
                self.webSocketMsgHandler.cancelIndicator();
            case "SPEED_DOWN":
                self.webSocketMsgHandler.speedDown();
                break;
            case "SPEED_UP":
                self.webSocketMsgHandler.speedUp();
                break;
            case "DRIVE_MODE":
                self.webSocketMsgHandler.driveMode()
                break
            case "LOGS":
                self.webSocketMsgHandler.toggleLogging()
                break
            case "NETWORK":
                self.webSocketMsgHandler.toggleNetwork()
                break
            case "SWITCH_CAMERA":
                NotificationCenter.default.post(name: .switchCamera, object: nil);
                break;
            default:
                break;
            }
        } else {
            do {
                let signalingMessage = try JSONDecoder().decode(AnswerEvent.self, from: text)
                if signalingMessage.webrtc_event.type == "offer" {
                    print("offer")
                    webRTCClient.receiveOffer(offerSDP: RTCSessionDescription(type: .offer, sdp: (signalingMessage.webrtc_event.sdp)), onCreateAnswer: { (answerSDP: RTCSessionDescription) -> Void in
                        self.sendSDP(sessionDescription: answerSDP)
                    })
                } else if signalingMessage.webrtc_event.type == "answer" {
                    print("answer")
                    webRTCClient.receiveAnswer(answerSDP: RTCSessionDescription(type: .answer, sdp: (signalingMessage.webrtc_event.sdp)));
                } else if signalingMessage.webrtc_event.type == "candidate" {
                }

            } catch {
                print(error)
            }
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
        let signalingMessage = JSON.toString(ServerOfferEvent(status: .init(WEB_RTC_EVENT: .init(type: type, sdp: sessionDescription.sdp)), roomId: roomId ));
        let data = signalingMessage.data(using: .utf8);
        print("sending to server ->",signalingMessage);
        mSocket.send(data: data!);
    }
}
struct serverMessage: Decodable {
    var driveCmd: serverCommand
    var roomId: String
}

struct serverCommand: Decodable {
    var l: Float
    var r: Float
}

struct serverCmd: Decodable {
    var command: String
    var roomId: String?
}

