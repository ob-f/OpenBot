package org.openbot.app.robot.pointGoalNavigation;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class InfoDialogFragment extends DialogFragment {

  public static final String TAG = InfoDialogFragment.class.getName();
  private AlertDialog dialog;

  public static InfoDialogFragment newInstance(String message) {
    InfoDialogFragment f = new InfoDialogFragment();

    Bundle args = new Bundle();
    args.putString("message", message);
    f.setArguments(args);

    return f;
  }

  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    String message = getArguments().getString("message");

    MaterialAlertDialogBuilder builder =
        new MaterialAlertDialogBuilder(getActivity())
            .setTitle("Info")
            .setMessage(message)
            .setNegativeButton("Stop", (dialogInterface, i) -> setFragmentResult(false))
            .setPositiveButton("Restart", (dialogInterface, i) -> setFragmentResult(true));

    dialog = builder.create();

    return dialog;
  }

  private void setFragmentResult(Boolean restart) {
    Bundle result = new Bundle();
    result.putBoolean("restart", restart);
    getParentFragmentManager().setFragmentResult(TAG, result);
  }
}
