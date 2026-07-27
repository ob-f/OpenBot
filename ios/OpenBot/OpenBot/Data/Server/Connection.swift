//
// Created by Nitish Yadav on 15/12/22.
//

import Foundation
import Network

var sharedConnection: Connection?

protocol sendInitialMessageDelegate: AnyObject {
    func sendMessage()
}

protocol startStreamDelegate: AnyObject {
    func startVideoStream()
}

class Connection: sendInitialMessageDelegate, startStreamDelegate {

    let connection: NWConnection
    // outgoing connection
    weak var msgDelegate: sendInitialMessageDelegate?
    weak var startStreamDelegate: startStreamDelegate?
    let fragmentType = FragmentType.shared

    /// initializing function; endpoint
    init(endpoint: NWEndpoint) {
        print("PeerConnection outgoing endpoint: \(endpoint)")
        let tcpOptions = NWProtocolTCP.Options()
        tcpOptions.enableKeepalive = true
        tcpOptions.keepaliveIdle = 2
        let parameters = NWParameters(tls: nil, tcp: tcpOptions)
        parameters.includePeerToPeer = true
        parameters.allowLocalEndpointReuse = true
        connection = NWConnection(to: endpoint, using: parameters)
        start()
        msgDelegate = self
        startStreamDelegate = self
    }

    /// initializing function; incoming connection
    init(connection: NWConnection) {
        self.connection = connection
        start()
    }

    /// function to start the connection
    func start() {
        connection.stateUpdateHandler = { newState in
            print("connection.stateUpdateHandler \(newState)")
            switch newState {
            case .ready:
                self.receiveMessage();
                self.msgDelegate?.sendMessage()
                self.startStreamDelegate?.startVideoStream()
            case .preparing:
                return
            default:
                break;
            }
        }
        connection.start(queue: .main)
    }
    
    /// function to send data in utf8 format.
    func send(_ message: String) {
        print("in the send function::::",message);
        connection.send(content: message.data(using: .utf8), contentContext: .defaultMessage, isComplete: true, completion: .contentProcessed({ error in
            print("Connection send error: \(String(describing: error))")
        }))
    }

    /// function to hold the connection to receive incoming messages; parse them based on the commands and generate notifications resp;
    func receiveMessage() {
        connection.receive(minimumIncompleteLength: 1, maximumLength: 100000) { data, _, _, error in
            if error != nil {
                return
            }
            if let data = data,
               let message = String(data: data, encoding: .utf8) {
                let commands = message.split(separator: "\n");
                for command in commands {
                    if command.contains("SWITCH_CAMERA") {
                        NotificationCenter.default.post(name: .switchCamera, object: nil);
                    }
                    if command.contains("answer") {
                        if(command.contains("{\"webrtc_event\":")){
                            NotificationCenter.default.post(name: .updateDataFromControllerApp, object: command.data(using: .utf8));
                        }
                        else{
                            let y = command.components(separatedBy: "{webrtc_event: ");
                            let newCommand = "{\"webrtc_event\":" + y[1];
                            NotificationCenter.default.post(name: .updateDataFromControllerApp, object: newCommand.data(using: .utf8));
                        }
                    }
                    if command.contains("driveCmd") {
                        NotificationCenter.default.post(name: .updateStringFromControllerApp, object: message);
                    }
                    if command.contains("command"){
                        NotificationCenter.default.post(name: .updateLightsCommandFromControllerApp, object: message);
                    }
                    print(command);
                }
            }
            self.receiveMessage()
        }
    }

    /// function to parse the message for the connection and send it.
    func sendMessage() {
        var msg = JSON.toString(ConnectionActiveEvent(status: .init(CONNECTION_ACTIVE: "true")));
        client.send(message: msg);
        msg = JSON.toString(VideoProtocolEvent(status: .init(VIDEO_PROTOCOL: "WEBRTC")));
        client.send(message: msg);
        msg = JSON.toString(VideoServerUrlEvent(status: .init(VIDEO_SERVER_URL: "")));
        client.send(message: msg);
        msg = JSON.toString(VideoCommandEvent(status: .init(VIDEO_COMMAND: "START")));
        client.send(message: msg);
        msg = JSON.toString(VehicleStatusEvent(status: .init(LOGS: false, NOISE: false, NETWORK: false, DRIVE_MODE: "GAME", INDICATOR_LEFT: false, INDICATOR_RIGHT: false, INDICATOR_STOP: true)));
        client.send(message: msg)
        msg = JSON.toString(FragmentTypeEvent(status: .init(FRAGMENT_TYPE: fragmentType.currentFragment)));
        client.send(message: msg);
    }

    /// function to start the video streams
    func startVideoStream() {
        WebRTCDelegates();
    }


}
