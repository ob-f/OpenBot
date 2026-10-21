#!/usr/bin/env python3
"""Compile openbot.ino in every combination of its global settings with arduino-cli.

For each robot body (OPENBOT) this compiles all combinations of NO_PHONE_MODE,
DEBUG and, if the sketch has it, HAS_GAMEPAD, on every board the body supports.
A body supports both boards unless its section has an error like
'#error "PCB_V1 needs an Arduino Nano board."'. Invalid combinations, and
unsupported boards, must fail with their #error message.

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


MCU_NAMES = {"NANO": "an Arduino Nano", "ESP32": "an ESP32"}


def robots(source: str) -> dict[str, list[str]]:
    """Robot body -> supported MCUs ("NANO", "ESP32"), in the order of the sketch."""
    result = {}
    robot = None
    for line in source.splitlines():
        m = re.match(r"#(?:if|elif) \(OPENBOT == (\w+)\)$", line)
        if m:
            robot = m.group(1)
            result.setdefault(robot, ["NANO", "ESP32"])
            continue
        m = re.match(r'#error "(\w+) needs (an Arduino Nano|an ESP32) board', line)
        if m and m.group(1) == robot:
            result[robot] = [
                mcu for mcu, name in MCU_NAMES.items() if name == m.group(2)
            ]
    return result


def check_defaults(source: str) -> list[str]:
    """Differences between the parts table in the settings and the bodies' DEFAULT_ values."""
    lines = source.splitlines()
    header = next(
        i
        for i, line in enumerate(lines)
        if line.startswith("//") and " DIY " in line and "PCB_V1/V2" in line
    )
    columns = []
    for token in lines[header].lstrip("/ ").split():
        first, *more = token.split("/")
        prefix = first.rsplit("_", 1)[0] + "_"
        columns.append([first] + [prefix + m for m in more])
    table = {}
    for line in lines[header + 1 :]:
        m = re.match(r"//\s+(\w+)\s+([01](?:\s+[01])+)\s*$", line)
        if not m:
            break
        table[m.group(1)] = m.group(2).split()
    body_section = source[source.index("// BODIES") : source.index("// Pin check")]
    defaults, robot = {}, None
    for line in body_section.splitlines():
        m = re.match(r"#(?:if|elif) \(OPENBOT == (\w+)\)$", line)
        if m:
            robot = m.group(1)
            defaults[robot] = set()
        m = re.match(r"#define DEFAULT_(\w+) 1$", line)
        if m and robot:
            defaults[robot].add(m.group(1))
    problems = [] if table else ["parts table not found"]
    for part, values in table.items():
        for bodies, value in zip(columns, values):
            for body in bodies:
                actual = int(part in defaults.get(body, set()))
                if actual != int(value):
                    problems.append(
                        f"{part} for {body}: the table says {value}, the body block {actual}"
                    )
    return problems


def has_define(source: str, name: str) -> bool:
    return len(re.findall(rf"^#define {name}\s", source, re.MULTILINE)) == 1


def combinations(source: str, robot: str, mcus: list[str]):
    """Yield (board, settings, expected_error) for every combination."""
    for mcu in MCU_NAMES:
        if mcu in mcus:
            yield from mcu_combinations(source, robot, mcu)
        else:
            # An unsupported board must fail with the body's error
            yield ("nano" if mcu == "NANO" else "esp32"), {
                "OPENBOT": robot
            }, f"{robot} needs {MCU_NAMES[mcus[0]]} board"


def mcu_combinations(source: str, robot: str, mcu: str):
    gamepad = has_define(source, "HAS_GAMEPAD")
    for no_phone, debug, pad in itertools.product(
        (0, 1), (0, 1), (0, 1) if gamepad else (0,)
    ):
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
    # Parts turned off: the speed sensors still need their interrupts without the sonar
    board = "nano" if mcu == "NANO" else "esp32"
    yield board, {
        "OPENBOT": robot,
        "NO_PHONE_MODE": 0,
        "DEBUG": 0,
        "HAS_SONAR": 0,
    }, None
    if gamepad and mcu == "ESP32":
        # The Bluepad32 board package without a gamepad has no BLE phone link
        settings = {"OPENBOT": robot, "NO_PHONE_MODE": 0, "DEBUG": 0, "HAS_GAMEPAD": 0}
        yield "bluepad32", settings, ERR_NO_BLE


def configure(source: str, settings: dict) -> str:
    for name, value in settings.items():
        pattern = re.compile(rf"^(#define {re.escape(name)})\s+\S+", re.MULTILINE)
        matches = pattern.findall(source)
        if len(matches) != 1:
            sys.exit(
                f"#define {name} found {len(matches)} times in {SKETCH.name}, expected once"
            )
        source = pattern.sub(rf"\g<1> {value}", source)
    return source


def robot_label(settings: dict) -> str:
    return " ".join(f"{k}={v}" for k, v in settings.items())


def tmp_prefix(line: str) -> str:
    """The temporary sketch path at the start of a compiler message, if any."""
    m = re.match(r"(\S*/openbot/)openbot\.ino:", line)
    return m.group(1) if m else ""


def compile_one(fqbn: str, settings: dict, expect_error: str | None) -> bool:
    label = " ".join(f"{k}={v}" for k, v in settings.items())
    with tempfile.TemporaryDirectory() as tmp:
        sketch_dir = pathlib.Path(tmp) / "openbot"
        shutil.copytree(SKETCH.parent, sketch_dir)
        (sketch_dir / "openbot_settings.h").unlink(
            missing_ok=True
        )  # use the settings in the sketch
        sketch = sketch_dir / "openbot.ino"
        sketch.write_text(
            configure(sketch.read_text(encoding="utf-8"), settings), encoding="utf-8"
        )
        result = subprocess.run(
            [
                "arduino-cli",
                "compile",
                "--fqbn",
                fqbn,
                "--warnings",
                "default",
                str(sketch_dir),
            ],
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
        )
    if expect_error is None:
        ok = result.returncode == 0
        verdict = "ok" if ok else "FAILED"
    else:
        ok = result.returncode != 0 and expect_error in result.stdout
        verdict = (
            "failed as expected" if ok else f"FAILED (expected error: {expect_error})"
        )
    print(f"::group::{verdict:>18} | {fqbn} | {label}", flush=True)
    print(result.stdout)
    print("::endgroup::", flush=True)
    if not ok:
        print(f"::error::{fqbn} {label}: {verdict}", flush=True)
        # Show the first compiler errors as annotations, so they appear without opening the log
        errors = [
            line.strip() for line in result.stdout.splitlines() if "error:" in line
        ]
        for line in errors[:5]:
            print(
                f"::error::{robot_label(settings)}: {line.replace(tmp_prefix(line), '')}",
                flush=True,
            )
    return ok


def main() -> int:
    parser = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument("--list-robots", action="store_true")
    parser.add_argument("--check-defaults", action="store_true")
    parser.add_argument("--cores", metavar="ROBOT")
    parser.add_argument("--robot")
    parser.add_argument("--fqbn")
    parser.add_argument("--set", action="append", default=[], metavar="NAME=VALUE")
    parser.add_argument("--expect-error", metavar="TEXT")
    args = parser.parse_args()

    source = read_sketch()
    types = robots(source)
    if args.check_defaults:
        problems = check_defaults(source)
        for problem in problems:
            print(f"::error::Parts table in the settings: {problem}")
        print(
            "Parts table matches the bodies"
            if not problems
            else f"{len(problems)} differences"
        )
        return 1 if problems else 0

    if args.list_robots:
        print(json.dumps(list(types)))
        return 0

    if args.cores:
        boards = {
            board for board, _, _ in combinations(source, args.cores, types[args.cores])
        }
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
