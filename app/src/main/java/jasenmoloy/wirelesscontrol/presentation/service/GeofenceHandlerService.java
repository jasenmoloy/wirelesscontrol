package jasenmoloy.wirelesscontrol.presentation.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
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
import jasenmoloy.wirelesscontrol.presentation.ui.MainActivity;

/**
 * Created by jasenmoloy on 3/14/16.
 */
public class GeofenceHandlerService extends Service implements
        OnGeofenceDataLoadFinishedListener, OnGeofenceSaveFinishedListener {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private class ServiceBinder extends Binder {
        public GeofenceHandlerService getService() {
            return GeofenceHandlerService.this;
        }
    }

    private class GlobalResponseReceiver extends BroadcastReceiver {
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

    private class LocalResponseReceiver extends BroadcastReceiver {
        public IntentFilter buildIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEODATA_REQUEST);
            intentFilter.addAction(Constants.ACTION_LOCATION_REQUEST);
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
            Intent newIntent;

            switch(intent.getAction()) {
                case Constants.BROADCAST_ACTION_GEODATA_REQUEST:
                    newIntent = new Intent(Constants.BROADCAST_ACTION_GEODATA_DELIVERY);
                    newIntent.putParcelableArrayListExtra(Constants.BROADCAST_EXTRA_KEY_GEODATALIST, mGeofenceDataManager.getGeofenceData());
                    LocalBroadcastManager.getInstance(GeofenceHandlerService.this).sendBroadcast(newIntent);
                    break;
                case Constants.ACTION_LOCATION_REQUEST:
                    newIntent = new Intent(Constants.ACTION_LOCATION_DELIVERY);
                    Location location = mLocationServices.getLastLocation();

                    newIntent.putExtra(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, location != null); //return true if location is not null

                    if(location != null) {
                        newIntent.putExtra(Constants.EXTRA_KEY_LOCATION, location);
                    }
                    else {
                        newIntent.putExtra(Constants.EXTRA_KEY_ERROR_CODE, 0); //JAM TODO: Provide a proper error code
                    }

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

    private static final String TAG = GeofenceHandlerService.class.getSimpleName();
    private static final int APP_NOTIFICATION_ID = 55;

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private GlobalResponseReceiver mGlobalReceiver;
    private LocalResponseReceiver mLocalReceiver;
    private LocationServicesManager mLocationServices;
    private GeofenceDataManager mGeofenceDataManager;

    //JAM TODO Find a better way to handle this
    private GeofenceData mNewGeofence;

    private PendingIntent mNotificationPendingIntent;

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

        //Build a pending intent that will be used to invoke the app from the notification
        Intent notifIntent = new Intent(this, MainActivity.class);
        mNotificationPendingIntent = TaskStackBuilder.create(this)
        .addNextIntent(notifIntent)
        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = buildForegroundNotification(getString(R.string.notification_content_initialized), mNotificationPendingIntent);

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
        sendGeofenceLoadedBroadcast(geofenceData);

        mLocationServices.performLocationServices();
        mLocationServices.initGeofenceData(geofenceData);
    }

    @Override
    public void onGeofenceDataLoadError() {
        sendGeofenceLoadedBroadcast(null);
    }

    @Override
    public void onGeofenceSaveSuccess() {
        sendGeofenceSavedBroadcast(true);
    }

    @Override
    public void onGeofenceSaveError() {
        sendGeofenceSavedBroadcast(false);
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
                if(!mGeofenceDataManager.isDataLoaded()) {
                    //Load Geofence data
                    mGeofenceDataManager.loadSavedGeofences(GeofenceHandlerService.this);
                }
                else {
                    sendGeofenceLoadedBroadcast(mGeofenceDataManager.getGeofenceData());
                }

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

    private void sendGeofenceLoadedBroadcast(ArrayList<GeofenceData> geofenceData) {
        //Let the service know to set up geofences to track
        Intent intent = new Intent(Constants.BROADCAST_ACTION_GEODATA_LOADED);
        Bundle intentBundle = new Bundle();
        intentBundle.putParcelableArrayList(Constants.BROADCAST_EXTRA_KEY_GEODATALIST, geofenceData);
        intent.putExtras(intentBundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendGeofenceSavedBroadcast(boolean success) {
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
        notifManager.notify(APP_NOTIFICATION_ID, buildForegroundNotification(content, mNotificationPendingIntent));
    }

    private Notification buildForegroundNotification(String content, PendingIntent contentIntent) {
        return new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(content)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_stat_standard)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
    }
}
