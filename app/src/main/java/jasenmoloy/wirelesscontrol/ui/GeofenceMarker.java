package jasenmoloy.wirelesscontrol.ui;

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

import jasenmoloy.wirelesscontrol.debug.Debug;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class GeofenceMarker {
    private static final String TAG = "GeofenceMarker";

    private Marker mMarker;
    private Circle mCircle;

    private MarkerOptions mMarkerOps;
    private CircleOptions mCircleOps;

    public LatLng getPosition() {
        return mMarkerOps == null ? mMarker.getPosition() : mMarkerOps.getPosition();
    }

    public double getRadius() {
        return mCircleOps == null ? mCircle.getRadius() : mCircleOps.getRadius();
    }

    public GeofenceMarker(LatLng position, double radius) {
        SetOptions(position, radius);
    }

    public void AddToMap(GoogleMap map) {
        if( mMarkerOps == null || mCircleOps == null ) {
            Debug.LogWarn(TAG, "Marker Options have not be set for this geofence marker. Has it already been added to another map?");
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

    public void AnimateCameraOnMarker(GoogleMap map) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), map.getMaxZoomLevel() * 0.7f); //JAM TODO: Move this value to resrouces file
        map.animateCamera(cameraUpdate);
    }

    public void MoveCameraOnMarker(GoogleMap map) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), map.getMaxZoomLevel() * 0.7f); //JAM TODO: Move this value to resrouces file
        map.moveCamera(cameraUpdate);
}

    /**
     * Removes the existing geofence marker and set up options to add to a new map.
     * @param position
     * @param radius
     */
    public void Reset(LatLng position, double radius) {
        Assert.assertNotNull(mMarker);
        Assert.assertNotNull(mCircle);

        //Remove our existing data
        mMarker.remove();
        mCircle.remove();

        //Set up our options in preparation to be added to a map.
        SetOptions(position, radius);
    }

    /**
     * Updated an existing marker on a map. If the marker hasn't been added to the map,
     * update the marker option in preparation to be added to a map.
     * @param position
     * @param radius
     */
    public void UpdateMarker(LatLng position, double radius) {
        if(mMarkerOps != null && mCircleOps != null) {
            SetOptions(position, radius);
        }
        else {
            mMarker.setPosition(position);
            mCircle.setCenter(position);
            mCircle.setRadius(radius);
        }
    }

    private void SetOptions(LatLng position, double radius) {
        //If they don't exist, create them.
        if(mMarkerOps == null) mMarkerOps = new MarkerOptions();
        if(mCircleOps == null) mCircleOps = new CircleOptions();

        mMarkerOps.position(position);

        mCircleOps.center(position);
        mCircleOps.radius(60.0);
        mCircleOps.fillColor(Color.argb(50, 0, 0, 255));
        mCircleOps.strokeWidth(4.0f);
        mCircleOps.radius(radius);
    }
}
