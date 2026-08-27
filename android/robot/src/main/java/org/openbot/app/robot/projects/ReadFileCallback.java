package org.openbot.app.robot.projects;

import java.io.IOException;

/** This is an interface that defines two methods for handling the results of reading a file. */
public interface ReadFileCallback {
  // This method is called when the file is successfully read.
  // It is passed the contents of the file as a string.
  void onFileReadSuccess(String fileContents);

  // This method is called when there is an exception while reading the file.
  // It is passed the exception as an argument.
  void onFileReadFailed(IOException e);
}
