package org.openbot.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.jetbrains.annotations.NotNull;
import org.openbot.R;
import org.openbot.databinding.DialogLanguagePickerBinding;
import org.openbot.utils.LocaleUtils;

public class LanguagePickerDialogFragment extends DialogFragment {

  private DialogLanguagePickerBinding binding;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
  }

  @Override
  public View onCreateView(
      @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = DialogLanguagePickerBinding.inflate(LayoutInflater.from(getContext()));

    binding.languageList.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.languageList.setAdapter(
        new LanguageAdapter(
            LocaleUtils.getLanguageOptions(requireContext()),
            LocaleUtils.getCurrentTag(),
            option -> {
              LocaleUtils.applyLocale(option.getTag());
              dismiss();
            }));

    binding.dismiss.setOnClickListener(v -> dismiss());
    return binding.getRoot();
  }
}
