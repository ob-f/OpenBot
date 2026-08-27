package org.openbot.app.robot.common;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.openbot.app.robot.R;
import org.openbot.app.robot.model.Category;
import org.openbot.app.robot.model.SubCategory;

public class FeatureList {
  // region Properties

  // Global
  public static final String ALL = "All";
  public static final String GENERAL = "General";
  public static final String LEGACY = "Legacy";
  public static final String DEFAULT = "Default";
  public static final String PROJECTS = "Projects";
  public static final String CONTROLLER = "Controller";
  public static final String CONTROLLER_MAPPING = "Controller Mapping";
  public static final String ROBOT_INFO = "Robot Info";

  // Game
  public static final String GAME = "Game";
  public static final String FREE_ROAM = "Free Roam";
  public static final String AR_MODE = "AR Mode";

  // Data Collection
  public static final String DATA_COLLECTION = "Data Collection";
  public static final String LOCAL_SAVE_ON_PHONE = "Local (save On Phone)";
  public static final String EDGE_LOCAL_NETWORK = "Edge (local Network)";
  public static final String CLOUD_FIREBASE = "Cloud (firebase)";
  public static final String CROWD_SOURCE = "Crowd-source (post/accept Data Collection Tasks)";

  // AI
  public static final String AI = "AI";
  public static final String AUTOPILOT = "Autopilot";
  public static final String PERSON_FOLLOWING = "Person Following";
  public static final String OBJECT_NAV = "Object Tracking";
  public static final String MODEL_MANAGEMENT = "Model Management";
  public static final String POINT_GOAL_NAVIGATION = "Point Goal Navigation";
  public static final String AUTONOMOUS_DRIVING = "Autonomous Driving";
  public static final String VISUAL_GOALS = "Visual Goals";
  public static final String SMART_VOICE = "Smart Voice (left/right/straight, Ar Core)";

  // Remote Access
  public static final String REMOTE_ACCESS = "Remote Access";
  public static final String WEB_INTERFACE = "Web Interface";
  public static final String ROS = "ROS";
  public static final String FLEET_MANAGEMENT = "Fleet Management";

  // Coding
  public static final String CODING = "Coding";
  public static final String BLOCK_BASED_PROGRAMMING = "Block-Based Programming";
  public static final String SCRIPTS = "Scripts";

  // Research
  public static final String RESEARCH = "Research";
  public static final String CLASSICAL_ROBOTICS_ALGORITHMS = "Classical Robotics Algorithms";
  public static final String BACKEND_FOR_LEARNING = "Backend For Learning";

  // Monitoring
  public static final String MONITORING = "Monitoring";
  public static final String SENSORS_FROM_CAR = "Sensors from Car";
  public static final String SENSORS_FROM_PHONE = "Sensors from Phone";
  public static final String MAP_VIEW = "Map View";
  // endregion

  @NotNull
  public static ArrayList<Category> getCategories() {
    ArrayList<Category> categories = new ArrayList<>();

    ArrayList<SubCategory> subCategories;

    subCategories = new ArrayList<>();
    subCategories.add(
        new SubCategory(FREE_ROAM, R.string.tile_free_roam, R.drawable.ic_game, "#FFFF6D00"));

    subCategories.add(
        new SubCategory(
            DATA_COLLECTION, R.string.tile_data_collection, R.drawable.ic_storage, "#93C47D"));
    subCategories.add(
        new SubCategory(
            CONTROLLER_MAPPING,
            R.string.tile_controller_mapping,
            R.drawable.ic_controller,
            "#7268A6"));
    subCategories.add(
        new SubCategory(
            ROBOT_INFO, R.string.tile_robot_info, R.drawable.ic_openbot_space, "#4B7BFF"));
    categories.add(new Category(GENERAL, R.string.tile_general, subCategories));

    subCategories = new ArrayList<>();
    subCategories.add(
        new SubCategory(AUTOPILOT, R.string.tile_autopilot, R.drawable.ic_autopilot, "#44525F"));
    subCategories.add(
        new SubCategory(
            OBJECT_NAV, R.string.tile_object_nav, R.drawable.ic_person_search, "#E7CE88"));
    subCategories.add(
        new SubCategory(
            POINT_GOAL_NAVIGATION,
            R.string.tile_point_goal_navigation,
            R.drawable.ic_baseline_golf_course,
            "#1BBFBF"));
    subCategories.add(
        new SubCategory(
            MODEL_MANAGEMENT,
            R.string.tile_model_management,
            R.drawable.ic_list_bulleted_48,
            "#BC7680"));
    categories.add(new Category(AI, R.string.tile_ai, subCategories));

    subCategories = new ArrayList<>();
    subCategories.add(
        new SubCategory(
            DEFAULT, R.string.tile_default_mode, R.drawable.ic_legacy_car, "#F86363"));
    categories.add(new Category(LEGACY, R.string.tile_legacy, subCategories));

    /*
        subCategories = new ArrayList<>();
        subCategories.add(new SubCategory(SMART_VOICE, R.drawable.ic_voice_over));
        subCategories.add(new SubCategory(VISUAL_GOALS, R.drawable.openbot_icon));
        categories.add(new Category(AI, subCategories));

        subCategories = new ArrayList<>();
        subCategories.add(new SubCategory(CONTROLLER, R.drawable.ic_controller));
        subCategories.add(new SubCategory(FREE_ROAM, R.drawable.ic_game, "#FFFF6D00"));
        subCategories.add(new SubCategory(AR_MODE, R.drawable.ic_game, "#B3FF6D00"));
        categories.add(new Category(GAME, subCategories));

        subCategories = new ArrayList<>();
        subCategories.add(new SubCategory(LOCAL_SAVE_ON_PHONE, R.drawable.ic_storage, "#93C47D"));
        subCategories.add(new SubCategory(EDGE_LOCAL_NETWORK, R.drawable.ic_network));
        subCategories.add(new SubCategory(CLOUD_FIREBASE, R.drawable.ic_cloud_upload));
        subCategories.add(new SubCategory(CROWD_SOURCE, R.drawable.openbot_icon));
        categories.add(new Category(DATA_COLLECTION, subCategories));

        subCategories = new ArrayList<>();
        subCategories.add(new SubCategory(WEB_INTERFACE, R.drawable.openbot_icon));
        subCategories.add(new SubCategory(ROS, R.drawable.openbot_icon));
        subCategories.add(new SubCategory(FLEET_MANAGEMENT, R.drawable.openbot_icon));
        categories.add(new Category(REMOTE_ACCESS, subCategories));

        subCategories = new ArrayList<>();
        subCategories.add(new SubCategory(BLOCK_BASED_PROGRAMMING, R.drawable.ic_code));
        subCategories.add(new SubCategory(SCRIPTS, R.drawable.ic_code));
        categories.add(new Category(CODING, subCategories));

        subCategories = new ArrayList<>();
        subCategories.add(
            new SubCategory(CLASSICAL_ROBOTICS_ALGORITHMS, R.drawable.openbot_icon));
        subCategories.add(new SubCategory(BACKEND_FOR_LEARNING, R.drawable.openbot_icon));
        categories.add(new Category(RESEARCH, subCategories));

        subCategories = new ArrayList<>();
        subCategories.add(new SubCategory(SENSORS_FROM_CAR, R.drawable.ic_electric_car));
        subCategories.add(new SubCategory(SENSORS_FROM_PHONE, R.drawable.ic_phonelink));
        subCategories.add(new SubCategory(MAP_VIEW, R.drawable.ic_map));
        categories.add(new Category(MONITORING, subCategories));
    */

    return categories;
  }
}
