package jasenmoloy.wirelesscontrol.mvp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

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

        public void onReceive(Context context, Intent intent) {
            Debug.logDebug(TAG, "onReceive() - action:" + intent.getAction());

            switch(intent.getAction()) {
                case Constants.BROADCAST_ACTION_LOCATIONSERVICES_CONNECTED:
                    mModel.loadGeofenceData(MainPresenterImpl.this);
                    break;
                case Constants.BROADCAST_ACTION_PERMISSION_REQUESTED:
                    mView.checkPermissions();
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
        mModel = new MainModelImpl();
        mReceiver = new ResponseReceiver();

        mContext = context;
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    public void registerReceiver(LocalBroadcastManager broadcastManager) {
        IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_ACTION_LOCATIONSERVICES_CONNECTED);
        intentFilter.addAction(Constants.BROADCAST_ACTION_PERMISSION_REQUESTED);

        mBroadcastManager = broadcastManager;

        mBroadcastManager.registerReceiver(mReceiver, intentFilter);
    }

    public void onResume() {
    }

    public void onDestroy() {
        mBroadcastManager.unregisterReceiver(mReceiver);
        mView = null;
    }

    public void onCardClicked(int position) {
        //JAM TODO: Open up an "edit geofence" activity
    }

    public void onAllPermissionsGranted() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.BROADCAST_ACTION_PERMISSIONS_GRANTED));
    }

    public void onGeofenceDataLoadSuccess(GeofenceData[] geofenceData) {
        List<GeofenceData> list = new Vector<GeofenceData>(geofenceData.length, 1); //Set capacityIncrement to 1 as the user will usually only add one more additional geofence at a time.

        //JAM TODO: Why is this set to a Vector?
        for (GeofenceData data: geofenceData) {
            list.add(data);
        }

        //Let the service know to set up geofences to track
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        Intent intent = new Intent(Constants.BROADCAST_ACTION_GEODATA_LOADED);
        Bundle intentBundle = new Bundle();
        intentBundle.putParcelableArray(Constants.BROADCAST_EXTRA_KEY_GEODATA, geofenceData);
        intent.putExtras(intentBundle);
        lbm.sendBroadcast(intent);

        //Let the UI know the data is loaded.
        mView.onCardDataLoaded(list);
    }

    public void onGeofenceDataLoadError() {

    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------
}
