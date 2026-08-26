package org.openbot.app.robot.projects;

import java.util.HashMap;
import java.util.Map;

public class TaskStorage {
    private Map<String, Map<String, String>> data = new HashMap<>();

    public void addTask(String classTypeId, Map<String, String> tasks) {
        data.put(classTypeId, tasks);
    }

    public void removeOnDetectTasks(String classTypeId) {
        Map<String, String> classTasks = data.get(classTypeId);
        if (classTasks != null) {
            classTasks.remove("onDetect");
        }
    }

    public void removeOnUnDetectTasks(String classTypeId) {
        Map<String, String> classTasks = data.get(classTypeId);
        if (classTasks != null) {
            classTasks.remove("onUnDetect");
        }
    }

    public Map<String, Map<String, String>> getData() {
        return data;
    }

    public void clear() {
        data.clear();
    }
}

