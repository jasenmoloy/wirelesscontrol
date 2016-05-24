package jasenmoloy.wirelesscontrol.mvp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;

/**
 * Created by jasenmoloy on 5/13/16.
 */
public class EditGeofenceModelImpl implements EditGeofenceModel {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    public static final String TAG = EditGeofenceModelImpl.class.getSimpleName();

    /// ----------------------
    /// Object Fields
    /// ----------------------
    private Context mContext;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public EditGeofenceModelImpl(Context context) {
        mContext = context;
    }

    @Override
    public void updateGeofence(int id, GeofenceData data) {
        Debug.logVerbose(TAG, "Updated GeofenceData data.displayName: " + data.displayName);
        Debug.logVerbose(TAG, "Updated GeofenceData data.name: " + data.name);
        Debug.logVerbose(TAG, "Updated GeofenceData data.lat:" + data.position.latitude + " data.long:" + data.position.longitude);
        Debug.logVerbose(TAG, "Updated GeofenceData data.radius: " + data.radius);

        //Let the service know to update the geofence with new information.
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        Intent intent = new Intent(Constants.BROADCAST_ACTION_UPDATE_GEOFENCE);
        Bundle intentBundle = new Bundle();
        intentBundle.putInt(Constants.BROADCAST_EXTRA_KEY_GEOFENCE_ID, id);
        intentBundle.putParcelable(Constants.BROADCAST_EXTRA_KEY_GEODATA, data);
        intent.putExtras(intentBundle);
        lbm.sendBroadcast(intent);
    }

    @Override
    public void deleteGeofence(int id) {
        //Let the service know we're deleting this geofence
        Intent intent = new Intent(Constants.BROADCAST_ACTION_DELETE_GEOFENCE);
        Bundle intentBundle = new Bundle();
        intentBundle.putInt(Constants.BROADCAST_EXTRA_KEY_GEOFENCE_ID, id);
        intent.putExtras(intentBundle);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
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
