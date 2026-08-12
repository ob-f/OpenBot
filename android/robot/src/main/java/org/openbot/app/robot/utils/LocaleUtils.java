package org.openbot.app.robot.utils;

import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import java.util.Arrays;
import java.util.List;
import org.openbot.app.robot.R;

/**
 * Supported in-app display languages, backed by AndroidX's per-app language API. See {@code
 * docs/dev/feature-multi-language-support.md} for the full design.
 */
public class LocaleUtils {

  private LocaleUtils() {}

  public static List<LanguageOption> getLanguageOptions(Context context) {
    return Arrays.asList(
        new LanguageOption(null, context.getString(R.string.system_default), R.drawable.ic_flag_globe),
        new LanguageOption("en", "English", R.drawable.ic_flag_us),
        new LanguageOption("de", "Deutsch", R.drawable.ic_flag_de),
        new LanguageOption("es", "Español", R.drawable.ic_flag_es),
        new LanguageOption("fr", "Français", R.drawable.ic_flag_fr),
        new LanguageOption("zh", "中文", R.drawable.ic_flag_cn),
        new LanguageOption("ko", "한국어", R.drawable.ic_flag_kr),
        new LanguageOption("hi", "हिन्दी", R.drawable.ic_flag_in));
  }

  /** Currently selected language tag, or {@code null} when following the system language. */
  public static String getCurrentTag() {
    LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
    if (locales.isEmpty()) return null;
    return locales.get(0).getLanguage();
  }

  public static void applyLocale(String tag) {
    LocaleListCompat locales =
        tag == null ? LocaleListCompat.getEmptyLocaleList() : LocaleListCompat.forLanguageTags(tag);
    AppCompatDelegate.setApplicationLocales(locales);
  }
}
