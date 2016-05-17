package jasenmoloy.wirelesscontrol.ui;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.squareup.leakcanary.LeakCanary;

import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.service.AutonomousGeofenceHandlerService;

/**
 * Created by jasenmoloy on 5/17/16.
 */
public class WirelessControlApplication extends Application {

    public static final String TAG = WirelessControlApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        Debug.logDebug(TAG, "onCreate()");

        //LeakCanary initialization
        LeakCanary.install(this);

        //Start the background service that will be alive for as long as possible
        initializeBackgroundService();
    }

    /**
     * Start the autonomous geofence handler service which loads and tracks geofences
     */
    private void initializeBackgroundService() {
        Intent intent = new Intent(this, AutonomousGeofenceHandlerService.class);
        startService(intent);
    }
}
