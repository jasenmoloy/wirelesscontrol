package jasenmoloy.wirelesscontrol.ui;

import android.content.Intent;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
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
import jasenmoloy.wirelesscontrol.helpers.UIHelper;
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

    private static final String TAG = AddGeofenceActivity.class.getSimpleName();
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;

    private class GeofenceNameTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //Stubbed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Stubbed
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(validateUserNameInput(s.toString()))
                UIHelper.enableButton(mSaveButton);
            else
                UIHelper.disableButton(mSaveButton);
        }
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    GoogleMap mMap;
    LocationManager mLocationManager;
    Location mLocation;

    GeofenceMarker mGeofence;
    EditText mGeofenceName;
    Button mSaveButton;

    AddGeofencePresenter mPresenter;

    GeofenceData mGeofenceSaveData;

    int mStandardGeofenceRadius; //JAM TODO: Should be moved to a global resources location

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
        String displayName = mGeofenceName.getText().toString();

        //JAM Convert user's input into something better for IDs and filenames
        String formattedName = createFormattedName(displayName);

        mGeofenceSaveData = new GeofenceData(
                displayName,
                formattedName,
                mGeofence.getPosition(),
                mGeofence.getRadius()
        );

        //JAM Clean and format the map before taking a snapshot
        try {
            mMap.setMyLocationEnabled(false);
            mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getMaxZoomLevel() * 0.8f));

            //JAM wait a moment until the location marker disappears from the map before taking a snapshot
            Thread.sleep(200L); //JAM TODO Resources file?
        }
        catch(SecurityException secEx) {
            Debug.logError(TAG, secEx.getMessage());
        }
        catch(InterruptedException ex) {
            Debug.logWarn(TAG, ex.getMessage());
        }

        //JAM Grab screenshot of the map
        mMap.snapshot(this);
    }

    @Override
    public void onGeofenceSaveSuccess() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //Prevents reinstantiation if the activity already exists
        startActivity(intent);

        //We no longer have use for this activity to lets destroy it
        finish();
    }

    @Override
    public void onGeofenceSaveError() {
        //Notify the user that an error has occurred
        Debug.showDebugOkDialog(this, "Save Error", "An error occurred while saving. Please try again.");

        //JAM TODO: Depending on the error, stay on the current screen and attempt to have the user save again (if possible).
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize map to begin in Los Angeles to start.
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                        new LatLng(34.0500, -118.2500), mMap.getMaxZoomLevel() * 0.8f)
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

        mStandardGeofenceRadius = getResources().getInteger(R.integer.standard_geofence_radius);

        //Set up the activity's view
        setContentView(R.layout.activity_addgeofence);

        //Initialize any viewGroup related fields
        mGeofenceName = (EditText) findViewById(R.id.addgeofence_name);
        mGeofenceName.addTextChangedListener(new GeofenceNameTextWatcher());

        //Grab the "Save" button
        mSaveButton = (Button) findViewById(R.id.addgeofence_savebutton);

        //Start the button as not clickable we don't have a name to save just yet
        UIHelper.disableButton(mSaveButton);

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
            ex.printStackTrace();
        }
    }

    private void initGeofenceMarker(LatLng position) {
        //Set the listener for our map.
        mMap.setOnCameraChangeListener(this);

        mGeofence = new GeofenceMarker(position, mStandardGeofenceRadius);
        mGeofence.addToMap(mMap);
    }

    private void UpdateGeofenceMarker(LatLng position) {
        Assert.assertNotNull(position);

        mGeofence.updateMarker(position, mStandardGeofenceRadius);
    }

    private boolean validateUserNameInput(String userInput) {
        if(userInput.length() == 0 || userInput.length() > 100)
            return false;

        return true;
    }

    private String createFormattedName(String str) {
        return str.replaceAll("[^a-zA-Z0-9-]", "_");
    }
}
