package jasenmoloy.wirelesscontrol.mvp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class AddGeofencePresenterImpl implements AddGeofencePresenter {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = "AddGeofencePresenterImpl";

    /// ----------------------
    /// Object Fields
    /// ----------------------

    public class ResponseReceiver extends BroadcastReceiver {
        private static final String TAG = "AddGeofencePresenterImpl.ResponseReceiver";

        public IntentFilter buildIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEOFENCE_SAVED);
            return intentFilter;
        }

        public void onReceive(Context context, Intent intent) {
            Debug.logDebug(TAG, "onReceive() - action:" + intent.getAction());

            switch(intent.getAction()) {
                case Constants.BROADCAST_ACTION_GEOFENCE_SAVED:
                    if( intent.getBooleanExtra(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, false) )
                        AddGeofencePresenterImpl.this.onGeofenceSaveSuccess();
                    else
                        AddGeofencePresenterImpl.this.onGeofenceSaveError();
                    break;
            }
        }

        // Prevents instantiation
        private ResponseReceiver() {

        }
    }

    AddGeofenceModel mModel;
    AddGeofenceView mView;

    LocalBroadcastManager mBroadcastManager;
    ResponseReceiver mReceiver;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public AddGeofencePresenterImpl(AddGeofenceView view, Context context) {
        mView = view;
        mModel = new AddGeofenceModelImpl(context);
        mReceiver = new ResponseReceiver();
    }

    public void registerReceiver(LocalBroadcastManager broadcastManager) {
        mBroadcastManager = broadcastManager;
        mBroadcastManager.registerReceiver(mReceiver, mReceiver.buildIntentFilter());
    }

    public void saveGeofence(GeofenceData data) {
        mModel.save(data, this);
        //JAM TODO: Tell the model to save out the data and let me know when it's done.
        //JAM TODO: Once the model is done saving, let the view know to send the user back to the main screen.
    }

    public void onCreate() {
        mModel.onCreate();
    }

    public void onDestroy() {
        mModel.onDestroy();
        mBroadcastManager.unregisterReceiver(mReceiver);
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    public void onGeofenceSaveSuccess() {
        mView.onGeofenceSaveSuccess();
    }

    public void onGeofenceSaveError() {
        //JAM TODO: Determine the issue and notify the view with the appropriate action.
        mView.onGeofenceSaveError();
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

}
