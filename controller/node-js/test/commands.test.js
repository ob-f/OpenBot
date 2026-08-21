/*
 * Unit tests for the OpenBot node-js controller command logic.
 *
 * These exercise the pure server-side command mapping that ultimately drives
 * the robot's left/right motors, so a regression here could send wrong motor
 * commands. Run with: `npm test` (uses Node's built-in test runner).
 */

const test = require('node:test')
const assert = require('node:assert')

const { Commands, DriveValue } = require('../server/commands.js')

// Creates a CommandHandler and captures every command string it sends.
function makeHandler () {
  const sent = []
  const commands = new Commands(msg => sent.push(msg))
  return { handler: commands.getCommandHandler(), sent }
}

test('goForward sends full forward drive command', () => {
  const { handler, sent } = makeHandler()
  handler.goForward()
  assert.deepStrictEqual(JSON.parse(sent[0]), { driveCmd: { l: 1, r: 1 } })
})

test('goBackward sends full reverse drive command', () => {
  const { handler, sent } = makeHandler()
  handler.goBackward()
  assert.deepStrictEqual(JSON.parse(sent[0]), { driveCmd: { l: -1, r: -1 } })
})

test('forwardLeft mixes left/right correctly', () => {
  const { handler, sent } = makeHandler()
  handler.forwardLeft()
  assert.deepStrictEqual(JSON.parse(sent[0]), { driveCmd: { l: -0.5, r: 1 } })
})

test('forwardRight mixes right/left correctly', () => {
  const { handler, sent } = makeHandler()
  handler.forwardRight()
  assert.deepStrictEqual(JSON.parse(sent[0]), { driveCmd: { l: 1, r: -0.5 } })
})

test('rotateLeft / rotateRight send opposing wheel commands', () => {
  const { handler, sent } = makeHandler()
  handler.rotateLeft()
  assert.deepStrictEqual(JSON.parse(sent[0]), { driveCmd: { l: -1, r: 1 } })

  const { handler: handler2, sent: sent2 } = makeHandler()
  handler2.rotateRight()
  assert.deepStrictEqual(JSON.parse(sent2[0]), { driveCmd: { l: 1, r: -1 } })
})

test('reset sends a zero drive command', () => {
  const { handler, sent } = makeHandler()
  handler.reset()
  assert.deepStrictEqual(JSON.parse(sent[0]), { driveCmd: { l: 0, r: 0 } })
})

test('consecutive identical drive commands are de-duplicated', () => {
  const { handler, sent } = makeHandler()
  handler.goForward()
  handler.goForward()
  handler.goForward()
  assert.strictEqual(sent.length, 1)
})

test('different drive commands are each sent', () => {
  const { handler, sent } = makeHandler()
  handler.goForward()
  handler.goBackward()
  assert.strictEqual(sent.length, 2)
})

test('sendCommand forwards the raw command string', () => {
  const { handler, sent } = makeHandler()
  handler.sendCommand('NOISE')
  assert.strictEqual(sent[0], '{command: NOISE }')
})

test('DriveValue clamps to [-1, 1] and read() rounds to 3 decimals', () => {
  const dv = new DriveValue()
  assert.strictEqual(dv.reset(), 0)
  assert.strictEqual(dv.min(), -1)
  assert.strictEqual(dv.max(), 1)

  // Before the fix this returned Math.round(0.12345) === 0 (2nd arg ignored).
  dv.write(0.12345)
  assert.strictEqual(dv.read(), 0.123)

  dv.write(0.9999)
  assert.strictEqual(dv.read(), 1)

  dv.write(-0.4567)
  assert.strictEqual(dv.read(), -0.457)
})
