//
//  SharedPreferences.swift
//  OpenBot
//
//  Created by Hardik Garg on 22/09/23.
//

import Foundation

public class SharedPreferencesManager {
    
    private let userDefaults = UserDefaults.standard

    public var control_mode : String = "control_mode";
    
    public var drive_mode : String = "drive_mode";
    
    public var speed_mode : String = "speed_mode";
    
    public var object_tracking_model : String = "object_tracking_model";
    
    public var object_type : String = "object_type";
    
    public var confidence : String = "confidence";
    
    public var device : String = "device";
    
    public var threads : String = "threads";
    
    public var autopilot_model : String = "autopilot_model";
    
    public var camera_switch : String = "camera_switch";
    
    public var dynamic_speed : String = "dynamic_speed";

    public var blocklyCode : String = "blocklyCode";

    public var web_signaling_server_url : String = "web_signaling_server_url";

    public func setControlMode(value:String){
        userDefaults.set(value, forKey: control_mode)
    }
    
    public func getControlMode() -> String? {
        return userDefaults.string(forKey: control_mode);
    }
    
    public func setDriveMode(value:String){
        userDefaults.set(value, forKey: drive_mode);
    }
    
    public func getDriveMode() -> String? {
        return userDefaults.string(forKey: drive_mode);
    }
    
    public func setSpeedMode(value:Float){
        userDefaults.set(value, forKey: speed_mode);
    }
    
    public func getSpeedMode() -> Float? {
        return userDefaults.float(forKey: speed_mode);
    }
    
    public func updateSensorData(value:Bool,sensor:String){
        userDefaults.set(value, forKey: sensor);
    }
    
    public func getSensorData(sensor:String) -> Any? {
        return userDefaults.value(forKey: sensor)
    }
    
    public func setObjectTrackModel(value:String){
        userDefaults.set(value, forKey: object_tracking_model);
    }
    
    public func getObjectTrackModel() -> String? {
        return userDefaults.string(forKey: object_tracking_model)
    }
    
    public func setObjectTrackingObject(value:String){
        userDefaults.set(value, forKey: object_type)
    }
    
    public func getObjectTrackingObject() -> String? {
        return userDefaults.string(forKey: object_type);
    }
    
    public func setObjectTrackConfidence(value:Int){
        userDefaults.set(value, forKey: confidence);
    }
    
    public func getObjectTrackConfidence() -> Any? {
        return userDefaults.value(forKey: confidence);
    }
    
    public func setDevice(value:String){
        userDefaults.set(value, forKey: device);
    }
    
    public func getDevice() -> String? {
        return userDefaults.string(forKey: device);
    }
    
    public func setThreads(value:String){
        userDefaults.set(value, forKey: threads);
    }
    
    public func getThreads() -> String? {
        return userDefaults.string(forKey: threads);
    }
    
    public func setAutopilotModel(value:String){
        userDefaults.set(value, forKey: autopilot_model);
    }
    
    public func getAutopilotModel() -> String? {
        return userDefaults.string(forKey: autopilot_model);
    }
    
    public func setCameraSwitch(value:String){
        userDefaults.set(value, forKey: camera_switch);
    }
    
    public func getCameraSwitch() -> String?{
        userDefaults.string(forKey: camera_switch);
    }
    
    public func setDynamicSpeed(value:Bool){
        userDefaults.set(value, forKey: dynamic_speed);
    }
    
    public func getDynamicSpeed() -> Any?{
        userDefaults.value(forKey: dynamic_speed);
    }

    public func setBlocklyCode(value:String){
        userDefaults.set(value, forKey: blocklyCode);
    }
    
    public func getBlocklyCode() -> String?{
        userDefaults.string(forKey: blocklyCode);
    }

    public func setWebSignalingServerUrl(value: String) {
        userDefaults.set(value, forKey: web_signaling_server_url)
    }

    public func getWebSignalingServerUrl() -> String {
        return userDefaults.string(forKey: web_signaling_server_url) ?? "ws://192.168.1.40:8080/ws"
    }
}
