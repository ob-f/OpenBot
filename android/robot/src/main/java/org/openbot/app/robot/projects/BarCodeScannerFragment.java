package org.openbot.app.robot.projects;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;
import androidx.navigation.Navigation;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URL;

import org.openbot.app.robot.R;
import org.openbot.app.robot.common.CameraFragment;
import org.openbot.app.robot.databinding.FragmentBarCodeScannerBinding;
import org.openbot.app.robot.utils.BotFunctionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class BarCodeScannerFragment extends CameraFragment {

  private FragmentBarCodeScannerBinding binding;
  private BarcodeDetector barcodeDetector;
  private BottomSheetBehavior successBottomSheetBehavior;
  private BottomSheetBehavior failedBottomSheetBehavior;
  private boolean barCodeAccess = true;
  private boolean cameraToggle = false;
  public static String finalCode;
  private String projectName;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment and set it as the root view for the fragment.
    binding = FragmentBarCodeScannerBinding.inflate(inflater, container, false);
    binding.barCodeLoader.setVisibility(View.GONE);
    // Get the BottomSheetBehavior objects for the success and error bottom sheets.
    successBottomSheetBehavior = BottomSheetBehavior.from(binding.qrBottomSheetSuccess);
    failedBottomSheetBehavior = BottomSheetBehavior.from(binding.qrBottomSheetError);
    // Set the initial state of both bottom sheets to hidden.
    successBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    failedBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    return inflateFragment(binding, inflater, container);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // Set click listeners for the cross, start, cancel, and re-scan buttons.
    binding.btnCross.setOnClickListener(v -> requireActivity().onBackPressed());
    binding.qrCancelBtn.setOnClickListener(
        v -> successBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));
    // Navigate to the BlocklyExecutingFragment when the start button is clicked.
    binding.qrStartBtn.setOnClickListener(
        v ->
            Navigation.findNavController(requireView())
                .navigate(R.id.action_barCodeScannerFragment_to_blocklyExecutingFragment));
    binding.reScanBtn.setOnClickListener(
        v -> failedBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));
    // Add callbacks for the success and error bottom sheets.
    successBottomSheetBehavior.addBottomSheetCallback(successBottomSheetCallback);
    failedBottomSheetBehavior.addBottomSheetCallback(failedBottomSheetCallback);
    if (preferencesManager.getCameraSwitch()) {
      toggleCamera();
      cameraToggle = true;
    }
    initialiseQrDetector();
  }

  /** Define a BottomSheetCallback object for the success bottom sheet. */
  private BottomSheetBehavior.BottomSheetCallback successBottomSheetCallback =
      new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
          // Handle state changes here of success bottomSheet.
          if (newState == BottomSheetBehavior.STATE_EXPANDED
              || newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
            // Make the overlay view visible when the bottom sheet is expanded or half-expanded.
            binding.barCodeLoader.setVisibility(View.GONE);
            binding.overlayView.setVisibility(View.VISIBLE);
          } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            // Make the overlay view invisible and set barCodeAccess is true to start detecting qr
            // code when the bottom sheet is hidden.
            binding.overlayView.setVisibility(View.GONE);
            barCodeAccess = true;
          }

          if (newState == BottomSheetBehavior.STATE_DRAGGING) {
            successBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
          }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
          // Handle sliding events here
        }
      };

  /** Define a BottomSheetCallback object for the failed bottom sheet. */
  private BottomSheetBehavior.BottomSheetCallback failedBottomSheetCallback =
      new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
          // Handle state changes here of failed bottomSheet.
          if (newState == BottomSheetBehavior.STATE_EXPANDED
              || newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
            // Make the overlay view visible when the bottom sheet is expanded or half-expanded.
            binding.barCodeLoader.setVisibility(View.GONE);
            binding.overlayView.setVisibility(View.VISIBLE);
          } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            // Make the overlay view invisible and set barCodeAccess is true to start detecting qr
            // code when the bottom sheet is hidden
            binding.overlayView.setVisibility(View.GONE);
            barCodeAccess = true;
          }
          if (newState == BottomSheetBehavior.STATE_DRAGGING) {
            failedBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
          }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
          // Handle sliding events here
        }
      };

  @Override
  protected void processControllerKeyData(String command) {}

  @Override
  protected void processUSBData(String data) {}

  @Override
  protected void processFrame(Bitmap image, ImageProxy imageProxy) {
    startQrDetection(image);
  }

  /** Initialize a new instance of the BarcodeDetector with all barcode formats supported. */
  private void initialiseQrDetector() {
    barcodeDetector =
        new BarcodeDetector.Builder(requireContext())
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build();
  }

  /**
   * start qr code detection If the image is not null, create a new Frame with the image and detect
   * any Qr code in it.
   *
   * @param image the image to detect qr code.
   */
  private void startQrDetection(Bitmap image) {
    if (image != null) {
      Frame frame = new Frame.Builder().setBitmap(image).build();
      SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);
      // If a qr code is detected and barCodeAccess is true, get the qr code value and call
      // extractFileID().
      if (barcodes.size() != 0 && barCodeAccess) {
        requireActivity()
            .runOnUiThread(
                () -> {
                  binding.overlayView.setVisibility(View.VISIBLE);
                  binding.barCodeLoader.setVisibility(View.VISIBLE);
                });
        barCodeAccess = false;
        String qrCodeValue = barcodes.valueAt(0).displayValue;
        // grCode value is in Json object
        Gson gson = new Gson();
        if (qrCodeValue.startsWith("{\"driveLink\"")) {
          JsonObject jsonObject = gson.fromJson(qrCodeValue, JsonObject.class); // get json
          String linkCode = jsonObject.get("driveLink").getAsString(); // get as string
          projectName = jsonObject.get("projectName").getAsString(); // get as string
          extractFileID(linkCode);
        } else {
          handleBottomSheet(false);
        }
      }
    }
  }

  /**
   * To extract the drive file id from URL Link.
   *
   * @param urlLink the link to use extract file id.
   */
  private void extractFileID(String urlLink) {
    // If the URL string contains "/file/d/", extract the file ID and call readFileFromDrive().
    if (urlLink.contains("/file/d/")) {
      String id = urlLink.substring(urlLink.indexOf("/file/d/") + 8, urlLink.indexOf("/edit"));
      try {
        readFileFromDrive(id);
      } catch (IOException e) {
        // If an IOException occurs while reading the file, handle the bottom sheet with a failure
        // state.
        handleBottomSheet(false);
        throw new RuntimeException(e);
      }
    } else {
      // If the URL string doesn't contain "/file/d/", handle the bottom sheet with a failure state.
      handleBottomSheet(false);
    }
  }

  /**
   * function to read google-services json file
   * @return
   */
  private String readJsonFile() {
    String jsonContent = null;
    try {
      // Open the file using an InputStream
      InputStream inputStream = requireContext().getAssets().open("google-services.json");

      System.out.println("is:::"+inputStream);

      // Read the InputStream into a StringBuilder
      StringBuilder stringBuilder = new StringBuilder();
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line);
      }

      // Close the InputStream and BufferedReader
      inputStream.close();
      bufferedReader.close();

      // Convert the StringBuilder to a String
      jsonContent = stringBuilder.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return jsonContent;
  }

  /**
   * function to get API key from json
   * @return
   */
  private String getCurrentKeyFromJson() {
    String jsonData = readJsonFile();
    String currentKey = "";
    try {
      // Parse the JSON data
      JSONObject jsonObject = new JSONObject(jsonData);

      // Navigate to the "client" array
      JSONArray clientArray = jsonObject.getJSONArray("client");

      // Get the first element of "client" array
      JSONObject firstClient = clientArray.getJSONObject(0);

      // Get the "api_key" array from the first client
      JSONArray apiKeyArray = firstClient.getJSONArray("api_key");

      // Get the first element of "api_key" array
      JSONObject firstApiKey = apiKeyArray.getJSONObject(0);

      // Get the "current_key" value
      currentKey = firstApiKey.getString("current_key");
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return currentKey;
  }


  /**
   * To read google drive file contents using file id, Call a new instance of the ReadFileTask with
   * the given file ID and a callback to handle success/failure.
   *
   * @param fileId
   * @throws IOException
   */
  private void readFileFromDrive(String fileId) throws IOException {
    String APIkey = getCurrentKeyFromJson();
    URL fileUrl =
            new URL("https://www.googleapis.com/drive/v3/files/"+fileId+"?alt=media&key="+APIkey);
    new ReadFileTask(
            fileUrl,
            new ReadFileCallback() {
              @Override
              public void onFileReadSuccess(String fileContents) {
                // If the file is read successfully, replace any bot function calls with "Android."
                // + functionName.
                String code = fileContents;
                for (String fun : BotFunctionUtils.botFunctionArray) {
                  if (code.contains(fun)) {
                    code = code.replace(fun, "Android." + fun);
                  }
                }
                // Set the finalCode to the modified code and handle the bottom sheet with a success
                // state.
                finalCode = code;
                Activity activity = getActivity();
                if (activity != null) {
                  requireActivity()
                      .runOnUiThread(
                          () ->
                              binding.qrMessage.setText(
                                  projectName
                                      + " file detected. Start to execute the code on your OpenBot."));
                  handleBottomSheet(true);
                }
              }

              @Override
              public void onFileReadFailed(IOException e) {
                // If an IOException occurs while reading the file, handle the bottom sheet with a
                // failure state.
                handleBottomSheet(false);
              }
            })
        .execute();
  }

  private void handleBottomSheet(boolean scanSuccess) {
    // If scanSuccess is true, expand the success bottom sheet, otherwise expand the failure bottom
    // sheet.
    if (scanSuccess) {
      successBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    } else {
      failedBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (cameraToggle) toggleCamera();
  }

  @Override
  public void onResume() {
    super.onResume();
    barCodeAccess = true;
    int orientation = getResources().getConfiguration().orientation;
    if (orientation == Configuration.ORIENTATION_LANDSCAPE
        || orientation == Configuration.ORIENTATION_PORTRAIT) {
      successBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
      failedBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
  }
}
