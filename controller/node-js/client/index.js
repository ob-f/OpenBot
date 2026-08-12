/*
 * Developed for the OpenBot project (https://openbot.org) by:
 *
 * Ivo Zivkov
 * izivkov@gmail.com
 *
 * Date: Mon Nov 29 2021
 */

import { Connection } from './connection.js'
import { Keyboard } from './keyboard.js'
import { BotMessageHandler } from './bot-message-handler'

(async () => {
  const connection = new Connection()
  const keyboard = new Keyboard()
  const botMessageHandler = new BotMessageHandler(connection)

  const onData = data => {
    botMessageHandler.handle(JSON.parse(data).status, connection)
  }

  const onQuit = () => {
    connection.stop()
  }

  await connection.start(onData)

  const onKeyPress = (key) => {
    // Send keypress to server
    connection.send(JSON.stringify({ KEYPRESS: key }))
  }

  keyboard.start(onKeyPress, onQuit)
})()
