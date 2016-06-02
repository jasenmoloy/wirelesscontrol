package jasenmoloy.wirelesscontrol.adapters;

import android.app.Application;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface MainPresenter extends Application.ActivityLifecycleCallbacks {
    int MAX_ALLOWABLE_GEOFENCES = 25;

    void onAllPermissionsGranted();
    boolean allowNewGeofence();
}
