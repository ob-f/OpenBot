package org.openbot.utils;

import android.content.Context;
import org.openbot.R;

public class DriveModeMapping {

    private static final String TAG = "DriveModeMapping";

    public static Enums.DriveMode getDriveMode(Context context, String localizedTitle) {
        // Match localized strings to the corresponding enums
        if (localizedTitle.equals(context.getString(R.string.drive_mode_gamepad))) {
            return Enums.DriveMode.GAME;
        } else if (localizedTitle.equals(context.getString(R.string.drive_mode_joystick))) {
            return Enums.DriveMode.JOYSTICK;
        } else if (localizedTitle.equals(context.getString(R.string.drive_mode_dual))) {
            return Enums.DriveMode.DUAL;
        }

        // Fallback case with logging
        android.util.Log.e("DriveModeMapping", "Unhandled DriveMode: " + localizedTitle);
        return Enums.DriveMode.GAME; // Provide a safe default
    }
}

