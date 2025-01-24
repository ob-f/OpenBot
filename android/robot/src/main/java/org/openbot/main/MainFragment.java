package org.openbot.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.openbot.R;
import org.openbot.common.FeatureList;
import org.openbot.databinding.FragmentMainBinding;
import org.openbot.model.SubCategory;
import org.openbot.original.DefaultActivity;
import timber.log.Timber;

public class MainFragment extends Fragment implements OnItemClickListener<SubCategory> {

  private MainViewModel mViewModel;
  private FragmentMainBinding binding;

  @Nullable
  @Override
  public View onCreateView(
          @NonNull LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState) {
    binding = FragmentMainBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.list.setAdapter(new CategoryAdapter(FeatureList.getCategories(), this));
  }

  @Override
  public void onItemClick(SubCategory subCategory) {
    Timber.d("onItemClick: %s", subCategory.getTitle());
    switch (subCategory.getTitle()) {
      case "Projects":
        Navigation.findNavController(requireView()).navigate(R.id.projectsFragment);
        break;
      case "Free Roam":
        Navigation.findNavController(requireView())
                .navigate(R.id.action_mainFragment_to_freeRoamFragment);
        break;
      case "Data Collection":
        Navigation.findNavController(requireView())
                .navigate(R.id.action_mainFragment_to_loggerFragment);
        break;
      case "Controller":
        // For a library module, uncomment the following line
        // intent = new Intent(this, ControllerActivity.class);
        // startActivity(intent);
        break;
      case "Controller Mapping":
        Navigation.findNavController(requireView())
                .navigate(R.id.action_mainFragment_to_controllerMappingFragment);
        break;
      case "Robot Info":
        Navigation.findNavController(requireView())
                .navigate(R.id.action_mainFragment_to_robotInfoFragment);
        break;
      case "Autopilot":
        Navigation.findNavController(requireView())
                .navigate(R.id.action_mainFragment_to_autopilotFragment);
        break;
      case "Object Nav":
        Navigation.findNavController(requireView())
                .navigate(R.id.action_mainFragment_to_objectNavFragment);
        break;
      case "Point Goal Navigation":
        Navigation.findNavController(requireView())
                .navigate(R.id.action_mainFragment_to_pointGoalNavigationFragment);
        break;
      case "Model Management":
        Navigation.findNavController(requireView())
                .navigate(R.id.action_mainFragment_to_modelManagementFragment);
        break;
      case "Default":
        Intent intent = new Intent(requireActivity(), DefaultActivity.class);
        startActivity(intent);
        break;
    }
  }
}