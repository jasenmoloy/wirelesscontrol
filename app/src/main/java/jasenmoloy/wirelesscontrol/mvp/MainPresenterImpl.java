package jasenmoloy.wirelesscontrol.mvp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

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

    private static final String TAG = MainPresenterImpl.class.getSimpleName();

    public class ResponseReceiver extends BroadcastReceiver {
        public IntentFilter buildIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEODATA_LOADED);
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEODATA_DELIVERY);
            intentFilter.addAction(Constants.BROADCAST_ACTION_PERMISSION_REQUESTED);
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEOFENCE_SAVED);
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEOFENCE_UPDATED);
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEOFENCE_DELETED);
            return intentFilter;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Debug.logDebug(TAG, "onReceive() - action:" + intent.getAction());

            ArrayList<GeofenceData> geoData;

            switch(intent.getAction()) {
                case Constants.BROADCAST_ACTION_GEODATA_LOADED:
                    geoData = intent.getParcelableArrayListExtra(Constants.BROADCAST_EXTRA_KEY_GEODATALIST);
                    mModel.initGeofences(geoData);

                    //Only reload the view's display if we're active, else the onStart() will handle it.
                    switch(mViewState) {
                        case RESUMED:
                        case PAUSED:
                            reloadGeofenceCards();
                            break;
                    }
                    break;
                case Constants.BROADCAST_ACTION_GEODATA_DELIVERY:
                    geoData = intent.getParcelableArrayListExtra(Constants.BROADCAST_EXTRA_KEY_GEODATALIST);
                    mModel.initGeofences(geoData);

                    //Only reload the view's display if we're active, else the onStart() will handle it.
                    switch(mViewState) {
                        case RESUMED:
                        case PAUSED:
                            reloadGeofenceCards();
                            break;
                    }
                    break;
                case Constants.BROADCAST_ACTION_PERMISSION_REQUESTED:
                    //JAM TODO: Handle when permissions are requested.
                    //mView.checkPermissions();
                    break;
                case Constants.BROADCAST_ACTION_GEOFENCE_SAVED:
                    if( intent.getBooleanExtra(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, false) ) {
                        mModel.addGeofence((GeofenceData) intent.getParcelableExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA));

                        //Only reload the view's display if we're active, else the onStart() will handle it.
                        switch(mViewState) {
                            case RESUMED:
                            case PAUSED:
                                reloadGeofenceCards();
                                break;
                        }
                    }
                    break;
                case Constants.BROADCAST_ACTION_GEOFENCE_UPDATED:
                    if( intent.getBooleanExtra(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, false) ) {
                        mModel.updateGeofence(intent.getIntExtra(Constants.BROADCAST_EXTRA_KEY_GEOFENCE_ID, -1), (GeofenceData) intent.getParcelableExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA));

                        //Only reload the view's display if we're active, else the onStart() will handle it.
                        switch(mViewState) {
                            case RESUMED:
                            case PAUSED:
                                reloadGeofenceCards();
                                break;
                        }
                    }
                    break;
                case Constants.BROADCAST_ACTION_GEOFENCE_DELETED:
                    if( intent.getBooleanExtra(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, false) ) {
                        MainPresenterImpl.this.mLocalBroadcastManager.sendBroadcast(new Intent(Constants.BROADCAST_ACTION_GEODATA_REQUEST));
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

    enum ViewState {
        CREATED,
        STARTED,
        RESUMED,
        PAUSED,
        STOPPED,
        DESTROYED
    }

    MainModel mModel;
    MainView mView;
    ResponseReceiver mReceiver;
    LocalBroadcastManager mLocalBroadcastManager;

    Context mContext;

    ViewState mViewState;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public MainPresenterImpl(MainView view, Context context) {
        mView = view;
        mModel = new MainModelImpl(context);
        mReceiver = new ResponseReceiver();

        mContext = context;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
    }

    public boolean allowNewGeofence() {
        if(mModel.getGeofenceData().size() < MAX_ALLOWABLE_GEOFENCES)
            return true;

        return false;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mViewState = ViewState.CREATED;
        mLocalBroadcastManager.registerReceiver(mReceiver, mReceiver.buildIntentFilter());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mViewState = ViewState.STARTED;

        reloadGeofenceCards();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mViewState = ViewState.RESUMED;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        mViewState = ViewState.PAUSED;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        mViewState = ViewState.STOPPED;
        mView.unloadGeofenceCards();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mViewState = ViewState.DESTROYED;

        mLocalBroadcastManager.unregisterReceiver(mReceiver);
        mView = null;
    }

    @Override
    public void onAllPermissionsGranted() {
        Debug.logDebug(TAG, "onAllPermissionsGranted()");

        mLocalBroadcastManager.sendBroadcast(new Intent(Constants.BROADCAST_ACTION_PERMISSIONS_GRANTED));
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

    private void reloadGeofenceCards() {
        //Let the UI know the data is loaded.
        if(mModel.isGeofenceDataInitialized())
            mView.loadGeofenceCards(mModel.getGeofenceData());
    }
}
