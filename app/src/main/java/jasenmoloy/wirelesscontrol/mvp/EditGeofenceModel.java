package jasenmoloy.wirelesscontrol.mvp;

import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataUpdateFinishedListener;
import jasenmoloy.wirelesscontrol.io.OnGeofenceSaveFinishedListener;

/**
 * Created by jasenmoloy on 5/13/16.
 */
public interface EditGeofenceModel {
    void updateGeofence(int id, GeofenceData data);
    void deleteGeofence(int id);
}
