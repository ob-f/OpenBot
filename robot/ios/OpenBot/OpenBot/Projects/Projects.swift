//
// Created by Nitish Yadav on 13/04/23.
//

import Foundation
import Foundation
/**
 Structure of project item
 */
public struct ProjectItem: Codable {
    var projectName: String
    var projectDate: String
    var projectId: String
}
/**
 structure of project data. This structure prototype contains project id and command of particular project
 */
public struct ProjectData: Codable {
    var projectId: String
    var projectCommand: String
}

/**
 structure of qr scanned data.
 */
public struct QrData: Codable {
    let driveLink: String
    let projectName: String
}


