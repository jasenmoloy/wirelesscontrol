package jasenmoloy.wirelesscontrol.adapters;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import jasenmoloy.wirelesscontrol.application.data.GeofenceData;

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
    private boolean mIsGeofenceDataInitialized;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public MainModelImpl(Context context) {
        mContext = context;
        mGeofenceData = new ArrayList<>();
        mIsGeofenceDataInitialized = false;
    }

    @Override
    public boolean isGeofenceDataInitialized() {
        return mIsGeofenceDataInitialized;
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
        mIsGeofenceDataInitialized = true;
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
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------
}
