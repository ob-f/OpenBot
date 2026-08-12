package org.openbot.app.robot.main;

import static org.openbot.app.robot.utils.Constants.PERMISSION_AUDIO;
import static org.openbot.app.robot.utils.Constants.PERMISSION_BLUETOOTH_CONNECT;
import static org.openbot.app.robot.utils.Constants.PERMISSION_BLUETOOTH_SCAN;
import static org.openbot.app.robot.utils.Constants.PERMISSION_CAMERA;
import static org.openbot.app.robot.utils.Constants.PERMISSION_LOCATION;
import static org.openbot.app.robot.utils.Constants.PERMISSION_STORAGE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import java.util.Objects;
import org.openbot.app.robot.R;
import org.openbot.app.robot.utils.Constants;
import org.openbot.app.robot.utils.LanguageOption;
import org.openbot.app.robot.utils.LocaleUtils;
import org.openbot.app.robot.utils.PermissionUtils;

public class SettingsFragment extends PreferenceFragmentCompat {
  private MainViewModel mViewModel;
  private SwitchPreferenceCompat camera;
  private SwitchPreferenceCompat storage;
  private SwitchPreferenceCompat location;
  private SwitchPreferenceCompat mic;
  private SwitchPreferenceCompat nearby;
  private Preference language;
  private final ActivityResultLauncher<String[]> requestPermissionLauncher =
      registerForActivityResult(
          new ActivityResultContracts.RequestMultiplePermissions(),
          result ->
              result.forEach(
                  (permission, granted) -> {
                    switch (permission) {
                      case PERMISSION_CAMERA:
                        if (granted) camera.setChecked(true);
                        else {
                          camera.setChecked(false);
                          PermissionUtils.showCameraPermissionSettingsToast(requireActivity());
                        }
                        break;
                      case PERMISSION_STORAGE:
                        if (granted) storage.setChecked(true);
                        else {
                          storage.setChecked(false);
                          PermissionUtils.showStoragePermissionSettingsToast(requireActivity());
                        }
                        break;
                      case PERMISSION_LOCATION:
                        if (granted) location.setChecked(true);
                        else {
                          location.setChecked(false);
                          PermissionUtils.showLocationPermissionSettingsToast(requireActivity());
                        }
                        break;
                      case PERMISSION_AUDIO:
                        if (granted) mic.setChecked(true);
                        else {
                          mic.setChecked(false);
                          PermissionUtils.showAudioPermissionSettingsToast(requireActivity());
                        }
                        break;
                      case PERMISSION_BLUETOOTH_SCAN:
                      case PERMISSION_BLUETOOTH_CONNECT:
                        if (PermissionUtils.hasNearbyPermission(requireActivity())) {
                          nearby.setChecked(true);
                        } else {
                          nearby.setChecked(false);
                          PermissionUtils.showNearbyPermissionSettingsToast(requireActivity());
                        }
                        break;
                    }
                  }));

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.root_preferences, rootKey);

    mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

    camera = findPreference("camera");
    if (camera != null) {
      camera.setChecked(PermissionUtils.hasCameraPermission(requireActivity()));
      camera.setOnPreferenceChangeListener(
          (preference, newValue) -> {
            if (camera.isChecked())
              PermissionUtils.startInstalledAppDetailsActivity(requireActivity());
            else {
              if (PermissionUtils.shouldAskForPermission(
                  requireActivity(), Constants.PERMISSION_CAMERA)) {
                PermissionUtils.markedPermissionAsAsked(
                    requireActivity(), Constants.PERMISSION_CAMERA);
                requestPermissionLauncher.launch(new String[] {Constants.PERMISSION_CAMERA});
              } else {
                PermissionUtils.startInstalledAppDetailsActivity(requireActivity());
              }
            }

            return false;
          });
    }

    storage = findPreference("storage");
    if (storage != null) {
      storage.setChecked(PermissionUtils.hasStoragePermission(requireActivity()));
      storage.setOnPreferenceChangeListener(
          (preference, newValue) -> {
            if (storage.isChecked())
              PermissionUtils.startInstalledAppDetailsActivity(requireActivity());
            else {
              if (PermissionUtils.shouldAskForPermission(
                  requireActivity(), Constants.PERMISSION_STORAGE)) {
                PermissionUtils.markedPermissionAsAsked(
                    requireActivity(), Constants.PERMISSION_STORAGE);
                requestPermissionLauncher.launch(new String[] {Constants.PERMISSION_STORAGE});
              } else PermissionUtils.startInstalledAppDetailsActivity(requireActivity());
            }

            return false;
          });
    }

    location = findPreference("location");
    if (location != null) {
      location.setChecked(PermissionUtils.hasLocationPermission(requireActivity()));
      location.setOnPreferenceChangeListener(
          (preference, newValue) -> {
            if (location.isChecked())
              PermissionUtils.startInstalledAppDetailsActivity(requireActivity());
            else {
              if (PermissionUtils.shouldAskForPermission(requireActivity(), PERMISSION_LOCATION)) {
                PermissionUtils.markedPermissionAsAsked(requireActivity(), PERMISSION_LOCATION);
                requestPermissionLauncher.launch(new String[] {PERMISSION_LOCATION});
              } else PermissionUtils.startInstalledAppDetailsActivity(requireActivity());
            }

            return false;
          });
    }

    mic = findPreference("mic");
    if (mic != null) {
      mic.setChecked(PermissionUtils.hasAudioPermission(requireActivity()));
      mic.setOnPreferenceChangeListener(
          (preference, newValue) -> {
            if (mic.isChecked())
              PermissionUtils.startInstalledAppDetailsActivity(requireActivity());
            else {
              if (PermissionUtils.shouldAskForPermission(
                  requireActivity(), Constants.PERMISSION_AUDIO)) {
                PermissionUtils.markedPermissionAsAsked(
                    requireActivity(), Constants.PERMISSION_AUDIO);
                requestPermissionLauncher.launch(new String[] {Constants.PERMISSION_AUDIO});
              } else PermissionUtils.startInstalledAppDetailsActivity(requireActivity());
            }
            return false;
          });
    }

    nearby = findPreference("nearby");
    if (nearby != null) {
      nearby.setChecked(PermissionUtils.hasNearbyPermission(requireActivity()));
      nearby.setOnPreferenceChangeListener(
          (preference, newValue) -> {
            if (nearby.isChecked())
              PermissionUtils.startInstalledAppDetailsActivity(requireActivity());
            else {
              if (PermissionUtils.shouldAskForPermission(requireActivity(), PERMISSION_BLUETOOTH_SCAN)
                  || PermissionUtils.shouldAskForPermission(
                      requireActivity(), PERMISSION_BLUETOOTH_CONNECT)) {
                PermissionUtils.markedPermissionAsAsked(requireActivity(), PERMISSION_BLUETOOTH_SCAN);
                PermissionUtils.markedPermissionAsAsked(
                    requireActivity(), PERMISSION_BLUETOOTH_CONNECT);
                requestPermissionLauncher.launch(
                    new String[] {PERMISSION_BLUETOOTH_SCAN, PERMISSION_BLUETOOTH_CONNECT});
              } else PermissionUtils.startInstalledAppDetailsActivity(requireActivity());
            }
            return false;
          });
    }

    ListPreference streamMode = findPreference("video_server");

    if (streamMode != null)
      streamMode.setOnPreferenceChangeListener(
          (preference, newValue) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.stream_change_body);
            builder.setPositiveButton(
                "Yes",
                (dialog, id) -> {
                  streamMode.setValue(newValue.toString());
                  restartApp();
                });
            builder.setNegativeButton(
                "Cancel", (dialog, id) -> streamMode.setValue(streamMode.getEntry().toString()));
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
          });

    ListPreference connectivityMode = findPreference("connection_type");

    if (connectivityMode != null)
      connectivityMode.setOnPreferenceChangeListener(
          (preference, newValue) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.stream_change_body);
            builder.setPositiveButton(
                "Yes",
                (dialog, id) -> {
                  connectivityMode.setValue(newValue.toString());
                  restartApp();
                });
            builder.setNegativeButton(
                "Cancel",
                (dialog, id) -> connectivityMode.setValue(connectivityMode.getEntry().toString()));
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
          });

    language = findPreference("app_language");
    if (language != null) {
      updateLanguageIcon();
      language.setOnPreferenceClickListener(
          preference -> {
            new LanguagePickerDialogFragment().show(getParentFragmentManager(), "language_picker");
            return true;
          });
    }
  }

  private void updateLanguageIcon() {
    if (language == null) return;
    String currentTag = LocaleUtils.getCurrentTag();
    for (LanguageOption option : LocaleUtils.getLanguageOptions(requireContext())) {
      if (Objects.equals(option.getTag(), currentTag)) {
        language.setIcon(option.getFlagResId());
        break;
      }
    }
  }

  private void restartApp() {
    new Handler()
        .postDelayed(
            () -> {
              final PackageManager pm = requireActivity().getPackageManager();
              final Intent intent =
                  pm.getLaunchIntentForPackage(requireActivity().getPackageName());
              requireActivity().finishAffinity(); // Finishes all activities.
              requireActivity().startActivity(intent); // Start the launch activity
              System.exit(0); // System finishes and automatically relaunches us.
            },
            100);
  }

  @Override
  public void onResume() {
    super.onResume();
    camera.setChecked(PermissionUtils.hasCameraPermission(requireActivity()));
    storage.setChecked(PermissionUtils.hasStoragePermission(requireActivity()));
    location.setChecked(PermissionUtils.hasLocationPermission(requireActivity()));
    mic.setChecked(PermissionUtils.hasAudioPermission(requireActivity()));
    nearby.setChecked(PermissionUtils.hasNearbyPermission(requireActivity()));
    updateLanguageIcon();
  }
}
