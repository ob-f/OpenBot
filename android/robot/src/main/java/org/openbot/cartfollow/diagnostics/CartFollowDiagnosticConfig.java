package org.openbot.cartfollow.diagnostics;

public class CartFollowDiagnosticConfig {
  public long frameLogIntervalMs = 200;
  public long cropIntervalMs = 1000;
  public long overlayIntervalMs = 1000;
  public boolean saveCrops = true;
  public boolean saveOverlays = true;
  public float paddingRatio = 0.08f;
  public int jpegQuality = 90;
}
