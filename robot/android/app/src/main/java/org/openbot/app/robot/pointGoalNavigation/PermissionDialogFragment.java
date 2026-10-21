package org.openbot.app.robot.pointGoalNavigation;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.openbot.app.robot.R;

public class PermissionDialogFragment extends DialogFragment {

  public static final String TAG = PermissionDialogFragment.class.getName();
  private AlertDialog dialog;

  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

    MaterialAlertDialogBuilder builder =
        new MaterialAlertDialogBuilder(getActivity())
            .setTitle("Permission")
            .setMessage(
                getResources().getString(R.string.camera_permission_denied)
                    + " "
                    + getResources().getString(R.string.permission_reason_ar_core))
            .setNegativeButton("Back", (dialogInterface, i) -> setFragmentResult("back"))
            .setNeutralButton("Retry", (dialogInterface, i) -> setFragmentResult("retry"))
            .setPositiveButton("Settings", (dialogInterface, i) -> setFragmentResult("settings"));

    dialog = builder.create();

    return dialog;
  }

  private void setFragmentResult(String choice) {
    Bundle result = new Bundle();
    result.putString("choice", choice);
    getParentFragmentManager().setFragmentResult(TAG, result);
  }
}
