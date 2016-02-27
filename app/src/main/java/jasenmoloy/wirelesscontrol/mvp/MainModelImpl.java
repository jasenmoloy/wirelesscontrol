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
        new GeofenceData("Home", new LatLng(34.051452, -118.31262), 60.0), //1041 S Manhattan Pl
            new GeofenceData("Work", new LatLng(37.7885067, -122.3989532), 100.0), //500 Howard St
            new GeofenceData("Mr Coffee", new LatLng(34.0642398, -118.3115321), 80.0), //537 S Western Ave
            new GeofenceData("Lupe's Place", new LatLng(33.930776, -117.338958), 90.0), //1204 Versailles Cir

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
