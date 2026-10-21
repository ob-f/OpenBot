package org.openbot.app.robot.logging;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.openbot.app.robot.R;
import org.openbot.app.robot.databinding.DialogSensorsBinding;
import org.openbot.app.robot.env.SharedPreferencesManager;
import org.openbot.app.robot.utils.Enums;

public class SensorsDialog extends DialogFragment {

  public SensorsDialog() {}

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
  }

  @SuppressLint("NotifyDataSetChanged")
  @Override
  public View onCreateView(
      @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    SharedPreferencesManager preferencesManager = new SharedPreferencesManager(requireContext());
    DialogSensorsBinding binding = DialogSensorsBinding.inflate(LayoutInflater.from(getContext()));
    SensorManager sensorManager =
        (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
    List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

    HashMap<String, Boolean> list = new LinkedHashMap<>();
    list.put(
        Enums.SensorType.VEHICLE.getSensor(),
        preferencesManager.getSensorStatus(Enums.SensorType.VEHICLE.getSensor()));

    // Every modern phone has GPS
    list.put(
        Enums.SensorType.GPS.getSensor(),
        preferencesManager.getSensorStatus(Enums.SensorType.GPS.getSensor()));

    for (Sensor sensor : sensorList) {
      for (Enums.SensorType name : Enums.SensorType.values())
        if (sensor.getStringType().toLowerCase().contains(name.getSensor().toLowerCase())) {
          list.put(name.getSensor(), preferencesManager.getSensorStatus(name.getSensor()));
          break;
        }
    }

    SensorListAdapter adapter =
        new SensorListAdapter(list, binding.selectAllCheck, preferencesManager);

    binding.listView.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.listView.setAdapter(adapter);
    binding.dismiss.setOnClickListener(v -> dismiss());

    int sensorStatusCount = 0;

    for (boolean b : new ArrayList<>(list.values()))
      if (b) sensorStatusCount++;
      else break;

    if (list.values().size() == sensorStatusCount) {
      binding.selectAllCheck.setChecked(true);
      binding.selectAllCheck.setText(getResources().getText(R.string.clearAll));
    } else {
      binding.selectAllCheck.setChecked(false);
      binding.selectAllCheck.setText(getResources().getText(R.string.selectAll));
    }

    binding.selectAllCheck.setOnClickListener(
        v -> {
          for (Map.Entry<String, Boolean> entry : list.entrySet())
            preferencesManager.setSensorStatus(binding.selectAllCheck.isChecked(), entry.getKey());

          binding.selectAllCheck.setChecked(binding.selectAllCheck.isChecked());
          binding.selectAllCheck.setText(
              binding.selectAllCheck.isChecked()
                  ? getResources().getText(R.string.clearAll)
                  : getResources().getText(R.string.selectAll));

          adapter.updateStatusValue(binding.selectAllCheck.isChecked());
          adapter.notifyDataSetChanged();
        });

    binding.delay.setText(String.valueOf(preferencesManager.getDelay()));
    binding.delay.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (!s.toString().isEmpty())
              preferencesManager.setDelay(Integer.parseInt(s.toString()));
          }
        });
    return binding.getRoot();
  }
}
