package org.openbot.utils;

import android.content.Context;
import org.openbot.R;

public class ControlModeMapping {

    private static final String TAG = "ControlModeMapping";

    public static Enums.ControlMode getControlMode(Context context, String localizedTitle) {
        // Match localized strings to the corresponding enums
        if (localizedTitle.equals(context.getString(R.string.control_mode_gamepad))) {
            return Enums.ControlMode.GAMEPAD;
        } else if (localizedTitle.equals(context.getString(R.string.control_mode_phone))) {
            return Enums.ControlMode.PHONE;
        } else if (localizedTitle.equals(context.getString(R.string.control_mode_webserver))) {
            return Enums.ControlMode.WEBSERVER;
        } else if (localizedTitle.equals(context.getString(R.string.control_mode_manette))) {
            return Enums.ControlMode.MANETTE;
        }

        // Fallback case with logging
        android.util.Log.e("ControlModeMapping", "Unhandled ControlMode: " + localizedTitle);
        return Enums.ControlMode.GAMEPAD; // Provide a safe default
    }
}
