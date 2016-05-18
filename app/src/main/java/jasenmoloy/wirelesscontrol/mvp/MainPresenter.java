package jasenmoloy.wirelesscontrol.mvp;

import android.app.Application;

import jasenmoloy.wirelesscontrol.io.OnGeofenceDataLoadFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface MainPresenter extends OnGeofenceDataLoadFinishedListener, Application.ActivityLifecycleCallbacks {
    void onAllPermissionsGranted();

    void onCardClicked(int position);
}
