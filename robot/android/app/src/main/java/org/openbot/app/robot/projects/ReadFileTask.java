package org.openbot.app.robot.projects;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/** call this class to read the public google drive file using file id. */
public class ReadFileTask extends AsyncTask<Void, Void, Void> {
  URL fileUrl;
  private ReadFileCallback callback;

  public ReadFileTask(URL fileUrl, ReadFileCallback callback) {
    this.fileUrl = fileUrl;
    this.callback = callback;
  }

  /**
   * This is an asynchronous task that will download a file from a URL and return its contents.
   *
   * @param voids The parameters of the task.
   * @return
   */
  protected Void doInBackground(Void... voids) {
    try {
      // Open a connection to the URL.
      HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
      // Set the HTTP request method to fetch the file.
      conn.setRequestMethod("GET");
      conn.connect();
      // Get an input stream from the connection.
      InputStream inputStream = conn.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      StringBuilder stringBuilder = new StringBuilder();
      // Read each line of the input stream and append it to a string builder.
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
      }
      // Close the reader and input stream.
      reader.close();
      inputStream.close();
      // Convert the contents of the string builder to a string.
      String fileContents = stringBuilder.toString();
      // Call the onPostExecute method with the file contents.
      onPostExecute(fileContents);
    } catch (IOException e) {
      // If there is an exception, call the onPostExecuteFailed method and print the stack trace.
      onPostExecuteFailed(e);
      e.printStackTrace();
    }
    // Return null as the result of the task.
    return null;
  }

  /**
   * ] This method is called after the doInBackground method has finished executing. It will be
   * passed the file contents as a string.
   *
   * @param fileContents
   */
  protected void onPostExecute(String fileContents) {
    // Check if the callback is not null.
    if (callback != null) {
      // Call the onFileReadSuccess method of the callback with the file contents.
      callback.onFileReadSuccess(fileContents);
    }
  }

  protected void onPostExecuteFailed(IOException e) {
    // Check if the callback is not null.
    if (callback != null) {
      // Call the onFileReadFailed method of the callback with the exception.
      callback.onFileReadFailed(e);
    }
  }
}
