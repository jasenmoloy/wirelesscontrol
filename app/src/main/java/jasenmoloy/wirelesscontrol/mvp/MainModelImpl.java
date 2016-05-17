package jasenmoloy.wirelesscontrol.mvp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataLoadFinishedListener;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class MainModelImpl implements MainModel {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private Context mContext;
    private ArrayList<GeofenceData> mGeofenceData;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public MainModelImpl(Context context) {
        mContext = context;
        mGeofenceData = new ArrayList<>();
    }

    @Override
    public ArrayList<GeofenceData> getGeofenceData() {
        return mGeofenceData;
    }

    @Override
    public void initGeofences(List<GeofenceData> data) {
        if(data == null)
            return;

        mGeofenceData.clear();
        mGeofenceData.addAll(data);
    }

    @Override
    public void addGeofence(GeofenceData data) {
        if(data == null)
            return;

        mGeofenceData.add(data);
    }

    @Override
    public void updateGeofence(int id, GeofenceData newData) {
        mGeofenceData.set(id, newData);
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
