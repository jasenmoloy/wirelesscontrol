package jasenmoloy.wirelesscontrol.service;

import android.app.IntentService;
import android.content.Intent;

import jasenmoloy.wirelesscontrol.debug.Debug;

/**
 * Created by jasenmoloy on 3/14/16.
 */
public class InitializeGeofenceDataService extends IntentService {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = "InitializeGeofenceDataService";

    /// ----------------------
    /// Object Fields
    /// ----------------------

    /// ----------------------
    /// Getters / Setters
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public InitializeGeofenceDataService() {
        // Use the TAG to name the worker thread.
        super(TAG);

        Debug.LogDebug(TAG, "SOM InitializeGeofenceDataService()");
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    @Override
    public void onCreate() {
        Debug.LogDebug(TAG, "SOM onCreate()");
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Debug.LogDebug(TAG, "SOM onHandleIntent()");
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------
}
