import 'package:flutter/material.dart';
import 'package:flutter_webrtc/flutter_webrtc.dart';
import 'package:nsd/nsd.dart';
import 'package:openbot_controller/screens/tiltingPhoneMode.dart';

import 'onScreenMode.dart';

class ControlSelector extends StatefulWidget {
  final dynamic updateMirrorView;
  final bool indicatorLeft;
  final bool indicatorRight;
  final List<Service> networkServices;
  final RTCPeerConnection? peerConnection;
  final bool isTiltingPhoneMode;
  final bool isScreenMode;
  final String fragmentType;

  const ControlSelector(
      this.updateMirrorView,
      this.indicatorLeft,
      this.indicatorRight,
      this.networkServices,
      this.peerConnection,
      this.isTiltingPhoneMode,
      this.isScreenMode,
      this.fragmentType,
      {super.key});

  @override
  State<StatefulWidget> createState() {
    return ControlSelectorState();
  }
}

class ControlSelectorState extends State<ControlSelector> {
  // Initial Selected Value
  String dropDownValue = 'No server';
  late List<DropdownMenuItem<String>> items = [];

  @override
  void initState() {
    super.initState();
    // items.clear();
  }

  @override
  Widget build(BuildContext context) {
    if (widget.isTiltingPhoneMode) {
      return GestureDetector(
          child: TiltingPhoneMode(fragmentType: widget.fragmentType));
    } else if (widget.isScreenMode) {
      return GestureDetector(
        child: OnScreenMode(widget.updateMirrorView, widget.indicatorLeft,
            widget.indicatorRight, widget.peerConnection, widget.fragmentType),
      );
    } else {
      return GestureDetector(
        child: OnScreenMode(widget.updateMirrorView, widget.indicatorLeft,
            widget.indicatorRight, widget.peerConnection, widget.fragmentType),
      );
    }
  }
}