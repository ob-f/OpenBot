//
// Created by Nitish Yadav on 26/08/22.
//

import Foundation
import CoreMotion
import CoreLocation
import CoreLocationUI

/// This class provides an interface to the IMU and GPS data
class sensorDataRetrieve: CMDeviceMotion, CLLocationManagerDelegate {
    static let shared: sensorDataRetrieve = sensorDataRetrieve()
    var accelerationX: Double = 0
    var accelerationY: Double = 0
    var accelerationZ: Double = 0
    var angularRateX: Double = 0
    var angularRateY: Double = 0
    var angularRateZ: Double = 0
    var magneticFieldX: Double = 0
    var magneticFieldY: Double = 0
    var magneticFieldZ: Double = 0
    let geoCoder = CLGeocoder()
    var altitude = Double.zero
    var pressure = Double.zero
    let motionManager = CMMotionManager()
    let locationManager = CLLocationManager()
    let altitudeManager = CMAltimeter()
    var updateInterval: Double = 0.03
    var sensorData: String = ""
    var location: CLLocation!

    /// Initialization routine
    override init() {
        super.init()
        motionManager.startAccelerometerUpdates()
        motionManager.accelerometerUpdateInterval = updateInterval
        motionManager.startGyroUpdates()
        motionManager.gyroUpdateInterval = updateInterval
        sampleIMU()
        motionManager.startMagnetometerUpdates()
        motionManager.magnetometerUpdateInterval = updateInterval
        sampleGPS()
    }

    required init?(coder: NSCoder) {
        super.init()
        fatalError("init(coder:) has not been implemented")
    }

    /// Sample GPS sensor
    func sampleGPS() {
        locationManager.requestAlwaysAuthorization()
        DispatchQueue.global().async {
            if CLLocationManager.locationServicesEnabled() {
                self.locationManager.delegate = self
                self.locationManager.startMonitoringSignificantLocationChanges()
            }
        }
    }

    /// Sample the GPS
    ///
    /// - Parameters:
    ///     - manager:
    ///     - didUpdateLocations:
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let currentLocation = locations.first else {
            return
        }
        geoCoder.reverseGeocodeLocation(currentLocation) { (placeMarks, error) in
            guard let currentLocPlacemark = placeMarks?.first else {
                return
            }
            if let location = currentLocPlacemark.location {
                self.location = location
            }
        }
    }

    /// Sample the IMU sensor
    func sampleIMU() {
        sampleAccelerometer()
        sampleGyroscope()
        sampleMagnetometer()
    }

    /// Sample the acceleration sensor
    ///
    /// - Returns: a 3D xyz acceleration vector in m/s^2 expressed in the sensor frame
    func sampleAccelerometer() {
        if let data = motionManager.accelerometerData {
            accelerationX = data.acceleration.x * 9.81 // in m/s^2
            accelerationY = data.acceleration.y * 9.81 // in m/s^2
            accelerationZ = data.acceleration.z * 9.81 // in m/s^2
        }
    }

    /// Sample the gyroscope sensor
    ///
    /// - Returns: a 3D xyz angular rate vector in rad/s expressed in the sensor frame
    func sampleGyroscope() {
        if let data = motionManager.gyroData {
            angularRateX = data.rotationRate.x // in rad/s
            angularRateY = data.rotationRate.y // in rad/s
            angularRateZ = data.rotationRate.z // in rad/s
        }
    }

    /// Sample the magnetometer
    ///
    /// - Returns: a 3D xyz magnetic field vector in uT expressed in the sensor frame
    func sampleMagnetometer() {
        if let data = motionManager.magnetometerData {
            magneticFieldX = data.magneticField.x // in microteslas
            magneticFieldY = data.magneticField.y // in microteslas
            magneticFieldZ = data.magneticField.z // in microteslas
        }
    }
}
