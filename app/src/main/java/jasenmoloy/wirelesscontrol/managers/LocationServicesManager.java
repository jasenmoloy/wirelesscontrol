package jasenmoloy.wirelesscontrol.managers;

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

import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;

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

    /// ----------------------
    /// Object Fields
    /// ----------------------

    GoogleApiClient mGoogleApiClient;

    Location mLastLocation;
    LocationRequest mLocationRequest;
    Context mContext;
    boolean mAreLocationsSettingsApproved;
    boolean mIsLocationTrackingActive;

    GoogleApiClient.ConnectionCallbacks mConnectionCallback;

    GoogleGeofenceManager mGoogleGeofenceManager;
    ArrayList<GeofenceData> tempData; //JAM TODO: This could be done better...

    /// ----------------------
    /// Getters / Setters
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public LocationServicesManager(Context context) {
        mContext = context;
        mAreLocationsSettingsApproved = false;
        mIsLocationTrackingActive = false;
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
        if(!mIsLocationTrackingActive)
            return;

        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            removeLocationUpdates();
        else
            Debug.logWarn(TAG, "Google API Client has not been connected yet!");
    }

    public void enableLocationUpdates() {
        if(mIsLocationTrackingActive)
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

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    public void onConnected(@Nullable Bundle connectionHint) {
        mConnectionCallback.onConnected(connectionHint);
        mConnectionCallback = null;
    }

    public void onConnectionSuspended(int cause) {
        mConnectionCallback.onConnectionSuspended(cause);
        mConnectionCallback = null;
    }

    public void onConnectionFailed(ConnectionResult result) {
        //JAM TODO: Implement me
    }

    public void onResult(LocationSettingsResult result) {
        Status status = result.getStatus();
        LocationSettingsStates states = result.getLocationSettingsStates();

        switch(status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                mAreLocationsSettingsApproved = true;

                //Now that we can successfully set locations settings, set up periodic locationsupdates.
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                mIsLocationTrackingActive = true;
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
        if(mLocationRequest == null)
            buildLocationSettings();
        else if(mAreLocationsSettingsApproved) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mIsLocationTrackingActive = true;
        }
        else
            Debug.logWarn(TAG, "Attempting to add location updates while the user hasn't approved the current location settings.");
    }

    private void removeLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mIsLocationTrackingActive = false;
    }

    private void setLastLocation(Location lastLocation) {
        mLastLocation = lastLocation;
        Debug.logVerbose(TAG, "Updating last location: " + lastLocation);
    }
}
