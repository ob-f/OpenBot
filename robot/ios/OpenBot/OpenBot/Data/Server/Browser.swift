//
// Created by Nitish Yadav on 15/12/22.
//

import Foundation
import Network

let browser = Browser()
var netService: NetService?


class Browser {
    var browser: NWBrowser

    /// initializing function. (creating Service with the name "_openbot._tcp.").
    init() {
        let parameters = NWParameters()
        parameters.includePeerToPeer = true

        browser = NWBrowser(for: .bonjour(type: "_openbot._tcp.", domain: nil), using: parameters)
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
                print(result)
                if case NWEndpoint.service = result.endpoint {
                    handler(result)
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
                        NotificationCenter.default.post(name: .clientConnected, object: nil);
                        sharedConnection = Connection(endpoint: browseResult.endpoint)
                    default:
                        print("fail")
                    }
                case .removed(let browseResult):
                    NotificationCenter.default.post(name: .clientDisConnected, object: nil);
                    print("removed \(browseResult.endpoint)")
                    sharedConnection = nil;
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

    ///To reset the connection.
    func reset() {
        browser.cancel()
        netService?.stop()
        netService = nil
    }


}