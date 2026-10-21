//
// Created by Nitish Yadav on 15/12/22.
//

import Foundation

var client = Client()

class Client {
    let browser = Browser()

    ///function to start the browser to create the client.
    func start() {
        browser.start { result in
            print("client.handler result: \(result)")
            print(" client is :", client)
        }
    }

    /// function to send the values to the connection.
    func send(message: String) {
        print("in send---->",sharedConnection)
        sharedConnection?.send(message + "\n");
    }
}
