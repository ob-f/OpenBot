package org.openbot.app.robot.projects;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;
import org.openbot.app.robot.R;
import org.openbot.app.robot.databinding.FragmentProjectsBinding;
import org.openbot.app.robot.env.SharedPreferencesManager;
import org.openbot.app.robot.googleServices.GoogleServices;
import org.openbot.app.robot.main.CommonRecyclerViewAdapter;
import org.openbot.app.robot.utils.BotFunctionUtils;

public class ProjectsFragment extends Fragment {
  private FragmentProjectsBinding binding;
  private GoogleServices googleServices;
  private DriveProjectsAdapter adapter;
  private BarCodeScannerFragment barCodeScannerFragment;
  private BottomSheetBehavior projectsBottomSheetBehavior;
  private final SparseArray<int[]> driveRes = new SparseArray<>();
  private SharedPreferencesManager sharedPreferencesManager;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    binding = FragmentProjectsBinding.inflate(inflater, container, false);
    sharedPreferencesManager = new SharedPreferencesManager(requireContext());
    googleServices = new GoogleServices(requireActivity(), requireContext(), newGoogleServices);
    projectsBottomSheetBehavior = BottomSheetBehavior.from(binding.projectsBottomSheet);
    projectsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    barCodeScannerFragment = new BarCodeScannerFragment();
    binding.signInButton.setOnClickListener(v -> signIn());
    projectsBottomSheetBehavior.addBottomSheetCallback(dpBottomSheetCallback);
    requireView()
        .findViewById(R.id.dp_start_btn)
        .setOnClickListener(
            v ->
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_projectsFragment_to_blocklyExecutingFragment));
    requireView()
        .findViewById(R.id.dp_cancel_btn)
        .setOnClickListener(
            v -> projectsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));

    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
      binding.signOutLayout.setVisibility(View.GONE);
      showProjectsRv();
    } else {
      binding.signOutLayout.setVisibility(View.VISIBLE);
    }

    binding.refreshLayout.setOnRefreshListener(this::showProjectsRv);
  }

  private void signIn() {
    Intent signInIntent = googleServices.mGoogleSignInClient.getSignInIntent();
    googleLogInActivityResultLauncher.launch(signInIntent);
  }

  ActivityResultLauncher<Intent> googleLogInActivityResultLauncher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            Intent data = result.getData();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            googleServices.handleSignInResult(task);
            if (task.isSuccessful()) {
              binding.signOutLayout.setVisibility(View.GONE);
              showProjectsRv();
            }
          });

  private final GoogleSignInCallback newGoogleServices =
      new GoogleSignInCallback() {
        @Override
        public void onSignInSuccess(FirebaseUser account) {}

        @Override
        public void onSignInFailed(Exception exception) {}

        @Override
        public void onSignOutSuccess() {}

        @Override
        public void onSignOutFailed(Exception exception) {}
      };

  private void showProjectsRv() {
    binding.noProjectsLayout.setVisibility(View.GONE);
    binding.refreshLayout.setVisibility(View.VISIBLE);
    googleServices.projectsList = sharedPreferencesManager.getProjectList();
    // Set Grid layout according to screen width dimension.
    int screenWidth = getResources().getDisplayMetrics().widthPixels;
    int itemWidth = getResources().getDimensionPixelSize(R.dimen.project_card_view);
    int numColumns = screenWidth / itemWidth;
    binding.projectsRv.setLayoutManager(new GridLayoutManager(requireActivity(), numColumns));
    driveRes.put(R.layout.projects_list_view, new int[] {R.id.project_name, R.id.project_date});
//    System.out.println("msg"+R.layout.projects_list_view+" "+R.id.project_name);
    setScanDeviceAdapter(
        new DriveProjectsAdapter(requireActivity(), googleServices.projectsList, driveRes),
        (itemView, position) ->
            onTapProjectItem(
                googleServices.projectsList.get(position).getProjectCommands(),
                googleServices.projectsList.get(position).getProjectName()),
        ((itemView, position) ->
            onLongTapProjectItem(
                googleServices.projectsList.get(position).getProjectName(),
                googleServices.projectsList.get(position).getProjectId())));
    binding.projectsLoader.setVisibility(View.VISIBLE);
    googleServices.accessDriveFiles(adapter, binding);
    System.out.println("print"+googleServices.projectsList);
    binding.projectsRv.setAdapter(adapter);
  }

  private void setScanDeviceAdapter(
      DriveProjectsAdapter adapter,
      @NonNull CommonRecyclerViewAdapter.OnItemClickListener onItemClickListener,
      CommonRecyclerViewAdapter.OnItemLongClickListener onItemLongClickListener) {
    this.adapter = adapter;
    adapter.setOnItemClickListener(onItemClickListener);
    adapter.setOnItemLongClickListener(onItemLongClickListener);
  }

  /** update projects screen when there is 0 projects on google drive account. */
  public void updateMessage(
      List<ProjectsDataInObject> driveFiles, FragmentProjectsBinding binding) {
    binding.projectsLoader.setVisibility(View.GONE);
    binding.refreshLayout.setRefreshing(false);
    if (driveFiles.size() <= 0) {
      binding.noProjectsLayout.setVisibility(View.VISIBLE);
    }
  }

  /**
   * This method is called when a project item is tapped. It performs to run project's command.
   *
   * @param fileContents
   * @param projectName
   */
  private void onTapProjectItem(String fileContents, String projectName) {
    String code = fileContents;
    for (String fun : BotFunctionUtils.botFunctionArray) {
      if (code.contains(fun)) {
        code = code.replace(fun, "Android." + fun);
      }
    }
    barCodeScannerFragment.finalCode = code;
    binding.dpMessage.setText(
        getString(R.string.project_file_detected, projectName.replace(".js", "")));
    projectsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
  }

  /**
   * This method is triggered when a long tap is performed on a project item. It displays an
   * AlertDialog with a confirmation message to delete the project.
   *
   * @param projectName
   * @param projectId
   */
  private void onLongTapProjectItem(String projectName, String projectId) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
    builder.setTitle("Delete this file ?").setMessage("You cannot restore this file later.");
    builder.setPositiveButton(
        "Delete",
        (dialog, id) -> {
          // Perform the file deletion using googleServices.deleteFile() method
          googleServices.deleteFile(projectId, projectName, adapter, binding);
          binding.projectsLoader.setVisibility(View.VISIBLE);
        });
    builder.setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());
    AlertDialog dialog = builder.create();
    dialog.show();
  }

  private BottomSheetBehavior.BottomSheetCallback dpBottomSheetCallback =
      new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
          // Handle state changes here
          if (newState == BottomSheetBehavior.STATE_DRAGGING) {
            projectsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
          }
          if (newState == BottomSheetBehavior.STATE_EXPANDED
              || newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
            binding.projectsLoader.setVisibility(View.GONE);
            binding.overlayView.setVisibility(View.VISIBLE);
          } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            binding.overlayView.setVisibility(View.GONE);
          }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
          // Handle sliding events here
        }
      };

  @Override
  public void onResume() {
    super.onResume();
    projectsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
  }
}
