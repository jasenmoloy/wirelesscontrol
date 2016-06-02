package jasenmoloy.wirelesscontrol.adapters;

import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.adapters.io.OnGeofenceSaveFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface AddGeofenceModel {
    interface Callback<T1> {
        void onSuccess(T1 acquiredObject);
        void onFailure(int errorCode);
    }

    void save(GeofenceData data, OnGeofenceSaveFinishedListener listener);

    void acquireLastKnownLocation(Callback callback);

    void onDestroy();
}
