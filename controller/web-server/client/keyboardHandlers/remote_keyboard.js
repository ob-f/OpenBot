// remote_keyboard.js
/*
 * Developed for the OpenBot project (https://openbot.org) by:
 *
 * Ivo Zivkov
 * izivkov@gmail.com
 *
 * Date: Mon Nov 29 2021
 */

export function RemoteKeyboard (commandHandler) {
  const pressedKeys = new Set()

  // Stop stuck keys when user leaves the browser tab.
  if (typeof window !== 'undefined') {
    window.addEventListener('blur', () => pressedKeys.clear())
  }

  this.processKey = keyPress => {
    switch (keyPress?.type) {
      case 'keyup':
        pressedKeys.delete(keyPress.key)
        if (['w', 's'].includes(keyPress.key)) {
          commandHandler.reset()
          break
        }

        if (['a', 'd'].includes(keyPress.key)) {
          if (pressedKeys.has('w')) {
            commandHandler.goForward()
            break
          }
          if (pressedKeys.has('s')) {
            commandHandler.goBackward()
            break
          }
          commandHandler.reset()
        }
        break

      case 'keydown': {
        // One command per key hold for toggles (N = noise, etc.), not every repeat tick.
        const isRepeat = pressedKeys.has(keyPress.key)
        pressedKeys.add(keyPress.key)
        switch (keyPress.key) {
          case 'w':
            if (pressedKeys.has('a')) {
              commandHandler.forwardLeft()
              break
            }
            if (pressedKeys.has('d')) {
              commandHandler.forwardRight()
              break
            }
            commandHandler.goForward()
            break
          case 's':
            if (pressedKeys.has('a')) {
              commandHandler.backwardLeft()
              break
            }
            if (pressedKeys.has('d')) {
              commandHandler.backwardRight()
              break
            }
            commandHandler.goBackward()
            break
          case 'a':
            if (pressedKeys.has('w')) {
              commandHandler.forwardLeft()
            } else if (pressedKeys.has('s')) {
              commandHandler.backwardLeft()
            } else {
              commandHandler.rotateLeft()
            }
            break
          case 'd':
            if (pressedKeys.has('w')) {
              commandHandler.forwardRight()
            } else if (pressedKeys.has('s')) {
              commandHandler.backwardRight()
            } else {
              commandHandler.rotateRight()
            }
            break
          case 'n':
            if (!isRepeat) commandHandler.sendCommand('NOISE')
            break
          case ' ':
            if (!isRepeat) commandHandler.sendCommand('LOGS')
            break
          case 'ArrowRight':
            if (!isRepeat) commandHandler.sendCommand('INDICATOR_RIGHT')
            break
          case 'ArrowLeft':
            if (!isRepeat) commandHandler.sendCommand('INDICATOR_LEFT')
            break
          case 'ArrowUp':
            if (!isRepeat) commandHandler.sendCommand('INDICATOR_STOP')
            break
          case 'ArrowDown':
            if (!isRepeat) commandHandler.sendCommand('NETWORK')
            break
          case 'm':
            if (!isRepeat) commandHandler.sendCommand('DRIVE_MODE')
            break
          case 'q':
            if (!isRepeat) commandHandler.sendCommand('SPEED_DOWN')
            break
          case 'e':
            if (!isRepeat) commandHandler.sendCommand('SPEED_UP')
            break
          case 'Escape':
            if (!isRepeat) commandHandler.reset()
            break
        }
        break
      }
    }
  }
}
