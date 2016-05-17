package jasenmoloy.wirelesscontrol.mvp;

import android.app.Application;

import jasenmoloy.wirelesscontrol.data.GeofenceData;

/**
 * Created by jasenmoloy on 5/13/16.
 */
public interface EditGeofencePresenter extends Application.ActivityLifecycleCallbacks {
    void saveGeofence(int id, GeofenceData geofenceData);
}
