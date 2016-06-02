package jasenmoloy.wirelesscontrol.adapters.io;

import java.util.ArrayList;

import jasenmoloy.wirelesscontrol.data.GeofenceData;

/**
 * Created by jasenmoloy on 2/26/16.
 */
public interface OnGeofenceDataLoadFinishedListener {
    void onGeofenceDataLoadError();

    void onGeofenceDataLoadSuccess(ArrayList<GeofenceData> geofenceData);
}
