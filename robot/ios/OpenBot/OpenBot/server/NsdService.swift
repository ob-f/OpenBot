//
// Created by Nitish Yadav on 15/06/23.
//

import Foundation
import Network

var servers: [String] = ["No Server"]
var serverItems: [ServerItem] = [];

class NsdService {
    var browser: NWBrowser;

    init() {
        let parameters = NWParameters()
        parameters.includePeerToPeer = true
        browser = NWBrowser(for: .bonjour(type: "_openbot-server._tcp.", domain: nil), using: parameters)
    }

    /// function to start the service and listen for the state changes into the connection.
    func start(handler: @escaping (NWBrowser.Result) -> Void) {
        browser.stateUpdateHandler = { newState in
            switch newState {
            case .failed(let error):
                print(error.localizedDescription)
            case .ready, .cancelled:
                break
            case let .waiting(error):
                print("Local network permission has been denied: \(error)")
                self.browser.cancel()
            default:
                break
            }
        }
        browser.browseResultsChangedHandler = { results, changes in
            for result in results {
                print("results are =======", result)
                if case NWEndpoint.service = result.endpoint {
                    handler(result)
                }

                if case NWEndpoint.service(let name, let type, let domain, let interface) = result.endpoint {
                    print("name  ========", name);
                    print("domain =========", domain);
                    print("type ==========", type);
                    print("interface =======", interface);
                    if !servers.contains(name) {
                        servers.append(name);
                    }
                    NotificationCenter.default.post(name: .server, object: nil);
                }
            }

            for (_, change) in changes.enumerated() {
                switch change {
                case .added(let browseResult):
                    switch browseResult.endpoint {
                    case .hostPort(let host, let port):
                        print("added hostPort \(host) \(port)")
                    case .service(let name, let type, let domain, let interface):
                        print("added service \(name) \(type) \(domain) \(String(describing: interface))")
                        let item = ServerItem(name: name, endpoint: browseResult.endpoint)
                        if !serverItems.contains(where: { item in
                            item.name == name;
                        }) {
                            serverItems.append(item);
                        }
                    default:
                        print("fail")
                    }
                case .removed(let browseResult):
                    print("removed \(browseResult.endpoint)")
                    serverConnection = nil;

                case .changed(_, let browseResult, let flags):
                    if flags.contains(.interfaceAdded) {
                        print("\(browseResult.endpoint) added interfaces")
                    }
                    if flags.contains(.interfaceRemoved) {
                        print("\(browseResult.endpoint) removed interfaces")
                    }
                default:
                    print("no change")
                }
            }
        }
        browser.start(queue: .main)
    }
}