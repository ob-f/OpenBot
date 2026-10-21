//
//  BlocklyTaskStorage.swift
//  OpenBot
//
//  Created by Hardik Garg on 07/09/23.
//

import Foundation

class taskStorage {

    // Static property to hold an array of tasks organized by classType
    static var taskArray: [[String: [[String: Any]]]] = []

    /**
     Function to add an attribute to the taskArray
     - Parameters:
       - classType:
       - task:
       - frames:
       - type:
     */
    static func addAttribute(classType: String, task: String, frames: Int, type: String) {
        if let index = taskArray.firstIndex(where: { $0.keys.contains(classType) }) {
            if var existingDict = taskArray[index][classType] {
                if type != "detect" {
                    existingDict.append(["frames": frames])
                }
                existingDict.append([type: task])
                taskArray[index][classType] = existingDict
            }
        } else {
            var dictArray: [[String: Any]] = []
            if type != "detect" {
                dictArray.append(["frames": frames])
            }
            dictArray.append([type: task])
            taskArray.append([classType: dictArray])
        }
    }

    /**
     Function to get the value of a specific attribute
     - Parameters:
       - classType:
       - type:
     - Returns:
     */
    static func getValueOfAttribute(classType: String, type: String) -> Any? {
        for i in taskArray {
            if let value = i[classType] {
                for j in value {
                    if let innerValue = j[type] {
                        return innerValue;
                    }
                }
            }
        }
        return nil;
    }

    /**
     Function to return the entire taskArray
     - Returns:
     */
    static func returnAttributeArray() -> [[String: [[String: Any]]]] {
        taskArray
    }

}
