package org.openbot.app.robot.projects;

import com.google.api.client.util.DateTime;

public class ProjectsDataInObject {
  private final String projectId;
  private final String projectName;
  private final DateTime date;
  private final String projectCommands;

  public ProjectsDataInObject(
      String projectId, String projectName, DateTime date, String projectCommands) {
    this.projectId = projectId;
    this.projectName = projectName;
    this.date = date;
    this.projectCommands = projectCommands;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getProjectName() {
    return projectName;
  }

  public DateTime getProjectDate() {
    return date;
  }

  public String getProjectCommands() {
    return projectCommands;
  }
}
