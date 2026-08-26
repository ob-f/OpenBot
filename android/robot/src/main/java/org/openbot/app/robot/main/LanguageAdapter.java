package org.openbot.app.robot.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.openbot.app.robot.databinding.ItemLanguageBinding;
import org.openbot.app.robot.utils.LanguageOption;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {

  public interface OnLanguageSelectedListener {
    void onLanguageSelected(LanguageOption option);
  }

  private final List<LanguageOption> options;
  private final OnLanguageSelectedListener listener;
  private String selectedTag;

  public LanguageAdapter(
      List<LanguageOption> options, String selectedTag, OnLanguageSelectedListener listener) {
    this.options = options;
    this.selectedTag = selectedTag;
    this.listener = listener;
  }

  @NotNull
  @Override
  public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        ItemLanguageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
    LanguageOption option = options.get(position);
    holder.binding.name.setText(option.getDisplayName());
    holder.binding.flag.setImageResource(option.getFlagResId());
    holder.binding.check.setVisibility(
        Objects.equals(option.getTag(), selectedTag) ? View.VISIBLE : View.INVISIBLE);
    holder.itemView.setOnClickListener(
        v -> {
          selectedTag = option.getTag();
          notifyDataSetChanged();
          listener.onLanguageSelected(option);
        });
  }

  @Override
  public int getItemCount() {
    return options.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    final ItemLanguageBinding binding;

    public ViewHolder(ItemLanguageBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
