/*
 * Developed for the OpenBot project (https://openbot.org) by:
 *
 * Ivo Zivkov
 * izivkov@gmail.com
 *
 * Date: Mon Nov 29 2021
 */

function Commands (sendToBot) {
  const commandHandler = new CommandHandler(sendToBot)

  this.getCommandHandler = () => {
    return commandHandler
  }
}

function DriveValue () {
  const MAX = 1.0
  const MIN = -1.0

  let value = 0.0

  this.reset = () => {
    value = 0
    return value
  }

  this.max = () => {
    value = MAX
    return value
  }

  this.min = () => {
    value = MIN
    return value
  }

  this.write = _value => {
    value = _value
    return value
  }

  this.read = () => {
    return Math.round(value * 1000) / 1000
  }
}

function CommandHandler (sendToBot) {
  const left = new DriveValue()
  const right = new DriveValue()
  const commandReducer = new DriveCommandReducer()

  this.sendCommand = (command) => {
    sendToBot(`{command: ${command} }`)
  }

  const sendDriveCommand = (left, right) => {
    commandReducer.send({ driveCmd: { l: left, r: right } }, sendToBot)
  }

  this.reset = () => {
    left.reset()
    right.reset()
    sendDriveCommand(0, 0)
  }

  this.forwardLeft = () => {
    sendDriveCommand(left.min() / 2, right.max())
  }

  this.forwardRight = () => {
    sendDriveCommand(left.max(), right.min() / 2)
  }

  this.backwardLeft = () => {
    sendDriveCommand(left.max() / 2, right.min())
  }

  this.backwardRight = () => {
    sendDriveCommand(left.min(), right.max() / 2)
  }

  this.rotateLeft = () => {
    sendDriveCommand(left.min(), right.max())
  }

  this.rotateRight = () => {
    sendDriveCommand(left.max(), right.min())
  }

  this.goForward = () => {
    sendDriveCommand(left.max(), right.max())
  }

  this.goBackward = () => {
    sendDriveCommand(left.min(), right.min())
  }
}

// Utility function to reduce number of commands being sent to the robot
// by not sending duplicate consecutive commands.
function DriveCommandReducer () {
  let lastCommand = null

  this.send = (commandAsJson, sendToBot) => {
    if (isEqual(commandAsJson, lastCommand)) {
      return
    }
    lastCommand = commandAsJson

    const strCommand = JSON.stringify(commandAsJson)
    sendToBot(strCommand)
  }

  const isEqual = (current, last) => {
    if (!last || !current) {
      return false
    }
    return last.driveCmd.l === current.driveCmd.l && last.driveCmd.r === current.driveCmd.r
  }
}

module.exports = { Commands, CommandHandler, DriveValue }
