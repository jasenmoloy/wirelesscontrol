package jasenmoloy.wirelesscontrol.managers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.service.GeofenceTransitionsIntentService;

/**
 * Created by jasenmoloy on 3/10/16.
 */
public class GeofenceManager implements ResultCallback {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = "GeofenceManager";

    /// ----------------------
    /// Object Fields
    /// ----------------------

    Context mContext;
    GoogleApiClient mApiClient;
    ArrayList<Geofence> mGeofences;
    Geofence.Builder mGeofenceBuilder;
    GeofencingRequest mGeofencingRequest;
    PendingIntent mGeofencePendingIntent;

    /// ----------------------
    /// Getters / Setters
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public GeofenceManager(Context context, GoogleApiClient apiClient) {
        mGeofences = new ArrayList<>();
        mContext = context;
        mApiClient = apiClient;
        mGeofencePendingIntent = null;
    }

    public void addGeofences(ArrayList<GeofenceData> data) {
        //Set up all geofence data
        if(mGeofenceBuilder == null)
            mGeofenceBuilder = new Geofence.Builder();

        for(GeofenceData geoData : data) {
            mGeofences.add(buildGeofence(mGeofenceBuilder, geoData));
        }

        deliverGeofences();
    }

    public void addGeofence(GeofenceData data) {
        if(mGeofenceBuilder == null)
            mGeofenceBuilder = new Geofence.Builder();

        mGeofences.add(buildGeofence(mGeofenceBuilder, data));

        deliverGeofences();
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    public void onResult(Result result) {
        Debug.logVerbose(TAG, "onResult(): " + result.getStatus());
        //JAM TODO: Implement me
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

    /**
     *
     * @return
     */
    private PendingIntent getGeofencePendingIntent() {
        if(mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Debug.logDebug(TAG, "GeofenceTransitionsIntentService.class:" + GeofenceTransitionsIntentService.class);

        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     *
     * @param builder
     * @param data
     * @return
     */
    private Geofence buildGeofence(Geofence.Builder builder, GeofenceData data) {
        builder.setRequestId(data.name);
        builder.setCircularRegion(data.position.latitude,
                data.position.longitude,
                (float) data.radius);
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        builder.setLoiteringDelay(5000);

        return mGeofenceBuilder.build();
    }

    /**
     *
     * @param geoData
     * @return
     */
    private GeofencingRequest buildGeofencingRequest(List<Geofence> geoData) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        //JAM Taken from: http://developer.android.com/training/location/geofencing.html
        /* In many cases, it may be preferable to use instead INITIAL_TRIGGER_DWELL,
        which triggers events only when the user stops for a defined duration within a geofence.
        This approach can help reduce "alert spam" resulting from large numbers notifications
        when a device briefly enters and exits geofences. Another strategy for getting best results
        from your geofences is to set a minimum radius of 100 meters. This helps account
        for the location accuracy of typical Wi-Fi networks, and also helps reduce device power
        consumption. */
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geoData);

        return builder.build();
    }

    /**
     *
     */
    private void deliverGeofences() {
        try {
            Debug.logVerbose(TAG, "deliverGeofences() - mGeofences.size:" + mGeofences.size());
            Debug.logVerbose(TAG, "deliverGeofences() - Adding Geofences to LocationServices...");


            PendingIntent pIntent = getGeofencePendingIntent();

            pIntent.send();

            LocationServices.GeofencingApi.addGeofences(
                    mApiClient,
                    buildGeofencingRequest(mGeofences),
                    pIntent
            ).setResultCallback(this);
        }
        catch(SecurityException secEx) {
            Debug.logWarn(TAG, secEx.getMessage());
            //TODO Request permissions to access the user's location.
        }
        catch(PendingIntent.CanceledException canEx) {

        }
    }
}
