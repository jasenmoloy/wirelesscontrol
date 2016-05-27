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

    boolean mEnabled;
    final Object mLock = new Object();

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public GoogleGeofenceManager(Context context, GoogleApiClient apiClient) {
        mGeofences = new ArrayList<>();
        mContext = context;
        mApiClient = apiClient;
        mGeofencePendingIntent = null;
        mEnabled = true; //Start this system enabled.
    }

    public boolean areGeofencesActive() {
        return mEnabled;
    }

    public void enableAll() {
        synchronized (mLock) {
            if(mEnabled)
                return; //If we're enabled already, geofences are already active in the API

            deliverToApi(mGeofences);
            mEnabled = true;
        }
    }

    public void disableAll() {
        synchronized (mLock) {
            if(!mEnabled)
                return;

            List<String> requestIds = new ArrayList<>(mGeofences.size());

            for(Geofence g : mGeofences) {
                requestIds.add(g.getRequestId());
            }

            removeFromApi(requestIds);
            mEnabled = false;
        }
    }

    public void initGeofences(ArrayList<GeofenceData> data) {
        Assert.assertNotNull(data);

        synchronized (mLock) {
            if (mGeofenceBuilder == null)
                mGeofenceBuilder = new Geofence.Builder();

            //Clear out any old geofences since we're initializing
            mGeofences.clear();

            //Initialize with all new data
            for (GeofenceData geoData : data) {
                mGeofences.add(buildGeofence(mGeofenceBuilder, geoData));
            }

            if (mEnabled)
                deliverToApi(mGeofences);
        }
    }

    public void addGeofence(GeofenceData data) {
        Assert.assertNotNull(data);

        synchronized (mLock) {
            if (mGeofenceBuilder == null)
                mGeofenceBuilder = new Geofence.Builder();

            //Create a new geofence and add it to the list.
            Geofence newGeofence = buildGeofence(mGeofenceBuilder, data);
            mGeofences.add(newGeofence);

            //Set up a list to be delivered
            if (mEnabled) {
                ArrayList<Geofence> newGeofences = new ArrayList<>(1);
                newGeofences.add(newGeofence);
                deliverToApi(newGeofences);
            }
        }
    }

    public void updateGeofence(int id, GeofenceData data) {
        Assert.assertTrue(id >= 0 && id < mGeofences.size());
        Assert.assertNotNull(data);

        synchronized (mLock) {
            if (mGeofenceBuilder == null)
                mGeofenceBuilder = new Geofence.Builder();

            //get old geofence request ID to remove it from location services
            String requestId = mGeofences.get(id).getRequestId();
            ArrayList<String> ids = new ArrayList<>(1);
            ids.add(requestId);
            removeFromApi(ids);

            //Create the new geofence data and reaplce our old data
            Geofence geofence = buildGeofence(mGeofenceBuilder, data);
            mGeofences.set(id, geofence);

            //Add it to our list and add it to LocationServices
            ArrayList<Geofence> geofences = new ArrayList<Geofence>(1);
            geofences.add(mGeofences.get(id));
            deliverToApi(geofences);
        }
    }

    public void deleteGeofence(int id) {
        Assert.assertTrue(id >= 0 && id < mGeofences.size());

        synchronized (mLock) {
            //get old geofence request ID to remove it from location services
            String requestId = mGeofences.get(id).getRequestId();
            ArrayList<String> ids = new ArrayList<>(1);
            ids.add(requestId);
            removeFromApi(ids);
        }
    }

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
        builder.setRequestId(data.displayName); //JAM TODO: This should not using the display name. This should use a proper ID. Currently patched for now in order to properly display the correct name in the notification.
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
     * @param geofences         List of geofences that are to be delivered to Google API.
     */
    private void deliverToApi(List<Geofence> geofences) {
        try {
            Debug.logVerbose(TAG, "deliverToApi() - mGeofences.size:" + geofences.size());
            Debug.logVerbose(TAG, "deliverToApi() - Adding Geofences (RequestsIds: " + geofences.toString() + ") to LocationServices...");

            LocationServices.GeofencingApi.addGeofences(
                    mApiClient,
                    buildGeofencingRequest(geofences),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        }
        catch(SecurityException secEx) {
            Debug.logWarn(TAG, secEx.getMessage());
            //TODO Request permissions to access the user's location.
        }
    }

    /**
     *
     * @param requestIds        List of request IDs associated to geofences that are to be removed
     */
    private void removeFromApi(List<String> requestIds) {
        try {
            Debug.logVerbose(TAG, "removeFromApi() - Removing Geofence (RequestIds:" + requestIds.toString() + ") from LocationServices...");

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
