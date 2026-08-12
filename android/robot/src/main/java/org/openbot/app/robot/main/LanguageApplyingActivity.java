package org.openbot.app.robot.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import org.openbot.app.robot.R;
import org.openbot.app.robot.utils.LocaleUtils;

/**
 * Branded loading screen shown as its own window while the app locale changes.
 *
 * <p>AppCompatDelegate.setApplicationLocales() recreates every tracked AppCompatActivity to
 * apply the new locale, which causes a black flash on the activity being recreated in place.
 * This screen sits on top of MainActivity in a separate window while that recreation happens
 * underneath, then fades itself out once it's done -- turning the unavoidable recreate into a
 * transition we control instead of a raw flash. It deliberately extends plain Activity, not
 * AppCompatActivity, so it isn't itself swept up in that recreation.
 */
public class LanguageApplyingActivity extends Activity {

  public static final String EXTRA_LANGUAGE_TAG = "language_tag";
  private static final long DISMISS_DELAY_MS = 500;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_language_applying);

    String tag = getIntent().getStringExtra(EXTRA_LANGUAGE_TAG);
    LocaleUtils.applyLocale(tag);

    new Handler(Looper.getMainLooper())
        .postDelayed(
            () -> {
              finish();
              overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            },
            DISMISS_DELAY_MS);
  }
}
