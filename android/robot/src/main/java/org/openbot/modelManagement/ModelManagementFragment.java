package org.openbot.modelManagement;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.openbot.googleServices.GoogleServices;

import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.nononsenseapps.filepicker.Utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.openbot.R;
import org.openbot.databinding.FragmentModelManagementBinding;
import org.openbot.googleServices.GoogleServices;
import org.openbot.main.OnItemClickListener;
import org.openbot.projects.GoogleSignInCallback;
import org.openbot.tflite.Model;
import org.openbot.utils.Constants;
import org.openbot.utils.FileUtils;
import org.openbot.utils.PermissionUtils;

public class ModelManagementFragment extends Fragment
        implements OnItemClickListener<Model>, ModelAdapter.OnItemClickListener<Model> {


    private FragmentModelManagementBinding binding;
    public static final String ALL = "ALL";
    private ModelAdapter adapter;
    private List<Model> masterList;
    private ActivityResultLauncher<Intent> mStartForResult;
    private OnBackPressedCallback onBackPressedCallback;
    private boolean isDownloading = false;

    private GoogleServices googleServices;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleServices = new GoogleServices(requireActivity(), requireContext(), new GoogleSignInCallback() {
            @Override
            public void onSignInSuccess(FirebaseUser account) {

            }

            @Override
            public void onSignInFailed(Exception exception) {

            }

            @Override
            public void onSignOutSuccess() {

            }

            @Override
            public void onSignOutFailed(Exception exception) {

            }
        });
        mStartForResult =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {

                                Intent intent = result.getData();
                                // Handle the Intent
                                assert intent != null;
                                List<Uri> files = Utils.getSelectedFilesFromResult(intent);

                                String fileName = getFileNameFromUri(files.get(0));
                                if (FileUtils.checkFileExistence(requireActivity(), fileName)) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                                    builder.setTitle(R.string.file_available_title);
                                    builder.setMessage(R.string.file_available_body);
                                    builder.setPositiveButton(
                                            "Yes", (dialog, id) -> processModelFromStorage(files, fileName));
                                    builder.setNegativeButton(
                                            "Cancel",
                                            (dialog, id) -> {
                                                // User cancelled the dialog
                                            });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                } else {
                                    processModelFromStorage(files, fileName);
                                }
                            }
                        });

        onBackPressedCallback =
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (isDownloading) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                            builder.setTitle(R.string.model_download_title);
                            builder.setMessage(R.string.model_download_body);
                            builder.setPositiveButton(
                                    "Yes",
                                    (dialog, id) -> {
                                        onBackPressedCallback.setEnabled(false);
                                        requireActivity().onBackPressed();
                                    });
                            builder.setNegativeButton("Cancel", (dialog, id) -> {
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            onBackPressedCallback.setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    }
                };
        requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    }

    /**
     * Extracts the file name from a given Uri.
     *
     * @param uri The Uri from which to extract the file name.
     * @return The extracted file name, or null if not found.
     */
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            // If the Uri uses the content:// scheme, use a ContentResolver to get the file name
            ContentResolver contentResolver = requireActivity().getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (displayNameIndex != -1) {
                            fileName = cursor.getString(displayNameIndex);
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        } else if (uri.getScheme().equals("file")) {
            // If the Uri uses the file:// scheme, directly extract the file name
            fileName = new File(uri.getPath()).getName();
        }
        return fileName;
    }

    private void processModelFromStorage(List<Uri> files, String fileName) {
        Model item =
                new Model(
                        masterList.size() + 1,
                        Model.CLASS.AUTOPILOT,
                        Model.TYPE.CMDNAV,
                        fileName,
                        Model.PATH_TYPE.FILE,
                        requireActivity().getFilesDir() + File.separator + fileName,
                        "256x96");

        EditModelDialogFragment edMbS =
                new EditModelDialogFragment(
                        item,
                        item1 -> {
                            try {
                                InputStream inputStream =
                                        requireActivity().getContentResolver().openInputStream(files.get(0));
                                FileUtils.copyFile(
                                        inputStream, fileName, requireActivity().getFilesDir().getAbsolutePath());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            masterList.add(item1);
                            showModels(loadModelList(binding.modelSpinner.getSelectedItem().toString()));
                            FileUtils.updateModelConfig(requireActivity(), requireContext(), masterList, false);
                            googleServices.createAndUploadJsonFile(masterList);
                            Gson gson = new Gson();
                            String json = gson.toJson(masterList);
                            System.out.println("filenameId:::"+masterList.indexOf(item1));
                            Toast.makeText(
                                            requireContext().getApplicationContext(),
                                            "Model added: " + fileName,
                                            Toast.LENGTH_SHORT)
                                    .show();
                        });
        edMbS.show(getChildFragmentManager(), edMbS.getTag());
    }

    private void openPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/octet-stream"); // Specify the MIME type for TFLite files
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        mStartForResult.launch(intent);
        googleServices.createAndUploadJsonFile(masterList);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentModelManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        masterList = FileUtils.loadConfigJSONFromAsset(requireActivity());

       // load all model first
        GoogleServices googleServices = new GoogleServices(requireActivity(), requireContext(), new GoogleSignInCallback() {
            @Override
            public void onSignInSuccess(FirebaseUser account) {

            }

            @Override
            public void onSignInFailed(Exception exception) {

            }

            @Override
            public void onSignOutSuccess() {

            }

            @Override
            public void onSignOutFailed(Exception exception) {

            }
        });
        List<String> modelTypes =
                Arrays.stream(Model.TYPE.values()).map(Enum::toString).collect(Collectors.toList());
        modelTypes.add(0, ALL);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(binding.autoSync, "rotation", 360f, 0f);
        rotation.setDuration(1000);
        rotation.setRepeatCount(ObjectAnimator.INFINITE);
        rotation.setInterpolator(new LinearInterpolator());

        ArrayAdapter<String> modelAdapter =

                new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_dropdown_item_1line, modelTypes);

        binding.modelSpinner.setAdapter(modelAdapter);
        binding.autoSync.setOnClickListener(v -> {
            rotation.start();
            googleServices.getConfigFileContent(rotation, binding.autoSync, adapter);
        });
        adapter = new ModelAdapter(loadModelList(ALL), requireContext(), this);

        binding.modelListContainer.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.modelListContainer.setAdapter(adapter);
        binding.modelListContainer.addItemDecoration(
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.modelSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        showModels(loadModelList(parent.getItemAtPosition(position).toString()));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        binding.addModel.setOnClickListener(
                v -> {
                    if (!PermissionUtils.hasStoragePermission(requireActivity()))
                        requestPermissionLauncher.launch(Constants.PERMISSION_STORAGE);
                    else if (PermissionUtils.shouldShowRational(
                            requireActivity(), Constants.PERMISSION_STORAGE)) {
                        PermissionUtils.showStoragePermissionModelManagementToast(requireActivity());
                    } else openPicker();
                });
    }

    // locally data saved in masterList
    // and individual data is called modelList


    private void showModels(List<Model> modelList) {
        adapter.setItems(modelList);
    }

    private List<Model> loadModelList(String filter) {

        if (masterList == null) masterList = new ArrayList<>();

        List<Model> modelInfoList = new ArrayList<>();
        if (filter.equals(Model.TYPE.CMDNAV.toString()) || filter.equals(ALL)) {
            modelInfoList.addAll(
                    masterList.stream()
                            .filter(f -> f.type.equals(Model.TYPE.CMDNAV))
                            .collect(Collectors.toList()));
        }

        if (filter.equals(Model.TYPE.GOALNAV.toString()) || filter.equals(ALL)) {
            modelInfoList.addAll(
                    masterList.stream()
                            .filter(f -> f.type.equals(Model.TYPE.GOALNAV))
                            .collect(Collectors.toList()));
        }

        if (filter.equals(Model.TYPE.DETECTOR.toString()) || filter.equals(ALL)) {
            modelInfoList.addAll(
                    masterList.stream()
                            .filter(f -> f.type.equals(Model.TYPE.DETECTOR))
                            .collect(Collectors.toList()));
        }

        return modelInfoList;
    }

    @Override
    public void onItemClick(Model item) {

        @SuppressLint("NotifyDataSetChanged") EditModelDialogFragment edMbS =
                new EditModelDialogFragment(
                        item,
                        item1 -> {
                            adapter.notifyDataSetChanged();
                            FileUtils.updateModelConfig(requireActivity(), requireContext(), masterList, false);
                        });
        edMbS.show(getChildFragmentManager(), edMbS.getTag());
    }

    @Override
    public boolean onModelDownloadClicked() {
        System.out.println("Download button clicked");
        ConnectivityManager cm =
                (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        isDownloading = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
        if (!isDownloading)
            Toast.makeText(
                            requireContext().getApplicationContext(),
                            "Please connect to the internet to download models....",
                            Toast.LENGTH_SHORT)
                    .show();
        return isDownloading;
    }

    @Override
    public void onModelDownloaded(boolean status, Model mItem) {
        masterList = FileUtils.loadConfigJSONFromAsset(requireActivity());
        if (status && isAdded()) {
            isDownloading = false;
            for (Model model : masterList) {
                if (model.id.equals(mItem.id)) {
                    model.setPath(requireActivity().getFilesDir() + File.separator + model.name);
                    model.setPathType(Model.PATH_TYPE.FILE);
                    FileUtils.updateModelConfig(requireActivity(), requireContext(), masterList, false);
                    //adapter.notifyDataSetChanged();
                    googleServices.createAndUploadJsonFile(masterList);
                    break;
                }
            }
        }
    }

    @Override
    public void onModelDelete(Model mItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.model_delete_title);
        builder.setMessage(R.string.model_delete_body);
        builder.setPositiveButton(
                "Yes",
                (dialog, id) -> {
                    new File(mItem.path).delete();
                    int index = masterList.indexOf(mItem);

                    Model originalModelConfig =
                            FileUtils.getOriginalModelFromConfig(requireActivity(), mItem);
                    if (originalModelConfig == null) {
                        if (mItem.pathType == Model.PATH_TYPE.FILE) {
                            masterList.remove(mItem);
                            adapter.notifyItemRemoved(index);
                        }
                    } else {
                        mItem.setPath(originalModelConfig.path);
                        mItem.setPathType(originalModelConfig.pathType);
                        adapter.notifyItemChanged(index);
                    }
                    FileUtils.updateModelConfig(requireActivity(), requireContext(), masterList, false);
                    requireActivity().runOnUiThread(() -> adapter.setItems(loadModelList(binding.modelSpinner.getSelectedItem().toString())));
                    googleServices.createAndUploadJsonFile(masterList);
                });
        builder.setNegativeButton("Cancel", (dialog, id) -> {
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            openPicker();
                        } else {
                            PermissionUtils.showStoragePermissionModelManagementToast(requireActivity());
                        }
                    });
}
