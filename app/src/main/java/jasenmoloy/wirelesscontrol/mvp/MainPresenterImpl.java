package jasenmoloy.wirelesscontrol.mvp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class MainPresenterImpl implements MainPresenter {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = "MainPresenterImpl";

    public class ResponseReceiver extends BroadcastReceiver {
        private static final String TAG = "MainPresenterImpl.ResponseReceiver";

        // Prevents instantiation
        private ResponseReceiver() {

        }

        public IntentFilter buildIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_ACTION_LOCATIONSERVICES_CONNECTED);
            intentFilter.addAction(Constants.BROADCAST_ACTION_PERMISSION_REQUESTED);
            intentFilter.addAction(Constants.BROADCAST_ACTION_SAVE_GEOFENCE);
            return intentFilter;
        }

        public void onReceive(Context context, Intent intent) {
            Debug.logDebug(TAG, "onReceive() - action:" + intent.getAction());

            switch(intent.getAction()) {
                case Constants.BROADCAST_ACTION_LOCATIONSERVICES_CONNECTED:
                    mModel.loadGeofenceData(MainPresenterImpl.this);
                    break;
                case Constants.BROADCAST_ACTION_PERMISSION_REQUESTED:
                    mView.checkPermissions();
                case Constants.BROADCAST_ACTION_SAVE_GEOFENCE:
                    GeofenceData geofence = intent.getParcelableExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA);
                    mModel.addGeofence(geofence);
                    reloadGeofenceData(mModel.getGeofenceData());
                    break;
            }
        }
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    MainModel mModel;
    MainView mView;
    ResponseReceiver mReceiver;
    LocalBroadcastManager mBroadcastManager;

    Context mContext;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public MainPresenterImpl(MainView view, Context context) {
        mView = view;
        mModel = new MainModelImpl(context);
        mReceiver = new ResponseReceiver();

        mContext = context;
    }

    public void registerReceiver(LocalBroadcastManager broadcastManager) {
        mBroadcastManager = broadcastManager;
        mBroadcastManager.registerReceiver(mReceiver, mReceiver.buildIntentFilter());
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    public void onResume() {
        Debug.logDebug(TAG, "JAM - onResume()");
    }

    public void onDestroy() {
        Debug.logDebug(TAG, "JAM - onDestroy()");

        mBroadcastManager.unregisterReceiver(mReceiver);
        mView = null;
    }

    public void onCardClicked(int position) {
        //JAM TODO: Open up an "edit geofence" activity
    }

    public void onAllPermissionsGranted() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.BROADCAST_ACTION_PERMISSIONS_GRANTED));
    }

    public void onGeofenceDataLoadSuccess(ArrayList<GeofenceData> geofenceData) {
        //Let the service know to set up geofences to track
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        Intent intent = new Intent(Constants.BROADCAST_ACTION_GEODATA_LOADED);
        Bundle intentBundle = new Bundle();
        intentBundle.putParcelableArrayList(Constants.BROADCAST_EXTRA_KEY_GEODATA, geofenceData);
        intent.putExtras(intentBundle);
        lbm.sendBroadcast(intent);

        reloadGeofenceData(geofenceData);
    }

    public void onGeofenceDataLoadError() {

    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

    private void reloadGeofenceData(ArrayList<GeofenceData> geofenceData) {
        //Let the UI know the data is loaded.
        mView.onCardDataLoaded(geofenceData);
    }
}
