package jasenmoloy.wirelesscontrol.mvp;

import jasenmoloy.wirelesscontrol.io.OnGeofenceDataLoadFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface MainModel {
    void loadGeofenceData(OnGeofenceDataLoadFinishedListener listener);
}
