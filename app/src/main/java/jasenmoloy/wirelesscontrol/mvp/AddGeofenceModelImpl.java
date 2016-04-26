package jasenmoloy.wirelesscontrol.mvp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.io.OnGeofenceSaveFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class AddGeofenceModelImpl implements AddGeofenceModel {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    public static final String TAG = "AddGeofenceModelImpl";

    /// ----------------------
    /// Object Fields
    /// ----------------------
    private Context mContext;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public AddGeofenceModelImpl(Context context) {
        mContext = context;
    }

    public void save(GeofenceData data, OnGeofenceSaveFinishedListener listener) {
        Debug.logVerbose(TAG, "GeofenceData data.name: " + data.name);
        Debug.logVerbose(TAG, "GeofenceData data.lat:" + data.position.latitude + " data.long:" + data.position.longitude);
        Debug.logVerbose(TAG, "GeofenceData data.radius: " + data.radius);

        //Let the service know to set up geofences to track
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        Intent intent = new Intent(Constants.BROADCAST_ACTION_SAVE_GEOFENCE);
        Bundle intentBundle = new Bundle();
        intentBundle.putParcelable(Constants.BROADCAST_EXTRA_KEY_GEODATA, data);
        intent.putExtras(intentBundle);
        lbm.sendBroadcast(intent);
    }

    public void onCreate() {

    }

    public void onDestroy() {

    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------
}
