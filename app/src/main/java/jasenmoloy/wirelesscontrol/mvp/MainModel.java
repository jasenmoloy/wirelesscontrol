package jasenmoloy.wirelesscontrol.mvp;

import java.util.ArrayList;

import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataLoadFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface MainModel {
    void loadGeofenceData(OnGeofenceDataLoadFinishedListener listener);
    void addGeofence(GeofenceData data);
    ArrayList<GeofenceData> getGeofenceData();
}
