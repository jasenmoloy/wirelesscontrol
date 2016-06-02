package jasenmoloy.wirelesscontrol.presentation.ui;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import jasenmoloy.wirelesscontrol.R;
import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.application.debug.Debug;

public class GeofenceMapFragment extends FragmentActivity implements OnMapReadyCallback {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = GeofenceMapFragment.class.getSimpleName();

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private GoogleMap mMap;

    private LocationManager mLocationManager;
    private Location mLocation;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user hasx
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        initMyLocationOnMap();
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_geofence_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

            LatLng currentPos = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPos, (mMap.getMaxZoomLevel() * 0.8f));


            //Update the camera to point to where the user is located and zoom in a bit.
            mMap.animateCamera(cameraUpdate);
        }
        catch(SecurityException secEx) {
            Debug.logWarn(TAG, secEx.getMessage());

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            Intent intent = new Intent(Constants.BROADCAST_ACTION_PERMISSION_REQUESTED);
            lbm.sendBroadcast(intent);
        }
        catch(Exception ex) {
            Debug.logError(TAG, ex.getMessage());
        }

    }
}
