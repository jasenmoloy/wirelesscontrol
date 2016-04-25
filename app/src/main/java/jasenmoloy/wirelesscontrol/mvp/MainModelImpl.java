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
        new GeofenceData("Cafe Loft", new LatLng(34.0634061,-118.3076793), 80.0), //3882 W 6th St
        new GeofenceData("Lupe's Place", new LatLng(33.930776, -117.338958), 90.0), //1204 Versailles Cir
        new GeofenceData("V's Home", new LatLng(34.064253,-118.2818659), 200.0), //450 S La Fayette Park Pl

    };

    /// ----------------------
    /// Object Fields
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public void loadGeofenceData(OnGeofenceDataLoadFinishedListener listener) {
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
