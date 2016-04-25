package jasenmoloy.wirelesscontrol.mvp;

import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.io.OnGeofenceSaveFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class AddGeofenceModelImpl implements AddGeofenceModel {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    public static final String TAG = "AddGeofenceModelImpl";

    /// ----------------------
    /// Object Fields
    /// ----------------------
    private GeofenceData mData;

    /// ----------------------
    /// Public Methods
    /// ----------------------
    public void save(GeofenceData data, OnGeofenceSaveFinishedListener listener) {
        //JAM TODO: Implement Saving!
        Debug.logWarn(TAG, "Save() called but not implemented!");

        Debug.logVerbose(TAG, "GeofenceData data.name: " + data.name);
        Debug.logVerbose(TAG, "GeofenceData data.lat:" + data.position.latitude + " data.long:" + data.position.longitude);
        Debug.logVerbose(TAG, "GeofenceData data.radius: " + data.radius);

        listener.onGeofenceSaveError(); //JAM TODO: Remove this once saving is implemented!
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
