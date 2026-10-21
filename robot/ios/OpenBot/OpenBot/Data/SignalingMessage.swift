//
// Created by Nitish Yadav on 03/01/23.
//

import Foundation

/// struct used to decode the signalling message
struct decodingSignalingMessage: Decodable {
    let type: String
    let sdp: String
    let candidate: Candidate?
}

/// Used to send the signal message to the WEBRTC connected controller device
struct SignalingMessage: Codable {
    let type: String
    let sdp: String
    let candidate: Candidate?

}

/// Used to create an WEBRTC event for answering the request.
struct AnswerEvent: Decodable {
    var webrtc_event: Answer

    struct Answer: Codable {
        var type: String
        var sdp: String
    }
}

struct ConnectionActiveEvent: Codable {
    var status: ConnectionActive

    struct ConnectionActive: Codable {
        var CONNECTION_ACTIVE: String
    }
}

struct VideoProtocolEvent: Codable {
    var status: VideoProtocol

    struct VideoProtocol: Codable {
        var VIDEO_PROTOCOL: String
    }
}

struct VideoServerUrlEvent: Codable {
    var status: VideoServerUrl

    struct VideoServerUrl: Codable {
        var VIDEO_SERVER_URL: String
    }
}


struct Candidate: Codable {
    var candidate: String
    var label: Int32
    var id: String
    var type: String
}

struct VideoCommandEvent: Codable {
    var status: VideoCommand

    struct VideoCommand: Codable {
        var VIDEO_COMMAND: String
    }
}

struct VehicleStatusEvent: Codable {
    var status: VehicleStatus

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

struct FragmentTypeEvent: Codable {
    var status: FragmentTypeStatus

    struct FragmentTypeStatus: Codable {
        var FRAGMENT_TYPE: String
    }
}

struct OfferEvent: Codable {
    var status: WebRTC

    struct WebRTC: Codable {
        var WEB_RTC_EVENT: Offer
    }

    struct Offer: Codable {
        var type: String
        var sdp: String
    }
}

struct CandidateEvent: Codable {
    var status: WebRTC

    struct WebRTC: Codable {
        var WEB_RTC_EVENT: Candidate
    }
}

struct OpenBotSignal : Codable {
    var openbot : Int
}
