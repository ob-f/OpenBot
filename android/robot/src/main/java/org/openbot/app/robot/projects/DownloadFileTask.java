package org.openbot.app.robot.projects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import timber.log.Timber;

public class DownloadFileTask extends AsyncTask<String, Integer, File> {

    private Context context;
    private OnDownloadProgressListener progressListener;
    private OnDownloadCompleteListener completeListener;

    // Interface to define callback functions
    public interface OnDownloadProgressListener {
        void onProgressUpdate(int progress);
    }

    public interface OnDownloadCompleteListener {
        void onDownloadComplete(File file);
    }

    public DownloadFileTask(Context context, OnDownloadProgressListener progressListener, OnDownloadCompleteListener completeListener) {
        this.context = context;
        this.progressListener = progressListener;
        this.completeListener = completeListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SuppressLint("TimberArgCount")
    @Override
    protected File doInBackground(String... params) {
        String fileUrl = params[0];
        File file = null;

        try {
            URL url = new URL(fileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            // Get file size for progress calculation
            int fileLength = urlConnection.getContentLength();

            // Create a new file in the app's internal storage directory
            file = new File(context.getFilesDir(), "downloaded_file.ext");
            FileOutputStream outputStream = new FileOutputStream(file);

            // Start downloading the file
            BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            byte[] data = new byte[1024];
            int total = 0;
            int count;
            while ((count = inputStream.read(data)) != -1) {
                total += count;
                int progress = (total * 100) / fileLength;
                publishProgress(progress);
                outputStream.write(data, 0, count);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
            Timber.e("DownloadError", e.getMessage());
        }

        return file;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (progressListener != null) {
            progressListener.onProgressUpdate(values[0]);
        }
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        if (completeListener != null) {
            completeListener.onDownloadComplete(file);
        }
    }
}
