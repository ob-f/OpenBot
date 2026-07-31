package org.openbot.app.robot.modelManagement;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.openbot.app.robot.R;
import org.openbot.app.robot.databinding.DialogEditModelBinding;
import org.openbot.app.robot.googleServices.GoogleServices;
import org.openbot.app.robot.main.OnItemClickListener;
import org.openbot.app.robot.tflite.Model;
import org.openbot.app.robot.utils.FileUtils;

public class EditModelDialogFragment extends DialogFragment {

    private DialogEditModelBinding binding;
    private final Model model;
    private List<Model> masterList;
    public GoogleServices googleServices;
    private final OnItemClickListener<Model> itemClickListener;

    public EditModelDialogFragment(Model model, OnItemClickListener<Model> itemClickListener) {
        this.model = model;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = DialogEditModelBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<String> modelTypes =
                Arrays.stream(Model.TYPE.values()).map(Enum::toString).collect(Collectors.toList());
        ArrayAdapter<String> modelAdapter =
                new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_dropdown_item_1line, modelTypes);
        binding.typeSpinner.setAdapter(modelAdapter);
        binding.typeSpinner.setSelection(model.type.ordinal());

        List<String> classes = new ArrayList<>();
        ArrayAdapter<String> classAdapter =
                new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, classes);
        binding.classSpinner.setAdapter(classAdapter);

        binding.typeSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selected = parent.getItemAtPosition(position).toString();
                        classAdapter.clear();
                        classAdapter.addAll(
                                Arrays.stream(Model.CLASS.values())
                                        .map(Enum::toString)
                                        .filter(
                                                f ->
                                                        selected.equals(Model.TYPE.CMDNAV.name())
                                                                == f.equals(Model.CLASS.AUTOPILOT.toString()))
                                        .filter(
                                                f ->
                                                        selected.equals(Model.TYPE.GOALNAV.name())
                                                                == f.equals(Model.CLASS.NAVIGATION.toString()))
                                        .collect(Collectors.toList()));
                        classAdapter.notifyDataSetChanged();
                        binding.classSpinner.setSelection(classAdapter.getPosition(model.classType.toString()));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        binding.edit.setOnClickListener(
                v -> {
                    binding.name.requestFocus();
                    InputMethodManager imm =
                            (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(binding.name, InputMethodManager.SHOW_IMPLICIT);
                });
        binding.name.setText(FileUtils.nameWithoutExtension(model.getName()));
        binding.inputWidth.setText(String.valueOf(model.getInputSize().getWidth()));
        binding.inputHeight.setText(String.valueOf(model.getInputSize().getHeight()));

        binding.btnSubmit.setOnClickListener(
                v -> {
                    System.out.println("btnClick");
                    model.setName(binding.name.getText().toString() + ".tflite");
                    model.setType(Model.TYPE.valueOf(binding.typeSpinner.getSelectedItem().toString()));
                    model.setClassType(
                            Model.CLASS.valueOf(binding.classSpinner.getSelectedItem().toString()));
                    model.setInputSize(
                            binding.inputWidth.getText().toString()
                                    + "x"
                                    + binding.inputHeight.getText().toString());
                    itemClickListener.onItemClick(model);
                    dismiss();
//                    googleServices.createAndUploadJsonFile();
                    System.out.println("After dismiss ");

                });
        binding.dismiss.setOnClickListener(v -> dismiss());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }
}
