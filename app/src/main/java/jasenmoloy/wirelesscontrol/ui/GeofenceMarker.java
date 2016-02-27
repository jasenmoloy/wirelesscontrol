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

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class GeofenceMarker {
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
        mMarker = map.addMarker(mMarkerOps);
        mMarkerOps = null;

        mCircle = map.addCircle(mCircleOps);
        mCircleOps = null;
    }

    public void AnimateCameraOnMarker(GoogleMap map) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), map.getMaxZoomLevel() * 0.7f);
        map.animateCamera(cameraUpdate);
    }

    public void MoveCameraOnMarker(GoogleMap map) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), map.getMaxZoomLevel() * 0.7f);
        map.moveCamera(cameraUpdate);
}

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
