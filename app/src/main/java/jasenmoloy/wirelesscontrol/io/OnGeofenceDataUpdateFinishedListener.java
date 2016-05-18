package jasenmoloy.wirelesscontrol.io;

import jasenmoloy.wirelesscontrol.data.GeofenceData;

/**
 * Created by jasenmoloy on 5/16/16.
 */
public interface OnGeofenceDataUpdateFinishedListener {
    void onGeofenceDataUpdateError();

    void onGeofenceDataUpdateSuccess(int position, GeofenceData updatedGeofence);
}
