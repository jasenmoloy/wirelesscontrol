package jasenmoloy.wirelesscontrol.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import junit.framework.Assert;

import jasenmoloy.wirelesscontrol.R;
import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.mvp.EditGeofencePresenter;
import jasenmoloy.wirelesscontrol.mvp.EditGeofencePresenterImpl;
import jasenmoloy.wirelesscontrol.mvp.EditGeofenceView;

/**
 * Created by jasenmoloy on 5/13/16.
 */
public class EditGeofenceActivity extends AppCompatActivity implements
        EditGeofenceView, GoogleMap.SnapshotReadyCallback, OnMapReadyCallback,
        GoogleMap.OnCameraChangeListener {

    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = "EditGeofenceActivity";
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1; //JAM TODO: Fix this!

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Location mLocation;

    private GeofenceMarker mGeofence;
    private EditText mGeofenceName;

    private EditGeofencePresenter mPresenter;

    private int mGeofenceSaveId;
    private GeofenceData mGeofenceSaveData;

    /// ----------------------
    /// Getters / Setters
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    @Override
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

        //JAM Clean and format the map before taking a snapshot
        try {
            mMap.setMyLocationEnabled(false);
            //mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getMaxZoomLevel() * 0.5f));
        }
        catch(SecurityException secEx) {
            Debug.logError(TAG, secEx.getMessage());
        }

        //JAM Grab screenshot of the map
        mMap.snapshot(this);
    }

    @Override
    public void onSaveSuccess() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //Prevents reinstantiation if the activity already exists
        startActivity(intent);
    }

    @Override
    public void onSaveFailure() {
        Debug.logWarn(TAG, "onGeofenceSaveError() - Called but not implemented!");

        //Notify the user that an error has occurred
        Debug.showDebugOkDialog(this, "Save Error", "An error occurred while saving. Please try again.");

        //JAM TODO: Depending on the error, stay on the current screen and attempt to have the user save again (if possible).
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize map to focus in on existing location
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                        mGeofenceSaveData.position, mMap.getMaxZoomLevel() * 0.6f)
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

    @Override
    public void onSnapshotReady(Bitmap map) {
        if(map == null) {
            //JAM Enable your location again
            try {
                mMap.setMyLocationEnabled(true);
            }
            catch(SecurityException secEx) {
                Debug.logError(TAG, secEx.getMessage());
            }

            onSaveFailure();
            return;
        }

        //Add bitmap to geofence save data
        mGeofenceSaveData.addBitmap(map);

        //Let presenter know we're ready to save the data
        mPresenter.saveGeofence(mGeofenceSaveId, mGeofenceSaveData);
        mGeofenceSaveData = null;
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Grab the intent that started us and get the GeofenceData we intend to edit
        Intent intent = getIntent();
        mGeofenceSaveId = intent.getIntExtra(Constants.BROADCAST_EXTRA_KEY_GEOFENCE_ID, -1);
        mGeofenceSaveData = intent.getParcelableExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA);

        Assert.assertTrue(mGeofenceSaveId >= 0); //ID needs to be within range to properly update
        Assert.assertNotNull(mGeofenceSaveData); //Make sure we actually have some data to update

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

        //Fill in all the info form the existing Geofence.
        mGeofenceName.setText(mGeofenceSaveData.name);

        mPresenter = new EditGeofencePresenterImpl(this, this);
        mPresenter.onActivityCreated(this, savedInstanceState);
    }

    @Override
    protected void onStart() {
        mPresenter.onActivityStarted(this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onActivityResumed(this);
    }

    @Override
    protected void onPause() {
        mPresenter.onActivityPaused(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        mPresenter.onActivityStopped(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mPresenter.onActivityDestroyed(this);
        super.onDestroy();
    }

    /// ----------------------
    /// Private Methods
    /// ----------------------

    private void initMyLocationOnMap()
    {
        try {
            mMap.setMyLocationEnabled(true);

            //Get the user's current position
            if( mLocationManager == null ) {
                mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
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