package jasenmoloy.wirelesscontrol.mvp;

import java.util.ArrayList;
import java.util.List;

import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataLoadFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface MainModel {
    void addGeofence(GeofenceData data);
    void addGeofence(List<GeofenceData> data);
    ArrayList<GeofenceData> getGeofenceData();
}
