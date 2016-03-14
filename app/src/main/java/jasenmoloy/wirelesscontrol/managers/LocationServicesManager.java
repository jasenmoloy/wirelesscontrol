package jasenmoloy.wirelesscontrol.managers;

import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

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

import java.util.List;

import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.ui.MainActivity;

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

    private static final String TAG = "LocationServicesManager";

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private GoogleApiClient mGoogleApiClient;

    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Activity mParentActivity;

    GeofenceManager mGeofenceManager;
    List<GeofenceData> tempData; //JAM TODO: This could be done better...

    /// ----------------------
    /// Getters / Setters
    /// ----------------------

    private void setLastLocation(Location lastLocation) {
        mLastLocation = lastLocation;
        Debug.LogVerbose(TAG, "Updating last location: " + lastLocation);
    }

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public LocationServicesManager(Activity parentActivity) {
        mParentActivity = parentActivity;
    }

    public void connect() {
        if(mGoogleApiClient == null)
            initializeApi();

        mGoogleApiClient.connect();
    }

    public void disconnect() {
        if(mGoogleApiClient == null)
            return;

        mGoogleApiClient.disconnect();
    }

    public void performLocationServices() {
        try {
            setLastLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));

            //If we can verify we can get a location, fine tune the setting and set up periodic updates
            buildLocationSettings();

            //Set up any geofencing we've loaded if there is any.
            if(tempData != null) {
                mGeofenceManager.addGeofences(tempData);
                tempData = null;
            }
        }
        catch(SecurityException secEx) {
            Debug.LogWarn(TAG, secEx.getMessage());
            requestLocationPermission();
        }
        catch(Exception ex) {
            Debug.LogError(TAG, ex.getMessage());
        }
    }

    public void sendGeofenceData(List<GeofenceData> data) {
        if(mGoogleApiClient.isConnected())
            mGeofenceManager.addGeofences(data);
        else
            tempData = data;
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    public void onConnected(Bundle connectionHint) {
        if ( ContextCompat.checkSelfPermission(mParentActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            requestLocationPermission();
        }
        else
            performLocationServices();
    }

    public void onConnectionSuspended(int cause) {
        //JAM TODO: Implement me
    }

    public void onConnectionFailed(ConnectionResult result) {
        //JAM TODO: Implement me
    }

    public void onResult(LocationSettingsResult result) {
        Status status = result.getStatus();
        LocationSettingsStates states = result.getLocationSettingsStates();

        switch(status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                //Now that we can successfully set locations settings, set up periodic locationsupdates.
                //JAM TODO: Might need to create a flag to see if the user has turned on locations updates or not and save it app-wide.
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    status.startResolutionForResult(mParentActivity, MainActivity.MY_PERMISSION_ACCESS_COARSE_LOCATION);
                }
                catch(IntentSender.SendIntentException e) {
                    //JAM Ignore? according to: http://developer.android.com/training/location/change-location-settings.html
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Debug.LogError(TAG, "onResult() - SETTINGS CHANGE UNAVAILABLE");
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
            GoogleApiClient.Builder builder = new GoogleApiClient.Builder(mParentActivity);

            builder.addConnectionCallbacks(this);
            builder.addOnConnectionFailedListener(this);
            builder.addApi(LocationServices.API);

            mGoogleApiClient = builder.build();

            //Initialize any location based services after we've created an API client
            mGeofenceManager = new GeofenceManager(mParentActivity, mGoogleApiClient);
        }
    }

    /// ----------------------
    /// Private Methods
    /// ----------------------

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(mParentActivity, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                MainActivity.MY_PERMISSION_ACCESS_COARSE_LOCATION);
    }

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
}