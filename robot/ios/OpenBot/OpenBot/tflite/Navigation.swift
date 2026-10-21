//
// Created by Nitish Yadav on 21/02/23.
//

import Foundation
import TensorFlowLite

class Navigation: Network {
    let IMAGE_MEAN: Float = 0.0
    let IMAGE_STD: Float = 255.0
    private var goalIndex: Int = 0
    private var imgIndex: Int = 0
    var goalData: Data?
    
    /// Initializes a point-goal navigation autopilot.
    ///
    /// - Parameters:
    ///     - model: the model considered in the inference process
    ///     - device: CPU, GPU or XNNPACK (neural engine)
    ///     - numThreads: number of threads used tin the inference process
    override init(model: Model, device: RuntimeDevice, numThreads: Int) {
        try! super.init(model: model, device: device, numThreads: numThreads)
        do {
            let index0 = try tflite?.input(at: 0)
            let index1 = try tflite?.input(at: 1)
            assignIndex(tensor: index0, index: 0)
            assignIndex(tensor: index1, index: 1)
        } catch {
            print("error:\(error)")
        }
    }

    /// Index  allocation to browse the tflite object
    ///
    /// - Parameters:
    ///     - tensor: a tensor structure from a tflite object
    ///     - index: the index to be assigned to the tensor
    func assignIndex(tensor: Tensor?, index: Int) {
        switch (tensor?.name) {
        case "serving_default_goal_input:0", "cmd_input":
            goalIndex = index
            break
        case "serving_default_img_input:0", "img_input":
            imgIndex = index
            break
        default:
            break
        }
    }

    /// Propagate an image and pose-error through a neural network to generate suitable control output.
    ///
    /// - Parameters
    ///   - goalDistance: metric distance to the goal
    ///   - goalSin: tangent projection of the normalized robot-to-goal vector expressed in the robot frame
    ///   - goalCos: normal projection of the normalized robot-to-goal vector expressed in the robot frame
    /// - Returns: A control reference to be sent to the robot
    func recognizeImage(pixelBuffer: CVPixelBuffer, goalDistance: Float, goalSin: Float, goalCos: Float) -> Control {
        
        // Crops the image to the biggest square in the center and scales it down to model dimensions.
        let scaledSize = CGSize(width: 160, height: 120)
        guard let scaledPixelBuffer = pixelBuffer.resizeAndCropTop(to: scaledSize, top: 30, preserveAspectRatio: false, with: self.preAllocatedMemoryPool!) else {
            return Control(left: 0, right: 0)
        }
        
        do {
            // Get a control reference.
            convertGoalToData(goalDistance: goalDistance, goalSin: goalSin, goalCos: goalCos)
            
            // Pre-proccess input image.
            let inputTensor = try tflite!.input(at: imgIndex)
            guard let rgbData = rgbDataFromBuffer(scaledPixelBuffer, isModelQuantized: inputTensor.dataType == .uInt8) else {
                return Control(left: 0, right: 0)
            }
            
            // Copy the input data into TensorFlow.
            try tflite?.copy(goalData!, toInputAt: goalIndex)
            try tflite?.copy(rgbData, toInputAt: imgIndex)
            
            // Run the inference call.
            try tflite?.invoke()
            
            // Post-Processing.
            let outputTensor = try tflite?.output(at: 0)
            let outputSize = outputTensor?.shape.dimensions.reduce(1, { x, y in x * y }) ?? 0
            let outputData = UnsafeMutableBufferPointer<Float32>.allocate(capacity: outputSize)
            _ = outputTensor?.data.copyBytes(to: outputData)
            return Control(left: outputData[0], right: outputData[1])
        } catch {
            print("error:\(error)")
            return Control(left: 0, right: 0)
        }
    }

    func convertGoalToData(goalDistance: Float, goalSin: Float, goalCos: Float) {
        var goalBytes: [UInt8] = []
        let distanceBytes = withUnsafeBytes(of: goalDistance) {
            Array($0)
        }
        let sinBytes = withUnsafeBytes(of: goalSin) {
            Array($0)
        }
        let cosBytes = withUnsafeBytes(of: goalCos) {
            Array($0)
        }
        goalBytes.append(contentsOf: distanceBytes)
        goalBytes.append(contentsOf: sinBytes)
        goalBytes.append(contentsOf: cosBytes)
        goalData = Data(bytes: &goalBytes, count: goalBytes.count)
    }


}
