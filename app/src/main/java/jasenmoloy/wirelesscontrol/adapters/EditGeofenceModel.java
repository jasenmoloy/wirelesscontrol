package jasenmoloy.wirelesscontrol.adapters;

import jasenmoloy.wirelesscontrol.data.GeofenceData;

/**
 * Created by jasenmoloy on 5/13/16.
 */
public interface EditGeofenceModel {
    void updateGeofence(int id, GeofenceData data);
    void deleteGeofence(int id);
}
