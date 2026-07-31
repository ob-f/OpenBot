package org.openbot.app.robot.projects;

import android.content.Context;
import android.util.SparseArray;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.api.client.util.DateTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.openbot.app.robot.R;
import org.openbot.app.robot.main.CommonRecyclerViewAdapter;

/**
 * This is a custom adapter for displaying a list of files representing projects in Google Drive.
 */
public class DriveProjectsAdapter extends CommonRecyclerViewAdapter<ProjectsDataInObject> {

  /**
   * Constructor for creating a new instance of the adapter.
   *
   * @param context
   * @param dataList
   * @param resLayoutAndViewIds
   */
  public DriveProjectsAdapter(
      @NonNull Context context,
      @NonNull List<ProjectsDataInObject> dataList,
      @NonNull SparseArray<int[]> resLayoutAndViewIds) {
    // Call the constructor of the superclass with the parameters provided.
    super(context, dataList, resLayoutAndViewIds);
  }

  /**
   * This method specifies the resource layout type for the list item at the given position.
   *
   * @param position the position of item
   * @return
   */
  @Override
  public int getItemResLayoutType(int position) {
    return R.layout.projects_list_view;
  }

  /**
   * This method binds the data for a file to a view holder for a list item.
   *
   * @param holder
   * @param data
   * @param position
   */
  @Override
  public void bindDataToItem(MyViewHolder holder, ProjectsDataInObject data, int position) {
    // Get the views from the view holder that will display the project name and date.
    TextView projectName = (TextView) holder.mViews.get(R.id.project_name);
    TextView updatedDate = (TextView) holder.mViews.get(R.id.project_date);

    // Modify the project name to remove the '.js' file extension and truncate it if it is too long.
    String projectNameModified = data.getProjectName().replace(".js", "");
    if (projectNameModified.length() > 12) {
      projectNameModified = projectNameModified.substring(0, 10) + "...";
    }
    // Set the text of the project name view to the modified project name.
    projectName.setText(projectNameModified);

    // Format the project date according to the UI design and set the text of the updated date view
    // to the formatted date.
    DateTime dateTime = new DateTime(String.valueOf(data.getProjectDate()));
    Instant instant = Instant.ofEpochMilli(dateTime.getValue());
    LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    String formattedDate = outputFormatter.format(localDate);
    updatedDate.setText(formattedDate);
  }
}
