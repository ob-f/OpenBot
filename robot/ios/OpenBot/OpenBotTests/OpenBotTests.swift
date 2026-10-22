//
//  OpenBotTests.swift
//  OpenBotTests
//
//  Created by Sparsh Jain on 22/07/22.
//
//

import XCTest
import simd
@testable import OpenBot

class OpenBotTests: XCTestCase {

    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testExample() throws {
        // This is an example of a functional test case.
        // Use XCTAssert and related functions to verify your tests produce the correct results.
        // Any test you write for XCTest can be annotated as throws and async.
        // Mark your test throws to produce an unexpected failure when your test encounters an uncaught error.
        // Mark your test async to allow awaiting for asynchronous code to complete. Check the results with assertions afterwards.
    }

    func testPerformanceExample() throws {
        // This is an example of a performance test case.
        self.measure {
            // Put the code you want to measure the time of here.
        }
    }

}

class PointGoalMathTests: XCTestCase {

    // Phone mounted on the robot ~15 cm above the floor, world y is up.
    private let camera = simd_float3(0, 0, 0)

    func testFloorTwoMetersAheadIsAccepted() {
        XCTAssertEqual(PointGoalMath.validate(camera: camera, goal: simd_float3(0, -0.15, -2)), .ok)
    }

    func testTapNotReachingTheFloorIsNotFloor() {
        XCTAssertEqual(PointGoalMath.validate(camera: camera, goal: nil), .notFloor)
    }

    func testWindowOrBuildingAboveCameraIsNotFloor() {
        XCTAssertEqual(PointGoalMath.validate(camera: camera, goal: simd_float3(0, 3, -40)), .notFloor)
    }

    func testGoalUnderTheRobotIsTooClose() {
        XCTAssertEqual(PointGoalMath.validate(camera: camera, goal: simd_float3(0, -0.15, -0.2)), .tooClose)
    }

    func testSpotThirtyCentimetersAwayMeasuresThirtyCentimeters() {
        // Camera 12 cm up; a floor spot 0.3 m ahead is atan(0.12 / 0.3) = 21.8 deg below horizon.
        let angle = atan2(Float(0.12), Float(0.3))
        let origin = simd_float3(0, 0.12, 0)
        let hit = PointGoalMath.intersectFloor(origin: origin, direction: simd_float3(0, -sin(angle), -cos(angle)), floorY: 0)
        XCTAssertNotNil(hit)
        XCTAssertEqual(PointGoalMath.horizontalDistance(origin, hit!), 0.3, accuracy: 1e-4)
        XCTAssertEqual(PointGoalMath.validate(camera: origin, goal: hit), .ok)
    }

    func testSpotTwoMetersToTheSideMeasuresTwoMeters() {
        let angle = atan2(Float(0.12), Float(2.0))
        let hit = PointGoalMath.intersectFloor(origin: simd_float3(0, 0.12, 0), direction: simd_float3(cos(angle), -sin(angle), 0), floorY: 0)
        XCTAssertEqual(hit!.x, 2, accuracy: 1e-3)
        XCTAssertEqual(hit!.z, 0, accuracy: 1e-5)
    }

    func testRayLevelOrUpNeverHitsTheFloor() {
        XCTAssertNil(PointGoalMath.intersectFloor(origin: simd_float3(0, 0.15, 0), direction: simd_float3(0, 0, -1), floorY: 0))
        XCTAssertNil(PointGoalMath.intersectFloor(origin: simd_float3(0, 0.15, 0), direction: simd_normalize(simd_float3(0, 0.5, -1)), floorY: 0))
    }

    func testFarSpotBeyondTrustedRangeIsRejected() {
        let angle = atan2(Float(0.12), Float(8.0))
        XCTAssertNil(PointGoalMath.intersectFloor(origin: simd_float3(0, 0.12, 0), direction: simd_float3(0, -sin(angle), -cos(angle)), floorY: 0))
    }

    func testDistanceIgnoresHeight() {
        XCTAssertEqual(PointGoalMath.horizontalDistance(simd_float3(0, 0, 0), simd_float3(3, -5, 4)), 5, accuracy: 1e-5)
    }

    func testFloorIsLargestPlaneBelowCamera() {
        let planes: [(y: Float, area: Float)] = [
            (y: -0.15, area: 6), // floor
            (y: 0.45, area: 1), // table above the camera
            (y: -0.5, area: 0.5), // small reflection under a shiny floor
        ]
        XCTAssertEqual(PointGoalMath.pickFloorY(planes: planes, cameraY: 0)!, -0.15, accuracy: 1e-5)
    }

    func testNoPlanesNoFloor() {
        XCTAssertNil(PointGoalMath.pickFloorY(planes: [], cameraY: 0))
    }
}

class ArrivalCheckTests: XCTestCase {

    func testFarAwayNotArrived() {
        var check = ArrivalCheck()
        XCTAssertFalse(check.hasArrived(distance: 2, deltaYaw: 0))
    }

    func testWithinTenCentimetersArrived() {
        var check = ArrivalCheck()
        XCTAssertTrue(check.hasArrived(distance: 0.09, deltaYaw: 0))
    }

    func testCloseAndStillAheadKeepsDriving() {
        var check = ArrivalCheck()
        XCTAssertFalse(check.hasArrived(distance: 0.25, deltaYaw: 0.2))
        XCTAssertFalse(check.hasArrived(distance: 0.18, deltaYaw: 0.3))
    }

    func testPassedTheGoalSidewaysArrived() {
        var check = ArrivalCheck()
        XCTAssertTrue(check.hasArrived(distance: 0.20, deltaYaw: 100 * Float.pi / 180))
    }

    func testStartingWithGoalBehindFarAwayKeepsDriving() {
        var check = ArrivalCheck()
        XCTAssertFalse(check.hasArrived(distance: 1.5, deltaYaw: Float.pi))
    }

    func testGettingFurtherAwayAfterClosestPointArrived() {
        var check = ArrivalCheck()
        XCTAssertFalse(check.hasArrived(distance: 0.22, deltaYaw: 0.4))
        XCTAssertFalse(check.hasArrived(distance: 0.15, deltaYaw: 0.8))
        XCTAssertTrue(check.hasArrived(distance: 0.19, deltaYaw: 0.9))
    }

    func testSmallNoiseNearClosestPointIsNotMovingAway() {
        var check = ArrivalCheck()
        XCTAssertFalse(check.hasArrived(distance: 0.20, deltaYaw: 0.2))
        XCTAssertFalse(check.hasArrived(distance: 0.21, deltaYaw: 0.2))
    }
}
