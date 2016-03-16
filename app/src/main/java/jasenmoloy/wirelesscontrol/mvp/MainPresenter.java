package jasenmoloy.wirelesscontrol.mvp;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import jasenmoloy.wirelesscontrol.io.OnGeofenceDataLoadFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface MainPresenter extends OnGeofenceDataLoadFinishedListener {
    void registerReceiver(LocalBroadcastManager broadcastManager);

    void onResume();

    void onDestroy();

    void onAllPermissionsGranted();

    void onCardClicked(int position);
}
