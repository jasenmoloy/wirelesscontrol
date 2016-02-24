package jasenmoloy.wirelesscontrol;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import jasenmoloy.wirelesscontrol.debug.Debug;

/**
 * Created by jasenmoloy on 2/17/16.
 */
public class AddGeofenceActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener {

    private static final String TAG = "AddGeofenceActivity";

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Location mLocation;
    private Marker mGeofenceMarker;
    private Circle mGeofenceCircle;


    /// ----------------------
    /// Public Methods
    /// ----------------------

    /// ----------------------
    /// Overwritten Methods
    /// ----------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_addgeofence);

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
        InitGeofenceMarker(mMap.getCameraPosition().target);

        //Check and/or request permission if the app can use the user's location.
        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);
        }
        else {
            InitMyLocationOnMap();
        }
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    public void onCameraChange(CameraPosition cameraPos) {
        UpdateGeofenceMarker(cameraPos.target);
    }

    public void onSaveButtonClick(View view) {
        //JAM TODO: Do some saving!

        Debug.LogDebug(TAG, "onSaveButtonClick()");
        Debug.LogVerbose(TAG, "onSaveButtonClick()");
        Debug.LogWarn(TAG, "onSaveButtonClick()");
        Debug.LogError(TAG, "onSaveButtonClick()");

        Debug.ShowDebugOkDialog(this, "onSaveButtonClick()", "Save Button has been clicked!");
    }

    /// ----------------------
    /// Private Methods
    /// ----------------------

    private void InitMyLocationOnMap()
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

    private void InitGeofenceMarker(LatLng position) {
        //Set the listener for our map.
        mMap.setOnCameraChangeListener(this);

        //Add a marker to your current location
        MarkerOptions markOps = new MarkerOptions();
        markOps.position(position);
        mGeofenceMarker = mMap.addMarker(markOps);

        //Add a radius to the current marker
        CircleOptions circleOps = new CircleOptions();
        circleOps.center(position);
        circleOps.radius(60.0);
        circleOps.fillColor(Color.argb(50, 0, 0, 255));
        circleOps.strokeWidth(4.0f);
        mGeofenceCircle = mMap.addCircle(circleOps);
    }

    private void UpdateGeofenceMarker(LatLng position) {
        //JAM TODO Check for null
        mGeofenceMarker.setPosition(position);
        mGeofenceCircle.setCenter(position);
    }
}
