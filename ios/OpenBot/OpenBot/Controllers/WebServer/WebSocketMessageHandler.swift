//
// Created by Nitish Yadav on 27/07/23.
//

import Foundation

class WebSocketMessageHandler {
    var audioPlayer = AudioPlayer.shared
    var gameController = GameController.shared
    init(){
        print("");
    }
    func indicatorLeft(){
        gameController.setIndicator(keyCommand: .LEFT)
    }
    func indicatorRight(){
        gameController.setIndicator(keyCommand: .RIGHT)
    }
    func cancelIndicator(){
        gameController.setIndicator(keyCommand: .STOP)
    }
    func driveCommand(control : Control){
        gameController.sendControlFromPhoneController(control: control);
    }
    func speedDown(){
        gameController.decreaseSpeedMode();
    }

    func speedUp(){
      gameController.increaseSpeedMode();
    }

    func driveMode(){
        NotificationCenter.default.post(name: .updateDriveMode, object: nil)
    }

    func toggleLogging(){
        NotificationCenter.default.post(name: .logData, object: nil)
    }

    func quit(){

    }

}