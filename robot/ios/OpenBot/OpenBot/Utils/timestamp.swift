//
// Created by Nitish Yadav on 27/09/22.
//

import Foundation

/// Timestamp generator for data recording purposes
///
/// - Returns: amount of time the system has been awake since the last time it was restarted in nanosecond.
func returnCurrentTimestamp() -> Int {
    Int(ProcessInfo.processInfo.systemUptime * 1000000000)
}

/// Precise timing function for fps count
///
/// - Returns: Time after 1970 in millisecond
func returnCurrentTimeStampSince1970() -> Int {
    let currentDate = Date()
    let since1970 = currentDate.timeIntervalSince1970
    return Int(since1970 * 1000)
}
