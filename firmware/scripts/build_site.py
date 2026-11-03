#!/usr/bin/env python3
"""Build the firmware setup page and its ready-made firmware for tools.openbot.org/firmware.

  build_site.py --build ROBOT --out DIR   compile ROBOT's ready-made firmware into DIR
  build_site.py --site DIR --builds DIR   put the page and the builds into DIR (e.g. _site/firmware)

The bodies, their parts, defaults and pins are read from openbot.ino into
bodies.json, so the page always matches the firmware.
"""

import argparse
import concurrent.futures
import json
import os
import pathlib
import re
import shutil
import subprocess
import sys
import tempfile

from compile_all import (
    BOARDS,
    SKETCH,
    app_size,
    app_too_big,
    configure,
    read_sketch,
    robots,
)

ROOT = SKETCH.parents[2]
WEB = SKETCH.parents[1] / "web"
IMAGES = {
    "DIY": "robot/android/app/src/main/res/drawable-hdpi/diy.gif",
    "PCB_V1": "docs/images/pcb_3d_v1.png",
    "PCB_V2": "docs/images/pcb_3d_v2.png",
    "RTR_TT": "docs/images/RTR_TT.jpg",
    "RTR_TT2": "robot/android/app/src/main/res/drawable-hdpi/rtr_tt.gif",
    "RTR_520": "docs/images/RTR_520.jpg",
    "RC_CAR": "docs/images/add_covers_2.JPG",
    "LITE": "docs/images/openbot_lite.jpg",
    "MTV": "docs/images/MTV/MTV.jpg",
}

# Parts, the pins each one needs, and its bit in the live settings (see openbot.ino)
PARTS = {
    "HAS_VOLTAGE_DIVIDER": (["PIN_VIN"], 0),
    "HAS_INDICATORS": (["PIN_LED_LI", "PIN_LED_RI"], 1),
    "HAS_SONAR": (["PIN_TRIGGER", "PIN_ECHO"], 2),
    "SONAR_MEDIAN": (["PIN_TRIGGER"], 11),
    "HAS_BUMPER": (["PIN_BUMPER"], 3),
    "HAS_SPEED_SENSORS_FRONT": (["PIN_SPEED_LF", "PIN_SPEED_RF"], 4),
    "HAS_SPEED_SENSORS_BACK": (["PIN_SPEED_LB", "PIN_SPEED_RB"], 5),
    "HAS_SPEED_SENSORS_MIDDLE": (["PIN_SPEED_LM", "PIN_SPEED_RM"], 6),
    "HAS_LEDS_FRONT": (["PIN_LED_LF", "PIN_LED_RF"], 7),
    "HAS_LEDS_BACK": (["PIN_LED_LB", "PIN_LED_RB"], 8),
    "HAS_LEDS_STATUS": (["PIN_LED_Y", "PIN_LED_G", "PIN_LED_B"], 9),
    "HAS_OLED": (["PIN_SDA", "PIN_SCL"], 10),
    "HAS_BLUETOOTH": ([], None),
}
MCU_BOARD = {"NANO": "nano", "ESP32": "esp32"}
# Default I2C pins of the OLED display
I2C_PINS = {
    "nano": {"PIN_SDA": "A4", "PIN_SCL": "A5"},
    "esp32": {"PIN_SDA": "21", "PIN_SCL": "22"},
}
# Parts that may share pins: the indicators blink the rear lights, the median filter smooths the sonar
SHARED = {"HAS_LEDS_BACK": "HAS_INDICATORS", "SONAR_MEDIAN": "HAS_SONAR"}
# ESP32 flash layout of the esp32 board package 2.0.17 (default partitions)
ESP32_IMAGES = [
    ("bootloader.bin", 0x1000),
    ("partitions.bin", 0x8000),
    ("boot_app0.bin", 0xE000),
    ("app.bin", 0x10000),
]


def modes(board: str) -> list[str]:
    """Ready-made builds per board; phone and no phone mode switch in the live settings."""
    return ["phone", "gamepad"] if board == "esp32" else ["phone"]


def mode_settings(mode: str) -> dict:
    return {
        "NO_PHONE_MODE": int(mode == "gamepad"),
        "HAS_GAMEPAD": int(mode == "gamepad"),
        "DEBUG": 0,
    }


def built_in(parts: dict, motor_pins: set, board: str) -> set:
    """Parts in the ready-made firmware: the standard parts, and on the ESP32 every other
    part whose pins are free (turned off until the user turns it on)."""
    chosen = {k for k, p in parts.items() if p["default"]}
    if board != "esp32":
        return chosen
    used = set(motor_pins)
    for k in chosen:
        used |= set(parts[k]["pins"])
    for k, p in parts.items():
        if k in chosen:
            continue
        shares = SHARED.get(k)
        if shares and shares not in chosen:
            continue
        free = set(p["pins"]) - (set(parts[shares]["pins"]) if shares else set())
        if not free & used:
            chosen.add(k)
            used |= set(p["pins"])
    return chosen


def body_blocks(source: str) -> dict[str, str]:
    """Body name -> its block in the BODIES section."""
    section = source[source.index("// BODIES") : source.index("// Pin check")]
    parts = re.split(r"^#(?:if|elif) \(OPENBOT == (\w+)\)$", section, flags=re.M)
    return {parts[i]: parts[i + 1] for i in range(1, len(parts), 2)}


def board_lines(block: str, board: str) -> list[str]:
    """The lines of a body block that apply to the board (DIY has a Nano and an ESP32 part)."""
    lines, state = [], "both"
    for line in block.splitlines():
        if re.match(r"#if \(MCU == NANO\)", line):
            state = "nano"
        elif line.startswith("#else") and state == "nano":
            state = "esp32"
        elif re.match(r"#if \(MCU != \w+\)", line):
            state = "skip"
        elif line.startswith("#endif") and state != "both":
            state = "both"
        elif state in ("both", board):
            lines.append(line)
    return lines


def body_info(source: str) -> list[dict]:
    blocks = body_blocks(source)
    bodies = []
    for body, mcus in robots(source).items():
        entry = {
            "name": body,
            "image": f"img/{pathlib.Path(IMAGES[body]).name}",
            "boards": {},
        }
        for mcu in mcus:
            board = MCU_BOARD[mcu]
            lines = board_lines(blocks[body], board)
            text = "\n".join(lines)
            pins = {
                m.group(1): m.group(2)
                for m in re.finditer(r"^const int (PIN_\w+) = (\w+);", text, re.M)
            }
            pins = {**I2C_PINS[board], **pins}
            motor_pins = {v for k, v in pins.items() if re.match(r"PIN_(PWM|DIR)_", k)}
            defaults = {
                m.group(1): 1
                for m in re.finditer(
                    r"^#define DEFAULT_(HAS_\w+|SONAR_MEDIAN) 1$", text, re.M
                )
            }
            voltages = {
                m.group(1): m.group(2).rstrip("f")
                for m in re.finditer(
                    r"^#define DEFAULT_(VOLTAGE_\w+) \(?([\d.]+)f?\)?", text, re.M
                )
            }
            parts = {}
            for part, (needs, live_bit) in PARTS.items():
                if part == "HAS_BLUETOOTH" and board != "esp32":
                    continue
                if not all(p in pins for p in needs):
                    continue
                parts[part] = {
                    "default": defaults.get(part, 0),
                    "pins": [pins[p] for p in needs],
                    "live": live_bit,
                }
            builtin = built_in(parts, motor_pins, board)
            for part, info in parts.items():
                info["builtin"] = int(part in builtin)
            entry["boards"][board] = {
                "parts": parts,
                "voltages": {
                    k: float(v)
                    for k, v in voltages.items()
                    if k != "VOLTAGE_DIVIDER_FACTOR"
                },
                # driving modes the page offers (phone and no phone share a build)
                "modes": ["phone", "nophone"]
                + (["gamepad"] if board == "esp32" else []),
            }
        bodies.append(entry)
    return bodies


def settings_template(source: str) -> str:
    """The SETTINGS section of openbot.ino, as the settings file starts out."""
    start = source.index("// 1. Robot body")
    end = source.index(
        "#endif\n\n//------------------------------------------------------//\n// DEFINITIONS"
    )
    return source[start:end].rstrip() + "\n"


def core_file(name: str) -> pathlib.Path:
    """A file of an installed ESP32 board package, e.g. boot_app0.bin."""
    packages = pathlib.Path.home() / ".arduino15" / "packages"
    found = sorted(
        packages.glob(f"esp32/hardware/esp32/2.0.17/tools/partitions/{name}")
    )
    if not found:
        sys.exit(f"{name} not found in the esp32 board package")
    return found[0]


def variants(robot: str | None) -> list[tuple]:
    """(name, robot, board, mode, settings) of each ready-made firmware."""
    source = read_sketch()
    info = {b["name"]: b for b in body_info(source)}
    out = []
    for body, mcus in robots(source).items():
        if robot not in (None, "all", body):
            continue
        for mcu in mcus:
            board = MCU_BOARD[mcu]
            parts = info[body]["boards"][board]["parts"]
            extras = {
                k: 1
                for k, p in parts.items()
                if p["builtin"] and not p["default"] and k != "HAS_BLUETOOTH"
            }
            for mode in modes(board):
                settings = {"OPENBOT": body, **mode_settings(mode), **extras}
                out.append((f"{body.lower()}-{board}-{mode}", board, mode, settings))
    return out


def build_one(name: str, board: str, mode: str, settings: dict, out: pathlib.Path):
    fqbn = BOARDS["bluepad32" if mode == "gamepad" else board][1]
    target = out / name
    target.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        sketch_dir = pathlib.Path(tmp) / "openbot"
        shutil.copytree(SKETCH.parent, sketch_dir)
        (sketch_dir / "openbot_settings.h").unlink(missing_ok=True)
        sketch = sketch_dir / "openbot.ino"
        text = configure(sketch.read_text(encoding="utf-8"), settings)
        # Ready-made firmware: extra parts start turned off (see reset_live_settings)
        sketch.write_text("#define READY_MADE 1\n" + text, encoding="utf-8")
        build_dir = pathlib.Path(tmp) / "build"
        result = subprocess.run(
            ["arduino-cli", "compile", "--fqbn", fqbn, "--output-dir", str(build_dir)]
            + [str(sketch_dir)],
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
        )
        too_big = app_too_big(build_dir) if result.returncode == 0 else ""
        if too_big:
            result = subprocess.CompletedProcess(
                result.args, 1, result.stdout + f"\nerror: {too_big}\n"
            )
        sizes = app_size(build_dir) if result.returncode == 0 else None
        if sizes and sizes[1]:
            used, size = sizes
            print(
                f"::notice::{name}: app {used // 1024} KB of {size // 1024} KB"
                f" ({size - used} bytes free)",
                flush=True,
            )
        if result.returncode == 0:
            if board == "nano":
                shutil.copy(build_dir / "openbot.ino.hex", target / "firmware.hex")
            else:
                for src, dst in [
                    ("openbot.ino.bootloader.bin", "bootloader.bin"),
                    ("openbot.ino.partitions.bin", "partitions.bin"),
                    ("openbot.ino.bin", "app.bin"),
                ]:
                    shutil.copy(build_dir / src, target / dst)
                shutil.copy(core_file("boot_app0.bin"), target / "boot_app0.bin")
        return name, fqbn, result.returncode, result.stdout


def build(robot: str, out: pathlib.Path, jobs: int) -> None:
    """Build in parallel; the first build of each board runs alone to fill the core cache."""
    todo = variants(robot)
    first, rest, seen = [], [], set()
    for v in todo:
        key = (v[1], v[2] == "gamepad")
        (rest if key in seen else first).append(v)
        seen.add(key)
    results = [build_one(*v, out) for v in first]
    with concurrent.futures.ThreadPoolExecutor(jobs) as pool:
        results += list(pool.map(lambda v: build_one(*v, out), rest))
    failed = []
    for name, fqbn, code, output in results:
        print(f"::group::{name} ({fqbn})\n{output}\n::endgroup::", flush=True)
        if code:
            print(f"::error::{name}: build failed", flush=True)
            # The first compiler errors as annotations, readable without the log
            errors = [line.strip() for line in output.splitlines() if "error:" in line]
            for line in errors[:5]:
                print(
                    f"::error::{name}: {re.sub(r'^.*/openbot/', '', line)}", flush=True
                )
            failed.append(name)
    print(f"{len(results) - len(failed)}/{len(results)} ready-made firmware built")
    if failed:
        sys.exit(f"{len(failed)} builds failed: {', '.join(sorted(failed))}")


def version() -> str:
    """The commit the site is built from, shown in the page's footer."""
    if os.environ.get(
        "SITE_VERSION"
    ):  # CI: the branch's commit, not a pull request's merge commit
        return os.environ["SITE_VERSION"][:7]
    result = subprocess.run(
        ["git", "rev-parse", "--short", "HEAD"],
        cwd=ROOT,
        capture_output=True,
        text=True,
    )
    return result.stdout.strip()


def site(out: pathlib.Path, builds: pathlib.Path | None) -> None:
    source = read_sketch()
    if out.exists():
        shutil.rmtree(out)
    out.parent.mkdir(parents=True, exist_ok=True)
    shutil.copytree(WEB, out)
    (out / "img").mkdir(exist_ok=True)
    # OpenBot branding from the PlayGround: the logo and the Gilroy font
    shutil.copy(
        ROOT / "open-code/src/assets/images/icon/open-bot-logo.png",
        out / "img" / "logo.png",
    )
    (out / "fonts").mkdir(exist_ok=True)
    for weight in ("Regular", "Medium", "SemiBold"):
        shutil.copy(
            ROOT / f"open-code/src/assets/font/Gilroy/Gilroy-{weight}.woff",
            out / "fonts",
        )
    for path in set(IMAGES.values()):
        shutil.copy(ROOT / path, out / "img" / pathlib.Path(path).name)
    firmware = {}
    if builds and builds.exists():
        for folder in sorted(
            p
            for p in builds.rglob("*")
            if p.is_dir() and (any(p.glob("firmware.hex")) or any(p.glob("app.bin")))
        ):
            target = out / "firmware" / folder.name
            shutil.copytree(folder, target, dirs_exist_ok=True)
            if (folder / "firmware.hex").exists():
                firmware[folder.name] = [
                    {"file": f"firmware/{folder.name}/firmware.hex"}
                ]
            else:
                firmware[folder.name] = [
                    {"file": f"firmware/{folder.name}/{f}", "address": a}
                    for f, a in ESP32_IMAGES
                ]
    data = {
        "bodies": body_info(source),
        "settings": settings_template(source),
        "firmware": firmware,
        "version": version(),
    }
    # New builds must not come from the browser's cache: version the file names
    stamp = data["version"] or "dev"
    index = out / "index.html"
    html = index.read_text(encoding="utf-8")
    for name in ("style.css", "app.js"):
        html = html.replace(f'"{name}"', f'"{name}?v={stamp}"')
    index.write_text(html, encoding="utf-8")
    app = out / "app.js"
    app.write_text(
        app.read_text(encoding="utf-8").replace('"./avr.js"', f'"./avr.js?v={stamp}"'),
        encoding="utf-8",
    )
    (out / "bodies.json").write_text(json.dumps(data, indent=1), encoding="utf-8")
    print(
        f"Site in {out}: {len(data['bodies'])} bodies, {len(firmware)} firmware builds"
    )


def main() -> int:
    parser = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument("--build", metavar="ROBOT", help="a body, or all")
    parser.add_argument("--jobs", type=int, default=os.cpu_count() or 2)
    parser.add_argument("--out", type=pathlib.Path)
    parser.add_argument("--site", type=pathlib.Path)
    parser.add_argument("--builds", type=pathlib.Path)
    args = parser.parse_args()
    if args.build:
        build(args.build, args.out, args.jobs)
    elif args.site:
        site(args.site, args.builds)
    else:
        parser.print_help()
    return 0


if __name__ == "__main__":
    sys.exit(main())
