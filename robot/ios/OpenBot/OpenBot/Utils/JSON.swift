//
// Created by Sparsh Jain on 06/01/23.
//

import Foundation

class JSON {
    /// function to convert the JSON object into String
    ///
    /// - Parameter object: JSON object
    /// - Returns: stringified JSON
    static func toString(_ object: Codable) -> String {
        let data = try! JSONEncoder().encode(object)
        let message = String(data: data, encoding: .utf8)!
        return message;
    }
}