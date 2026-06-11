//
// Created by Nitish Yadav on 31/07/23.
//

import Foundation
import GoogleSignIn
import FirebaseAuth

@available(iOS 13.0, *)
class NativeWebSocket: NSObject, WebSocketProvider {
    let url = URL(string: "ws://192.168.1.40:8080/ws")!

    var roomId: String {
        Auth.auth().currentUser?.email ?? ""
    }
    static let shared: NativeWebSocket = NativeWebSocket();
    var delegate: WebSocketProviderDelegate?
    private var socket: URLSessionWebSocketTask?
    private lazy var urlSession: URLSession = URLSession(configuration: .default, delegate: self, delegateQueue: nil)
    private var onReady: (() -> Void)?
    private var isOpen = false

    override init() {
        super.init()
    }

    func connect() {
        connectWebServer()
    }

    func connectWebServer(onReady: (() -> Void)? = nil) {
        if let onReady = onReady {
            self.onReady = onReady
        }
        if isOpen {
            sendRoomJoin()
            self.onReady?()
            self.onReady = nil
            return
        }
        socket?.cancel(with: .goingAway, reason: nil)
        isOpen = false
        let socket = urlSession.webSocketTask(with: url)
        socket.resume()
        self.socket = socket
        readMessage()
    }

    func send(data: Data) {
        guard let text = String(data: data, encoding: .utf8) else { return }
        socket?.send(.string(text)) { _ in }
    }

    private func sendRoomJoin() {
        guard !roomId.isEmpty else { return }
        let response = try! JSONEncoder().encode(responseId(roomId: roomId, clientType: "robot"))
        send(data: response)
    }

    private func readMessage() {
        self.socket?.receive { [weak self] message in
            guard let self = self else {
                return
            }
            switch message {
            case .success(.data(let data)):
                self.delegate?.webSocket(self, didReceiveData: data)
                self.readMessage()
            case .success(.string(let text)):
                if text.contains("request-roomId") {
                    self.sendRoomJoin()
                }
                // deliver web socket messages on the main thread
                DispatchQueue.main.async {
                    self.delegate?.webSocket(self, didReceiveData: text)
                    NotificationCenter.default.post(name: .updateDataFromControllerApp, object: text)
                }
                self.readMessage()

            case .success:
                debugPrint("Warning: Expected to receive data format but received a string. Check the websocket server config.")
                self.readMessage()

            case .failure:
                self.disconnect()
            }
        }
    }

    private func disconnect() {
        isOpen = false
        self.socket?.cancel()
        self.socket = nil
        NotificationCenter.default.post(name: .clientDisConnected, object: nil);
        self.delegate?.webSocketDidDisconnect(self)
    }
}

@available(iOS 13.0, *)
extension NativeWebSocket: URLSessionWebSocketDelegate, URLSessionDelegate {
    func urlSession(_ session: URLSession, webSocketTask: URLSessionWebSocketTask, didOpenWithProtocol protocol: String?) {
        isOpen = true
        self.delegate?.webSocketDidConnect(self)
        NotificationCenter.default.post(name: .clientConnected, object: nil);
        sendRoomJoin()
        onReady?()
        onReady = nil
    }

    func urlSession(_ session: URLSession, webSocketTask: URLSessionWebSocketTask, didCloseWith closeCode: URLSessionWebSocketTask.CloseCode, reason: Data?) {
        self.disconnect()
    }
}

struct requestId: Decodable {
    var roomId: String
}

struct responseId: Encodable {
    var roomId: String
    var clientType: String
}
