package org.openbot.app.robot.playground;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.openbot.app.robot.databinding.FragmentPlaygroundBinding;
import org.openbot.app.robot.utils.FileUtils;

import java.io.File;

public class PlayGroundViewFragment extends Fragment {
  private FragmentPlaygroundBinding binding;
  private WebView playgroundView;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    binding = FragmentPlaygroundBinding.inflate(inflater, container, false);
    playgroundView = (WebView) binding.playgroundWebView;
    return binding.getRoot();
  }

  @SuppressLint("SetJavaScriptEnabled")
  @Override
  public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    WebSettings webSettings = playgroundView.getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setDomStorageEnabled(true);
    webSettings.setAllowContentAccess(true);
    webSettings.setAllowFileAccess(true);
    playgroundView.setWebViewClient(new WebViewClient());
    playgroundView.setWebChromeClient(new MyWebChromeClient());
    webSettings.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36");
    playgroundView.loadUrl("https://www.openbot.itinker.io");
  }

  private class MyWebChromeClient extends WebChromeClient {
    public ValueCallback<Uri[]> uploadMessage;
    private final ActivityResultLauncher<Intent> fileChooserLauncher;

    public MyWebChromeClient() {
      fileChooserLauncher =
          registerForActivityResult(
              new ActivityResultContracts.StartActivityForResult(),
              result -> {
                handleFileSelectionResult(result.getResultCode(), result.getData());
              });
    }

    // For Lollipop 5.0+ Devices
    @Override
    public boolean onShowFileChooser(
        WebView mWebView,
        ValueCallback<Uri[]> filePathCallback,
        FileChooserParams fileChooserParams) {
      if (uploadMessage != null) {
        uploadMessage.onReceiveValue(null);
      }
      uploadMessage = filePathCallback;

      Intent intent = fileChooserParams.createIntent();
      try {
        fileChooserLauncher.launch(intent);
      } catch (ActivityNotFoundException e) {
        uploadMessage = null;
        Toast.makeText(requireActivity(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
        return false;
      }
      return true;
    }

    private void handleFileSelectionResult(int resultCode, Intent data) {
      if (resultCode == Activity.RESULT_OK && data != null) {
        if (uploadMessage != null) {
          Uri[] resultUris = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
          uploadMessage.onReceiveValue(resultUris);
          uploadMessage = null;
          return;
        }
      }
      Toast.makeText(requireActivity(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }
  }
}
