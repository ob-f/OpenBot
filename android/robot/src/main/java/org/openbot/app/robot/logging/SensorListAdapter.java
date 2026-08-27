package org.openbot.app.robot.logging;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.openbot.app.robot.R;
import org.openbot.app.robot.databinding.ItemSensorBinding;
import org.openbot.app.robot.env.SharedPreferencesManager;

public class SensorListAdapter extends RecyclerView.Adapter<SensorListAdapter.ViewHolder> {

  private final List<String> names;
  private List<Boolean> status;
  private final SharedPreferencesManager preferencesManager;
  private CheckBox allSelector;

  public SensorListAdapter(
      HashMap<String, Boolean> items,
      CheckBox allSelector,
      SharedPreferencesManager preferencesManager) {
    names = new ArrayList<>(items.keySet());
    status = new ArrayList<>(items.values());
    this.preferencesManager = preferencesManager;
    this.allSelector = allSelector;
  }

  @NotNull
  @Override
  public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        ItemSensorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.checkBox.setText(names.get(position));
    holder.checkBox.setChecked(status.get(position));
    holder.checkBox.setOnClickListener(
        v -> {
          status.set(position, holder.checkBox.isChecked());
          preferencesManager.setSensorStatus(holder.checkBox.isChecked(), names.get(position));
          allSelector.setChecked(getStatusChange());
          allSelector.setText(
              allSelector.isChecked()
                  ? allSelector.getContext().getResources().getString(R.string.clearAll)
                  : allSelector.getContext().getResources().getString(R.string.selectAll));
        });
  }

  private boolean getStatusChange() {
    for (boolean b : status) if (!b) return false;
    return true;
  }

  public void updateStatusValue(boolean value) {
    for (int i = 0; i < status.size(); i++) status.set(i, value);
  }

  @Override
  public int getItemCount() {
    return names.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public CheckBox checkBox;

    public ViewHolder(ItemSensorBinding binding) {
      super(binding.getRoot());
      checkBox = binding.check;
    }
  }
}
