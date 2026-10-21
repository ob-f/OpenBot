//
// Created by Nitish Yadav on 15/06/23.
//

import Foundation
import Network
class ServerListener {
    let browser = NsdService();
    ///function to start the browser to create the client.
    func start() {
        browser.start { result in
            print("client.handler result: \(result)")
        }
    }

/// Function run after a service added
    func nsdServiceDidChangeServers(_ nsdService: NsdService) {
        print("Something is added to the server");
    }
}