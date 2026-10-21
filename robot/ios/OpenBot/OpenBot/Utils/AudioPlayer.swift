//
// Created by Nitish Yadav on 16/05/23.
//

import AVFoundation
import UIKit
import AVKit

/**
    class for playing audio
 */
class AudioPlayer : AVPlayer, AVSpeechSynthesizerDelegate  {
    var player: AVPlayer?
    var playerItem: AVPlayerItem?
    var speechSynthesizer = AVSpeechSynthesizer()
    static let shared : AudioPlayer = AudioPlayer();

    /**
     Initializer of AudioPlayer class with observers
     */
    override init(){
        super.init()
        NotificationCenter.default.addObserver(self, selector: #selector(playerItemDidFinishPlaying(_:)), name: .AVPlayerItemDidPlayToEndTime, object: player?.currentItem)
        player?.currentItem?.addObserver(self, forKeyPath: "status", options: [.new, .initial], context: nil)
        speechSynthesizer.delegate = self
    }

    /**
     Destroying objects of Audio Player
     */
    deinit {
        NotificationCenter.default.removeObserver(self)
        player?.currentItem?.removeObserver(self, forKeyPath: "status")
    }

    /**
     Called after audio file finished playing
     - Parameter notification:
     */
    @objc func playerItemDidFinishPlaying(_ notification: Notification) {
    }

    /**

     - Parameters:
       - keyPath:
       - object:
       - change:
       - context:
     */
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey: Any]?, context: UnsafeMutableRawPointer?) {
        if keyPath == "status" {
            if let statusInt = change?[.newKey] as? Int, let status = AVPlayer.Status(rawValue: statusInt) {
            }
        }
    }

    /**
     private function to play the audio
     - Parameter name: name of file from assets to be played
     */
    private func play(name: String) {
        guard let assetData = NSDataAsset(name: name)?.data else {
            print("Asset data not found.")
            return
        }
        let tempDirectoryURL = FileManager.default.temporaryDirectory
        let tempFileURL = tempDirectoryURL.appendingPathComponent("\(name)_audio.mp3")
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)
            try assetData.write(to: tempFileURL)
            let playerItem = AVPlayerItem(url: tempFileURL)
            player = AVPlayer(playerItem: playerItem)
            player?.currentItem?.addObserver(self as! NSObject, forKeyPath: "status", options: [.new, .initial], context: nil)
            player?.play()
        } catch {
            print("Error writing asset data to temporary file: \(error.localizedDescription)")
        }
    }

    /**
     function to play drive mode ie. Dual, Joystick and game
     - Parameter driveMode:
     */
    func playDriveMode(driveMode: DriveMode) {
        switch driveMode {
        case .DUAL:
            play(name: "dual_drive_control");
            break;
        case .JOYSTICK:
            play(name: "joystick_control")
            break;

        case .GAME:
            play(name: "video_game_control");
            break;
        }
    }

    /**
     Function to play speed mode ie. Slow, Normal and Fast
     - Parameter speedMode:
     */
    func playSpeedMode(speedMode: SpeedMode) {
        switch speedMode {
        case .SLOW:
            play(name: "slow_speed");
            break;
        case .NORMAL:
            play(name: "normal_speed");
            break;
        case .FAST:
            play(name: "fast_speed");
            break;
        }
    }

    /**
     Function to play noice
     - Parameter isEnabled:
     */
    func playNoise(isEnabled: Bool) {
        if isEnabled {
            play(name: "noise_enabled");
        } else {
            play(name: "noise_disabled");
        }
    }

    /**
     Function to play whether logging enable or not
     - Parameter isEnabled:
     */
    func playLogging(isEnabled : Bool){
        if isEnabled{
            play(name: "logging_started");
        }
        else{
            play(name: "logging_stopped");
        }
    }

    private func playString(input:String){
        if !speechSynthesizer.isSpeaking {
            let utterance = AVSpeechUtterance(string: input)
            utterance.voice = AVSpeechSynthesisVoice(language: "en-GB")
            utterance.rate = 0.3
            speechSynthesizer.speak(utterance)
        }
    }
    
    func playInputString(input:String){
        if(input != ""){
            playString(input: input)
        }
    }
}

