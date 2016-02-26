package jasenmoloy.wirelesscontrol.mvp;

import com.google.android.gms.maps.model.LatLng;

import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataLoadFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class MainModelImpl implements MainModel {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    public static final GeofenceData[] mTestGeofenceData = {
        new GeofenceData("Home", new LatLng(35.0500, -119.2500), 60.0),
            new GeofenceData("Work", new LatLng(36.0500, -120.2500), 100.0),
            new GeofenceData("Coffee Shop", new LatLng(33.0500, -124.2500), 80.0),
            new GeofenceData("Alex's Place", new LatLng(30.0500, -110.2500), 90.0),

    };

    /// ----------------------
    /// Object Fields
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public void LoadGeofenceData(OnGeofenceDataLoadFinishedListener listener) {
        //JAM TODO: Actually attempt to load some data
        listener.onGeofenceDataLoadSuccess(mTestGeofenceData);
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------
}
