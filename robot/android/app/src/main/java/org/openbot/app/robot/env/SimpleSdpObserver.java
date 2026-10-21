package org.openbot.app.robot.env;

import android.util.Log;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

class SimpleSdpObserver implements SdpObserver {

  @Override
  public void onCreateSuccess(SessionDescription sessionDescription) {}

  @Override
  public void onSetSuccess() {}

  @Override
  public void onCreateFailure(String s) {}

  @Override
  public void onSetFailure(String s) {
    Log.i(null, "Got error: " + s);
  }
}
