package jasenmoloy.wirelesscontrol.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import junit.framework.Assert;

import jasenmoloy.wirelesscontrol.R;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.mvp.AddGeofencePresenter;
import jasenmoloy.wirelesscontrol.mvp.AddGeofencePresenterImpl;
import jasenmoloy.wirelesscontrol.mvp.AddGeofenceView;

/**
 * Created by jasenmoloy on 2/17/16.
 */
public class AddGeofenceActivity extends AppCompatActivity implements AddGeofenceView, GoogleMap.SnapshotReadyCallback {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = "AddGeofenceActivity";
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Location mLocation;

    private GeofenceMarker mGeofence;
    private EditText mGeofenceName;

    private AddGeofencePresenter mPresenter;

    private GeofenceData mGeofenceSaveData;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    public void onCameraChange(CameraPosition cameraPos) {
        UpdateGeofenceMarker(cameraPos.target);
    }

    public void onSaveButtonClick(View view) {
        mGeofenceSaveData = new GeofenceData(
                mGeofenceName.getText().toString(),
                mGeofence.getPosition(),
                mGeofence.getRadius()
        );

        //JAM TODO: Verify data is correct before attempting to save

        //JAM Grab screenshot of the map
        mMap.snapshot(this);
    }

    public void onGeofenceSaveSuccess() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //Prevents reinstantiation if the activity already exists
        startActivity(intent);
    }

    public void onGeofenceSaveError() {
        Debug.logWarn(TAG, "onGeofenceSaveError() - Called but not implemented!");

        //Notify the user that an error has occurred
        Debug.showDebugOkDialog(this, "Save Error", "An error occurred while saving. Please try again.");

        //JAM TODO: Depending on the error, stay on the current screen and attempt to have the user save again (if possible).
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize map to begin in Los Angeles to start.
        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        new LatLng(34.0500, -118.2500), mMap.getMaxZoomLevel() * 0.6f)
        );

        //Initialize new geofence marker that will be placed
        initGeofenceMarker(mMap.getCameraPosition().target);

        //Check and/or request permission if the app can use the user's location.
        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);
        }
        else {
            initMyLocationOnMap();
        }
    }

    public void onSnapshotReady(Bitmap map) {
        if(map == null) {
            onGeofenceSaveError();
            return;
        }

        //Add bitmap to geofence save data
        mGeofenceSaveData.addBitmap(map);

        //Let presenter know we're ready to save the data
        mPresenter.saveGeofence(mGeofenceSaveData);
        mGeofenceSaveData = null;
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up the activity's view
        setContentView(R.layout.activity_addgeofence);

        //Initialize any viewGroup related fields
        mGeofenceName = (EditText) findViewById(R.id.addgeofence_name);

        //Set the toolbar according to the activity layout
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myChildToolbar);

        //Enable the "Up" button to go back to the parent activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.addgeofence_geofencemap);
        mapFragment.getMapAsync(this);


        mPresenter = new AddGeofencePresenterImpl(this, this);
        mPresenter.onCreate();
        mPresenter.registerReceiver(LocalBroadcastManager.getInstance(this));
    }

    @Override
    protected void onStart() {
        //Stubbed
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Stubbed
    }

    @Override
    protected void onPause() {
        //Stubbed
        super.onPause();
    }

    @Override
    protected void onStop() {
        //Stubbed
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDestroy();
        super.onDestroy();
    }

    /// ----------------------
    /// Private Methods
    /// ----------------------

    private void initMyLocationOnMap()
    {
        try {
            mMap.setMyLocationEnabled(true);

            if( mLocationManager == null ) {
                mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            //Get the user's current position
            LatLng currentPos = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPos, mMap.getMaxZoomLevel() * 0.8f);

            //Update the marker to that position before the users see the map animation
            UpdateGeofenceMarker(currentPos);

            //Update the camera to point to where the user is located and zoom in a bit.
            mMap.animateCamera(cameraUpdate);
        }
        catch(SecurityException secEx) {
            //TODO Request permissions to access the user's location.
        }
        catch(Exception ex) {
            //TODO Print out a log.
        }
    }

    private void initGeofenceMarker(LatLng position) {
        //Set the listener for our map.
        mMap.setOnCameraChangeListener(this);

        mGeofence = new GeofenceMarker(position, 60.0); //JAM TODO: Move this to resources file.
        mGeofence.addToMap(mMap);
    }

    private void UpdateGeofenceMarker(LatLng position) {
        Assert.assertNotNull(position);
        //JAM TODO Check for null
        mGeofence.updateMarker(position, 60.0); //JAM TODO: Move this to resources file.
    }
}
