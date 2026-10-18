#!/usr/bin/env python3
"""Compile openbot.ino in every combination of its global settings with arduino-cli.

For each robot type (OPENBOT) this compiles all combinations of NO_PHONE_MODE,
DEBUG and, if the sketch has it, HAS_GAMEPAD, on the matching board. Invalid
combinations must fail with their #error message.

  compile_all.py --list-robots        robot types as JSON (for the CI matrix)
  compile_all.py --cores ROBOT        arduino-cli cores the robot needs
  compile_all.py --robot ROBOT        compile all combinations for one robot
  compile_all.py                      compile all combinations for all robots
  compile_all.py --fqbn FQBN --set NAME=VALUE ...   compile one configuration

Only #defines that appear exactly once in the sketch can be set; per-robot
settings would be ambiguous.
"""

import argparse
import itertools
import json
import pathlib
import re
import shutil
import subprocess
import sys
import tempfile

SKETCH = pathlib.Path(__file__).resolve().parents[1] / "openbot" / "openbot.ino"

BOARDS = {
    "nano": ("arduino:avr", "arduino:avr:nano"),
    "esp32": ("esp32:esp32@2.0.17", "esp32:esp32:esp32"),
    "bluepad32": ("esp32-bluepad32:esp32@4.1.0", "esp32-bluepad32:esp32:esp32"),
}

ERR_GAMEPAD_PHONE = "HAS_GAMEPAD is for no phone mode"
ERR_GAMEPAD_NANO = "HAS_GAMEPAD needs an ESP32 body"
ERR_NO_BLE = "This board package has no Bluetooth phone link"


def read_sketch() -> str:
    return SKETCH.read_text(encoding="utf-8")


def robots(source: str) -> dict[str, str]:
    """Robot type -> MCU ("NANO" or "ESP32"), in the order of the sketch."""
    result = {}
    robot = None
    for line in source.splitlines():
        m = re.match(r"#(?:if|elif) \(OPENBOT == (\w+)\)$", line)
        if m:
            robot = m.group(1)
            continue
        m = re.match(r"#define MCU (\w+)", line)
        if m and robot and robot not in result:
            result[robot] = m.group(1)
    return result


def has_define(source: str, name: str) -> bool:
    return len(re.findall(rf"^#define {name}\s", source, re.MULTILINE)) == 1


def combinations(source: str, robot: str, mcu: str):
    """Yield (board, settings, expected_error) for every combination."""
    gamepad = has_define(source, "HAS_GAMEPAD")
    for no_phone, debug, pad in itertools.product((0, 1), (0, 1), (0, 1) if gamepad else (0,)):
        settings = {"OPENBOT": robot, "NO_PHONE_MODE": no_phone, "DEBUG": debug}
        if gamepad:
            settings["HAS_GAMEPAD"] = pad
        if mcu == "NANO":
            board = "nano"
        else:
            board = "bluepad32" if pad else "esp32"
        error = None
        if pad and not no_phone:
            error = ERR_GAMEPAD_PHONE
        elif pad and mcu == "NANO":
            error = ERR_GAMEPAD_NANO
        yield board, settings, error
    if gamepad and mcu == "ESP32":
        # The Bluepad32 board package without a gamepad has no BLE phone link
        settings = {"OPENBOT": robot, "NO_PHONE_MODE": 0, "DEBUG": 0, "HAS_GAMEPAD": 0}
        yield "bluepad32", settings, ERR_NO_BLE


def configure(source: str, settings: dict) -> str:
    for name, value in settings.items():
        pattern = re.compile(rf"^(#define {re.escape(name)})\s+\S+", re.MULTILINE)
        matches = pattern.findall(source)
        if len(matches) != 1:
            sys.exit(f"#define {name} found {len(matches)} times in {SKETCH.name}, expected once")
        source = pattern.sub(rf"\g<1> {value}", source)
    return source


def compile_one(fqbn: str, settings: dict, expect_error: str | None) -> bool:
    label = " ".join(f"{k}={v}" for k, v in settings.items())
    with tempfile.TemporaryDirectory() as tmp:
        sketch_dir = pathlib.Path(tmp) / "openbot"
        shutil.copytree(SKETCH.parent, sketch_dir)
        sketch = sketch_dir / "openbot.ino"
        sketch.write_text(configure(sketch.read_text(encoding="utf-8"), settings), encoding="utf-8")
        result = subprocess.run(
            ["arduino-cli", "compile", "--fqbn", fqbn, "--warnings", "default", str(sketch_dir)],
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
        )
    if expect_error is None:
        ok = result.returncode == 0
        verdict = "ok" if ok else "FAILED"
    else:
        ok = result.returncode != 0 and expect_error in result.stdout
        verdict = "failed as expected" if ok else f"FAILED (expected error: {expect_error})"
    print(f"::group::{verdict:>18} | {fqbn} | {label}", flush=True)
    print(result.stdout)
    print("::endgroup::", flush=True)
    if not ok:
        print(f"::error::{fqbn} {label}: {verdict}", flush=True)
    return ok


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--list-robots", action="store_true")
    parser.add_argument("--cores", metavar="ROBOT")
    parser.add_argument("--robot")
    parser.add_argument("--fqbn")
    parser.add_argument("--set", action="append", default=[], metavar="NAME=VALUE")
    parser.add_argument("--expect-error", metavar="TEXT")
    args = parser.parse_args()

    source = read_sketch()
    types = robots(source)

    if args.list_robots:
        print(json.dumps(list(types)))
        return 0

    if args.cores:
        boards = {board for board, _, _ in combinations(source, args.cores, types[args.cores])}
        print(" ".join(sorted(BOARDS[b][0] for b in boards)))
        return 0

    if args.fqbn:
        settings = {}
        for item in args.set:
            name, sep, value = item.partition("=")
            if not sep or not name or not value:
                sys.exit(f"Invalid --set {item!r}, expected NAME=VALUE")
            settings[name] = value
        return 0 if compile_one(args.fqbn, settings, args.expect_error) else 1

    selected = [args.robot] if args.robot else list(types)
    for robot in selected:
        if robot not in types:
            sys.exit(f"Unknown robot {robot!r}, known: {', '.join(types)}")
    results = []
    for robot in selected:
        for board, settings, error in combinations(source, robot, types[robot]):
            results.append(compile_one(BOARDS[board][1], settings, error))
    failed = results.count(False)
    print(f"{len(results) - failed}/{len(results)} combinations as expected")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
