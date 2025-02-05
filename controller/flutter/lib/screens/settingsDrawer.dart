import 'package:flutter/material.dart';
import 'package:nsd/nsd.dart';
import 'package:openbot_controller/globals.dart';

class SettingsDrawer extends StatefulWidget {
  final List<Service> networkServices;
  final Function(bool, bool) onSettingsChanged;

  SettingsDrawer(this.networkServices, this.onSettingsChanged, {super.key});

  @override
  _SettingsDrawerState createState() => _SettingsDrawerState();
}

class _SettingsDrawerState extends State<SettingsDrawer> {
  List<bool> isSelected = [false, false];
  String dropDownValue = 'No server';
  late List<DropdownMenuItem<String>> items = [];
  bool isNoise = false;
  bool isNetwork = false;

  // Function to generate DropdownMenuItem widgets
  List<DropdownMenuItem<String>> buildDropdownMenuItems() {
    items = [
      DropdownMenuItem(
        value: 'No server',
        child: Container(
          height: 30,
          width: 85,
          decoration: const BoxDecoration(
            borderRadius: BorderRadius.all(Radius.circular(30)),
            color: Color(0xFF0071C5),
          ),
          alignment: Alignment.center,
          child: const Text(
            'No server',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 18,
              color: Color(0xFFffffff),
            ),
          ),
        ),
      ),
    ];
    items.addAll(widget.networkServices.map((discovery) {
      return DropdownMenuItem(
        value: discovery.name,
        child: Container(
          height: 30,
          width: 85,
          decoration: const BoxDecoration(
            borderRadius: BorderRadius.all(Radius.circular(3)),
            color: Color(0xFF0071C5),
          ),
          alignment: Alignment.center,
          child: Text(
            discovery.name ?? '',
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 18,
              color: Color(0xFFffffff),
            ),
          ),
        ),
      );
    }));
    return items;
  }

  @override
  Widget build(BuildContext context) {
    return Drawer(
      backgroundColor: const Color(0xFF202020),
      child: ListView(
        padding: EdgeInsets.zero,
        children: [
          Padding(
              padding: const EdgeInsets.only(left: 30, top: 35),
              child: Row(children: [
                const Text(
                  'Controller',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.w500,
                    color: Color(0xFF0071C5),
                  ),
                ),
                const SizedBox(
                  width: 30,
                ),
                ToggleButtons(
                  borderRadius: BorderRadius.circular(10),
                  selectedBorderColor: const Color(0xFF0071C5).withOpacity(0.4),
                  disabledBorderColor: const Color(0xFF0071C5).withOpacity(0.4),
                  onPressed: (int newIndex) {
                    setState(() {
                      for (int index = 0; index < isSelected.length; index++) {
                        if (index == newIndex) {
                          isSelected[index] = true;
                          if (index == 0) {
                            setState(() {
                              widget.onSettingsChanged(true, false);
                            });
                          } else {
                            setState(() {
                              widget.onSettingsChanged(false, true);
                            });
                          }
                        } else {
                          isSelected[index] = false;
                        }
                      }
                    });
                  },
                  isSelected: isSelected,
                  children: [
                    Image.asset(
                      "images/tilting_phone_icon.png",
                      height: 33,
                      width: 33,
                    ),
                    Image.asset(
                      "images/controller_icon.png",
                      height: 33,
                      width: 33,
                    )
                  ],
                ),
              ])),
          Padding(
            padding: const EdgeInsets.only(left: 30, top: 35),
            child: Row(children: [
              const Text(
                'Server',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w500,
                  color: Color(0xFF0071C5),
                ),
              ),
              const SizedBox(
                width: 54,
              ),
              DropdownButton(
                value: dropDownValue,
                borderRadius: const BorderRadius.all(Radius.circular(3)),
                underline: Container(),
                dropdownColor: const Color(0xFF0071C5),
                style: const TextStyle(
                  fontSize: 18,
                  color: Color(0xFFffffff),
                ),
                menuMaxHeight: 150,
                items: buildDropdownMenuItems(),
                onChanged: (String? serverName) {
                  setState(() {
                    dropDownValue = serverName!;
                  });
                  if (serverName != "No server") {
                    clientSocket?.writeln("{server: $serverName}");
                  } else {
                    clientSocket?.writeln("{server: noServerFound}");
                  }
                },
              ),
            ]),
          ),
          Padding(
            padding: const EdgeInsets.only(left: 30, top: 35),
            child: Row(children: [
              const Text(
                'Noise',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w500,
                  color: Color(0xFF0071C5),
                ),
              ),
              const SizedBox(
                width: 54,
              ),

              Switch(
                  value: isNoise,
                  onChanged: (bool value) {
                    setState(() {
                      isNoise = value;
                    });
                    if (isNoise) {
                      clientSocket?.writeln("{command: NOISE}");
                    }
                  },
                activeColor: const Color(0xFF0071C5),
              )
            ]),
          ),
        ],
      ),
    );
  }
}
