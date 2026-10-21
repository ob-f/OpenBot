//
// Created by Nitish Yadav on 04/08/23.
//

import Foundation
struct ServerDecodingSignalingMessage: Decodable {

    let type: String
    let sdp: String
    let candidate: Candidate?
    let roomId: String
}

struct ServerSignalingMessage: Codable {
    let type: String
    let sdp: String
    let roomId: String
    let candidate: ServerCandidate?

}

struct ServerAnswerEvent: Decodable {

    var webrtc_event: Answer
    let roomId: String
    struct Answer: Codable {
        var type: String
        var sdp: String
    }

}

struct ServerConnectionActiveEvent: Codable {

    var status: ConnectionActive
    let roomId: String
    struct ConnectionActive: Codable {
        var CONNECTION_ACTIVE: String
    }
}

struct ServerVideoProtocolEvent: Codable {

    var status: VideoProtocol
    let roomId: String
    struct VideoProtocol: Codable {
        var VIDEO_PROTOCOL: String
    }
}

struct ServerVideoServerUrlEvent: Codable {

    var status: VideoServerUrl
    let roomId: String
    struct VideoServerUrl: Codable {
        var VIDEO_SERVER_URL: String
    }
}

struct ServerCandidate: Codable {
    var candidate: String
    var label: Int32
    var id: String
    var type: String
    let roomId: String
}

struct ServerVideoCommandEvent: Codable {
    var status: VideoCommand
    let roomId: String
    struct VideoCommand: Codable {
        var VIDEO_COMMAND: String
    }

}

struct ServerVehicleStatusEvent: Codable {
    var status: VehicleStatus
    let roomId: String
    struct VehicleStatus: Codable {
        var LOGS: Bool
        var NOISE: Bool
        var NETWORK: Bool
        var DRIVE_MODE: String
        var INDICATOR_LEFT: Bool
        var INDICATOR_RIGHT: Bool
        var INDICATOR_STOP: Bool
    }
}

struct ServerOfferEvent: Codable {
    var status: WebRTC
    let roomId: String
    struct WebRTC: Codable {
        var WEB_RTC_EVENT: Offer
    }

    struct Offer: Codable {
        var type: String
        var sdp: String
    }
}

struct ServerCandidateEvent: Codable {
    var status: WebRTC
    let roomId: String
    struct WebRTC: Codable {
        var WEB_RTC_EVENT: Candidate
    }
}

struct ServerOpenBotSignal : Codable {
    var openbot : Int
    let roomId: String
}

