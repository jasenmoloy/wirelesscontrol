package jasenmoloy.wirelesscontrol.mvp;

import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.io.OnGeofenceSaveFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface AddGeofencePresenter extends OnGeofenceSaveFinishedListener {
    void SaveGeofence(GeofenceData geofenceData);
}
