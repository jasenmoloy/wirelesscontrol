package jasenmoloy.wirelesscontrol.presentation.ui;

import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import junit.framework.Assert;

import jasenmoloy.wirelesscontrol.application.debug.Debug;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class GeofenceMarker {
    private static final String TAG = GeofenceMarker.class.getSimpleName();

    private Marker mMarker;
    private Circle mCircle;

    private MarkerOptions mMarkerOps;
    private CircleOptions mCircleOps;

    public LatLng getPosition() {
        return mMarkerOps == null ? mMarker.getPosition() : mMarkerOps.getPosition();
    }

    public int getRadius() {
        return mCircleOps == null ? (int)mCircle.getRadius() : (int)mCircleOps.getRadius();
    }

    public GeofenceMarker(LatLng position, int radius) {
        setOptions(position, radius);
    }

    public void addToMap(GoogleMap map) {
        if( mMarkerOps == null || mCircleOps == null ) {
            Debug.logWarn(TAG, "Marker Options have not be set for this geofence marker. Has it already been added to another map?");
            return;
        }

        if( mMarker != null && mCircle != null ) {
            mMarker.remove();
            mMarker = null;

            mCircle.remove();
            mCircle = null;
        }

        mMarker = map.addMarker(mMarkerOps);
        mMarkerOps = null;

        mCircle = map.addCircle(mCircleOps);
        mCircleOps = null;
    }

    public void animateCameraOnMarker(GoogleMap map) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), map.getMaxZoomLevel() * 0.75f); //JAM TODO: Need to get access to context to get resources
        map.animateCamera(cameraUpdate);
    }

    public void moveCameraOnMarker(GoogleMap map) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), map.getMaxZoomLevel() * 0.75f); //JAM TODO: Need to get access to context to get resources
        map.moveCamera(cameraUpdate);
}

    /**
     * Removes the existing geofence marker and set up options to add to a new map.
     * @param position
     * @param radius
     */
    public void reset(LatLng position, int radius) {
        Assert.assertNotNull(mMarker);
        Assert.assertNotNull(mCircle);

        //Remove our existing data
        mMarker.remove();
        mCircle.remove();

        //Set up our options in preparation to be added to a map.
        setOptions(position, radius);
    }

    /**
     * Updated an existing marker on a map. If the marker hasn't been added to the map,
     * update the marker option in preparation to be added to a map.
     * @param position
     * @param radius
     */
    public void updateMarker(LatLng position, int radius) {
        if(mMarkerOps != null && mCircleOps != null) {
            setOptions(position, radius);
        }
        else {
            mMarker.setPosition(position);
            mCircle.setCenter(position);
            mCircle.setRadius(radius);
        }
    }

    private void setOptions(LatLng position, int radius) {
        //If they don't exist, create them.
        if(mMarkerOps == null) mMarkerOps = new MarkerOptions();
        if(mCircleOps == null) mCircleOps = new CircleOptions();

        mMarkerOps.position(position);

        mCircleOps.center(position);
        mCircleOps.radius(radius);
        mCircleOps.fillColor(Color.argb(50, 0, 0, 255)); //JAM TODO: Move values to resources file
        mCircleOps.strokeWidth(0.0f); //JAM TODO: Move values to resources file
    }
}
