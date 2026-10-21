#!/bin/python3

import serial
import time


def test_serial_connect():
    SerialPortObj = serial.Serial("/dev/ttyUSB0", baudrate=115200)
    print("Apply half max speed for 5s")
    string = "c255,255\n"
    SerialPortObj.write(bytes(string, encoding="utf-8"))
    time.sleep(5)
    string = "c0,0\n"
    SerialPortObj.write(bytes(string, encoding="utf-8"))

    SerialPortObj.close()


if __name__ == "__main__":
    test_serial_connect()
