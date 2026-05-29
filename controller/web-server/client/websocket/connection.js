/*
 * Developed for the OpenBot project (https://openbot.org) by:
 *
 * Ivo Zivkov
 * izivkov@gmail.com
 *
 * Date: Mon Nov 29 2021
 */

import {ErrorDisplay} from '../utils/error-display.js'

// One socket per tab — keyboard and video signaling share it.
let sharedSocket = null
let onSocketReadyCallback = null

export function Connection () {
    const errDisplay = new ErrorDisplay()

    this.send = () => {}

    // Vite dev (8081) proxies /ws to the Node server on 8080.
    const getWebSocketUrl = () => {
        const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
        if (window.location.port === '8081') {
            return `${wsProtocol}//${window.location.host}/ws`
        }
        return `${wsProtocol}//${window.location.hostname}:8080/ws`
    }

    const connectToServer = () => {
        if (sharedSocket && sharedSocket.readyState === WebSocket.OPEN) {
            return Promise.resolve(sharedSocket)
        }
        if (sharedSocket) {
            sharedSocket.close()
            sharedSocket = null
        }

        const ws = new WebSocket(getWebSocketUrl())
        return new Promise((resolve, reject) => {
            const timeout = setTimeout(() => {
                reject(new Error('WebSocket connection timeout'))
            }, 10000)

            ws.onopen = () => {
                clearTimeout(timeout)
                sharedSocket = ws
                errDisplay.reset()
                if (onSocketReadyCallback) {
                    onSocketReadyCallback()
                }
                resolve(ws)
            }

            ws.onerror = () => {
                clearTimeout(timeout)
                reject(new Error('WebSocket connection failed'))
            }
        })
    }

    this.start = async (onData, onReady) => {
        if (onReady) {
            onSocketReadyCallback = onReady
        }

        const ws = await connectToServer()

        this.send = (data) => {
            if (sharedSocket && sharedSocket.readyState === WebSocket.OPEN) {
                sharedSocket.send(data)
            }
        }

        ws.onmessage = (webSocketMessage) => {
            try {
                const msg = JSON.parse(webSocketMessage.data)
                // Server wants our Google email as room id (send after sign-in).
                if (msg.roomId === 'request-roomId') {
                    if (onSocketReadyCallback) {
                        onSocketReadyCallback()
                    }
                    return
                }
            } catch (error) {
                return
            }
            onData(webSocketMessage.data)
        }

        ws.onclose = () => {
            errDisplay.set('Disconnected from the server. To reconnect, reload this page.')
            if (sharedSocket === ws) {
                sharedSocket = null
            }
        }
    }

    this.stop = () => {
        if (sharedSocket != null) {
            sharedSocket.close()
            sharedSocket = null
        }
    }
}
