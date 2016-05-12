package jasenmoloy.wirelesscontrol.mvp;

import android.app.Application;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import jasenmoloy.wirelesscontrol.io.OnGeofenceDataLoadFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface MainPresenter extends OnGeofenceDataLoadFinishedListener, Application.ActivityLifecycleCallbacks {
    void registerReceiver(LocalBroadcastManager broadcastManager);

    void onAllPermissionsGranted();

    void onCardClicked(int position);
}
