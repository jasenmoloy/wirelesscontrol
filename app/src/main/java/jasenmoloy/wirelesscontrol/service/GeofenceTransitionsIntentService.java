package jasenmoloy.wirelesscontrol.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import jasenmoloy.wirelesscontrol.R;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.debug.ErrorMessages;
import jasenmoloy.wirelesscontrol.managers.GeofenceTransitionManager;
import jasenmoloy.wirelesscontrol.ui.MainActivity;

/**
 * Created by jasenmoloy on 3/10/16.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = GeofenceTransitionsIntentService.class.getSimpleName();

    /// ----------------------
    /// Object Fields
    /// ----------------------

    /// ----------------------
    /// Getters / Setters
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Debug.logVerbose(TAG, "--- onHandleIntent() ---");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        
        if (geofencingEvent.hasError()) {
            String errorMessage = ErrorMessages.getGeofenceErrorString(this,
                    geofencingEvent.getErrorCode());
            Debug.logError(TAG, errorMessage);
            return;
        }

        // Get the transition info
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        Debug.logVerbose(TAG,
                "GEOFENCE_TRANSITION_ENTER: " + Geofence.GEOFENCE_TRANSITION_ENTER + " " +
                        "GEOFENCE_TRANSITION_DWELL: " + Geofence.GEOFENCE_TRANSITION_DWELL + " " +
                        "GEOFENCE_TRANSITION_EXIT: " + Geofence.GEOFENCE_TRANSITION_EXIT);

        Debug.logVerbose(TAG, "--- geofenceTransition: " + geofenceTransition);

        switch(geofenceTransition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                GeofenceTransitionManager.get().onTransitionEnter(triggeringGeofences);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                GeofenceTransitionManager.get().onTransitionDwell(triggeringGeofences);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                GeofenceTransitionManager.get().onTransitionExit(triggeringGeofences);
                break;
            default:
                Debug.logError(TAG, getString(R.string.geofence_transition_invalid_type,
                        geofenceTransition));
                break;
        }
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------
}
