/*
 * Developed for the OpenBot project (https://openbot.org) by:
 *
 * Ivo Zivkov
 * izivkov@gmail.com
 *
 * Date: Mon Nov 29 2021
 */

import {Connection} from './websocket/connection.js'
import {Keyboard} from './keyboardHandlers/keyboard.js'
import {BotMessageHandler} from './keyboardHandlers/bot-message-handler'
import {Commands} from './keyboardHandlers/commands'
import {RemoteKeyboard} from './keyboardHandlers/remote_keyboard'
import {signInWithCustomToken} from 'firebase/auth'
import {auth, googleSigIn, googleSignOut} from './firebase/authentication'
import {localStorageKeys} from './utils/constants'

const connection = new Connection();
(async () => {
    const keyboard = new Keyboard()
    const botMessageHandler = new BotMessageHandler(connection)

    // Robot wraps toggles/video in {status: {...}}; drive commands are flat JSON.
    const onData = data => {
        const msg = JSON.parse(data)
        const status = msg.status ?? msg
        botMessageHandler.handle(status)
    }

    const onQuit = () => {
        connection.stop()
    }

    // roomId must match robot app Google account or the server will not relay.
    const sendToBot = (key) => {
        if (!signedInUser?.email) {
            console.error('Cannot send command: sign in with Google first (same account as robot app).')
            return
        }
        const msg = JSON.parse(key)
        const payload =
            msg.driveCmd !== undefined
                ? {driveCmd: msg.driveCmd, roomId: signedInUser.email}
                : {command: msg.command, roomId: signedInUser.email}
        connection.send(JSON.stringify(payload))
    }

    const command = new Commands(sendToBot)
    const remoteKeyboard = new RemoteKeyboard(command.getCommandHandler())

    const onKeyPress = (key) => {
        remoteKeyboard.processKey(key)
    }

    // Show keys even if signaling is down; drive needs sign-in + server.
    keyboard.start(onKeyPress, onQuit)

    try {
        await connection.start(onData, () => {
            if (signedInUser?.email) {
                sendId()
            }
        })
    } catch (error) {
        console.error('WebSocket connection failed (start signaling server on port 8080):', error)
    }
})()

let storedUser = null
try {
    const raw = localStorage.getItem(localStorageKeys.user)
    if (raw && raw !== 'null') {
        storedUser = JSON.parse(raw)
    }
} catch (error) {
    console.error('Failed to parse stored user', error)
}
export let signedInUser = storedUser

const signInButton = document.getElementsByClassName('google-sign-in-button')[0]
signInButton.addEventListener('click', handleSignInButtonClick)
const cancelButton = document.getElementById('logout-cancel-button')
const okButton = document.getElementById('logout-ok-button')
cancelButton.addEventListener('click', handleCancelButtonClick)
okButton.addEventListener('click', handleOkButtonClick)
const subscribeButton = document.getElementById('subscribe-button')
subscribeButton.addEventListener('click', handleSubscription)

function handleSignInButtonClick() {
    if (localStorage.getItem(localStorageKeys.isSignIn) === 'false') {
        googleSigIn()
            .then((user) => {
                signedInUser = user
                const signInBtn = document.getElementsByClassName('google-sign-in-button')[0]
                signInBtn.innerText = user.displayName
                localStorage.setItem(localStorageKeys.user, JSON.stringify(user))
                localStorage.setItem(localStorageKeys.isSignIn, true.toString())
                sendId()
            })
            .catch((error) => {
                console.error('Error signing in:', error)
            })
    } else {
        showLogoutWrapper()
        hideExpirationWrapper()
    }
}

// Join signaling room as browser (robot uses clientType: robot).
function sendId() {
    if (!signedInUser?.email) {
        return
    }
    connection.send(JSON.stringify({
        roomId: signedInUser.email,
        clientType: 'browser'
    }))
}

function signOut() {
    signedInUser = null
    localStorage.setItem(localStorageKeys.user, null)
    localStorage.setItem(localStorageKeys.isSignIn, false.toString())
    const signInBtn = document.getElementsByClassName('google-sign-in-button')[0]
    signInBtn.innerText = 'Sign in with Google'
    googleSignOut()
}

function handleCancelButtonClick() {
    hideLogoutWrapper()
}

function hideLogoutWrapper() {
    const logout = document.getElementsByClassName('logout-wrapper')[0]
    logout.style.display = 'none'
}

function showLogoutWrapper() {
    const logout = document.getElementsByClassName('logout-wrapper')[0]
    logout.style.display = 'block'
}

function hideExpirationWrapper() {
    const expire = document.getElementsByClassName('plan-expiration-model')[0]
    expire.style.display = 'none'
}

function handleOkButtonClick() {
    hideLogoutWrapper()
    signOut()
}

function handleSubscription() {
    console.log('Navigate to subscription page')
}

export function getCookie(cname) {
    const name = cname + '='
    const decodedCookie = decodeURIComponent(document.cookie)
    const ca = decodedCookie.split(';')
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i]
        while (c.charAt(0) === ' ') {
            c = c.substring(1)
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length)
        }
    }
    return ''
}

export const deleteCookie = (name) => {
    document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:01 GMT;'
}


handleServerDetailsOnSSO()
handleAuthChangedOnRefresh()

function handleSingleSignOn() {
    const cookie = getCookie(localStorageKeys.user)
    if (cookie) {
        const result = cookie
        localStorage.setItem(localStorageKeys.isSignIn, 'true')
        signInWithCustomToken(auth, result).then((res) => {
            signedInUser = res.user
            const signInBtn = document.getElementsByClassName('google-sign-in-button')[0]
            signInBtn.innerText = res.user.displayName
            localStorage.setItem(localStorageKeys.user, JSON.stringify(res.user))
            localStorage.setItem(localStorageKeys.isSignIn, true.toString())
            deleteCookie(localStorageKeys.user)
            sendId()
        })
            .catch((error) => {
                console.log('error::', error)
            })
    }
}


function handleServerDetailsOnSSO() {
    const cookie = getCookie(localStorageKeys.user)
    if (cookie) {
        handleSingleSignOn()
    }
}

function handleAuthChangedOnRefresh() {
    if (localStorage.getItem(localStorageKeys.isSignIn) === 'true') {
        setTimeout(() => {
            auth.onAuthStateChanged((res) => {
                if (res != null) {
                    signedInUser = res
                    const signInBtn = document.getElementsByClassName('google-sign-in-button')[0]
                    signInBtn.innerText = res.displayName
                    localStorage.setItem(localStorageKeys.user, JSON.stringify(res))
                    localStorage.setItem(localStorageKeys.isSignIn, 'true')
                    sendId()
                }
            })
        }, 1000)
    }
}
