import { flashNano } from "./avr.js";

const ESPTOOL = "https://unpkg.com/esptool-js@0.5.4/bundle.js";

const BODY_NAMES = {
  DIY: "DIY",
  PCB_V1: "DIY with PCB V1",
  PCB_V2: "DIY with PCB V2",
  RTR_TT: "Ready-to-Run TT",
  RTR_TT2: "Ready-to-Run TT (ESP32)",
  RTR_520: "Ready-to-Run 520",
  RC_CAR: "RC truck",
  LITE: "OpenBot Lite",
  MTV: "Multi Terrain Vehicle",
};
const BOARD_NAMES = { nano: "Arduino Nano", esp32: "ESP32 Dev Module" };
const MODES = {
  phone: { name: "Phone app", text: "The phone drives the robot over USB or Bluetooth." },
  gamepad: { name: "Gamepad", text: "Drive with a Bluetooth gamepad, no phone needed." },
  nophone: { name: "No controller", text: "The robot drives by itself and steers around obstacles." },
};
const MODE_SHORT = { phone: "driven by the phone app", gamepad: "driven by a gamepad", nophone: "drives by itself" };

// Parts in the order of the robot's startup line
const PARTS = [
  { key: "HAS_VOLTAGE_DIVIDER", group: "Sensors", name: "Battery voltage", text: "Voltage divider to measure the battery", log: "voltage" },
  { key: "HAS_INDICATORS", group: "Lights", name: "Indicators", text: "Turn signals", log: "indicators" },
  { key: "HAS_SONAR", group: "Sensors", name: "Ultrasonic sensor", text: "Measures the distance to obstacles in front", log: "sonar" },
  { key: "SONAR_MEDIAN", group: "Sensors", name: "Smooth sonar readings", text: "Median filter over the last readings", needs: "HAS_SONAR" },
  { key: "HAS_BUMPER", group: "Sensors", name: "Bumper", text: "Detects collisions", log: "bumper" },
  { key: "HAS_SPEED_SENSORS_FRONT", group: "Wheel speed sensors", name: "Front wheels", log: "speed sensors front" },
  { key: "HAS_SPEED_SENSORS_BACK", group: "Wheel speed sensors", name: "Back wheels", log: "speed sensors back" },
  { key: "HAS_SPEED_SENSORS_MIDDLE", group: "Wheel speed sensors", name: "Middle wheels", log: "speed sensors middle" },
  { key: "HAS_LEDS_FRONT", group: "Lights", name: "Front lights", log: "front lights" },
  { key: "HAS_LEDS_BACK", group: "Lights", name: "Rear lights", log: "rear lights" },
  { key: "HAS_LEDS_STATUS", group: "Lights", name: "Status LEDs", log: "status LEDs" },
  { key: "HAS_OLED", group: "Other", name: "OLED display", text: "Shows the robot's status; needs the Adafruit SSD1306 and GFX libraries", log: "OLED" },
  { key: "HAS_BLUETOOTH", group: "Other", name: "Bluetooth link to the phone", text: "Connect the app without a cable", log: "Bluetooth" },
];
const GROUPS = ["Sensors", "Wheel speed sensors", "Lights", "Other"];
// Parts that may share pins, like the indicators blinking the rear lights
const PIN_SHARING = { HAS_INDICATORS: "rear", HAS_LEDS_BACK: "rear" };

const BATTERIES = [
  { id: "2s", name: "2S Li-ion", low: 6.8, max: 8.4 },
  { id: "3s", name: "3S Li-ion", low: 10.2, max: 12.6 },
  { id: "6aa", name: "6 × AA", low: 6.0, max: 9.0 },
  { id: "8aa", name: "8 × AA", low: 8.0, max: 12.0 },
];

const $ = (id) => document.getElementById(id);
const el = (tag, props = {}, ...children) => {
  const node = Object.assign(document.createElement(tag), props);
  node.append(...children.filter((c) => c !== null && c !== undefined));
  return node;
};

let data;
const state = { body: "DIY", board: "esp32", mode: "phone", parts: {}, battery: "body", low: null, max: null };
let busy = false;

// ---------- State ----------

const body = () => data.bodies.find((b) => b.name === state.body);
const boardInfo = () => body().boards[state.board];
// Phone and no phone mode share a build and switch in the live settings; the gamepad has its own
const firmwareKey = () => `${state.body.toLowerCase()}-${state.board}-${state.mode === "gamepad" ? "gamepad" : "phone"}`;

function resetParts() {
  state.parts = {};
  for (const [key, part] of Object.entries(boardInfo().parts)) state.parts[key] = !!part.default;
  if (!boardInfo().modes.includes(state.mode)) state.mode = "phone";
}

function partOn(key) {
  if (key === "HAS_BLUETOOTH" && state.mode === "gamepad") return false;
  return !!state.parts[key];
}

function voltages() {
  const v = boardInfo().voltages;
  if (state.battery === "body") return { low: v.VOLTAGE_LOW, max: v.VOLTAGE_MAX, custom: false };
  if (state.battery === "custom") return { low: state.low ?? v.VOLTAGE_LOW, max: state.max ?? v.VOLTAGE_MAX, custom: true };
  const b = BATTERIES.find((x) => x.id === state.battery);
  if (!b) return { low: v.VOLTAGE_LOW, max: v.VOLTAGE_MAX, custom: false };
  return { low: b.low, max: b.max, custom: true };
}

function pinClashes() {
  const used = [];
  for (const [key, part] of Object.entries(boardInfo().parts)) {
    if (!partOn(key) || key === "SONAR_MEDIAN") continue;
    for (const pin of part.pins) used.push({ pin, key, group: PIN_SHARING[key] || key });
  }
  const clashes = [];
  for (let i = 0; i < used.length; i++) {
    for (let j = i + 1; j < used.length; j++) {
      if (used[i].pin === used[j].pin && used[i].group !== used[j].group) clashes.push([used[i], used[j]]);
    }
  }
  return clashes;
}

// Bluetooth is off in gamepad mode because the gamepad firmware has no phone link
const forcedOff = (key) => key === "HAS_BLUETOOTH" && state.mode === "gamepad";

// Parts the ready-made firmware can't match: turned on but not built in,
// or turned off but not switchable on a flashed robot
function partsNeedingBuild() {
  return Object.entries(boardInfo().parts)
    .filter(([key, part]) => !forcedOff(key) && ((partOn(key) && !part.builtin) || (!partOn(key) && part.default && part.live === null)))
    .map(([key]) => PARTS.find((p) => p.key === key).name);
}

// Parts Update settings only can never change: those the robot can't switch live, changed
// from their default. Live parts are checked against what the robot reports.
function partsNotLive() {
  return Object.entries(boardInfo().parts)
    .filter(([key, part]) => !forcedOff(key) && part.live === null && partOn(key) !== !!part.default)
    .map(([key]) => PARTS.find((p) => p.key === key).name);
}

const MIN_VOLTS = 1;
const MAX_VOLTS = 40;

// What's wrong with a custom battery ("" if it's fine)
function batteryProblem() {
  if (state.battery !== "custom") return "";
  const { low, max } = state;
  if (!Number.isFinite(low) || !Number.isFinite(max)) return "Enter the Empty and Full voltages of your battery.";
  if (low < MIN_VOLTS || max > MAX_VOLTS) return `The battery voltages must be between ${MIN_VOLTS} and ${MAX_VOLTS} V.`;
  if (low >= max) return "Empty must be lower than Full.";
  return "";
}

function liveOffMask() {
  let mask = 0;
  for (const [key, part] of Object.entries(boardInfo().parts)) {
    if (part.live !== null && !partOn(key)) mask |= 1 << part.live;
  }
  return mask;
}

// The robot this page last flashed or read: its body, board and the parts in its firmware
let robot = null;

function loadRobot() {
  try { robot = JSON.parse(localStorage.getItem("openbot-robot")); } catch (e) { robot = null; }
}

function saveRobot(info) {
  robot = info;
  try { localStorage.setItem("openbot-robot", JSON.stringify(info)); } catch (e) {}
}

// Parts in the ready-made firmware for the current body and board
function builtInMask() {
  let mask = 0;
  for (const part of Object.values(boardInfo().parts)) {
    if (part.live !== null && part.builtin) mask |= 1 << part.live;
  }
  return mask;
}

// Parts turned on here that the given firmware doesn't have
function missingFrom(have) {
  return Object.entries(boardInfo().parts)
    .filter(([key, part]) => part.live !== null && partOn(key) && !((have >> part.live) & 1))
    .map(([key]) => PARTS.find((p) => p.key === key).name);
}

function addMessage(names) {
  return `The firmware on your robot doesn't include ${names.join(", ")}. Use Flash firmware to add ${names.length > 1 ? "them" : "it"}.`;
}

// Why Update settings only can't do what is selected: { why: "" } if it can. A reason from
// what the page remembers about the robot can be out of date (after an Arduino IDE upload), so
// clicking checks the robot again ("recheck").
function applyBlocker(clashes) {
  if (!("serial" in navigator)) return { why: "Updating the settings needs Chrome or Edge on a computer." };
  if (clashes) return { why: "Two parts use the same pin. Turn one of them off." };
  const battery = batteryProblem();
  if (battery) return { why: battery };
  const notLive = partsNotLive();
  if (notLive.length) return { why: `${notLive.join(", ")} can only be changed with the Arduino IDE.` };
  if (robot && robot.body === state.body && robot.board === state.board) {
    if (robot.gamepad !== undefined && robot.gamepad !== (state.mode === "gamepad")) {
      return { why: gamepadMessage(), recheck: true };
    }
    const missing = missingFrom(robot.have);
    if (missing.length) return { why: addMessage(missing), recheck: true };
  }
  return { why: "" };
}

function gamepadMessage() {
  return state.mode === "gamepad" ? "This robot doesn't have the gamepad firmware. Use Flash firmware." : "This robot has the gamepad firmware. Use Flash firmware to drive with the phone app or without a controller.";
}

function save() {
  try { localStorage.setItem("openbot-firmware", JSON.stringify(state)); } catch (e) {}
}

function restore() {
  try {
    const saved = JSON.parse(localStorage.getItem("openbot-firmware"));
    const b = saved && data.bodies.find((x) => x.name === saved.body);
    if (b && b.boards[saved.board]) Object.assign(state, saved);
    if (!["body", "custom", ...BATTERIES.map((x) => x.id)].includes(state.battery)) state.battery = "body";
  } catch (e) {}
}

// ---------- Rendering ----------

// Show very wide pictures (like a circuit board) whole instead of cropped
function fitImage(event) {
  const img = event.target;
  img.classList.toggle("wide", img.naturalWidth > 2 * img.naturalHeight);
}

function render() {
  renderBodies();
  renderBoard();
  renderModes();
  renderParts();
  renderBatteries();
  renderRobot();
  save();
}

function renderBodies() {
  $("bodies").replaceChildren(...data.bodies.map((b) => {
    const tile = el("button", { type: "button", className: "body-tile", role: "radio" },
      el("img", { src: b.image, alt: "", loading: "lazy", onload: fitImage }),
      el("span", { textContent: BODY_NAMES[b.name] || b.name }),
      el("small", { textContent: Object.keys(b.boards).map((x) => BOARD_NAMES[x].replace(" Dev Module", "")).join(" or ") }));
    tile.setAttribute("aria-checked", b.name === state.body);
    tile.onclick = () => {
      if (busy) return;
      state.body = b.name;
      if (!b.boards[state.board]) state.board = Object.keys(b.boards)[0];
      state.battery = "body";
      resetParts();
      render();
    };
    return tile;
  }));
}

function renderBoard() {
  const boards = Object.keys(body().boards);
  if (boards.length === 1) {
    $("board").replaceChildren(el("p", { className: "board-fixed" }, "This body uses an ", el("strong", { textContent: BOARD_NAMES[boards[0]] }), "."));
    return;
  }
  const group = el("div", { className: "segmented", role: "radiogroup", ariaLabel: "Board" });
  for (const b of boards) {
    const button = el("button", { type: "button", role: "radio", textContent: BOARD_NAMES[b] });
    button.setAttribute("aria-checked", b === state.board);
    button.onclick = () => {
      if (busy || b === state.board) return;
      state.board = b;
      resetParts();
      render();
    };
    group.append(button);
  }
  $("board").replaceChildren(group);
}

function renderModes() {
  const available = boardInfo().modes;
  $("modes").replaceChildren(...Object.entries(MODES).map(([id, mode]) => {
    const ok = available.includes(id);
    const button = el("button", { type: "button", className: "mode", role: "radio", disabled: !ok },
      el("strong", { textContent: mode.name }),
      el("span", { textContent: ok ? mode.text : "Needs an ESP32." }));
    button.setAttribute("aria-checked", id === state.mode);
    button.onclick = () => {
      if (busy) return;
      state.mode = id;
      render();
    };
    return button;
  }));
}

function renderParts() {
  const available = boardInfo().parts;
  const clashes = pinClashes();
  const clashing = new Set(clashes.flat().map((c) => c.key));
  const groups = GROUPS.map((group) => {
    const rows = PARTS.filter((p) => p.group === group && p.key in available && (!p.needs || partOn(p.needs))).map((p) => {
      const part = available[p.key];
      const forced = forcedOff(p.key);
      const on = partOn(p.key);
      let text = p.text || "";
      if (p.key === "HAS_INDICATORS" && available.HAS_LEDS_BACK && available.HAS_LEDS_BACK.pins.join() === part.pins.join()) text = "Blink the rear lights";
      if (forced) text = "Off: the gamepad uses Bluetooth";
      const input = el("input", { type: "checkbox", checked: on, disabled: forced || busy });
      input.setAttribute("aria-label", p.name);
      input.onchange = () => {
        state.parts[p.key] = input.checked;
        render();
      };
      const info = el("div", { className: "part-text" }, el("strong", { textContent: p.name }), text ? el("small", { textContent: text }) : null);
      if (clashing.has(p.key)) info.append(el("small", { className: "warn", textContent: `Uses the same pin as another part (pin ${part.pins.join(", ")})` }));
      const row = el("div", { className: "part" }, info, el("label", { className: "switch" }, input, el("span")));
      if (!forced && on !== !!part.default) row.classList.add("changed");
      return row;
    });
    return rows.length ? el("div", { className: "part-group" }, el("h3", { textContent: group }), ...rows) : null;
  }).filter(Boolean);
  $("parts").replaceChildren(...groups);
}

function renderBatteries() {
  const v = boardInfo().voltages;
  const choices = [{ id: "body", name: "Standard", low: v.VOLTAGE_LOW, max: v.VOLTAGE_MAX }, ...BATTERIES, { id: "custom", name: "Custom" }];
  $("batteries").replaceChildren(...choices.map((c) => {
    const chip = el("button", { type: "button", className: "chip", role: "radio" }, c.name,
      c.low ? el("small", { textContent: `${c.low.toFixed(1)}–${c.max.toFixed(1)} V` }) : null);
    chip.setAttribute("aria-checked", state.battery === c.id);
    chip.onclick = () => {
      if (busy) return;
      if (c.id === "custom" && state.battery !== "custom") {
        const now = voltages();
        state.low = now.low;
        state.max = now.max;
      }
      state.battery = c.id;
      render();
    };
    return chip;
  }));
  const custom = state.battery === "custom";
  $("custom-battery").hidden = !custom;
  if (custom) {
    $("volt-low").value = voltages().low;
    $("volt-max").value = voltages().max;
  }
}

function startupLines() {
  const items = PARTS.filter((p) => p.log && p.key in boardInfo().parts && partOn(p.key)).map((p) => p.log);
  if (state.mode !== "phone") items.push("no phone mode");
  if (state.mode === "gamepad") items.push("gamepad");
  const lines = [
    { text: "r", cls: "dim" },
    { text: `OpenBot ${state.body} (${state.board === "nano" ? "Nano" : "ESP32"}): ${items.length ? items.join(", ") : "none"}` },
  ];
  if (state.mode !== "phone" && !partOn("HAS_SONAR")) lines.push({ text: "OpenBot warning: no sonar, so the robot drives straight ahead", cls: "warn" });
  for (const [a, b] of pinClashes()) {
    const name = (k) => PARTS.find((p) => p.key === k).name;
    lines.push({ text: `error: ${name(a.key)} and ${name(b.key)} use the same pin (${a.pin})`, cls: "error" });
  }
  return lines;
}

function showLines(lines) {
  $("monitor").replaceChildren(...lines.map((l) => el("div", { className: l.cls || "", textContent: l.text })));
  $("monitor").scrollTop = $("monitor").scrollHeight;
}

function renderRobot() {
  const b = body();
  $("robot-image").onload = fitImage;
  $("robot-image").src = b.image;
  $("robot-image").alt = BODY_NAMES[b.name];
  $("robot-title").textContent = BODY_NAMES[b.name] || b.name;
  $("robot-sub").textContent = `${BOARD_NAMES[state.board]}, ${MODE_SHORT[state.mode]}`;
  if (!busy) {
    $("monitor-state").textContent = "at startup";
    showLines(startupLines());
  }

  const clashes = pinClashes().length > 0;
  const extras = partsNeedingBuild();
  const battery = batteryProblem();
  $("battery-error").hidden = !battery;
  $("battery-error").textContent = battery;
  let flashNote = "";
  let canFlash = true;
  if (!window.isSecureContext) {
    canFlash = false;
    flashNote = `Flashing needs a secure page. Open it at https:// or, on your own computer, at http://localhost:${location.port || 80}/firmware/ instead of ${location.host}.`;
  } else if (!("serial" in navigator)) {
    canFlash = false;
    flashNote = "Flashing needs Chrome or Edge on a computer. You can still download the settings file.";
  } else if (clashes) {
    canFlash = false;
    flashNote = "Two parts use the same pin. Turn one of them off.";
  } else if (battery) {
    canFlash = false;
    flashNote = battery;
  } else if (extras.length) {
    canFlash = false;
    flashNote = `${extras.join(", ")} can only be changed with the Arduino IDE. Download the settings file and upload it there.`;
  } else if (!data.firmware[firmwareKey()]) {
    canFlash = false;
    flashNote = "There's no ready-made firmware for this combination yet. Download the settings file instead.";
  } else {
    flashNote = "Flashes the firmware and sets your battery and parts. Connect the board by USB; you'll choose its port next.";
  }
  $("flash").disabled = busy || !canFlash;
  $("flash-note").textContent = flashNote;
  $("download").disabled = busy || clashes || !!battery;
  // Blocked, not disabled, so a click or hover can show why
  const blocker = applyBlocker(clashes);
  const why = blocker.why;
  const apply = $("apply");
  apply.disabled = busy;
  apply.classList.toggle("blocked", !!why);
  apply.setAttribute("aria-disabled", why ? "true" : "false");
  apply.title = why;
  apply.dataset.why = why;
  apply.dataset.recheck = blocker.recheck ? "true" : "";
  if (!why) $("apply-why").textContent = "";
}

// ---------- Settings file ----------

function settingsFile() {
  let text = data.settings;
  const set = (name, value) => {
    text = text.replace(new RegExp(`^#define ${name} \\S+`, "m"), `#define ${name} ${value}`);
  };
  set("OPENBOT", state.body);
  set("NO_PHONE_MODE", state.mode === "phone" ? 0 : 1);
  set("HAS_GAMEPAD", state.mode === "gamepad" ? 1 : 0);
  for (const [key, part] of Object.entries(boardInfo().parts)) {
    if (forcedOff(key)) continue;
    if (partOn(key) !== !!part.default) set(key, partOn(key) ? 1 : 0);
  }
  const v = voltages();
  if (v.custom) {
    set("VOLTAGE_LOW", `${v.low.toFixed(1)}f`);
    set("VOLTAGE_MAX", `${v.max.toFixed(1)}f`);
  }
  const header = `// OpenBot settings: ${BODY_NAMES[state.body]} with an ${BOARD_NAMES[state.board]}, ${MODE_SHORT[state.mode]}\n` +
    "// Made with tools.openbot.org/firmware. Put this file next to openbot.ino, then upload with the Arduino IDE.\n\n";
  return header + text;
}

function download() {
  const blob = new Blob([settingsFile()], { type: "text/plain" });
  const link = el("a", { href: URL.createObjectURL(blob), download: "openbot_settings.h" });
  link.click();
  URL.revokeObjectURL(link.href);
  const board = BOARD_NAMES[state.board];
  const partitions = state.board === "esp32" ? " with the Minimal SPIFFS partition scheme" : "";
  showLines([...startupLines(), { text: `Downloaded openbot_settings.h. In the Arduino IDE, select ${board}${partitions} and upload openbot.ino.`, cls: "ok" }]);
  $("download-note").textContent = `Saved as openbot_settings.h. Select ${board}${partitions} in the Arduino IDE.`;
}

// ---------- Serial: flashing and live settings ----------

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
let logLines = [];

function log(text, cls = "") {
  logLines.push({ text, cls });
  showLines(logLines);
}

function progress(fraction) {
  $("progress").hidden = fraction === null;
  $("progress-bar").style.width = `${Math.round((fraction || 0) * 100)}%`;
}

class LineReader {
  constructor(port) {
    this.port = port;
    this.lines = [];
    this.partial = "";
  }

  async open() {
    await this.port.open({ baudRate: 115200 });
    this.writer = this.port.writable.getWriter();
    this.reader = this.port.readable.getReader();
    const decoder = new TextDecoder();
    this.reading = (async () => {
      try {
        for (;;) {
          const { value, done } = await this.reader.read();
          if (done) break;
          const parts = (this.partial + decoder.decode(value, { stream: true })).split(/\r?\n/);
          this.partial = parts.pop();
          this.lines.push(...parts.filter((l) => l.length));
        }
      } catch (e) {
        // closed
      }
    })();
  }

  async restart(board) {
    // Restart the robot so it prints its startup line
    if (board === "esp32") {
      await this.port.setSignals({ dataTerminalReady: false, requestToSend: true });
      await sleep(120);
      await this.port.setSignals({ dataTerminalReady: false, requestToSend: false });
    } else {
      await this.port.setSignals({ dataTerminalReady: false, requestToSend: false });
      await sleep(120);
      await this.port.setSignals({ dataTerminalReady: true, requestToSend: true });
    }
    this.lines = [];
  }

  async waitFor(match, timeout) {
    const until = Date.now() + timeout;
    while (Date.now() < until) {
      const i = this.lines.findIndex(match);
      if (i >= 0) return this.lines.splice(0, i + 1).pop();
      await sleep(20);
    }
    return null;
  }

  async send(text) {
    await this.writer.write(new TextEncoder().encode(text + "\n"));
  }

  async close() {
    try { await this.reader.cancel(); } catch (e) {}
    await this.reading;
    this.reader.releaseLock();
    this.writer.releaseLock();
    await this.port.close();
  }
}

function parseLive(line) {
  const values = {};
  const parts = line.slice(1).split(":");
  for (let i = 0; i + 1 < parts.length; i += 2) values[parts[i]] = parts[i + 1];
  return values;
}

async function applyLive(port) {
  const reader = new LineReader(port);
  await reader.open();
  try {
    log("Restarting the robot to read its settings", "dim");
    await reader.restart(state.board);
    const startup = await reader.waitFor((l) => l.startsWith("OpenBot "), 8000);
    if (!startup) throw new Error("The robot didn't start up. Is the OpenBot firmware on it?");
    log(startup);
    await sleep(300);
    await reader.send("p");
    const info = await reader.waitFor((l) => l.startsWith("pboard:"), 3000);
    if (!info) throw new Error("The robot didn't answer the settings. Its firmware may be older than this page; flash it first.");
    const found = parseLive(info);
    const type = (startup.match(/^OpenBot (\S+) \(/) || [])[1];
    saveRobot({ body: type, board: found.board, have: Number(found.have), gamepad: / gamepad$|gamepad,/.test(startup) });
    // Settings for another body or board must never be written to this robot
    if (type !== state.body || found.board !== state.board) {
      const name = BODY_NAMES[type] || type || "an unknown body";
      throw new Error(`This robot reports ${name} with an ${BOARD_NAMES[found.board] || found.board}, but ${BODY_NAMES[state.body]} with an ${BOARD_NAMES[state.board]} is selected. Select your robot above, or use Flash firmware.`);
    }
    if (robot.gamepad !== (state.mode === "gamepad")) throw new Error(gamepadMessage());
    const missing = missingFrom(Number(found.have));
    if (missing.length) throw new Error(addMessage(missing));
    const v = voltages();
    const command = `pvlow:${v.low.toFixed(2)}:vmax:${v.max.toFixed(2)}:off:${liveOffMask()}:nophone:${state.mode === "phone" ? 0 : 1}`;
    await reader.send(command);
    const reply = await reader.waitFor((l) => l.startsWith("pboard:"), 3000);
    if (!reply) throw new Error("The robot didn't answer the settings.");
    const live = parseLive(reply);
    // Standard parts of the body that are turned off (the startup line lists the parts that are on)
    const off = PARTS.filter((p) => boardInfo().parts[p.key]?.default && boardInfo().parts[p.key].live !== null && (Number(live.off) >> boardInfo().parts[p.key].live) & 1).map((p) => p.name);
    log(`Battery ${Number(live.vlow).toFixed(1)}–${Number(live.vmax).toFixed(1)} V${off.length ? `, turned off: ${off.join(", ")}` : ""}`, "ok");
    const now = await reader.waitFor((l) => l.startsWith("OpenBot "), 2000);
    if (now) log(now);
  } finally {
    await reader.close();
  }
}

async function fetchBytes(file) {
  const response = await fetch(file, { cache: "no-cache" });
  if (!response.ok) throw new Error(`Couldn't download ${file}`);
  return new Uint8Array(await response.arrayBuffer());
}

async function flashEsp32(port, files) {
  const { ESPLoader, Transport } = await import(ESPTOOL);
  const transport = new Transport(port, false);
  const terminal = { clean() {}, writeLine: (line) => line && log(line, "dim"), write() {} };
  const loader = new ESPLoader({ transport, baudrate: 460800, romBaudrate: 115200, terminal });
  try {
    log("Connecting to the ESP32. If this takes long, hold its BOOT button.", "dim");
    await loader.main();
    const images = [];
    for (const f of files) images.push({ data: loader.ui8ToBstr(await fetchBytes(f.file)), address: f.address });
    const total = images.reduce((sum, i) => sum + i.data.length, 0);
    const before = (index) => images.slice(0, index).reduce((sum, i) => sum + i.data.length, 0);
    await loader.writeFlash({
      fileArray: images,
      flashSize: "keep",
      flashMode: "keep",
      flashFreq: "keep",
      eraseAll: false,
      compress: true,
      reportProgress: (index, written) => progress((before(index) + written) / total),
    });
    await loader.after("hard_reset");
  } finally {
    await transport.disconnect();
  }
}

async function run(task) {
  if (busy) return;
  let port;
  try {
    port = await navigator.serial.requestPort();
  } catch (e) {
    return;  // no port chosen
  }
  busy = true;
  logLines = [];
  render();
  try {
    await task(port);
    $("monitor-state").textContent = "done";
  } catch (e) {
    log(e.message || String(e), "error");
    $("monitor-state").textContent = "stopped";
  } finally {
    busy = false;
    progress(null);
    const lines = logLines;
    render();
    showLines(lines);
  }
}

async function flash() {
  await run(async (port) => {
    $("monitor-state").textContent = "flashing";
    const files = data.firmware[firmwareKey()];
    progress(0);
    if (state.board === "nano") {
      const hex = new TextDecoder().decode(await fetchBytes(files[0].file));
      await flashNano(port, hex, { log: (t) => log(t, "dim"), onProgress: progress });
    } else {
      await flashEsp32(port, files);
    }
    progress(null);
    log("Firmware flashed", "ok");
    saveRobot({ body: state.body, board: state.board, have: builtInMask(), gamepad: state.mode === "gamepad" });
    $("monitor-state").textContent = "applying settings";
    await sleep(500);
    await applyLive(port);
  });
}

async function apply() {
  const why = $("apply").dataset.why;
  if (why) {
    $("apply-why").textContent = why;
    // Remembered from an earlier visit: check the robot again, it may have new firmware
    if (!$("apply").dataset.recheck) return;
  }
  await run(async (port) => {
    $("monitor-state").textContent = "applying settings";
    await applyLive(port);
  });
}

// ---------- Start ----------

async function start() {
  data = await (await fetch("bodies.json", { cache: "no-cache" })).json();
  restore();
  loadRobot();
  if (!Object.keys(state.parts).length) resetParts();
  $("download").onclick = download;
  $("usb-help-open").onclick = () => $("usb-help").showModal();
  for (const [tab, way] of [["tab-flash", "way-flash"], ["tab-ide", "way-ide"]]) {
    $(tab).onclick = () => {
      for (const [t, w] of [["tab-flash", "way-flash"], ["tab-ide", "way-ide"]]) {
        $(t).setAttribute("aria-selected", t === tab);
        $(w).hidden = w !== way;
      }
    };
  }
  $("usb-help-close").onclick = () => $("usb-help").close();
  $("usb-help").onclick = (e) => { if (e.target === $("usb-help")) $("usb-help").close(); };
  if (data.version) $("version").textContent = `Version ${data.version}`;
  $("flash").onclick = flash;
  $("apply").onclick = apply;
  const custom = () => {
    const number = (id) => { const v = parseFloat($(id).value); return Number.isFinite(v) ? v : null; };
    state.low = number("volt-low");
    state.max = number("volt-max");
    renderRobot();
    save();
  };
  $("volt-low").oninput = custom;
  $("volt-max").oninput = custom;
  render();
}

start().catch((e) => {
  document.body.prepend(el("p", { className: "load-error", textContent: `The page couldn't load its data: ${e.message}` }));
});
