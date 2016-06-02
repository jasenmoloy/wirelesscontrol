package jasenmoloy.wirelesscontrol.adapters;

import android.support.v4.content.LocalBroadcastManager;

import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.io.OnGeofenceSaveFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface AddGeofencePresenter extends OnGeofenceSaveFinishedListener {
    void registerReceiver(LocalBroadcastManager broadcastManager);

    void initializeMapPosition();

    void saveGeofence(GeofenceData geofenceData);

    void onCreate();

    void onDestroy();
}
