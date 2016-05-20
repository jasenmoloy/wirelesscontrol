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

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.service.GeofenceTransitionsIntentService;

/**
 * Created by jasenmoloy on 3/10/16.
 */
public class GoogleGeofenceManager implements ResultCallback {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = GoogleGeofenceManager.class.getSimpleName();

    /// ----------------------
    /// Object Fields
    /// ----------------------

    Context mContext;
    GoogleApiClient mApiClient;
    ArrayList<Geofence> mGeofences;
    Geofence.Builder mGeofenceBuilder;
    PendingIntent mGeofencePendingIntent;

    /// ----------------------
    /// Getters / Setters
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public GoogleGeofenceManager(Context context, GoogleApiClient apiClient) {
        mGeofences = new ArrayList<>();
        mContext = context;
        mApiClient = apiClient;
        mGeofencePendingIntent = null;
    }

    public void initGeofences(ArrayList<GeofenceData> data) {
        //Set up all geofence data
        if(mGeofenceBuilder == null)
            mGeofenceBuilder = new Geofence.Builder();

        for(GeofenceData geoData : data) {
            mGeofences.add(buildGeofence(mGeofenceBuilder, geoData));
        }

        deliverGeofences(mGeofences);
    }

    public void addGeofence(GeofenceData data) {
        if(mGeofenceBuilder == null)
            mGeofenceBuilder = new Geofence.Builder();

        ArrayList<Geofence> newGeofences = new ArrayList<>(1);
        newGeofences.add(buildGeofence(mGeofenceBuilder, data));
        mGeofences.addAll(newGeofences);
        deliverGeofences(newGeofences);
    }

    public void updateGeofence(int id, GeofenceData data) {
        if(mGeofenceBuilder == null)
            mGeofenceBuilder = new Geofence.Builder();

        Assert.assertFalse(id < 0);
        Assert.assertNotNull(data);

        //get old geofence request ID to remove it from location services
        String requestId = mGeofences.get(id).getRequestId();
        ArrayList<String> ids = new ArrayList<>(1);
        ids.add(requestId);
        removeGeofences(ids);

        //Create the new geofence data and reaplce our old data
        Geofence geofence = buildGeofence(mGeofenceBuilder, data);
        mGeofences.set(id, geofence);

        //Add it to our list and add it to LocationServices
        ArrayList<Geofence> geofences = new ArrayList<Geofence>(1);
        geofences.add(mGeofences.get(id));
        deliverGeofences(geofences);
    }

    public void deleteGeofence(int id) {
        Assert.assertFalse(id < 0);

        //get old geofence request ID to remove it from location services
        String requestId = mGeofences.get(id).getRequestId();
        ArrayList<String> ids = new ArrayList<>(1);
        ids.add(requestId);
        removeGeofences(ids);
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    public void onResult(Result result) {
        Debug.logVerbose(TAG, "onResult(): " + result.getStatus());
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
     * @param geofences
     */
    private void deliverGeofences(List<Geofence> geofences) {
        try {
            Debug.logVerbose(TAG, "deliverGeofences() - mGeofences.size:" + geofences.size());
            Debug.logVerbose(TAG, "deliverGeofences() - Adding Geofences (RequestsIds: " + geofences.toString() + " to LocationServices...");


            PendingIntent pIntent = getGeofencePendingIntent();

            pIntent.send();

            LocationServices.GeofencingApi.addGeofences(
                    mApiClient,
                    buildGeofencingRequest(geofences),
                    pIntent
            ).setResultCallback(this);
        }
        catch(SecurityException secEx) {
            Debug.logWarn(TAG, secEx.getMessage());
            //TODO Request permissions to access the user's location.
        }
        catch(PendingIntent.CanceledException canEx) {
            Debug.logWarn(TAG, canEx.getMessage());
        }
    }

    /**
     *
     */
    private void removeGeofences(List<String> requestIds) {
        try {
            Debug.logVerbose(TAG, "removeGeofences() - Removing Geofence (RequestIds:" + requestIds.toString() + " from LocationServices...");

            LocationServices.GeofencingApi.removeGeofences(
                    mApiClient,
                    requestIds
            ).setResultCallback(this);
        }
        catch(SecurityException secEx) {
            Debug.logWarn(TAG, secEx.getMessage());
            //TODO Request permissions to access the user's location.
        }
    }
}
