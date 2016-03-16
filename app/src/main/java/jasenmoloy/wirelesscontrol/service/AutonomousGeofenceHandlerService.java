package jasenmoloy.wirelesscontrol.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.managers.LocationServicesManager;

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

        public void onReceive(Context context, Intent intent) {
            Debug.LogDebug(TAG, "--- onReceive() ---");

            switch(intent.getAction()) {
                case Constants.BROADCAST_ACTION_GEODATA_LOADED:
                    mLocationServices.performLocationServices();
                    GeofenceData[] geoData = (GeofenceData[]) intent.getParcelableArrayExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA);
                    mLocationServices.sendGeofenceData(geoData);
                    break;
                case Constants.BROADCAST_ACTION_PERMISSIONS_GRANTED:
                    initializeLocationServices();
                    break;
            }
        }
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    BroadcastReceiver mReceiver;

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
        IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_ACTION_GEODATA_LOADED);
        intentFilter.addAction(Constants.BROADCAST_ACTION_PERMISSIONS_GRANTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mLocationServices.disconnect();
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

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(AutonomousGeofenceHandlerService.this);

                Intent intent = new Intent(Constants.BROADCAST_ACTION_LOCATIONSERVICES_CONNECTED);
                lbm.sendBroadcast(intent);
            }
        }).start();
    }
}
