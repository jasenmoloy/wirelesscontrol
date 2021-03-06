package jasenmoloy.wirelesscontrol.application.managers;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.LocationSettingsStates;

import java.util.ArrayList;

import jasenmoloy.wirelesscontrol.application.data.Constants;
import jasenmoloy.wirelesscontrol.application.data.GeofenceData;
import jasenmoloy.wirelesscontrol.application.debug.Debug;

/**
 * Created by jasenmoloy on 3/9/16.
 */
public class LocationServicesManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult>,
        LocationListener {

    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = LocationServicesManager.class.getSimpleName();

    private enum LocationUpdateState {
        NOT_INITIALIZED,
        DISABLE_ON_INIT,
        ENABLE_ON_INIT,
        ENABLED,
        DISABLED
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private GoogleApiClient mGoogleApiClient;

    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Context mContext;
    private boolean mAreLocationsSettingsApproved;
    private LocationUpdateState mLocationUpdateState;

    private GoogleApiClient.ConnectionCallbacks mConnectionCallback;

    private GoogleGeofenceManager mGoogleGeofenceManager;
    private ArrayList<GeofenceData> tempData; //JAM TODO: This could be done better...

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public LocationServicesManager(Context context) {
        mContext = context;
        mAreLocationsSettingsApproved = false;
        mLocationUpdateState = LocationUpdateState.NOT_INITIALIZED;
    }

    public boolean isConnected() {
        if(mGoogleApiClient == null)
            return false;

        return mGoogleApiClient.isConnected();
    }

    public void connect(GoogleApiClient.ConnectionCallbacks callback) {
        if(mGoogleApiClient == null)
            initializeApi();

        if(callback == null)
            mGoogleApiClient.blockingConnect();
        else {
            mConnectionCallback = callback;
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        if(mGoogleApiClient == null)
            return;

        mGoogleApiClient.unregisterConnectionCallbacks(this);
        mGoogleApiClient.disconnect();
    }

    public void performLocationServices() {
        try {
            setLastLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));

            //If we can verify we can get a location, fine tune the setting and set up periodic updates
            buildLocationSettings();
        }
        catch(SecurityException secEx) {
            Debug.logWarn(TAG, secEx.getMessage());

            Intent intent = new Intent(Constants.BROADCAST_ACTION_PERMISSION_REQUESTED);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
        catch(Exception ex) {
            Debug.logError(TAG, ex.getMessage());
        }
    }

    public Location getLastLocation() {
        return mLastLocation;
    }

    public boolean areGeofencesActive() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            return mGoogleGeofenceManager.areGeofencesActive();
        else
            Debug.logWarn(TAG, "Google API Client has not been connected yet!");

        return false;
    }

    public void disableGeofences() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleGeofenceManager.disableAll();
        else
            Debug.logWarn(TAG, "Google API Client has not been connected yet!");
    }

    public void enableGeofences() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleGeofenceManager.enableAll();
        else
            Debug.logWarn(TAG, "Google API Client has not been connected yet!");
    }

    public void disableLocationUpdates() {
        if(mLocationUpdateState == LocationUpdateState.DISABLED)
            return;

        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            removeLocationUpdates();
        else
            Debug.logWarn(TAG, "Google API Client has not been connected yet!");
    }

    public void enableLocationUpdates() {
        if(mLocationUpdateState == LocationUpdateState.ENABLED)
            return;

        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            addLocationsUpdates();
        else
            Debug.logWarn(TAG, "Google API Client has not been connected yet!");
    }

    public void initGeofenceData(ArrayList<GeofenceData> data) {
        Debug.logDebug(TAG, "sendGeofenceData() --- data.size():" + data.size());

        //Don't add any data if it's empty or doesn't exist.
        if(data == null || data.size() == 0)
            return;

        if(mGoogleApiClient.isConnected())
            mGoogleGeofenceManager.initGeofences(data);
        else
            tempData = data;
    }

    public void sendGeofenceData(GeofenceData data) {
        Debug.logDebug(TAG, "sendGeofenceData() --- data.name:" + data.name);

        //JAM Don't add data if it doesn't exist
        if(data == null)
            return;

        if(mGoogleApiClient.isConnected())
            mGoogleGeofenceManager.addGeofence(data);
        else
            Debug.logError(TAG, "Google API Client has not been connected yet! Disregarding geofence!");
    }

    public void updateGeofenceData(int id, GeofenceData data) {
        Debug.logDebug(TAG, "updateGeofenceData() --- data.id: " + id + " data.name:" + data.name);

        //JAM Don't add data if it doesn't exist
        if(id < 0 || data == null)
            return;

        if(mGoogleApiClient.isConnected()) {
            mGoogleGeofenceManager.updateGeofence(id, data);
        }
        else
            Debug.logError(TAG, "Google API Client has not been connected yet! Disregarding geofence!");
    }

    public void deleteGeofence(int id) {
        Debug.logDebug(TAG, "deleteGeofence() --- data.id: " + id);

        //JAM Don't delete if it doesn't exist
        if(id < 0)
            return;

        if(mGoogleApiClient.isConnected()) {
            mGoogleGeofenceManager.deleteGeofence(id);
        }
        else
            Debug.logError(TAG, "Google API Client has not been connected yet! Disregarding geofence!");
    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        mConnectionCallback.onConnected(connectionHint);
        mConnectionCallback = null;
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mConnectionCallback.onConnectionSuspended(cause);
        mConnectionCallback = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        //JAM TODO: Implement me
    }

    @Override
    public void onResult(LocationSettingsResult result) {
        Status status = result.getStatus();
        LocationSettingsStates states = result.getLocationSettingsStates();

        switch(status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                mAreLocationsSettingsApproved = true;

                //Now that we can successfully set locations settings,
                //set up periodic locations updates.
                if(mLocationUpdateState == LocationUpdateState.NOT_INITIALIZED ||
                        mLocationUpdateState == LocationUpdateState.ENABLE_ON_INIT) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    mLocationUpdateState = LocationUpdateState.ENABLED;
                }
                else if(mLocationUpdateState == LocationUpdateState.DISABLE_ON_INIT) {
                    mLocationUpdateState = LocationUpdateState.DISABLED;
                }
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
                Intent intent = new Intent(Constants.BROADCAST_ACTION_PERMISSION_REQUESTED);
                lbm.sendBroadcast(intent);
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Debug.logError(TAG, "onResult() - SETTINGS CHANGE UNAVAILABLE");
                break;
        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        setLastLocation(loc);
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    protected void initializeApi() {
        if(mGoogleApiClient == null) {
            GoogleApiClient.Builder builder = new GoogleApiClient.Builder(mContext);

            builder.addConnectionCallbacks(this);
            builder.addOnConnectionFailedListener(this);
            builder.addApi(LocationServices.API);

            mGoogleApiClient = builder.build();

            //Initialize any location based services after we've created an API client
            mGoogleGeofenceManager = new GoogleGeofenceManager(mContext, mGoogleApiClient);
        }
    }

    /// ----------------------
    /// Private Methods
    /// ----------------------

    private void buildLocationSettings() {
        LocationRequest request = new LocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        request.setInterval(5000L); //JAM TODO: Should set this via settings
        request.setFastestInterval(1000L); //JAM TODO: Is this the right place to have this?
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); //JAM TODO: will have to change this depending on how close we are to established geofences.
        mLocationRequest = request;

        builder.addLocationRequest(request);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(this);
    }

    private void addLocationsUpdates() {
        if(mLocationRequest == null) {
            mLocationUpdateState = LocationUpdateState.ENABLE_ON_INIT;
            buildLocationSettings();
        }
        else if(mAreLocationsSettingsApproved) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mLocationUpdateState = LocationUpdateState.ENABLED;
        }
        else
            Debug.logWarn(TAG, "Attempting to add location updates while the user hasn't approved the current location settings.");
    }

    private void removeLocationUpdates() {
        if(mLocationUpdateState == LocationUpdateState.NOT_INITIALIZED) {
            mLocationUpdateState = LocationUpdateState.DISABLE_ON_INIT;
        }
        else if(mLocationUpdateState == LocationUpdateState.ENABLED) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mLocationUpdateState = LocationUpdateState.DISABLED;
        }
    }

    private void setLastLocation(Location lastLocation) {
        mLastLocation = lastLocation;
        Debug.logVerbose(TAG, "Updating last location: " + lastLocation);
    }
}
