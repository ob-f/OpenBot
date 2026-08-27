package org.openbot.app.robot.modelManagement;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.koushikdutta.ion.Ion;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.openbot.app.robot.databinding.ItemModelBinding;
import org.openbot.app.robot.projects.DownloadFileTask;
import org.openbot.app.robot.projects.ReadFileCallback;
import org.openbot.app.robot.projects.ReadFileTask;
import org.openbot.app.robot.tflite.Model;
import org.openbot.app.robot.utils.FileUtils;

public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ViewHolder> {

  private List<Model> mValues;
  private Context mContext;
  private final OnItemClickListener<Model> itemClickListener;

  public interface OnItemClickListener<T> {
    void onItemClick(T item);

    boolean onModelDownloadClicked();

    void onModelDownloaded(boolean status, Model mItem);

    void onModelDelete(Model mItem);
  }

  public ModelAdapter(List<Model> items, Context context, OnItemClickListener<Model> itemClickListener) {
    mValues = items;
    mContext = context;
    this.itemClickListener = itemClickListener;
  }

  @NotNull
  @Override
  public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        ItemModelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.title.setText(FileUtils.nameWithoutExtension(mValues.get(position).getName()));
    holder.title.setOnClickListener(v -> itemClickListener.onItemClick(holder.mItem));
    holder.imgDownload.setOnClickListener(
        v -> {
          if (itemClickListener.onModelDownloadClicked()) {
            holder.imgDownload.setClickable(false);
            // Check the download model path and call the appropriate API to download the model.
            if(holder.mItem.path.startsWith("https://drive.google.com")){
              downloadFromDrive(holder);
            } else {
              Ion.with(holder.itemView.getContext())
                      .load(holder.mItem.path)
                      .progress(
                              (downloaded, total) -> {
                                System.out.println("" + downloaded + " / " + total);
                                holder.progressBar.setProgress((int) (downloaded * 100 / total));
                              })
                      //              .write(new File("/sdcard/openbot/tf.tflite"))
                      .write(
                              new File(
                                      holder.itemView.getContext().getFilesDir()
                                              + File.separator
                                              + holder.mItem.name))
                      .setCallback(
                              (e, file) -> {
                                holder.imgDownload.setClickable(true);
                                holder.imgDownload.setVisibility(View.GONE);
                                holder.imgDelete.setVisibility(View.VISIBLE);
                                holder.progressBar.setProgress(0);
                                itemClickListener.onModelDownloaded(e == null, holder.mItem);
                              });
            }
          }
        });

    holder.imgDownload.setVisibility(
        (holder.mItem.pathType == Model.PATH_TYPE.URL) ? View.VISIBLE : View.GONE );
    holder.imgDelete.setVisibility(
        (holder.mItem.pathType == Model.PATH_TYPE.FILE )  ? View.VISIBLE : View.GONE);
    holder.imgDelete.setOnClickListener(v -> itemClickListener.onModelDelete(holder.mItem));
    holder.title.setAlpha((holder.mItem.pathType == Model.PATH_TYPE.URL) ? 0.7f : 1f);
  }

  /**
   * Downloads a model from Google Drive using the provided ViewHolder.
   *
   * @param holder The ViewHolder associated with the download task.
   */
  private void downloadFromDrive(ViewHolder holder) {
    new DownloadFileTask(mContext, progress -> {
      holder.progressBar.setProgress(progress);
      // Update your progress bar or do any other UI updates based on progress.
    }, file -> {
      // Handle the downloaded file here, for example, write its content to a new file.
      try {
        File newFile = new File(
                holder.itemView.getContext().getFilesDir()
                + File.separator
                + holder.mItem.name);
        InputStream inputStream = new FileInputStream(file);
        OutputStream outputStream = new FileOutputStream(newFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
        holder.imgDownload.setClickable(true);
        holder.imgDownload.setVisibility(View.GONE);
        holder.imgDelete.setVisibility(View.VISIBLE);
        holder.progressBar.setProgress(0);
        itemClickListener.onModelDownloaded(true, holder.mItem);
        outputStream.close();
        inputStream.close();
      } catch (IOException e) {
        // Handle errors and notify listener on download failure
        holder.imgDownload.setClickable(true);
        holder.progressBar.setProgress(0);
        itemClickListener.onModelDownloaded(false, holder.mItem);
        e.printStackTrace();
      }
    }).execute(holder.mItem.path);

  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public void setItems(List<Model> modelList) {
    this.mValues = modelList;
    notifyDataSetChanged();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final FrameLayout imgDownload;
    public final ImageView imgDelete;
    public Model mItem;
    public ProgressBar progressBar;

    public ViewHolder(ItemModelBinding binding) {
      super(binding.getRoot());

      title = binding.title;
      imgDownload = binding.downloadModel;
      progressBar = binding.progressBar;
      imgDelete = binding.deleteModel;
    }
  }
}
