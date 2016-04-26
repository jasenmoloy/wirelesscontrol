package jasenmoloy.wirelesscontrol.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.managers.LocationServicesManager;
import jasenmoloy.wirelesscontrol.mvp.MainPresenterImpl;

/**
 * Created by jasenmoloy on 3/14/16.
 */
public class AutonomousGeofenceHandlerService extends Service {
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
            return intentFilter;
        }

        public void onReceive(Context context, Intent intent) {
            Debug.logDebug(TAG, "onReceive() - action:" + intent.getAction());

            switch(intent.getAction()) {
                case Constants.BROADCAST_ACTION_GEODATA_LOADED:
                    mLocationServices.performLocationServices();
                    ArrayList<GeofenceData> geoData = intent.getParcelableArrayListExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA);
                    mLocationServices.sendGeofenceData(geoData);
                    break;
                case Constants.BROADCAST_ACTION_PERMISSIONS_GRANTED:
                    initializeLocationServices();
                    break;
                case Constants.BROADCAST_ACTION_SAVE_GEOFENCE:
                    GeofenceData geofence = intent.getParcelableExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA);
                    mLocationServices.sendGeofenceData(geofence);
                    break;

            }
        }
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    ResponseReceiver mReceiver;
    LocationServicesManager mLocationServices;

    /// ----------------------
    /// Getters / Setters
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public AutonomousGeofenceHandlerService() {
        mReceiver = new ResponseReceiver();
        mLocationServices = new LocationServicesManager(this);
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
                sendBroadcast(Constants.BROADCAST_ACTION_LOCATIONSERVICES_CONNECTED);
            }
        }).start();
    }

    private void sendBroadcast(String action) {
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(AutonomousGeofenceHandlerService.this).sendBroadcast(intent);
    }
}
