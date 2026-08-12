package org.openbot.app.robot.pointGoalNavigation;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.openbot.app.robot.R;
import org.openbot.app.robot.databinding.SetGoalDialogViewBinding;

public class SetGoalDialogFragment extends DialogFragment {

  public static final String TAG = SetGoalDialogFragment.class.getName();
  private AlertDialog dialog;
  private SetGoalDialogViewBinding binding;

  public static SetGoalDialogFragment newInstance() {
    SetGoalDialogFragment f = new SetGoalDialogFragment();
    return f;
  }

  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    LayoutInflater inflater = requireActivity().getLayoutInflater();

    binding = SetGoalDialogViewBinding.inflate(inflater, null, false);

    MaterialAlertDialogBuilder builder =
        new MaterialAlertDialogBuilder(getActivity())
            .setTitle(R.string.set_goal_title)
            .setMessage(R.string.set_goal_message)
            .setView(binding.getRoot())
            .setNeutralButton(R.string.cancel, (dialogInterface, i) -> setFragmentResult(false))
            .setPositiveButton(R.string.start, (dialogInterface, i) -> setFragmentResult(true));

    dialog = builder.create();

    return dialog;
  }

  private void setFragmentResult(boolean start) {
    Bundle result = new Bundle();
    result.putBoolean("start", start);
    result.putFloat("forward", Float.parseFloat(binding.forward.getText().toString()));
    result.putFloat("left", Float.parseFloat(binding.left.getText().toString()));
    getParentFragmentManager().setFragmentResult(TAG, result);
  }
}
