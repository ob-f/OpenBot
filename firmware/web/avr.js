// Flash an Arduino Nano over Web Serial with the STK500 protocol of its bootloader.
// New bootloader (Optiboot): 115200 baud. Old bootloader: 57600 baud.

const OK = [0x14, 0x10];
const PAGE = 128;

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// Intel HEX -> bytes from address 0
export function parseHex(text) {
  const bytes = new Uint8Array(32768).fill(0xff);
  let base = 0;
  let end = 0;
  for (const raw of text.split(/\r?\n/)) {
    const line = raw.trim();
    if (!line.startsWith(":")) continue;
    const data = line.slice(1).match(/../g).map((h) => parseInt(h, 16));
    const [count, hi, lo, type] = data;
    const address = (hi << 8) | lo;
    if (type === 0) {
      for (let i = 0; i < count; i++) bytes[base + address + i] = data[4 + i];
      end = Math.max(end, base + address + count);
    } else if (type === 2) {
      base = ((data[4] << 8) | data[5]) << 4;
    } else if (type === 4) {
      base = ((data[4] << 8) | data[5]) << 16;
    } else if (type === 1) {
      break;
    }
  }
  return bytes.slice(0, Math.ceil(end / PAGE) * PAGE);
}

class Link {
  constructor(port) {
    this.port = port;
    this.buffer = [];
  }

  async open(baudRate) {
    await this.port.open({ baudRate });
    this.writer = this.port.writable.getWriter();
    this.reader = this.port.readable.getReader();
    this.reading = (async () => {
      try {
        for (;;) {
          const { value, done } = await this.reader.read();
          if (done) break;
          this.buffer.push(...value);
        }
      } catch (e) {
        // closed
      }
    })();
  }

  async close() {
    try { await this.reader.cancel(); } catch (e) {}
    await this.reading;
    this.reader.releaseLock();
    this.writer.releaseLock();
    await this.port.close();
  }

  async reset() {
    // Like the Arduino IDE: pull DTR/RTS low, then release them; the Nano resets into its bootloader
    await this.port.setSignals({ dataTerminalReady: false, requestToSend: false });
    await sleep(250);
    await this.port.setSignals({ dataTerminalReady: true, requestToSend: true });
    await sleep(50);
    this.buffer = [];
  }

  async send(bytes) {
    await this.writer.write(new Uint8Array(bytes));
  }

  async receive(count, timeout = 500) {
    const until = Date.now() + timeout;
    while (this.buffer.length < count) {
      if (Date.now() > until) throw new Error("No answer from the board");
      await sleep(2);
    }
    return this.buffer.splice(0, count);
  }

  async command(bytes, answer = 0, timeout = 500) {
    await this.send([...bytes, 0x20]);
    const reply = await this.receive(answer + 2, timeout);
    if (reply[0] !== OK[0] || reply[reply.length - 1] !== OK[1]) throw new Error("Unexpected answer from the bootloader");
    return reply.slice(1, -1);
  }

  async sync() {
    for (let attempt = 0; attempt < 12; attempt++) {
      this.buffer = [];
      try {
        await this.command([0x30], 0, 150);
        return true;
      } catch (e) {
        await sleep(30);
      }
    }
    return false;
  }
}

async function program(link, bytes, onProgress) {
  await link.command([0x50]);  // enter programming mode
  const pages = bytes.length / PAGE;
  for (let p = 0; p < pages; p++) {
    const word = (p * PAGE) >> 1;
    await link.command([0x55, word & 0xff, word >> 8]);  // load address
    await link.command([0x64, 0x00, PAGE, 0x46, ...bytes.subarray(p * PAGE, (p + 1) * PAGE)], 0, 1000);  // program page
    onProgress((p + 1) / pages / 2);
  }
  for (let p = 0; p < pages; p++) {
    const word = (p * PAGE) >> 1;
    await link.command([0x55, word & 0xff, word >> 8]);
    const read = await link.command([0x74, 0x00, PAGE, 0x46], PAGE, 1000);  // read page
    for (let i = 0; i < PAGE; i++) {
      if (read[i] !== bytes[p * PAGE + i]) throw new Error(`Verification failed at byte ${p * PAGE + i}`);
    }
    onProgress(0.5 + (p + 1) / pages / 2);
  }
  await link.command([0x51]);  // leave programming mode, the sketch starts
}

// Flash the hex file; tries the new bootloader first, then the old one
export async function flashNano(port, hexText, { log, onProgress }) {
  const bytes = parseHex(hexText);
  for (const baud of [115200, 57600]) {
    const link = new Link(port);
    await link.open(baud);
    try {
      log(`Connecting to the bootloader at ${baud} baud`);
      await link.reset();
      if (!(await link.sync())) {
        log(baud === 115200 ? "No answer, trying the old bootloader" : "No answer from the old bootloader either");
        continue;
      }
      log(`Writing ${bytes.length} bytes`);
      await program(link, bytes, onProgress);
      return;
    } finally {
      await link.close();
    }
  }
  throw new Error("The Nano didn't answer. Check the USB cable and that no other program (like the Arduino IDE) has the port open.");
}
