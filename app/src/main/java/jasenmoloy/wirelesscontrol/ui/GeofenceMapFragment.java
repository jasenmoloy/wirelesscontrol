package jasenmoloy.wirelesscontrol.ui;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import jasenmoloy.wirelesscontrol.R;

public class GeofenceMapFragment extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;

    private GoogleMap mMap;

    private LocationManager mLocationManager;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_geofence_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case MY_PERMISSION_ACCESS_FINE_LOCATION:
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //User has granted permission, grab their location.
                    InitMyLocationOnMap();
                }
                else {
                    //User has denied permissions. Should present a dialog explaining that this is required for this app to work properly.
                }
                break;
            default:
                break;
        }
        return;
    }


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

        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);
        }
        else {
            InitMyLocationOnMap();
        }


        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);

//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

    private void InitMyLocationOnMap()
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
            //TODO Request permissions to access the user's location.
        }
        catch(Exception ex) {
            //TODO Print out a log.
        }

    }
}
