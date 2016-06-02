package jasenmoloy.wirelesscontrol.adapters;

import android.app.Application;

import jasenmoloy.wirelesscontrol.application.data.GeofenceData;

/**
 * Created by jasenmoloy on 5/13/16.
 */
public interface EditGeofencePresenter extends Application.ActivityLifecycleCallbacks {
    void saveGeofence(int id, GeofenceData geofenceData);
    void deleteGeofence(int id);
}
