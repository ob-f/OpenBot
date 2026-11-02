package org.openbot.app.robot.googleServices;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.FirebaseApp;
import org.openbot.app.robot.R;

/**
 * The repository has a dummy google-services.json, so builds from source and pull request builds
 * start without the project's Firebase configuration. Google sign-in, and with it everything that
 * needs it, only works with the real one.
 */
public final class FirebaseSetup {

  /** Project ID of the dummy google-services.json. */
  private static final String DUMMY_PROJECT_ID = "example-project";

  private FirebaseSetup() {}

  public static boolean isConfigured() {
    try {
      return !DUMMY_PROJECT_ID.equals(FirebaseApp.getInstance().getOptions().getProjectId());
    } catch (IllegalStateException e) {
      return false;
    }
  }

  /** Explains why sign-in isn't available. Returns false if sign-in shouldn't start. */
  public static boolean checkSignIn(Context context) {
    if (isConfigured()) return true;
    new AlertDialog.Builder(context)
        .setMessage(R.string.firebase_not_configured)
        .setPositiveButton(android.R.string.ok, null)
        .show();
    return false;
  }
}
