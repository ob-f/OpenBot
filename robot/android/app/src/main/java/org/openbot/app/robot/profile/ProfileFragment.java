package org.openbot.app.robot.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import org.openbot.app.robot.R;
import org.openbot.app.robot.databinding.FragmentProfileBinding;
import org.openbot.app.robot.env.SharedPreferencesManager;
import org.openbot.app.robot.googleServices.GoogleServices;
import org.openbot.app.robot.projects.GoogleSignInCallback;

public class ProfileFragment extends Fragment {

  private FragmentProfileBinding binding;
  private GoogleServices googleServices;
  private SharedPreferencesManager sharedPreferencesManager;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for Profile fragment
    binding = FragmentProfileBinding.inflate(inflater, container, false);
    googleServices = new GoogleServices(requireActivity(), requireContext(), newGoogleServices);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    sharedPreferencesManager = new SharedPreferencesManager(requireContext());
    binding.signInButton.setOnClickListener(v -> signIn());
    binding.logoutBtn.setOnClickListener(v -> signOut());
    binding.editProfileBtn.setOnClickListener(
        v ->
            Navigation.findNavController(requireView())
                .navigate(R.id.action_profileFragment_to_EditProfileFragment));
  }

  /** launch activity to choose google account for google signIn. */
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
          });

  /** open alert b0x to confirm user for signOut. */
  private void signOut() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
    builder.setTitle("Confirm Logout");
    builder.setMessage("Are you sure you want to logout?");
    builder.setCancelable(true);

    builder.setPositiveButton("LOG OUT", (dialog, id) -> googleServices.signOut());
    builder.setNegativeButton("CANCEL", (dialog, id) -> dialog.cancel());
    AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }

  /** update fragment layout after user signedIn. */
  private GoogleSignInCallback newGoogleServices =
      new GoogleSignInCallback() {
        @Override
        public void onSignInSuccess(FirebaseUser account) {
          binding.signOutScreen.setVisibility(View.GONE);
          binding.profileSettings.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSignInFailed(Exception exception) {
          binding.signOutScreen.setVisibility(View.VISIBLE);
          binding.profileSettings.setVisibility(View.GONE);
        }

        @Override
        public void onSignOutSuccess() {
          binding.signOutScreen.setVisibility(View.VISIBLE);
          binding.profileSettings.setVisibility(View.GONE);
          sharedPreferencesManager.setProjectLIst(new ArrayList<>());
        }

        @Override
        public void onSignOutFailed(Exception exception) {}
      };
}
