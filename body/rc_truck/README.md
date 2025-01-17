# OpenBot: RC-Truck Body

<p align="center">
  <span>English</span> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

We have designed a robot body for widely available 1:16 scale RC-toy trucks/buggies (such as [this](https://www.amazon.de/dp/B00M3J7DJW) on Amazon).

![RC-Truck-Banner](/docs/images/rc-truck-banner.jpg)

We also have a generic [body](/body/) designed for a simple wheeled robot which relies on low-cost, readily available hobby hardware. Build instructions for the regular OpenBot can be found [here](/body/README.md). 

## Chassis

The chassis of OpenBot RC-Truck consists of two main components: (a) A 1:16 scale RC-toy truck of your choice and (b) a number of custom-designed parts which we provide and can be 3D-printed.

### 1:16 RC-toy truck

To build your own OpenBot RC-Truck, you will need a 1:16 scale RC-toy truck/buggy. We provide Amazon links to compatible RC-toy trucks for Germany ([EU](https://www.amazon.de/dp/B00M3J7DJW)), ([EU](https://www.amazon.de/dp/B088FGVYNW)), and the United States ([US](https://www.amazon.com/gp/product/B09C8XMPQ9)) with fast shipping. A number of similar 1:16 scale toy trucks can also be found on other online retailers such as ebay, Alibaba, or AliExpress, often for discounted price but with slow shipping speed. 

Irrespective of the retailer and the version of RC-toy truck you choose for your build, make sure that it is indeed a 1:16 scale RC-truck. This is important because the 3D printed parts we provide are currently designed to fit only 1:16 scale trucks with some minor adjustments (more on this later). Some examples of compatible 1:16 scale RC-toy trucks/buggies are shown below.

<p float="left">
  <a href="https://www.amazon.de/dp/B00M3J7DJW" target="_blank"> <img src="/docs/images/rc_toy_1.jpg" width="34%" /> &nbsp
  </a>
  <a href="https://www.amazon.com/gp/product/B09C8XMPQ9" target="_blank"> <img src="/docs/images/rc_toy_2.jpg" width="27%" /> &nbsp &nbsp &nbsp &nbsp
  </a>
  <a href="https://www.amazon.de/dp/B088FGVYNW" target="_blank"> <img src="/docs/images/rc_toy_3.jpg" width="27%" />
  </a>
</p>


### 3D printing

You will need to print the following parts in order to build your OpenBot RC-Truck.

1) ```main_frame``` ([STL](cad/rc_truck_body/main_frame.stl), [STEP](cad/rc_truck_body/main_frame.step))
2) ```side_cover``` \[x2\] ([STL](cad/rc_truck_body/side_cover.stl), [STEP](cad/rc_truck_body/side_cover.step))
3) ```phone_mount_bottom``` ([STL](../phone_mount/phone_mount_bottom.stl), [STEP](../phone_mount/phone_mount_bottom.step))
4) ```phone_mount_top``` ([STL](../phone_mount/phone_mount_top.stl), [STEP](../phone_mount/phone_mount_top.step))

Notice that \[xN\] indicates the number of copies (i.e., N) you need to print of a particular part (wherever applicable).

Following parts are optional (but recommended) to make your OpenBot RC-Truck more compact and aesthetically pleasing.

5) ```camera_elevator``` ([STL](cad/rc_truck_body/camera_elevator.stl), [STEP](cad/rc_truck_body/camera_elevator.step))
6) ```electronics_cover``` \[x2\] ([STL](cad/rc_truck_body/electronics_cover.stl), [STEP](cad/rc_truck_body/electronics_cover.step))
7) ```spacer``` \[x4\] ([STL](cad/rc_truck_body/spacer.stl), [STEP](cad/rc_truck_body/spacer.step))
8) ```front_light_spacer``` \[x2\] ([STL](cad/rc_truck_body/front_light_spacer.stl), [STEP](cad/rc_truck_body/front_light_spacer.step))

For all the above parts, your build plate needs to be at least 260mmx220mm, which is the print size of the ```main_frame```.

Since a lot of common 3D printers have a smaller build volume (usually 220mmx220mm), there are two more options that can work. 
First option is to print the ```main_frame``` at 45 degrees with additional support material. 
Second option requires modifying the original ```main_frame``` part. We recommend using [Autodesk Fusion 360](https://www.autodesk.com/products/fusion-360/overview) for such CAD modifications (Fusion 360 has a free 1-year academic license available). 
For this option, we make its [STEP](/body/cad/rc_truck_body/main_frame.step) file available, which you can cut into two/three smaller parts. 
The resulting sub-parts will then fit on a standard (i.e., 220mmx220mm) build plate and can be joined together after printing. 
In future, we may also release such a modular version of the ```main_frame``` here. All other parts require a minimum build plate of 220mmx60mm.

On an Ultimaker S5, we achieved good results with the following settings:

- layer height: 0.2mm
- wall thickness: 1.5mm
- infill density: 20%
- infill pattern: grid
- print speed 80 mm/s
- no support

We were able to print the chassis with PLA, CPE and ABS. In our experience the print was not affected very much by the print settings. However, if you have the patience, printing slower and with smaller layer height will improve the print. Also adding a support structure can improve the print, but it will require additional work to remove afterwards.

Before you proceed with the build, you may need to clean the 3D print. However, using the above settings, we did not need any filing or cleaning during our build process. If possible, we recommend using a combination of two different colors (for example green/black or red/black) for printing different parts of the same OpenBot RC-Truck as shown below. 

**Tip:** Click on the images to open them in full resolution in a new tab.

<p float="left">
  <img src="/docs/images/3d_print_rc_1.png" width="32%" />
  <img src="/docs/images/3d_print_rc_2.png" width="32%" /> 
  <img src="/docs/images/3d_print_rc_3.png" width="32%" />
</p>


## Assembly

While it is possible to build your OpenBot RC-Truck with a DIY approach similar to the regular OpenBot (see DIY build components and instructions for OpenBot [here](/body/README.md)), we recommend using the OpenBot [custom PCB](/body/pcb) for building and assembling the OpenBot RC-Truck. This option is recommended if you desire a cleaner build or want to build multiple OpenBot RC-Trucks. An additional advantage of using our [custom PCB](/body/pcb) is that you can use the same components to build and switch between different OpenBot bodies.

### Bill of materials

OpenBot RC-Truck mainly relies on readily available hobby electronics. We provide Amazon links for Germany (EU) and the United States (US) with fast shipping. If you have the patience to wait a bit longer, you can also get the components a lot cheaper from AliExpress (AE). You will need the following components.

#### Required components

- 1x RC-toy truck/buggy ([EU](https://www.amazon.de/dp/B00M3J7DJW), [EU](https://www.amazon.de/dp/B088FGVYNW), [US](https://www.amazon.com/gp/product/B09C8XMPQ9))
- 1x Arduino Nano ([EU](https://www.amazon.de/dp/B01MS7DUEM), [US](https://www.amazon.com/dp/B00NLAMS9C), [AE](https://www.aliexpress.com/item/32866959979.html))
- 1x OpenBot [Custom PCB](/body/pcb)
- 1x USB OTG cable ([EU](https://www.amazon.de/gp/product/B075M4CQHZ) ,[US](https://www.amazon.com/dp/B07LBHKTMM), [AE](https://www.aliexpress.com/item/10000330515850.html))
- 1x spring or rubber band ([EU](https://www.amazon.de/gp/product/B01N30EAZO/), [US](https://www.amazon.com/dp/B008RFVWU2), [AE](https://www.aliexpress.com/item/33043769059.html))
- 6x M3x25 screw ([EU](https://www.amazon.de/dp/B07KFL3SSV), [US](https://www.amazon.com/dp/B07WJL3P3X), [AE](https://www.aliexpress.com/item/4000173341865.html))
- 6x M3 nut ([EU](https://www.amazon.de/dp/B07JMF3KMD), [US](https://www.amazon.com/dp/B071NLDW56), [AE](https://www.aliexpress.com/item/32977174437.html))
- Dupont cables ([EU](https://www.amazon.de/dp/B07KYHBVR7), [US](https://www.amazon.com/dp/B07GD2BWPY), [AE](https://www.aliexpress.com/item/4000766001685.html))

#### Optional components

- 1x Ultrasonic Sensor ([EU](https://www.amazon.de/dp/B00LSJWRXU), [US](https://www.amazon.com/dp/B0852V181G/), [AE](https://www.aliexpress.com/item/32713522570.html))
- 2x On/Off Switch ([EU](https://www.amazon.de/dp/B07QB22J62), [US](https://www.amazon.com/dp/B01N2U8PK0), [AE](https://www.aliexpress.com/item/1000005699023.html))
- 4x Orange LED 5mm ([EU](https://www.amazon.de/gp/product/B01NCL0UTQ), [US](https://www.amazon.com/dp/B077XD7MVB), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 4x Red LED 5mm ([EU](https://www.amazon.de/dp/B083HN3CLY), [US](https://www.amazon.com/dp/B077X95F7C), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 2x White LED lamps ([EU](https://www.amazon.de/-/en/gp/product/B06XTQSZDX), [US](https://www.amazon.com/gp/product/B01N2UPAD8), [AE](https://de.aliexpress.com/item/1005002991235830.html))
- Variable Resistor for LEDs ([EU](https://www.amazon.de/gp/product/B081TXJJGV), [US](https://www.amazon.com/dp/B0711MB4TL), [AE](https://de.aliexpress.com/item/1005003610664176.html))


### Build instructions

**Tip:** Click on the images to open them in full resolution in a new tab.

1. Disassemble the RC-toy truck. Remove its top cover and unscrew the four mouting pins from the base as shown in the figures below. Keep all four mounting pins and their respective screws safe, since you will be using them to mount the ```main_frame``` onto the RC-Truck body after all the wiring is done. All compatible RC-toy trucks come with two motors: one for throttle and the other for steering, a speed controller (with a built-in 5-7V UBEC) for the throttle motor, and a 2S 7.4V LiPo battery pack. Unmount and remove the battery pack from the base of the truck and recharge it with the charger that came with the truck. Expose/losen the wire connectors for both motors as well as the UBEC output from the speed controller. In our case, the UBEC output was 6V.
    <p float="left">
      <img src="/docs/images/rc_truck_disassembly_1.JPG" width="32%" />
      <img src="/docs/images/rc_truck_disassembly_2.JPG" width="32%" /> 
      <img src="/docs/images/rc_truck_disassembly_3.JPG" width="32%" />
    </p>
2. Notice that the two dimensions d1 amd d2 (as shown below) on the ```main_frame``` are dependent on the model of the RC-toy truck used. We designed our ```main_frame``` part for [this](https://www.amazon.de/dp/B00M3J7DJW) RC-toy truck model. Based on what (1:16 scale) truck you use, you may need to adjust these dimensions slightly using the ```main_frame``` [STEP](/body/cad/rc_truck_body/main_frame.step) file. We recommend using [Autodesk Fusion 360](https://www.autodesk.com/products/fusion-360/overview) for such CAD modifications (Fusion 360 has a free 1-year academic license available). Also, note that the small wedge/triangle on the ```main_frame``` represents the forward direction.
    <p float="left">
      <img src="/docs/images/main-frame-dimensions.png" width="32%" />
      <img src="/docs/images/main-frame-direction.png" width="32%" />
    </p>   
3. (Optional) Install the ON/OFF switch for powering the robot. You can simply do this by cutting the positive wire that goes from speed controller to the battery and soldering the switch in-between the two split parts of this wire. Please ensure that the switch connectors are insulated via shrink tube or electric tape and the power cable is long enough so that the switch can fit through the rectangular opening on the back side of the ```main_frame``` after assembly (see the figure below).
    <p float="left">
      <img src="/docs/images/main-frame-switch.png" width="32%" />
      <img src="/docs/images/switch-power.jpg" width="32%" />
    </p>
4. (Optional) Install the ultrasonic sensor through the front grill of the ```main_frame```. You can use hot glue to keep it in place if needed. Gently push the connector into a straight position before putting it in place. This will make accessing the connector easier after assembly. Run the dupont cables from the ultrasonic connector all the way back to the rectangular opening on the back side of the ```main_frame```.
    <p float="left">
      <img src="/docs/images/install-ultrasonic-1.png" width="32%" />
      <img src="/docs/images/ultrasonic-sensor.jpg" width="32%" />
      <img src="/docs/images/install-ultrasonic-2.png" width="32%" />
    </p>
5. (Optional) Install the orange LEDs for the indicator signals both at front and back of the ```main_frame```. You can use hot glue to keep them in place if needed. For each side i.e., left and right, you need to connect the front and back LEDs in parallel. To achieve this, simply connect their positive and negative terminals together respectively. Similar to the ultrasonic sensor cable, run the postive and negative dupont cables from both left and right indicator signals all the way back to the rectangular opening on the back side of the ```main_frame``` where they will connect to their respective indicator signal pins (both +ve and -ve) on the PCB. 
    <p float="left">
      <img src="/docs/images/insert-leds-orange-1.png" width="32%" />
      <img src="/docs/images/orange-led.jpg" width="32%" />
      <img src="/docs/images/insert-leds-orange-2.png" width="32%" />
    </p>
**Tip:** To avoid cluttering and potential grounding mistakes during wiring, it is recommended to form a unified ground loop for the negative terminals of all the LEDs. This simply means running a wire underneath the ```main_frame``` which connects all the negative terminals of the LEDs. This ground loop can then be connected to the Arduino Nano ground pin using a single dupont cable, which is run to the rectangular opening on the back side of the ```main_frame```.

6. (Optional) Install the front LED lamps. You can use hot glue to keep the base in place and screw the lamp into its respective base through the front opening on each side. Connect both front LED lamps in parallel by connecting their positive and negative terminals together respectively. Since these lamps operate on 6V, you can connect them directly to the UBEC output by their positive terminals. Connect the negative terminals to the ground loop (see the tip above). The internal resistance of these LEDs is fairly high so there is no need to add any external resistance. After installing the LED lamps, insert and hot glue the two ```front_light_spacers``` on each side to lock the LEDs in place.
    <p float="left">
      <img src="/docs/images/insert-lamps-1.png" width="32%" />
      <img src="/docs/images/led-lamp-wiring.jpg" width="32%" />
      <img src="/docs/images/add_front_light_spacer.png" width="32%" />
    </p>
7. (Optional) Install the Red LEDs for rear lights. You can use hot glue to keep them in place if needed. Connect all four Red LEDs in parallel; i.e., connect their positive and negative terminals together repectively. The negative terminals will go to the ground, while the positive terminals will be collectively connected to the UBEC output via an appropriate voltage divider (see the next step for details on voltage divider construction). 
    <p float="left">
      <img src="/docs/images/insert-leds-red.png" width="32%" />
      <img src="/docs/images/red-led.jpg" width="32%" />
    </p>
8. (Optional) Install the voltage divider for rear Red LEDs. Most color LEDs (e.g. Red, Orange, Yellow etc.) operate on 2-3V and not the traditional 5V, which is the normal operating voltage of the Arduino Nano. Therefore, a voltage divider is needed in order to operate these LEDs safely. For indicator signals, we already have a built-in voltage divider in our custom PCB. So, you do not need to do anything for using the indicator signal (i.e., orange) LEDs. However, if you choose to add rear light i.e., Red LEDs as well, then an external voltage divider is required for them. We recommend using a variable resistor of 10kΩ or higher for making your voltage divider. Based on your UBEC output voltage (6V in our case), you need to set up a voltage divider with 2-3V output. This can be done by applying the UBEC output on the external ends of the resistor and by turning the screw on its top and monitoring the output voltage using a digital multimeter in between the ground and the middle terminal (see figure below). Once the output voltage of the variable resistance i.e., the voltage divider is set to the appropriate 2-3V range, lock its screw in place using some hot glue and fix its position underneath the ```main_frame``` in a convenient position.
    <p float="left">
      <img src="/docs/images/variable-resistor.jpg" width="32%" />
      <img src="/docs/images/voltage-divider-animation.png" width="32%" />
    </p>
9. (Optional) You can also use a single or two separate ON/OFF switches for turning the front and rear LEDs ON and OFF. Please follow instructions in Step 3 to install a switch (or multiple switches) for this purpose.
10. Now you are almost done with the wiring of the robot. At this point, take some time to ensure that all wires and connections underneath the ```main_frame``` are correct and well insulated using either shrink tube or electric tape. Use hot glue to keep any loose wires in place so they do not come in contact with the wheels or any moving parts of the robot after assembly. Make sure all cables from motors, speed controller UBEC, LEDs, and ultrasonic sensor can freely make it out of the rectangular opening on the back side of the ```main_frame```.
11. Mount the ```phone_mount_bottom``` to the ```main_frame``` using two M3x25 screws and nuts. Optionally, you can insert one or more ```camera_elevators``` in between if you would like to adjust the vertical height of your phone mount. If you use a ```camera_elevator``` you will need M3x35 or longer screws for mounting the phone mount onto the ```main_frame```.
    <p float="left">
      <img src="/docs/images/add_phone_mount_bottom.png" width="32%" />
      <img src="/docs/images/add_phone_mount_bottom_elevator.png" width="32%" /> 
    </p>
10. Insert the ```phone_mount_top``` and install the spring or rubber band.
    <p float="left">
      <img src="/docs/images/add_phone_mount_top.png" width="32%" />
    </p>
11. Insert the two ```side_covers``` into their respective slots.
    <p float="left">
      <img src="/docs/images/add_side_covers.png" width="32%" />
      <img src="/docs/images/add_side_covers_2.png" width="32%" />
    </p>    
12. Mount the ```main_frame``` onto the RC-Truck body using the four mounting pins and their respective screws. Make sure all cable connectors and the power switch for the robot are accessible through the rectangular opening on the back side of the ```main_frame``` for PCB connections. Pull out the battery connector from the triangular opening on the front of the ```main_frame```.
    <p float="left">
      <img src="/docs/images/add_main_frame_1.JPG" width="32%" />
      <img src="/docs/images/add_main_frame_2.png" width="32%" />
      <img src="/docs/images/add_main_frame_3.JPG" width="32%" />
    </p>
12. Mount the PCB with four M3x25 screws and nuts with four ```spacers``` in between on the back side of ```main_frame```. Mount the Arduino Nano onto the PCB and attach the USB OTG cable to the Arduino Nano's USB port.
    <p float="left">
      <img src="/docs/images/pcb_assembly.JPG" width="32%" />
    </p>
13. Connect the ultrasonic sensor cables to the connector marked "sonar" on the PCB. Make sure the +ve/-ve polarity and the data lines are correctly matched between the sensor and the PCB ports.
14. Connect the left and right indicator LED cables to their respective indicator signal connectors on the PCB. Ensure the correct polarity of +ve and -ve LED terminals.
15. Connect the UBEC output (+6V) to the Vin pin of the Arduino Nano (optional, Arduino can also be powered by phone), and the UBEC GND to the Arduino GND pin (next to Vin).
16. Connect the UBEC output (+6V) to the +ve terminals of the steering servo, the front LED lamps, and the rear Red LEDs through the voltage divider.
17. Connect the ground cable of the steering servo to the GND pin of Arduino as well.
18. Connect the PWM cable of the throttle servo (from the speed controller) to pin A0 on the Arduino Nano or PCB breakout.
19. Connect the PWM cable of the steering servo to pin A1 on the Arduino Nano or PCB breakout.
**Tip:** If you have created a unified ground loop for the LED wiring, then connect the ground loop cable to one of the Arduino GND pins as well. Arduino Nano has three GND pins available. If you have not constructed a ground loop, then make sure that all LEDs, the steering servo, sensors, the Arduino Nano, and the speed controller's UBEC share the same ground with appropriate wiring and connections.
21. Connect the battery pack at the front and keep it in place using some velcro or mounting tape. Having the battery at front makes it easily accessible for recharging. This placement also helps with balancing the robot weight when a smartphone is mounted on top.
22. Put on the front and back ```electronics_covers```. Pull out the USB OTG cable from the rear ```electronics_cover``` gap for connecting it to an android smartphone.
<p float="left">
      <img src="/docs/images/add_covers_1.png" width="32%" />
      <img src="/docs/images/add_covers_2.JPG" width="32%" />
    </p>

## Next

Flash the [Arduino Firmware](../../firmware/README.md)
