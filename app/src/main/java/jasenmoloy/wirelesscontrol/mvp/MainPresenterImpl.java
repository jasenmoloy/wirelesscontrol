package jasenmoloy.wirelesscontrol.mvp;

import android.app.Activity;
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

        public IntentFilter buildIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEODATA_LOADED);
            intentFilter.addAction(Constants.BROADCAST_ACTION_PERMISSION_REQUESTED);
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEOFENCE_SAVED);
            return intentFilter;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Debug.logDebug(TAG, "onReceive() - action:" + intent.getAction());

            switch(intent.getAction()) {
                case Constants.BROADCAST_ACTION_GEODATA_LOADED:
                    ArrayList<GeofenceData> geoData = intent.getParcelableArrayListExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA);
                    mModel.addGeofence(geoData);
                    reloadGeofenceData(geoData);
                    break;
                case Constants.BROADCAST_ACTION_PERMISSION_REQUESTED:
                    mView.checkPermissions();
                    break;
                case Constants.BROADCAST_ACTION_GEOFENCE_SAVED:
                    if( intent.getBooleanExtra(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, false) ) {
                        mModel.addGeofence((GeofenceData) intent.getParcelableExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA));
                        reloadGeofenceData(mModel.getGeofenceData());
                    }
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

    @Override
    public void registerReceiver(LocalBroadcastManager broadcastManager) {
        mBroadcastManager = broadcastManager;
        mBroadcastManager.registerReceiver(mReceiver, mReceiver.buildIntentFilter());
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        //JAM TODO: Issue with this as the activity will current start before the card data is loaded.
        //mView.loadGeofenceCards(mModel.getGeofenceData());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Debug.logDebug(TAG, "JAM - onResume()");
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        //JAM TODO: Issue with this as the activity will current start before the card data is loaded so unloading it will cause problems.
        //mView.unloadGeofenceCards();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Debug.logDebug(TAG, "JAM - onDestroy()");

        mBroadcastManager.unregisterReceiver(mReceiver);
        mView = null;
    }

    @Override
    public void onCardClicked(int position) {
        //JAM TODO: Open up an "edit geofence" activity
    }

    @Override
    public void onAllPermissionsGranted() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.BROADCAST_ACTION_PERMISSIONS_GRANTED));
    }

    @Override
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

    @Override
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
