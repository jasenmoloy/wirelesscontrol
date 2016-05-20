package jasenmoloy.wirelesscontrol.mvp;

import java.util.ArrayList;
import java.util.List;

import jasenmoloy.wirelesscontrol.data.GeofenceData;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface MainModel {
    boolean isGeofenceDataInitialized();

    ArrayList<GeofenceData> getGeofenceData();

    void initGeofences(List<GeofenceData> data);

    void addGeofence(GeofenceData data);

    void updateGeofence(int id, GeofenceData newData);
}
