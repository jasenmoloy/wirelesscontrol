package jasenmoloy.wirelesscontrol.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;

import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.application.debug.Debug;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class AddGeofencePresenterImpl implements AddGeofencePresenter {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = AddGeofencePresenterImpl.class.getSimpleName();

    private class ResponseReceiver extends BroadcastReceiver {
        public IntentFilter buildIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEOFENCE_SAVED);
            return intentFilter;
        }

        @Override
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

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private AddGeofenceModel mModel;
    private AddGeofenceView mView;

    private LocalBroadcastManager mBroadcastManager;
    private ResponseReceiver mReceiver;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public AddGeofencePresenterImpl(AddGeofenceView view, Context context) {
        mView = view;
        mModel = new AddGeofenceModelImpl(context);
        mReceiver = new ResponseReceiver();
    }

    @Override
    public void registerReceiver(LocalBroadcastManager broadcastManager) {
        mBroadcastManager = broadcastManager;
        mBroadcastManager.registerReceiver(mReceiver, mReceiver.buildIntentFilter());
    }

    @Override
    public void initializeMapPosition() {
        mModel.acquireLastKnownLocation(new AddGeofenceModel.Callback() {
            @Override
            public void onSuccess(Object acquiredObject) {
                mView.initializeMyLocationOnMap((Location) acquiredObject);
            }

            @Override
            public void onFailure(int errorCode) {
                mView.displayLocationNotFoundToast();
            }
        });
    }

    @Override
    public void saveGeofence(GeofenceData data) {
        mModel.save(data, this);
        //JAM TODO: Tell the model to save out the data and let me know when it's done.
        //JAM TODO: Once the model is done saving, let the view know to send the user back to the main screen.
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
        mBroadcastManager.unregisterReceiver(mReceiver);
        mModel.onDestroy();
    }

    @Override
    public void onGeofenceSaveSuccess() {
        mView.onGeofenceSaveSuccess();
    }

    @Override
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
