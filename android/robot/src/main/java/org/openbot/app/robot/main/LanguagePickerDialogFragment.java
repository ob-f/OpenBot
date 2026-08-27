package org.openbot.app.robot.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.jetbrains.annotations.NotNull;
import org.openbot.app.robot.R;
import org.openbot.app.robot.databinding.DialogLanguagePickerBinding;
import org.openbot.app.robot.utils.LocaleUtils;

public class LanguagePickerDialogFragment extends DialogFragment {

  private DialogLanguagePickerBinding binding;
  private String pendingTag;
  private boolean tagPending = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
  }

  @Override
  public View onCreateView(
      @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = DialogLanguagePickerBinding.inflate(inflater, container, false);

    binding.languageList.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.languageList.setAdapter(
        new LanguageAdapter(
            LocaleUtils.getLanguageOptions(requireContext()),
            LocaleUtils.getCurrentTag(),
            option -> {
              // Let the dialog window fully close before starting the loading screen,
              // otherwise their transitions race each other.
              pendingTag = option.getTag();
              tagPending = true;
              dismiss();
            }));

    binding.dismiss.setOnClickListener(v -> dismiss());
    return binding.getRoot();
  }

  @Override
  public void onDismiss(@NonNull DialogInterface dialog) {
    super.onDismiss(dialog);
    if (tagPending) {
      tagPending = false;
      Intent intent = new Intent(requireActivity(), LanguageApplyingActivity.class);
      intent.putExtra(LanguageApplyingActivity.EXTRA_LANGUAGE_TAG, pendingTag);
      requireActivity().startActivity(intent);
      requireActivity()
          .overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
