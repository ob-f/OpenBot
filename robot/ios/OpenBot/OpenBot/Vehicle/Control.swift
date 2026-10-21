//
// Created by Sparsh Jain on 03/09/22.
//

import Foundation

// Lookup table for single digit precision value simplification
// private let lookupTable: [Float] = {
//     var table = [Float]()
    //     for i in -10...10 {
        //         table.append(Float(i) / 10)
        //     }
    //     return table
    // }()

/// A simple data container for the robot control
class Control {
    // Left and right controls represents the fraction of maximal duty cycle of a
    // PWM signal sent to the motors of the robot's left and right wheels.
    private var left: Float = 0
    private var right: Float = 0
    
    /// Rounding routine to avoid jamming the BLE connection
    ///
    /// - Parameters: Input value
    /// - Returns: Rounded value
    //   private func controlLookup(_ value: Float) -> Float {
        //      if value > 1.0 {
            //          return 1.0
            //     } else if value < -1.0 {
            //         return -1.0
            //     }
        //     let index = Int((value * 10).rounded())+10
        //     return lookupTable[index]
        // }
    
    /// Initialization routine
    init() {
    }
    
    /// Initialization routine
    ///
    /// - Parameters:
    ///     - left input value
    ///     - right input value
    init(left: Float, right: Float) {
        // self.left = controlLookup(left)
        // self.right = controlLookup(right)
        self.left = left
        self.right = right
        //print("Control: ", left, ",", right)
    }
    
    /// Left control getter
    public func getLeft() -> Float {
        left
    }
    
    /// Right control getter
    public func getRight() -> Float {
        right
    }
}
