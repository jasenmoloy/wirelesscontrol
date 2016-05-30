package jasenmoloy.wirelesscontrol.presentation.ui;

import android.app.Application;
import android.content.Intent;

import com.squareup.leakcanary.LeakCanary;

import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.presentation.service.GeofenceHandlerService;

/**
 * Created by jasenmoloy on 5/17/16.
 */
public class BaseApp extends Application {
    /// ----------------------
    /// Class Fields
    /// ----------------------
    private static final String TAG = BaseApp.class.getSimpleName();

    private static BaseApp msInstance;
    public static BaseApp get() {
        if(msInstance == null) {
            throw new NullPointerException("BaseApp instance is null. Did you set the instance as the first thing in onCreate()?");
        }

        return msInstance;
    }

    /// ----------------------
    /// Public Methods
    /// ----------------------

    @Override
    public void onCreate() {
        super.onCreate();

        Debug.logDebug(TAG, "onCreate()");

        //Set the global instance of our BaseApp
        msInstance = this;

        //LeakCanary initialization
        LeakCanary.install(this);

        //Start the background service that will be alive for as long as possible
        initializeBackgroundService();
    }

    /// ----------------------
    /// Private Methods
    /// ----------------------

    /**
     * Start the autonomous geofence handler service which loads and tracks geofences
     */
    private void initializeBackgroundService() {
        Intent intent = new Intent(this, GeofenceHandlerService.class);
        startService(intent);
    }
}
