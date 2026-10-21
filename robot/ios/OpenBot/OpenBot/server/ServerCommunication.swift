//
// Created by Nitish Yadav on 15/06/23.
//

import Foundation
import Network

var serverConnection: ServerCommunication?

class ServerCommunication {
    let connection: NWConnection
    static var serverEndPoint: Endpoint?
    var timer : Timer?;
    // outgoing connection
    /// initializing function; endpoint
    init(endpoint: NWEndpoint) {
        print("PeerConnection outgoing endpoint: \(endpoint) endPoint interface is : \(endpoint.interface)")
        let tcpOptions = NWProtocolTCP.Options()
        tcpOptions.enableKeepalive = true
        tcpOptions.keepaliveIdle = 2
        let parameters = NWParameters(tls: nil, tcp: tcpOptions)
        parameters.includePeerToPeer = true
        connection = NWConnection(to: endpoint, using: parameters)
        start()

    }

    /// initializing function; incoming connection
    init(connection: NWConnection) {
        self.connection = connection
        start()
    }

    /// function to start the connection
    func start() {
        connection.stateUpdateHandler = { newState in
            switch newState {
            case .ready:
                if let endpoint = self.connection.currentPath?.remoteEndpoint {
                    if case let NWEndpoint.hostPort(host, port) = endpoint {
                        let end = self.connection.endpoint
                        if case NWEndpoint.service(let name, let type, let domain, let interface) = end {
                            let hostComponents = host.debugDescription.components(separatedBy: "%")
                            let ipAddress = hostComponents.first ?? ""
                            print("ip address is :", ipAddress);
                            ServerCommunication.serverEndPoint = Endpoint(name: name, host: ipAddress, port: port.debugDescription);
                        }
                    }
                }
                self.modelListHandler();
            case .preparing:
                return;
            default:
                break;
            }
        }
        connection.start(queue: .main)
    }


    func callApi(completion: @escaping (Data?, Error?) -> Void) {
        let models: [String] = [];
        guard let host = ServerCommunication.serverEndPoint?.host else {
            print("Invalid URL host")
            return
        }
        guard let port = ServerCommunication.serverEndPoint?.port else {
            print("Invalid URL port");
            return
        }
        guard let url = URL(string: "http://\(host):\(port)/models") else {
            print("Invalid URL found")
            return
        }
        let config = URLSessionConfiguration.default

        let session = URLSession(configuration: config)
        let task = session.dataTask(with: url) { (data, response, error) in
            // Handle the response
            if let error = error {
                print("Request error: \(error.localizedDescription)")
            } else if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200 {
                    // Request succeeded
                    if let data = data {
                        completion(data, nil);
                    }
                } else {
                    // Request failed with a non-200 status code
                    print("Request failed with status code: \(httpResponse.statusCode)")
                    completion(nil, error);
                }
            }
        }

// Start the task
        task.resume()
    }

    func modelListHandler() {
        timer = Timer.scheduledTimer(withTimeInterval: 10, repeats: true) { [self] timer in
            callApi { data, error in
                if let data = data {
                    do {
                        let models = try JSONDecoder().decode([model].self, from: data);
                        for model in models {
                            downloadFile(name: model.name ?? "");
                        }
                    } catch {
                        print(error);
                    }
                }
                if let error = error {
                    print(error);
                }

            }
        }
    }

    func downloadFile(name: String) {
        guard let host = ServerCommunication.serverEndPoint?.host else {
            print("Invalid URL host")
            return
        }
        guard let port = ServerCommunication.serverEndPoint?.port else {
            print("Invalid URL port");
            return
        }
        guard let url = URL(string: "http://\(host):\(port)/models/\(name)") else {
            print("Invalid URL found")
            return
        }
        let config = URLSessionConfiguration.default

        let session = URLSession(configuration: config)
        let task = session.dataTask(with: url) { (data, response, error) in
            // Handle the response
            if let error = error {
                print("Request error: \(error.localizedDescription)")
            } else if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200 {
                    // Request succeeded
                    if let data = data {
                        // Process the received data
                        // Example: Convert data to a string
                        let documentsUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
                        let destinationUrl = documentsUrl.appendingPathComponent(name);
                        if FileManager().fileExists(atPath: destinationUrl.path) {
                            print("File already exists [\(destinationUrl.path)]")
                        } else {
                            if let _ = try? data.write(to: destinationUrl, options: Data.WritingOptions.atomic) {
                                print("file downloaded successfully");
                                //update the config.json
                                var allModels = Common.loadAllModelItems();
                                let model = ModelItem(id: (Common.loadAllModelItems().last?.id ?? Common.loadAllModelItems().count) + 1, class: "AUTOPILOT", type: "CMDNAV", name: name, pathType: "FILE", path: "", inputSize: "256x96");
                                allModels.append(model);
                                try! Common.saveConfigFileToDocument(modelItems: allModels);
                            } else {
                                print("unable to download  \(name)");
                            }
                        }
                    }
                } else {
                    // Request failed with a non-200 status code
                    print("Request failed with status code: \(httpResponse.statusCode)")
                }
            }
        }

// Start the task
        task.resume()
    }

}

public struct model: Decodable {
    var name: String;
    var mtime: Int;
}