package jasenmoloy.wirelesscontrol.io;

import jasenmoloy.wirelesscontrol.data.GeofenceData;

/**
 * Created by jasenmoloy on 2/26/16.
 */
public interface OnGeofenceDataLoadFinishedListener {
    void onGeofenceDataLoadError();

    void onGeofenceDataLoadSuccess(GeofenceData[] geofenceData);
}
