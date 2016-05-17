package jasenmoloy.wirelesscontrol.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataLoadFinishedListener;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataUpdateFinishedListener;
import jasenmoloy.wirelesscontrol.io.OnGeofenceSaveFinishedListener;
import jasenmoloy.wirelesscontrol.managers.GeofenceDataManager;
import jasenmoloy.wirelesscontrol.managers.LocationServicesManager;

/**
 * Created by jasenmoloy on 3/14/16.
 */
public class AutonomousGeofenceHandlerService extends Service implements
        OnGeofenceDataLoadFinishedListener, OnGeofenceSaveFinishedListener {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = "AutonomousGeofenceHandlerService";

    public class ServiceBinder extends Binder {
        public AutonomousGeofenceHandlerService getService() {
            return AutonomousGeofenceHandlerService.this;
        }
    }

    public class ResponseReceiver extends BroadcastReceiver {
        private static final String TAG = "AutonomousGeofenceHandlerService.ResponseReceiver";

        // Prevents instantiation
        private ResponseReceiver() {

        }

        public IntentFilter buildIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEODATA_LOADED);
            intentFilter.addAction(Constants.BROADCAST_ACTION_PERMISSIONS_GRANTED);
            intentFilter.addAction(Constants.BROADCAST_ACTION_SAVE_GEOFENCE);
            intentFilter.addAction(Constants.BROADCAST_ACTION_UPDATE_GEOFENCE);
            return intentFilter;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Debug.logDebug(TAG, "onReceive() - action:" + intent.getAction());

            switch(intent.getAction()) {
                case Constants.BROADCAST_ACTION_GEODATA_LOADED:
                    mLocationServices.performLocationServices();
                    ArrayList<GeofenceData> geoData = intent.getParcelableArrayListExtra(Constants.BROADCAST_EXTRA_KEY_GEODATALIST);
                    mLocationServices.initGeofenceData(geoData);
                    break;
                case Constants.BROADCAST_ACTION_PERMISSIONS_GRANTED:
                    initializeLocationServices();
                    break;
                case Constants.BROADCAST_ACTION_SAVE_GEOFENCE:
                    mNewGeofence = intent.getParcelableExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA);
                    mGeofenceDataManager.addGeofence(mNewGeofence, AutonomousGeofenceHandlerService.this);
                    mLocationServices.sendGeofenceData(mNewGeofence);
                    break;
                case Constants.BROADCAST_ACTION_UPDATE_GEOFENCE:
                    int id = intent.getIntExtra(Constants.BROADCAST_EXTRA_KEY_GEOFENCE_ID, -1);
                    GeofenceData updateData = intent.getParcelableExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA);

                    mGeofenceDataManager.updateGeofence(id, updateData, new OnGeofenceDataUpdateFinishedListener() {
                        @Override
                        public void onGeofenceDataUpdateError() {
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(AutonomousGeofenceHandlerService.this);
                            Intent intent = new Intent(Constants.BROADCAST_ACTION_GEOFENCE_UPDATED);
                            Bundle intentBundle = new Bundle();
                            intentBundle.putBoolean(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, false);
                            intent.putExtras(intentBundle);
                            lbm.sendBroadcast(intent);
                        }

                        @Override
                        public void onGeofenceDataUpdateSuccess(int position, GeofenceData updatedGeofence) {
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(AutonomousGeofenceHandlerService.this);
                            Intent intent = new Intent(Constants.BROADCAST_ACTION_GEOFENCE_UPDATED);
                            Bundle intentBundle = new Bundle();
                            intentBundle.putBoolean(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, true);
                            intentBundle.putInt(Constants.BROADCAST_EXTRA_KEY_GEOFENCE_ID, position);
                            intentBundle.putParcelable(Constants.BROADCAST_EXTRA_KEY_GEODATA, updatedGeofence);
                            intent.putExtras(intentBundle);
                            lbm.sendBroadcast(intent);
                        }
                    });

                    mLocationServices.updateGeofenceData(id, updateData);
                    break;
            }
        }
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    ResponseReceiver mReceiver;
    LocationServicesManager mLocationServices;
    GeofenceDataManager mGeofenceDataManager;

    //JAM TODO Find a better way to handle this
    GeofenceData mNewGeofence;

    /// ----------------------
    /// Getters / Setters
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public AutonomousGeofenceHandlerService() {
        mReceiver = new ResponseReceiver();
        mLocationServices = new LocationServicesManager(this);
        mGeofenceDataManager = new GeofenceDataManager(this);
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Register the receivers
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mReceiver.buildIntentFilter());
    }

    @Override
    public void onDestroy() {
        mLocationServices.disconnect();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder();
    }

    @Override
    public void onGeofenceDataLoadSuccess(ArrayList<GeofenceData> geofenceData) {
        sendGeofenceLoadBroadcast(geofenceData);
    }

    @Override
    public void onGeofenceDataLoadError() {
        sendGeofenceLoadBroadcast(null);
    }

    @Override
    public void onGeofenceSaveSuccess() {
        sendGeofenceSaveBroadcast(true);
    }

    @Override
    public void onGeofenceSaveError() {
        sendGeofenceSaveBroadcast(false);
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

    private void initializeLocationServices() {
        //Create a thread to wait until the blocking connect call is made
        new Thread(new Runnable() {
            public void run() {
                mLocationServices.connect(true);

                //Load Geofence data
                loadGeofenceData();
            }
        }).start();
    }

    private void loadGeofenceData() {
        mGeofenceDataManager.loadSavedGeofences(this);
    }

    private void sendBroadcast(String action) {
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(AutonomousGeofenceHandlerService.this).sendBroadcast(intent);
    }

    private void sendGeofenceLoadBroadcast(ArrayList<GeofenceData> geofenceData) {
        //Let the service know to set up geofences to track
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(Constants.BROADCAST_ACTION_GEODATA_LOADED);
        Bundle intentBundle = new Bundle();
        intentBundle.putParcelableArrayList(Constants.BROADCAST_EXTRA_KEY_GEODATALIST, geofenceData);
        intent.putExtras(intentBundle);
        lbm.sendBroadcast(intent);
    }

    private void sendGeofenceSaveBroadcast(boolean success) {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(Constants.BROADCAST_ACTION_GEOFENCE_SAVED);
        Bundle intentBundle = new Bundle();
        intentBundle.putBoolean(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, success);
        intentBundle.putParcelable(Constants.BROADCAST_EXTRA_KEY_GEODATA, mNewGeofence);
        intent.putExtras(intentBundle);
        lbm.sendBroadcast(intent);

        mNewGeofence = null;
    }
}
