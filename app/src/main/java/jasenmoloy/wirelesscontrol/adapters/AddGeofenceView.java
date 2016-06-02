package jasenmoloy.wirelesscontrol.adapters;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import jasenmoloy.wirelesscontrol.adapters.io.OnGeofenceSaveFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface AddGeofenceView extends OnMapReadyCallback, GoogleMap.OnCameraChangeListener, OnGeofenceSaveFinishedListener {
    void initializeMyLocationOnMap(Location lastKnownLocation);

    void displayLocationNotFoundToast();
}
