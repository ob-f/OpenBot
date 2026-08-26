package org.openbot.app.robot.server;

import java.util.Set;

public interface ServerListener {
  void onAddModel(String model);

  void onRemoveModel(String model);

  void onConnectionEstablished(String ipAddress);

  void onServerListChange(Set<String> servers);
}
