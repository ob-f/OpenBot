package org.openbot.common;

import android.content.Context;
import java.util.ArrayList;
import org.openbot.R;
import org.openbot.model.Category;
import org.openbot.model.SubCategory;

public class FeatureList {
  // region Properties

  // Global
  public static final int ALL = R.string.all;
  public static final int GENERAL = R.string.general;
  public static final int LEGACY = R.string.legacy;
  public static final int DEFAULT = R.string.default_t;
  public static final int PROJECTS = R.string.projects;
  public static final int CONTROLLER = R.string.controller;
  public static final int CONTROLLER_MAPPING = R.string.controller_mapping;
  public static final int ROBOT_INFO = R.string.robot_info;

  // Game
  public static final int GAME = R.string.game;
  public static final int FREE_ROAM = R.string.free_roam;
  public static final int AR_MODE = R.string.ar_mode;

  // Data Collection
  public static final int DATA_COLLECTION = R.string.data_collection;
  public static final int LOCAL_SAVE_ON_PHONE = R.string.local_save_on_phone;
  public static final int EDGE_LOCAL_NETWORK = R.string.edge_local_network;
  public static final int CLOUD_FIREBASE = R.string.cloud_firebase;
  public static final int CROWD_SOURCE = R.string.crowd_source;

  // AI
  public static final int AI = R.string.ai;
  public static final int AUTOPILOT = R.string.autopilot;
  public static final int PERSON_FOLLOWING = R.string.person_following;
  public static final int OBJECT_NAV = R.string.object_nav;
  public static final int MODEL_MANAGEMENT = R.string.model_management;
  public static final int POINT_GOAL_NAVIGATION = R.string.point_goal_navigation;
  public static final int AUTONOMOUS_DRIVING = R.string.autonomous_driving;
  public static final int VISUAL_GOALS = R.string.visual_goals;
  public static final int SMART_VOICE = R.string.smart_voice;

  // Remote Access
  public static final int REMOTE_ACCESS = R.string.remote_access;
  public static final int WEB_INTERFACE = R.string.web_interface;
  public static final int ROS = R.string.ros;
  public static final int FLEET_MANAGEMENT = R.string.fleet_management;

  // Coding
  public static final int CODING = R.string.coding;
  public static final int BLOCK_BASED_PROGRAMMING = R.string.block_based_programming;
  public static final int SCRIPTS = R.string.scripts;

  // Research
  public static final int RESEARCH = R.string.research;
  public static final int CLASSICAL_ROBOTICS_ALGORITHMS = R.string.classical_robotics_algorithms;
  public static final int BACKEND_FOR_LEARNING = R.string.backend_for_learning;

  // Monitoring
  public static final int MONITORING = R.string.monitoring;
  public static final int SENSORS_FROM_CAR = R.string.sensors_from_car;
  public static final int SENSORS_FROM_PHONE = R.string.sensors_from_phone;
  public static final int MAP_VIEW = R.string.map_view;
  // endregion

  public static String getString(Context context, int resourceId) {
    return context.getString(resourceId);
  }

  public static ArrayList<Category> getCategories() {
    ArrayList<Category> categories = new ArrayList<>();

    ArrayList<SubCategory> subCategories;

    subCategories = new ArrayList<>();
    subCategories.add(new SubCategory(FREE_ROAM, R.drawable.ic_game, "#FFFF6D00"));

    subCategories.add(new SubCategory(DATA_COLLECTION, R.drawable.ic_storage, "#93C47D"));
    subCategories.add(new SubCategory(CONTROLLER_MAPPING, R.drawable.ic_controller, "#7268A6"));
    subCategories.add(new SubCategory(ROBOT_INFO, R.drawable.ic_openbot_space, "#4B7BFF"));
    categories.add(new Category(GENERAL, subCategories));

    subCategories = new ArrayList<>();
    subCategories.add(new SubCategory(AUTOPILOT, R.drawable.ic_autopilot, "#44525F"));
    subCategories.add(new SubCategory(OBJECT_NAV, R.drawable.ic_person_search, "#E7CE88"));
    subCategories.add(new SubCategory(POINT_GOAL_NAVIGATION, R.drawable.ic_baseline_golf_course, "#1BBFBF"));
    subCategories.add(new SubCategory(MODEL_MANAGEMENT, R.drawable.ic_list_bulleted_48, "#BC7680"));
    categories.add(new Category(AI, subCategories));

    subCategories = new ArrayList<>();
    subCategories.add(new SubCategory(DEFAULT, R.drawable.ic_legacy_car, "#F86363"));
    categories.add(new Category(LEGACY, subCategories));

    return categories;
  }
}
