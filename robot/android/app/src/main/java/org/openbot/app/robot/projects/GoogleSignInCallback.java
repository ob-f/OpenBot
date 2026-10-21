package org.openbot.app.robot.projects;

import com.google.firebase.auth.FirebaseUser;

/**
 * This is an interface that defines four methods for handling the results of google signIn and
 * signOut services.
 */
public interface GoogleSignInCallback {
  void onSignInSuccess(FirebaseUser account);

  void onSignInFailed(Exception exception);

  void onSignOutSuccess();

  void onSignOutFailed(Exception exception);
}
