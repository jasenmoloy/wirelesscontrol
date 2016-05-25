package jasenmoloy.wirelesscontrol.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

import jasenmoloy.wirelesscontrol.R;
import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataDeleteFinishedListener;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataLoadFinishedListener;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataUpdateFinishedListener;
import jasenmoloy.wirelesscontrol.io.OnGeofenceSaveFinishedListener;
import jasenmoloy.wirelesscontrol.managers.GeofenceDataManager;
import jasenmoloy.wirelesscontrol.managers.LocationServicesManager;
import jasenmoloy.wirelesscontrol.ui.MainActivity;

/**
 * Created by jasenmoloy on 3/14/16.
 */
public class GeofenceHandlerService extends Service implements
        OnGeofenceDataLoadFinishedListener, OnGeofenceSaveFinishedListener {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = GeofenceHandlerService.class.getSimpleName();
    private static final int APP_NOTIFICATION_ID = 55;

    public class ServiceBinder extends Binder {
        public GeofenceHandlerService getService() {
            return GeofenceHandlerService.this;
        }
    }

    public class GlobalResponseReceiver extends BroadcastReceiver {
        public IntentFilter buildIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            return intentFilter;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Debug.logDebug(TAG, "onReceive() - action:" + intent.getAction());

            switch(intent.getAction()) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        switch(networkInfo.getDetailedState()) {
                            case CONNECTED:
                                if(networkInfo.isConnected())
                                    Debug.logVerbose(TAG, networkInfo.getTypeName() + " is connected. Disabling location updates.");
                                    mLocationServices.disableLocationUpdates();
                                break;
                            case DISCONNECTED:
                                if(!networkInfo.isConnected()) {
                                    Debug.logVerbose(TAG, networkInfo.getTypeName() + " is disconnected. Enabling location updates.");
                                    mLocationServices.enableLocationUpdates();
                                }
                                break;
                        }
                    }
                    break;
            }
        }

        // Prevents instantiation
        private GlobalResponseReceiver() {

        }
    }

    public class LocalResponseReceiver extends BroadcastReceiver {
        public IntentFilter buildIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEODATA_LOADED);
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEODATA_REQUEST);
            intentFilter.addAction(Constants.BROADCAST_ACTION_PERMISSIONS_GRANTED);
            intentFilter.addAction(Constants.BROADCAST_ACTION_SAVE_GEOFENCE);
            intentFilter.addAction(Constants.BROADCAST_ACTION_UPDATE_GEOFENCE);
            intentFilter.addAction(Constants.BROADCAST_ACTION_DELETE_GEOFENCE);
            intentFilter.addAction(Constants.ACTION_NOTIFICATION_UPDATE);
            return intentFilter;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Debug.logDebug(TAG, "onReceive() - action:" + intent.getAction());
            int id;

            switch(intent.getAction()) {
                case Constants.BROADCAST_ACTION_GEODATA_LOADED:
                    mLocationServices.performLocationServices();
                    ArrayList<GeofenceData> geoData = intent.getParcelableArrayListExtra(Constants.BROADCAST_EXTRA_KEY_GEODATALIST);
                    mLocationServices.initGeofenceData(geoData);
                    break;
                case Constants.BROADCAST_ACTION_GEODATA_REQUEST:
                    Intent newIntent = new Intent(Constants.BROADCAST_ACTION_GEODATA_DELIVERY);
                    newIntent.putParcelableArrayListExtra(Constants.BROADCAST_EXTRA_KEY_GEODATALIST, mGeofenceDataManager.getGeofenceData());
                    LocalBroadcastManager.getInstance(GeofenceHandlerService.this).sendBroadcast(newIntent);
                    break;
                case Constants.BROADCAST_ACTION_PERMISSIONS_GRANTED:
                    initializeLocationServices();
                    break;
                case Constants.BROADCAST_ACTION_SAVE_GEOFENCE:
                    mNewGeofence = intent.getParcelableExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA);
                    mGeofenceDataManager.addGeofence(mNewGeofence, GeofenceHandlerService.this);
                    mLocationServices.sendGeofenceData(mNewGeofence);
                    break;
                case Constants.BROADCAST_ACTION_UPDATE_GEOFENCE:
                    id = intent.getIntExtra(Constants.BROADCAST_EXTRA_KEY_GEOFENCE_ID, -1);
                    GeofenceData updateData = intent.getParcelableExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA);
                    mLocationServices.updateGeofenceData(id, updateData);
                    mGeofenceDataManager.updateGeofence(id, updateData, new OnGeofenceDataUpdateFinishedListener() {
                        @Override
                        public void onGeofenceDataUpdateError() {
                            Intent intent = new Intent(Constants.BROADCAST_ACTION_GEOFENCE_UPDATED);
                            intent.putExtra(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, false);
                            LocalBroadcastManager.getInstance(GeofenceHandlerService.this).sendBroadcast(intent);
                        }

                        @Override
                        public void onGeofenceDataUpdateSuccess(int position, GeofenceData updatedGeofence) {
                            Intent intent = new Intent(Constants.BROADCAST_ACTION_GEOFENCE_UPDATED);
                            Bundle intentBundle = new Bundle();
                            intentBundle.putBoolean(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, true);
                            intentBundle.putInt(Constants.BROADCAST_EXTRA_KEY_GEOFENCE_ID, position);
                            intentBundle.putParcelable(Constants.BROADCAST_EXTRA_KEY_GEODATA, updatedGeofence);
                            intent.putExtras(intentBundle);
                            LocalBroadcastManager.getInstance(GeofenceHandlerService.this).sendBroadcast(intent);
                        }
                    });
                    break;
                case Constants.BROADCAST_ACTION_DELETE_GEOFENCE:
                    id = intent.getIntExtra(Constants.BROADCAST_EXTRA_KEY_GEOFENCE_ID, -1);
                    mLocationServices.deleteGeofence(id);
                    mGeofenceDataManager.deleteGeofence(id, new OnGeofenceDataDeleteFinishedListener() {
                        @Override
                        public void onGeofenceDataDeleteError() {
                            Intent intent = new Intent(Constants.BROADCAST_ACTION_GEOFENCE_DELETED);
                            intent.putExtra(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, false);
                            LocalBroadcastManager.getInstance(GeofenceHandlerService.this).sendBroadcast(intent);
                        }

                        @Override
                        public void onGeofenceDataDeleteSuccess() {
                            Intent intent = new Intent(Constants.BROADCAST_ACTION_GEOFENCE_DELETED);
                            intent.putExtra(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, true);
                            LocalBroadcastManager.getInstance(GeofenceHandlerService.this).sendBroadcast(intent);
                        }
                    });
                    break;
                case Constants.ACTION_NOTIFICATION_UPDATE:
                    String notificationContent = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_CONTENT);
                    UpdateNotification(notificationContent);
                    break;
            }
        }

        // Prevents instantiation
        private LocalResponseReceiver() {

        }
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    GlobalResponseReceiver mGlobalReceiver;
    LocalResponseReceiver mLocalReceiver;
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

    public GeofenceHandlerService() {
        mGlobalReceiver = new GlobalResponseReceiver();
        mLocalReceiver = new LocalResponseReceiver();
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
        Debug.logDebug(TAG, "onCreate()");

        //Register the receivers
        registerReceiver(mGlobalReceiver, mGlobalReceiver.buildIntentFilter());
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalReceiver, mLocalReceiver.buildIntentFilter());

        Notification notification = buildNotification("Initialized");

        //JAM TODO: Set up the task stack for the user to enter the app through the notification
        Intent notifIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);

        startForeground(APP_NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        Debug.logDebug(TAG, "onDestroy()");

        //Unregister all receivers
        unregisterReceiver(mGlobalReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalReceiver);

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
        GoogleApiClient.ConnectionCallbacks callback = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                //Load Geofence data
                mGeofenceDataManager.loadSavedGeofences(GeofenceHandlerService.this);

                //Determine if we should turn on location updates
                determineLocationUpdates();
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        };

        //Create a thread to wait until the blocking connect call is made
        if(!mLocationServices.isConnected())
            mLocationServices.connect(callback);
        else
            callback.onConnected(null);
    }

    private void sendBroadcast(String action) {
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(GeofenceHandlerService.this).sendBroadcast(intent);
    }

    private void sendGeofenceLoadBroadcast(ArrayList<GeofenceData> geofenceData) {
        //Let the service know to set up geofences to track
        Intent intent = new Intent(Constants.BROADCAST_ACTION_GEODATA_LOADED);
        Bundle intentBundle = new Bundle();
        intentBundle.putParcelableArrayList(Constants.BROADCAST_EXTRA_KEY_GEODATALIST, geofenceData);
        intent.putExtras(intentBundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendGeofenceSaveBroadcast(boolean success) {;
        Intent intent = new Intent(Constants.BROADCAST_ACTION_GEOFENCE_SAVED);
        Bundle intentBundle = new Bundle();
        intentBundle.putBoolean(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, success);
        intentBundle.putParcelable(Constants.BROADCAST_EXTRA_KEY_GEODATA, mNewGeofence);
        intent.putExtras(intentBundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        mNewGeofence = null;
    }

    private void determineLocationUpdates() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(networkInfo.isConnected()) {
            mLocationServices.disableLocationUpdates();
        }
    }

    private void UpdateNotification(String content) {
        NotificationManager notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifManager.notify(APP_NOTIFICATION_ID, buildNotification(content));
    }

    private Notification buildNotification(String content) {
        return new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
    }
}
