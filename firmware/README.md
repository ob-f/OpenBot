# Firmware

<p align="center">
  <span>English</span> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

We use a microcontroller unit (MCU) to act as a bridge between the robot body and the smartphone.  We provide our [firmware](openbot/openbot.ino) for the Arduino Nano with an ATmega328P microcontroller as well as for the ESP32 development kit.

## Features

The main task of the MCU is to handle the low-level control of the vehicle and provide readings from low-level vehicle-mounted sensors. The MCU receives the vehicle controls and indicator signals via the serial connection. It converts the controls to PWM signals for the motor controller and toggles the LEDs according to the indicator signal. The Arduino program also keeps track of the wheel rotations by counting the interrupts of optical sensors on the left and right front wheels. It calculates the battery voltage by a scaled moving average of measurements at the voltage divider circuit. It can also measure the distance to obstacles in front of the car with an optional ultrasonic sensor. These measurements are sent back to the Android application through the serial link.

## Setup

First you have to set up your hardware configuration at the beginning of the code. If you did the DIY build (using the L298N motor driver), set `OPENBOT DIY`.
If you used the custom PCB, check the version and set either `OPENBOT PCB_V1` or `OPENBOT PCB_V2`. If you have a OpenBot kit set `OPENBOT RTR_TT`. If you have retrofitted an RC truck, set `OPENBOT RC_CAR`. If you use the smaller DIY version for education, set `OPENBOT LITE`. If you use the OpenBot Ready-to-Run kit with 520-motors, set `OPENBOT RTR_520`. if you built the Multi Terrain Vehicle, you should set `OPENBOT MTV`. To run the `OpenBot DIY` with the ESP32 set OpenBot `DIY_ESP32`.

## Bluetooth

You can run the OpenBot via bluetooth as well, for that you can enable the bluetooth by setting `BLUETOOTH 1` (disable: 0). For bluetooth to work you need OpenBot with ESP32 boards like `(RTR_520, MTV, DIY_ESP32)`.

## Config

Next, you need to configure which features you want to enable. Disabled features are not compiled to save memory and make the code faster. If a flag is not defined, the feature will be disabled. Each model has some default settings, that you may need to change depending on your configuration.

- Enable the voltage divider by setting `HAS_VOLTAGE_DIVIDER 1` (disable: 0). If you have a voltage divider, you should also specify the `VOLTAGE_DIVIDER_FACTOR` which is computed as (R1+R2)/R2, `VOLTAGE_MIN` which is the minimum voltage to drive the motors, `VOLTAGE_LOW` which is the minimum battery voltage and `VOLTAGE_MAX` which is the maximum battery voltage.
- Enable the indicator LEDs by setting `HAS_INDICATORS 1` (disable: 0).
- Enable the front/back speed sensors by setting `HAS_SPEED_SENSORS_FRONT 1` / `HAS_SPEED_SENSORS_BACK 1` (disable: 0).
- Enable the ultrasonic sensor by setting `HAS_SONAR 1` (disable: 0). Enable the median filter for sonar measurements by setting `USE_MEDIAN 1` (disable: 0).
- Enable the bumper sensor which is used to detect collisions by setting `HAS_BUMPER 1` (disable: 0).
- Enable the OLED display by setting `HAS_OLED 1` (disable: 0).
- Enable the front/back/status LEDs by setting `HAS_LEDS_FRONT 1` / `HAS_LEDS_BACK 1` / `HAS_LEDS_STATUS 1` (disable: 0).

### Dependencies

If you have enabled the speed sensors or the ultrasonic sensor, you need to install the [PinChangeInterrupt](https://github.com/NicoHood/PinChangeInterrupt) library. The Arduino Nano only has two external interrupt pins (D2 and D3) and D3 is also one of only six pins that support PWM. Fortunately, it also has three port interrupts that cover all pins on the Arduino. This library parses these port interrupts allowing all pins of the Arduino to be used as interrupts.

If you have enabled the OLED, you need to install the libraries [Adafruit_SSD1306](https://github.com/adafruit/Adafruit_SSD1306) and [Adafruit_GFX Library](https://github.com/adafruit/Adafruit-GFX-Library).

You can install libraries by following these steps:

1. Open the Library Manager: `Tools` :arrow_right: `Manage Libraries`
2. Enter the name of the library in the search bar.
3. Select the latest version and click install. If you have already installed the library it will show and you may be able to update it.

<p float="left">
  <img src="../docs/images/manage_libraries.jpg" height="300" />
  <img src="../docs/images/install_library.jpg" height="300" /> 
</p>

### Chinese clone Nano (e.g. US link)

You may need to download the [WCH340](http://www.wch.cn/product/CH340.html) drivers from the chip manufacturer (Chinese):

- [Windows](http://www.wch.cn/downloads/CH341SER_EXE.html)
- [Linux](http://www.wch.cn/download/CH341SER_LINUX_ZIP.html)
- [Mac](http://www.wch.cn/download/CH341SER_MAC_ZIP.html)

### ESP32 development kit

To install the ESP32 board in your Arduino IDE, follow these next instructions:

1. In your Arduino IDE, go to **File> Preferences**:
<p align="center">
  <img src="../docs/images/arduino-ide-open-preferences.png" width="300" alt="App GUI"/>
</p>

2. Enter *https://dl.espressif.com/dl/package_esp32_index.json* into the “_Additional Board Manager URLs_” field as shown in the figure below. Then, click the “OK” button:
<p align="center">
  <img src="../docs/images/arduino_preferences.png" width="600" alt="App GUI"/>
</p>

**Note:** if you already have the ESP8266 boards URL, you can separate the URLs with a comma as follows:

    https://dl.espressif.com/dl/package_esp32_index.json,
    http://arduino.esp8266.com/stable/package_esp8266com_index.json

3. Open the Boards Manager. Go to **Tools > Board > Boards Manager**:
<p align="center">
  <img src="../docs/images/arduino_boardsManager.png" width="800" alt="App GUI"/>
</p>

4. Search for ESP32. Look for “ESP32 by Espressif Systems“ and make sure to select version 2.0.17 from the drop-down. Any version 3.x.x will not work! Click on install:
<p align="center">
  <img src="../docs/images/arduino_installing.png" width="600" alt="App GUI"/>
</p>

5. You should now have everything to successfully flash the ESP32 board of your OpenBot using the Arduino development environment.
<p align="center">
  <img src="../docs/images/arduino_ESP32-Board-add-on-in-Arduino-IDE-installed.png" width="600" alt="App GUI"/>
</p>

6. To flash the OpenBot with your new code, simply select **ESP32 Dev Module** in the menu **Tools > Board > ESP32 Arduino**. Note that additional content as well as troubleshooting of the ESP32 flashing prcess can be found in the following [link](https://randomnerdtutorials.com/installing-the-esp32-board-in-arduino-ide-windows-instructions/).
<p align="center">
  <img src="../docs/images/arduino_windows-select-board.png" width="600" alt="App GUI"/>
</p>

If you have trouble uploading try reducing the baud rate. Go to tools and select the upload speed as 115200.
<p align="center">
<img src="../docs/images/arduino_upload-speed-select.png" width="600" alt="App GUI"/>
</p>

## Upload

### Settings (Arduino nano setup)

- `Tools` :arrow_right: `Board` :arrow_right: `Arduino AVR Boards` :arrow_right: `Arduino Nano`
- `Tools` :arrow_right: `Processor` :arrow_right: `ATmega328P (Old Bootloader)`
- `Tools` :arrow_right: `Port` :arrow_right: `*Select the USB port*`

:memo: NOTE: Currently, most cheap Arduino Nano boards come with the _Old Bootloader_. However, depending on the seller you may also get one with the new bootloader. So if you are unable to upload the firmware, chances are that you need to change the processor to _ATmega328P_.

### Settings (ESP32 setup)

- `Tools` :arrow_right: `Board` :arrow_right: `ESP32 Arduino` :arrow_right: `ESP32 Dev Module`
- `Tools` :arrow_right: `Port` :arrow_right: `*Select the USB port*`

### Uploading the firmware

The firmware can now be uploaded through `Sketch` :arrow_right: `Upload` or by pressing the upload button (right arrow).
![Firmware Upload](../docs/images/firmware_upload.png)

### Testing

This section explains how to test all functionalities of the car after the firmware was flashed successfully.

1. Confirm that:
   1. the wheels are not connected to the car
   2. the Arduino is connected to the computer
   3. the correct USB port is selected
2. Open the Serial Monitor: `Tools` :arrow_right: `Serial Monitor`

#### Sending messages to the OpenBot

You can also send messages to the Arduino by typing a command into the input field on the top and then pressing send. The following commands are available (provided the necessary features are supported by the robot):

- `c<left>,<right>` where `<left>` and `<right>` are both in the range [-255,255]. A value of `0` will stop the motors. A value of `255` applies the maximum voltage driving the motors at the full speed forward. Lower values lead to proportionally lower voltages and speeds. Negative values apply the corresponding voltages in reverse polarity driving the motors in reverse.
- `i<left>,<right>` where `<left>` and `<right>` are both in the range [0,1] and correspond to the left and right indicator LEDs. For example, `i1,0` turns on the left indicator, `i0,1` the right indicator and `i1,1` both indicators. Enabled indicator lights will flash once per second. A value of `i0,0` turns the indicators off. Only one state at a time is possible.
- `l<front>,<back>` where `<front>` and `<back>` are both in the range [0,255] and correspond to the brightness of the front and back LEDs.
- `s<time_ms>` where `<time_ms>` corresponds to the time in ms between sonar measurements triggered (default = 1000). After the sonar reading is aquired the message is sent to the robot. If it times out, the specified `MAX_SONAR_DISTANCE` is sent.
- `w<time_ms>` where `<time_ms>` corresponds to the time in ms between wheel odometry measurements sent to the robot (default = 1000). The wheel speed is monitored continuously and and the rpm is computed as average over the specified time interval.
- `v<time_ms>` where `<time_ms>` corresponds to the time in ms between voltage measurements sent to the robot (default = 1000). The voltage is monitored continuously and filtered via a moving average filter of size 10. In addition to setting the time interval for voltage readings, sending this command will also trigger messages that report the minimum voltage to drive the motors (`vmin:<value>`), minimum battery voltage (`vlow:<value>`) and maximum battery vollage (`vmax:<value>`).
- `h<time_ms>` where `<time_ms>` corresponds to the time in ms after which the robot will stop if no new heartbeat message was received (default = -1).
- `b<time_ms>` where `<time_ms>` corresponds to the time in ms after which the bumper trigger will be reset (default = 750).
- `n<color>,<state>` where `<color>` corresponds to a status LED (`b` = blue, `g` = green, `y` = yellow) and `state` to its value (`0` = off, `1` = on).
- `f` will send a request to the OpenBot to return a message with the robot type and its features, e.g. voltage measurement (`v`), indicators (`i`), sonar (`s`), bump sensors (`b`), wheel odometry (`wf`, `wb`), LEDs (`lf`, `lb`, `ls`), etc. For example, for the `RTR_V1` version of OpenBot the message would look like this: `fRTR_V1:v:i:s:b:wf:wb:lf:lb:ls:`.

#### Receiving messages from the OpenBot

Depending on your configuration you may see different messages.

![Serial Monitor](../docs/images/serial_monitor.png)

- Messages that start with `v` report the battery voltage. If you connect the battery to the car (i.e. turn on the switch), it should show the battery voltage. If you disconnect the battery (i.e. turn off the switch), it should show a small value.
- Messages that start with `w` report readings of the speed sensors measured in revolutions per second (rpm). Each hole in the encoder disk will increment a counter by plus/minus one depending on the direction. You can set the number of holes with the parameter `DISK_HOLES`. If you are using the standard disk with 20 holes, there will be 20 counts for each revolution of the wheel.
- Messages that start with `s` report the estimated free space in front of the ultrasonic sensor in cm.
- Messages that start with `b` report collisions. The codes `lf` (left front), `rf` (right front), `cf` (center front), `lb` (left back), `rb` (right back) indicate which sensor triggered the collision.

#### Test procedure

Before you proceed, make sure the tires are removed. You will need the Serial Monitor open to send commands and you will see the messages received from your OpenBot. If you have the OLED display installed, you will also see the vehicle status displayed there in a more human-readable format. The following test procedure can be used to test all functionalities of the car:

1. Turn on the car and observe the battery voltage (the number after the `v`). You can verify the reading with a multimeter and adjust the `VOLTAGE_DIVIDER_FACTOR` if necessary.
2. If you have an ultrasonic sensor installed:
   1. Hold your hand in front of the sensor and move it back and forth. You should see the readings (the number after the `s`) change correspondingly.
   2. We have observed that the ultrasonic sensor is very sensitive to vibrations! So it is advisable to make sure you will get reliable readings during operation by performing the following test:
      1. Place the OpenBot with the ultrasonic sensor installed such that there is at least 200cm of free space in front of it. You should see a reading of `200` or more.
      2. Observe the readings on the serial monitor for some time and then enter the command `c128,128`.
      3. If the sensor readings change significantly, you will need to dampen the vibrations transmitted to the ultrasonic sensor from the chassis (e.g. add some silicon, adjust the mounting position).
3. If you have the speed sensors installed:
   1. Make sure, you have plenty of free space in front of the ultrasonic sensor. The reading (the number after the `s`) needs to be at least above the `STOP_DISTANCE` which is `10` by default.
   2. Send the command `c128,128`. The motors will start spinning at _slow speed_ (50% PWM). The speed sensor readings (values after the `w`) are reported in rpm and should be between 250 and 300 for the RTR_TT version depending on the SOC of the battery. If you are using the DIY version or a weaker battery, values may be lower. Check that all motors are spinning forward and that the speed sensor readings are positive.
   3. Try sending different controls and observe the speed sensor readings. For example, the command `c-128,-128` will spin all motors backward at _slow speed_ (50% PWM). The command `c255,-255` will spin the left motors forward and the right motors backward at _fast speed_ (100% PWM). The command `c-192,192` will spin the left motors backward and the right motors forward at _normal speed_ (75% PWM).
4. Stop the motors by sending the command `c0,0` or by holding your hand in front of the ultrasonic sensor
5. If you have the indicator LEDs installed, send the command `i1,0` and observe the left indicator light flashing. The send the command `i0,1` and observe the right indicator light flashing. Finally, turn the indicator off by sending the command `i0,0`.

### No Phone Mode

Before testing the car with a smartphone that has the OpenBot application installed, you can also test the car without a phone first. Simply set the option `NO_PHONE_MODE` to `1`. The car will now drive at _normal_speed_ (75% PWM) and slow down as it detects obstacles with the ultrasonic sensor. Once it gets close to the `TURN_THRESHOLD` (default: 50cm), it will start turning in a random direction and turn on the LED on that side. If the estimated free space in front of the car falls below the `TURN_THRESHOLD`, it will slowly go backwards and both LEDs will turn on. Note that both the car and the Arduino need to be powered. The Arduino can be powered by connecting the 5V pin to the 5V output of the L298N motor driver, or by connecting the USB cable to a power source (e.g. phone).

Before running the car, we recommend to remove the tires, connect the Arduino to a computer and observe the serial monitor like in the section [Testing](#testing). The output on the serial monitor is a bit easier to parse (same as OLED) and shows the battery voltage, the rpm for the left and right motors and the estimated free space in front of the car. You can move a large object back and forth in front of ultrasonic sensor and observe the speed of the motors changing.

:warning: WARNING: If you do not have an ultrasonic sensor installed or if it is disabled, the car will just drive forward at _normal_speed_ (75% PWM) and will eventually collide. Even with the sensor installed, the car may collide occasionally due to noisy readings.

## Using other MCUs (requirements)

You can use any other MCU with the following features:

- 1x USB-to-TTL Serial (communication with the smartphone)
- 4x PWM output (control the motors)
- 1x analog pin for battery monitoring
- 2x digital pin for the speed sensors
- 1x digital pin for the ultrasonic sensor (optional)
- 2x digital pin for the indicator LEDs (optional)

## Next

Compile and run the [Android App](../android/README.md)
