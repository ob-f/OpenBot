/*
 * Developed for the OpenBot project (https://openbot.org) by:
 *
 * Ivo Zivkov
 * izivkov@gmail.com
 *
 * Date: Mon Nov 29 2021
 */

import {WebRTC} from '../webRTC/webrtc.js'
import {ErrorDisplay} from '../utils/error-display.js'
import {Buttons} from './buttons.js'

export function BotMessageHandler (connection) {
    const webRtc = new WebRTC(connection)
    const buttons = new Buttons(connection)
    const errDisplay = new ErrorDisplay()

    this.handle = (msg) => {
        if (msg === undefined || msg === null) {
            return
        }

        if (msg.SWITCH_CAMERA !== undefined) {
            console.log('[Web] SWITCH_CAMERA result from robot:', msg.SWITCH_CAMERA)
        }

        const msgType = Object.keys(msg)[0]
        switch (msgType) {
            case 'VIDEO_PROTOCOL':
                if (msg.VIDEO_PROTOCOL !== 'WEBRTC') {
                    errDisplay.set('Only WebRTC video supported. Please set your andoid app for WebRTC')
                } else {
                    errDisplay.reset()
                }
                break

            case 'VIDEO_COMMAND':
                switch (msg.VIDEO_COMMAND) {
                    case 'START':
                        webRtc.start()
                        buttons.setMirrored(false)
                        break

                    case 'STOP':
                        webRtc.stop()
                        break
                }
                break

            // Robot sends uppercase key; browser answer uses webrtc_event below.
            case 'WEB_RTC_EVENT': {
                let event = msg.WEB_RTC_EVENT
                if (typeof event === 'string') {
                    try {
                        event = JSON.parse(event)
                    } catch (error) {
                        console.error('Invalid WEB_RTC_EVENT JSON', error)
                        return
                    }
                }
                webRtc.handle(event)
                break
            }
            case 'webrtc_event':
                webRtc.handle(msg.webrtc_event)
                break

            default:
                break
        }
    }
}
