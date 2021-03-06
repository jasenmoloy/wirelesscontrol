package jasenmoloy.wirelesscontrol.adapters.io;

import jasenmoloy.wirelesscontrol.application.data.GeofenceData;

/**
 * Created by jasenmoloy on 5/16/16.
 */
public interface OnGeofenceDataUpdateFinishedListener {
    void onGeofenceDataUpdateError();

    void onGeofenceDataUpdateSuccess(int position, GeofenceData updatedGeofence);
}
