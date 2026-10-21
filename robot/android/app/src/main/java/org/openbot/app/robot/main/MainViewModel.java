package org.openbot.app.robot.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.openbot.app.robot.model.SubCategory;
import org.openbot.app.robot.vehicle.Vehicle;

public class MainViewModel extends ViewModel {

  private final MutableLiveData<SubCategory> selectedMode = new MutableLiveData<>();

  public void selectMode(SubCategory subCategory) {
    selectedMode.setValue(subCategory);
  }

  public LiveData<SubCategory> getSelectedMode() {
    return selectedMode;
  }

  private final MutableLiveData<String> usbData = new MutableLiveData<>();

  public void setDeviceData(String data) {
    usbData.setValue(data);
  }

  public LiveData<String> getDeviceData() {
    return usbData;
  }

  private final MutableLiveData<Vehicle> vehicle = new MutableLiveData<>();

  public void setVehicle(Vehicle data) {
    vehicle.setValue(data);
  }

  public LiveData<Vehicle> getVehicle() {
    return vehicle;
  }

  private final MutableLiveData<Boolean> usbStatus = new MutableLiveData<>();

  public void setUsbStatus(Boolean data) {
    usbStatus.setValue(data);
  }

  public LiveData<Boolean> getUsbStatus() {
    return usbStatus;
  }
}
