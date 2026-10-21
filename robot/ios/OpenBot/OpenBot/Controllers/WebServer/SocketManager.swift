//
// Created by Nitish Yadav on 25/07/23.
//

import Foundation
import SocketIO
class WebSocketManager : NSObject {
    var isConnected :Bool = false;
    static let shared = WebSocketManager()
    var socket: SocketIOClient!
    let manager = SocketManager(socketURL: URL(string: "https://openbot-web-socket-server.onrender.com/")!, config: [.log(true), .compress])

    override init() {
        super.init()
        socket = manager.defaultSocket
    }

    func connectSocket() {
        socket.connect()
    }

    func receiveMsg() {
        socket.on("new message here") { (dataArray, ack) in
            print(dataArray.count)
        }
    }
}

