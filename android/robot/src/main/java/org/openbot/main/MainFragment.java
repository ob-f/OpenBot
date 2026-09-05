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
      case FeatureList.PROJECTS:
        Navigation.findNavController(requireView()).navigate(R.id.projectsFragment);
        break;
      case FeatureList.FREE_ROAM:
        Navigation.findNavController(requireView())
            .navigate(R.id.action_mainFragment_to_freeRoamFragment);
        break;

      case FeatureList.DATA_COLLECTION:
        Navigation.findNavController(requireView())
            .navigate(R.id.action_mainFragment_to_loggerFragment);
        break;

      case FeatureList.CONTROLLER:
        // For a library module, uncomment the following line
        // intent = new Intent(this, ControllerActivity.class);
        // startActivity(intent);
        break;

      case FeatureList.CONTROLLER_MAPPING:
        Navigation.findNavController(requireView())
            .navigate(R.id.action_mainFragment_to_controllerMappingFragment);
        break;

      case FeatureList.ROBOT_INFO:
        Navigation.findNavController(requireView())
            .navigate(R.id.action_mainFragment_to_robotInfoFragment);
        break;

      case FeatureList.AUTOPILOT:
        Navigation.findNavController(requireView())
            .navigate(R.id.action_mainFragment_to_autopilotFragment);
        break;

      case FeatureList.OBJECT_NAV:
        Navigation.findNavController(requireView())
            .navigate(R.id.action_mainFragment_to_objectNavFragment);
        break;

      case FeatureList.CART_SIMULATOR:
        Navigation.findNavController(requireView())
            .navigate(R.id.action_mainFragment_to_cartSimFragment);
        break;

      case FeatureList.REAL_CART_FOLLOW:
        Navigation.findNavController(requireView())
            .navigate(R.id.action_mainFragment_to_realCartFollowFragment);
        break;

      case FeatureList.VOICE_CART_SIMULATOR:
        Navigation.findNavController(requireView()).navigate(R.id.voiceCartSimulatorFragment);
        break;

      case FeatureList.PERSON_CROP_COLLECTOR:
        Navigation.findNavController(requireView())
            .navigate(R.id.action_mainFragment_to_personCropCollectorFragment);
        break;

      case FeatureList.PERSON_SEQUENCE_COLLECTOR:
        Navigation.findNavController(requireView())
            .navigate(R.id.action_mainFragment_to_personSequenceCollectorFragment);
        break;

      case FeatureList.POINT_GOAL_NAVIGATION:
        Navigation.findNavController(requireView())
            .navigate(R.id.action_mainFragment_to_pointGoalNavigationFragment);
        break;

      case FeatureList.MODEL_MANAGEMENT:
        Navigation.findNavController(requireView())
            .navigate(R.id.action_mainFragment_to_modelManagementFragment);
        break;

      case FeatureList.DEFAULT:
        Intent intent = new Intent(requireActivity(), DefaultActivity.class);
        startActivity(intent);
        break;
    }
  }
}
