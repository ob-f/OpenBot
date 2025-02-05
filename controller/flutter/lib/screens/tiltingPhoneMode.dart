import 'dart:async';

import 'package:flutter/material.dart';
import 'package:openbot_controller/utils/forwardSpeed.dart';
import 'package:openbot_controller/utils/phoneSensorToDualDriveConverter.dart';
import 'package:sensors_plus/sensors_plus.dart';
import 'package:openbot_controller/globals.dart';
import 'driveCommandReducer.dart';

class TiltingPhoneMode extends StatefulWidget {
  final String fragmentType;

  const TiltingPhoneMode({required this.fragmentType, super.key});

  @override
  State<StatefulWidget> createState() {
    return TiltingPhoneModeState();
  }
}

class TiltingPhoneModeState extends State<TiltingPhoneMode> {
  Timer? forwardSpeedTimer;
  Timer? backwardSpeedTimer;
  bool forward = false;
  bool reverse = false;
  late double azimuth;
  late double pitch;
  late double roll;
  var phoneAccelerometerToDualDriveConverted =
      PhoneSensorToDualDriveConverter();
  double leftSpeedValue = 0;
  double rightSpeedValue = 0;
  String typeOfFragment = "";

  @override
  void initState() {
    accelerometerEvents.listen((event) {
      azimuth = event.x;
      pitch = event.y;
      roll = event.z;
      var sliderValues =
          phoneAccelerometerToDualDriveConverted.convert(azimuth, pitch, roll);
      leftSpeedValue = sliderValues.left;
      rightSpeedValue = sliderValues.right;
    });
    super.initState();
  }

  @override
  void didUpdateWidget(covariant TiltingPhoneMode oldWidget) {
    // TODO: implement didUpdateWidget
    setState(() {
      typeOfFragment = widget.fragmentType;
    });
    super.didUpdateWidget(oldWidget);
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async {
        return true;
      },
      child: Scaffold(
        backgroundColor: Colors.transparent,
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Expanded(
                child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                GestureDetector(
                    onTapDown: (details) {
                      setState(() {
                        ForwardSpeed.reset();
                        reverse = true;
                      });
                    },
                    onTapUp: (details) {
                      setState(() {
                        ForwardSpeed.reset();
                        reverse = false;
                      });
                    },
                    onLongPressStart: (detail) {
                      setState(() {
                        reverse = true;
                        backwardSpeedTimer = Timer.periodic(
                            const Duration(milliseconds: 333), (t) {
                          double decrementSpeed = ForwardSpeed.minNegative / 3;
                          ForwardSpeed.decrementNegative(decrementSpeed);
                          DriveCommandReducer.filter(
                              rightSpeedValue, leftSpeedValue);
                        });
                      });
                    },
                    onLongPressEnd: (detail) {
                      setState(() {
                        reverse = false;
                      });
                      if (backwardSpeedTimer != null) {
                        ForwardSpeed.reset();
                        DriveCommandReducer.filter(0, 0);
                        backwardSpeedTimer!.cancel();
                      }
                    },
                    // Image tapped
                    child: Container(
                      margin: const EdgeInsets.fromLTRB(40, 0, 0, 20),
                      // padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(5),
                        color: reverse
                            ? const Color(0xFF292929)
                            : const Color(0xFF292929).withOpacity(0.3),
                      ),
                      child: Image.asset(
                        "images/reverse_icon.png",
                        height: 80,
                        width: 64,
                      ),
                    )),
                Container(
                    alignment: AlignmentDirectional.bottomEnd,
                    margin: const EdgeInsets.only(bottom: 20),
                    child: Row(children: [
                      GestureDetector(
                          onTap: () {
                            if (typeOfFragment == "DataCollection") {
                              clientSocket?.writeln("{command: LOGS}");
                            }
                          }, // Image tapped
                          child: Container(
                            padding: const EdgeInsets.all(15),
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(45),
                              color: typeOfFragment == "DataCollection"
                                  ? Colors.white.withOpacity(0.5)
                                  : Colors.grey.withOpacity(0.5),
                            ),
                            child: const Icon(
                              Icons.file_present,
                              color: Colors.blue,
                            ),
                          )),
                      const SizedBox(
                        width: 15,
                      ),
                      GestureDetector(
                          onTap: () {
                            if (typeOfFragment == "Autopilot" ||
                                typeOfFragment == "ObjectDetection") {
                              clientSocket?.writeln("{command: NETWORK}");
                            }
                          }, // Image tapped
                          child: Container(
                            padding: const EdgeInsets.all(15),
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(45),
                              color: typeOfFragment == "Autopilot" ||
                                      typeOfFragment == "ObjectDetection"
                                  ? Colors.white.withOpacity(0.5)
                                  : Colors.grey.withOpacity(0.5),
                            ),
                            child: const Icon(
                              Icons.network_check,
                              color: Colors.blue,
                            ),
                          )),
                    ])),
                GestureDetector(
                    onTapDown: (details) {
                      setState(() {
                        forward = true;
                        ForwardSpeed.reset();
                      });
                    },
                    onTapUp: (details) {
                      setState(() {
                        forward = false;
                        ForwardSpeed.reset();
                      });
                    },
                    onLongPressStart: (detail) {
                      setState(() {
                        forward = true;
                        forwardSpeedTimer = Timer.periodic(
                            const Duration(milliseconds: 200), (t) {
                          double incrementValue =
                              (ForwardSpeed.max - ForwardSpeed.value) / 5;
                          ForwardSpeed.increment(incrementValue);
                          DriveCommandReducer.filter(
                              rightSpeedValue, leftSpeedValue);
                        });
                      });
                    },
                    onLongPressEnd: (detail) {
                      setState(() {
                        forward = false;
                      });
                      if (forwardSpeedTimer != null) {
                        ForwardSpeed.reset();
                        DriveCommandReducer.filter(0, 0);
                        forwardSpeedTimer!.cancel();
                      }
                    },
                    // Image tapped
                    child: Container(
                      margin: const EdgeInsets.fromLTRB(0, 0, 40, 20),
                      // padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(5),
                        color: forward
                            ? const Color(0xFF292929)
                            : const Color(0xFF292929).withOpacity(0.3),
                      ),
                      child: Image.asset(
                        "images/forward_icon.png",
                        height: 110,
                        width: 70,
                      ),
                    )),
              ],
            ))
          ],
        ),
      ),
    );
  }
}
