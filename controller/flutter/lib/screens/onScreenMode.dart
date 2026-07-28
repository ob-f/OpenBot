import 'package:flutter/material.dart';
import 'package:flutter_webrtc/flutter_webrtc.dart';
import 'package:openbot_controller/screens/driveCommandReducer.dart';
import 'package:openbot_controller/utils/forwardSpeed.dart';

import 'component/onScreenIcon.dart';

class OnScreenMode extends StatefulWidget {
  final dynamic updateMirrorView;
  final bool indicatorLeft;
  final bool indicatorRight;
  final RTCPeerConnection? peerConnection;
  final String fragmentType;

  const OnScreenMode(this.updateMirrorView, this.indicatorLeft,
      this.indicatorRight, this.peerConnection, this.fragmentType,
      {super.key});

  @override
  State<StatefulWidget> createState() {
    return OnScreenModeState();
  }
}

class OnScreenModeState extends State<OnScreenMode> {
  double sliderValueLeft = 0;
  double sliderValueRight = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Container(
            margin: const EdgeInsets.only(left: 50),
            child: Theme(
                data: Theme.of(context).copyWith(
                  sliderTheme: SliderThemeData(
                    thumbShape: SquareSliderComponentShape(),
                    trackShape: const MyRoundedRectSliderTrackShape(),
                  ),
                ),
                child: RotatedBox(
                  quarterTurns: -1,
                  child: Slider(
                    value: sliderValueLeft,
                    onChanged: (value) => {
                      setState(() => sliderValueLeft = value.toPrecision(2)),
                      DriveCommandReducer.filter(
                          sliderValueRight, sliderValueLeft)
                    },
                    onChangeEnd: (value) => {
                      setState(() => sliderValueLeft = 0),
                      DriveCommandReducer.filter(
                          sliderValueRight, sliderValueLeft)
                    },
                    min: -1,
                    max: 1,
                    activeColor: Colors.white,
                    inactiveColor: const Color(0xFF292929),
                  ),
                )),
          ),
          Container(
            alignment: AlignmentDirectional.bottomEnd,
            margin: const EdgeInsets.only(bottom: 20),
            child: OnScreenIcon(widget.updateMirrorView, widget.indicatorLeft,
                widget.indicatorRight, widget.peerConnection,widget.fragmentType),
          ),
          Container(
              margin: const EdgeInsets.only(right: 50),
              child: Theme(
                  data: Theme.of(context).copyWith(
                    sliderTheme: SliderThemeData(
                      thumbShape: SquareSliderComponentShape(),
                      trackShape: const MyRoundedRectSliderTrackShape(),
                    ),
                  ),
                  child: RotatedBox(
                    quarterTurns: -1,
                    child: Slider(
                      value: sliderValueRight,
                      onChanged: (value) => {
                        setState(() => sliderValueRight = value.toPrecision(2)),
                        DriveCommandReducer.filter(
                            sliderValueRight, sliderValueLeft)
                      },
                      onChangeEnd: (value) => {
                        setState(() => sliderValueRight = 0),
                        DriveCommandReducer.filter(
                            sliderValueRight, sliderValueLeft)
                      },
                      min: -1,
                      max: 1,
                      activeColor: Colors.white,
                      inactiveColor: const Color(0xFF292929),
                    ),
                  ))),
        ],
      ),
    );
  }
}

class SquareSliderComponentShape extends SliderComponentShape {
  @override
  Size getPreferredSize(bool isEnabled, bool isDiscrete) {
    return const Size(20, 50);
  }

  @override
  void paint(PaintingContext context, Offset center,
      {required Animation<double> activationAnimation,
        required Animation<double> enableAnimation,
        required bool isDiscrete,
        required TextPainter labelPainter,
        required RenderBox parentBox,
        required SliderThemeData sliderTheme,
        required TextDirection textDirection,
        required double value,
        required double textScaleFactor,
        required Size sizeWithOverflow}) {
    final Canvas canvas = context.canvas;
    canvas.drawRRect(
      RRect.fromRectAndRadius(
        Rect.fromCenter(center: center, width: 10, height: 55),
        const Radius.circular(2),
      ),
      Paint()..color = const Color(0xFF0071c5),
    );
  }
}

class MyRoundedRectSliderTrackShape extends SliderTrackShape
    with BaseSliderTrackShape {
  const MyRoundedRectSliderTrackShape();

  @override
  void paint(
      PaintingContext context,
      Offset offset, {
        required RenderBox parentBox,
        required SliderThemeData sliderTheme,
        required Animation<double> enableAnimation,
        required TextDirection textDirection,
        required Offset thumbCenter,
        Offset? secondaryOffset,
        bool isDiscrete = false,
        bool isEnabled = false,
        double additionalTrackHeight = 30,
      }) {
    if (sliderTheme.trackHeight == null) {
      return;
    }

    final ColorTween activeTrackColorTween = ColorTween(
        begin: sliderTheme.disabledActiveTrackColor,
        end: sliderTheme.activeTrackColor);
    final ColorTween inactiveTrackColorTween = ColorTween(
        begin: sliderTheme.disabledInactiveTrackColor,
        end: sliderTheme.inactiveTrackColor);
    final Paint activePaint = Paint()
      ..color = activeTrackColorTween.evaluate(enableAnimation)!;
    final Paint inactivePaint = Paint()
      ..color = inactiveTrackColorTween.evaluate(enableAnimation)!;
    final Paint leftTrackPaint;
    final Paint rightTrackPaint;
    switch (textDirection) {
      case TextDirection.ltr:
        leftTrackPaint = activePaint;
        rightTrackPaint = inactivePaint;
        break;
      case TextDirection.rtl:
        leftTrackPaint = inactivePaint;
        rightTrackPaint = activePaint;
        break;
    }

    final Rect trackRect = getPreferredRect(
      parentBox: parentBox,
      offset: offset,
      sliderTheme: sliderTheme,
      isEnabled: isEnabled,
      isDiscrete: isDiscrete,
    );
    const Radius activeTrackRadius = Radius.circular(5);

    context.canvas.drawRRect(
      RRect.fromLTRBAndCorners(
        trackRect.left,
        trackRect.top - (additionalTrackHeight / 2),
        thumbCenter.dx,
        trackRect.bottom + (additionalTrackHeight / 2),
        topLeft: activeTrackRadius,
        bottomLeft: activeTrackRadius,
      ),
      leftTrackPaint,
    );
    context.canvas.drawRRect(
      RRect.fromLTRBAndCorners(
        thumbCenter.dx,
        trackRect.top - (additionalTrackHeight / 2),
        trackRect.right,
        trackRect.bottom + (additionalTrackHeight / 2),
        topRight: activeTrackRadius,
        bottomRight: activeTrackRadius,
      ),
      rightTrackPaint,
    );
  }
}