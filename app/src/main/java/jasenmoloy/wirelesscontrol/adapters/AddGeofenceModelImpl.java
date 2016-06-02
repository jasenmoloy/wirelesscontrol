package jasenmoloy.wirelesscontrol.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.Map;

import jasenmoloy.wirelesscontrol.application.data.Constants;
import jasenmoloy.wirelesscontrol.application.data.GeofenceData;
import jasenmoloy.wirelesscontrol.application.debug.Debug;
import jasenmoloy.wirelesscontrol.adapters.io.OnGeofenceSaveFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class AddGeofenceModelImpl implements AddGeofenceModel {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = AddGeofenceModelImpl.class.getSimpleName();

    //JAM TODO: Make this generic and implement a useable class for all models that needs simple callbacks.
    private class LocalBroadcastReceiver extends BroadcastReceiver {
        private Map<String, Callback> mCallbacks;

        public IntentFilter buildIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.ACTION_LOCATION_DELIVERY);
            return intentFilter;
        }

        public void attachCallbackToAction(String actionId, Callback callback) {
            Assert.assertNotNull(actionId);
            Assert.assertNotNull(callback);

            //JAM TODO: Compare it to our list of intent filters. Otherwise we don't have a way to call our callback.

            mCallbacks.put(actionId, callback);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Debug.logDebug(TAG, "onReceive() - action:" + intent.getAction());

            String action = intent.getAction();

            if(!mCallbacks.containsKey(action)) {
                Debug.logWarn(TAG, "No callback exists for " + intent.getAction() + "!");
                return;
            }

            switch(action) {
                case Constants.ACTION_LOCATION_DELIVERY:
                    if(intent.getBooleanExtra(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, false)) {
                        mCallbacks.get(action).onSuccess(intent.getParcelableExtra(Constants.EXTRA_KEY_LOCATION));
                    }
                    else {
                        mCallbacks.get(action).onFailure(intent.getIntExtra(Constants.EXTRA_KEY_ERROR_CODE, 0));
                    }
                    break;
            }
        }

        private LocalBroadcastReceiver() {
            mCallbacks = new HashMap<>();
        }
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private Context mContext;
    private LocalBroadcastManager mLocalBroadcaster;
    private LocalBroadcastReceiver mLocalReceiver;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public AddGeofenceModelImpl(Context context) {
        mContext = context;
        mLocalBroadcaster = LocalBroadcastManager.getInstance(mContext);
        mLocalReceiver = new LocalBroadcastReceiver();

        mLocalBroadcaster.registerReceiver(mLocalReceiver, mLocalReceiver.buildIntentFilter());
    }

    @Override
    public void save(GeofenceData data, OnGeofenceSaveFinishedListener listener) {
        Debug.logVerbose(TAG, "GeofenceData data.displayName: " + data.displayName);
        Debug.logVerbose(TAG, "GeofenceData data.name: " + data.name);
        Debug.logVerbose(TAG, "GeofenceData data.lat:" + data.position.latitude + " data.long:" + data.position.longitude);
        Debug.logVerbose(TAG, "GeofenceData data.radius: " + data.radius);

        //Let the service know to set up geofences to track
        Intent intent = new Intent(Constants.BROADCAST_ACTION_SAVE_GEOFENCE);
        intent.putExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA, data);
        mLocalBroadcaster.sendBroadcast(intent);
    }

    @Override
    public void acquireLastKnownLocation(Callback callback) {
        Intent intent = new Intent(Constants.ACTION_LOCATION_REQUEST);
        mLocalReceiver.attachCallbackToAction(Constants.ACTION_LOCATION_DELIVERY, callback);
        mLocalBroadcaster.sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        mLocalBroadcaster.unregisterReceiver(mLocalReceiver);
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------
}
