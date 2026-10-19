#!/usr/bin/env python3
"""Build the firmware setup page and its ready-made firmware for tools.openbot.org/firmware.

  build_site.py --build ROBOT --out DIR   compile ROBOT's ready-made firmware into DIR
  build_site.py --site DIR --builds DIR   put the page and the builds into DIR (e.g. _site/firmware)

The bodies, their parts, defaults and pins are read from openbot.ino into
bodies.json, so the page always matches the firmware.
"""

import argparse
import json
import pathlib
import re
import shutil
import subprocess
import sys
import tempfile

from compile_all import BOARDS, SKETCH, configure, read_sketch, robots

ROOT = SKETCH.parents[2]
WEB = SKETCH.parents[1] / "web"
IMAGES = {
    "DIY": "android/robot/src/main/res/drawable-hdpi/diy.gif",
    "PCB_V1": "docs/images/pcb_3d_v1.png",
    "PCB_V2": "docs/images/pcb_3d_v2.png",
    "RTR_TT": "docs/images/RTR_TT.jpg",
    "RTR_TT2": "android/robot/src/main/res/drawable-hdpi/rtr_tt.gif",
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
    "SONAR_MEDIAN": (["PIN_TRIGGER"], None),
    "HAS_BUMPER": (["PIN_BUMPER"], 3),
    "HAS_SPEED_SENSORS_FRONT": (["PIN_SPEED_LF", "PIN_SPEED_RF"], 4),
    "HAS_SPEED_SENSORS_BACK": (["PIN_SPEED_LB", "PIN_SPEED_RB"], 5),
    "HAS_SPEED_SENSORS_MIDDLE": (["PIN_SPEED_LM", "PIN_SPEED_RM"], 6),
    "HAS_LEDS_FRONT": (["PIN_LED_LF", "PIN_LED_RF"], None),
    "HAS_LEDS_BACK": (["PIN_LED_LB", "PIN_LED_RB"], None),
    "HAS_LEDS_STATUS": (["PIN_LED_Y", "PIN_LED_G", "PIN_LED_B"], None),
    "HAS_OLED": ([], None),
    "HAS_BLUETOOTH": ([], None),
}
MCU_BOARD = {"NANO": "nano", "ESP32": "esp32"}
# ESP32 flash layout of the esp32 board package 2.0.17 (default partitions)
ESP32_IMAGES = [
    ("bootloader.bin", 0x1000),
    ("partitions.bin", 0x8000),
    ("boot_app0.bin", 0xE000),
    ("app.bin", 0x10000),
]


def modes(board: str) -> list[str]:
    return ["phone", "nophone", "gamepad"] if board == "esp32" else ["phone", "nophone"]


def mode_settings(mode: str) -> dict:
    return {
        "NO_PHONE_MODE": int(mode != "phone"),
        "HAS_GAMEPAD": int(mode == "gamepad"),
        "DEBUG": 0,
    }


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
            entry["boards"][board] = {
                "parts": parts,
                "voltages": {
                    k: float(v)
                    for k, v in voltages.items()
                    if k != "VOLTAGE_DIVIDER_FACTOR"
                },
                "modes": modes(board),
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


def build(robot: str, out: pathlib.Path) -> None:
    source = read_sketch()
    mcus = robots(source)[robot]
    for mcu in mcus:
        board = MCU_BOARD[mcu]
        for mode in modes(board):
            name = f"{robot.lower()}-{board}-{mode}"
            fqbn = BOARDS["bluepad32" if mode == "gamepad" else board][1]
            target = out / name
            target.mkdir(parents=True, exist_ok=True)
            with tempfile.TemporaryDirectory() as tmp:
                sketch_dir = pathlib.Path(tmp) / "openbot"
                shutil.copytree(SKETCH.parent, sketch_dir)
                (sketch_dir / "openbot_settings.h").unlink(missing_ok=True)
                sketch = sketch_dir / "openbot.ino"
                settings = {"OPENBOT": robot, **mode_settings(mode)}
                sketch.write_text(
                    configure(sketch.read_text(encoding="utf-8"), settings),
                    encoding="utf-8",
                )
                build_dir = pathlib.Path(tmp) / "build"
                result = subprocess.run(
                    [
                        "arduino-cli",
                        "compile",
                        "--fqbn",
                        fqbn,
                        "--output-dir",
                        str(build_dir),
                        str(sketch_dir),
                    ],
                    stdout=subprocess.PIPE,
                    stderr=subprocess.STDOUT,
                    text=True,
                )
                print(
                    f"::group::{name} ({fqbn})\n{result.stdout}\n::endgroup::",
                    flush=True,
                )
                if result.returncode:
                    sys.exit(f"::error::{name}: build failed")
                if board == "nano":
                    shutil.copy(build_dir / "openbot.ino.hex", target / "firmware.hex")
                else:
                    shutil.copy(
                        build_dir / "openbot.ino.bootloader.bin",
                        target / "bootloader.bin",
                    )
                    shutil.copy(
                        build_dir / "openbot.ino.partitions.bin",
                        target / "partitions.bin",
                    )
                    shutil.copy(core_file("boot_app0.bin"), target / "boot_app0.bin")
                    shutil.copy(build_dir / "openbot.ino.bin", target / "app.bin")


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
    }
    (out / "bodies.json").write_text(json.dumps(data, indent=1), encoding="utf-8")
    print(
        f"Site in {out}: {len(data['bodies'])} bodies, {len(firmware)} firmware builds"
    )


def main() -> int:
    parser = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument("--build", metavar="ROBOT")
    parser.add_argument("--out", type=pathlib.Path)
    parser.add_argument("--site", type=pathlib.Path)
    parser.add_argument("--builds", type=pathlib.Path)
    args = parser.parse_args()
    if args.build:
        build(args.build, args.out)
    elif args.site:
        site(args.site, args.builds)
    else:
        parser.print_help()
    return 0


if __name__ == "__main__":
    sys.exit(main())
