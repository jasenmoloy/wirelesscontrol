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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import jasenmoloy.wirelesscontrol.helpers.UIHelper;
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

    private static final String TAG = EditGeofenceActivity.class.getSimpleName();
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1; //JAM TODO: Move this to a global constants file

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

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Location mLocation;

    private GeofenceMarker mGeofence;
    private EditText mGeofenceName;
    Button mSaveButton;

    private EditGeofencePresenter mPresenter;

    private int mGeofenceSaveId;
    private GeofenceData mGeofenceSaveData;

    private int mStandardGeofenceRadius; //JAM TODO: Should be moved to a global resources location

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_deletegeofence:
                mPresenter.deleteGeofence(mGeofenceSaveId);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    public void onEditSuccess() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //Prevents reinstantiation if the activity already exists
        startActivity(intent);

        //We no longer have use for this activity to lets destroy it
        finish();
    }

    @Override
    public void onEditFailure() {
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
                        mGeofenceSaveData.position, mMap.getMaxZoomLevel() * 0.8f)
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

            onEditFailure();
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

        mStandardGeofenceRadius = getResources().getInteger(R.integer.standard_geofence_radius);

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

        //Fill in all the info form the existing Geofence.
        mGeofenceName.setText(mGeofenceSaveData.displayName);

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

        if(userInput.equals(".") || userInput.equals(".."))
            return false;

        return true;
    }

    private String createFormattedName(String str) {
        return str.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
