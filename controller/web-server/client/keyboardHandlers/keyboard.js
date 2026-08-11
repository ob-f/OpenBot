/*
 * Developed for the OpenBot project (https://openbot.org) by:
 *
 * Ivo Zivkov
 * izivkov@gmail.com
 *
 * Date: Mon Nov 29 2021
 */

export function Keyboard () {
  // 'key' is added as innerHTML
  const menuTable = [
    { keypressCode: 'w', key: 'W', description: 'Go forward' },
    { keypressCode: 's', key: 'S', description: 'Go backward' },
    { keypressCode: 'a', key: 'A', description: 'Turn left' },
    { keypressCode: 'd', key: 'D', description: 'Turn right' },
    { keypressCode: 'q', key: 'Q', description: 'Speed down' },
    { keypressCode: 'e', key: 'E', description: 'Speed up' },
    { keypressCode: 'm', key: 'M', description: 'Drive mode' },
    { keypressCode: 'n', key: 'N', description: 'Toggle noise' },
    { keypressCode: 'ArrowLeft', key: '<img src="/icons/arrow.svg" style="transform: rotate(90deg)" />', description: 'Left indicator' },
    { keypressCode: 'ArrowRight', key: '<img src="/icons/arrow.svg" style="transform: rotate(-90deg)" />', description: 'Right indicator' },
    { keypressCode: 'ArrowUp', key: '<img src="/icons/arrow.svg" style="transform: rotate(180deg)" />', description: 'Cancel indicators' },
    { keypressCode: 'ArrowDown', key: '<img src="/icons/arrow.svg" style="transform: rotate(0deg)" />', description: 'Network mode' },
    { keypressCode: ' ', key: '<div class="space-symbol"></div>', description: 'Toggle logging' },
    { keypressCode: 'Escape', key: 'Esc', description: 'Quit' }
  ]

  // build a set of the keypress codes for a quick lookup
  const keypressSet = new Set(menuTable.map(item => item.keypressCode))

  // currently pressed keys
  const pressedKeys = new Set()

  this.start = (onKeyPress, onQuit) => {
    const listItems = createMenuList()

    // append all 'li' elements to the root element
    const rootElement = document.getElementById('command-list')
    listItems.forEach(liItem => rootElement.appendChild(liItem))

    processKeys(onKeyPress, listItems, onQuit)
    processPointerActions(onKeyPress, listItems)
  }

  // create HTML "li" elements from 'menuTable'
  const createMenuList = () => {
    const listItems = menuTable.map(item => {
      const liItem = document.createElement('li')

      const keySymbol = document.createElement('div')
      keySymbol.innerHTML = item.key
      keySymbol.classList.add('key-display')

      const keyDescription = document.createElement('p')
      keyDescription.innerText = item.description
      keyDescription.classList.add('key-desc')

      liItem.appendChild(keySymbol)
      liItem.appendChild(keyDescription)
      liItem.setAttribute('key', item.key)
      liItem.setAttribute('keypressCode', item.keypressCode)

      return liItem
    })
    return listItems
  }

  const highlightPressedKeys = list => {
    list.forEach(liItem => {
      const keypressName = liItem.getAttribute('keypressCode')

      if (pressedKeys.has(keypressName)) {
        liItem.classList.add('key-pressed')
      } else {
        liItem.classList.remove('key-pressed')
      }
    })
  }

  const isValid = key => keypressSet.has(key)

  const processKeys = (onKeypress, keyList, onQuit) => {
    document.addEventListener('keydown', (event) => {
      if (!isValid(event.key)) {
        return
      }

      pressedKeys.add(event.key)
      highlightPressedKeys(keyList)

      const { key, type } = event
      onKeypress({ key: key, type: type })

      if (event.key === 'Escape') {
        onQuit()
      }
    }, false)

    document.addEventListener('keyup', (event) => {
      if (!isValid(event.key)) {
        return
      }

      pressedKeys.delete(event.key)
      highlightPressedKeys(keyList)

      const { key, type } = event
      onKeypress({ key: key, type: type })
    }, false)

    window.addEventListener('blur', () => {
      pressedKeys.clear()
      highlightPressedKeys(keyList)
    })
  }

  // Tap rows in the sidebar like pressing keys (mobile / mouse).
  const processPointerActions = (onKeyPress, keyList) => {
    const pressedByPointer = new Set()

    const sendKey = (key, type) => {
      onKeyPress({ key, type })
      if (type === 'keydown') {
        pressedKeys.add(key)
      } else {
        pressedKeys.delete(key)
      }
      highlightPressedKeys(keyList)
    }

    keyList.forEach((liItem) => {
      const key = liItem.getAttribute('keypressCode')

      liItem.addEventListener('pointerdown', (event) => {
        event.preventDefault()
        if (pressedByPointer.has(key)) {
          return
        }
        pressedByPointer.add(key)
        liItem.setPointerCapture(event.pointerId)
        sendKey(key, 'keydown')
      })

      const release = () => {
        if (!pressedByPointer.has(key)) {
          return
        }
        pressedByPointer.delete(key)
        sendKey(key, 'keyup')
      }

      liItem.addEventListener('pointerup', release)
      liItem.addEventListener('pointercancel', release)
      liItem.addEventListener('pointerleave', release)
    })
  }
}
