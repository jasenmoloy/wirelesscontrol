package jasenmoloy.wirelesscontrol.mvp;

import java.util.List;
import java.util.Vector;

import jasenmoloy.wirelesscontrol.data.GeofenceData;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class MainPresenterImpl implements MainPresenter {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    /// ----------------------
    /// Object Fields
    /// ----------------------

    MainModel mModel;
    MainView mView;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public MainPresenterImpl(MainView view) {
        mView = view;
        mModel = new MainModelImpl();
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    public void onResume() {
        mModel.LoadGeofenceData(this);
    }

    public void onDestroy() {
        mView = null;
    }

    public void onCardClicked(int position) {
        //JAM TODO: Open up an "edit geofence" activity
    }

    public void onGeofenceDataLoadSuccess(GeofenceData[] geofenceData) {
        List<GeofenceData> list = new Vector<GeofenceData>(geofenceData.length, 1); //Set capacityIncrement to 1 as the user will usually only add one more additional geofence at a time.

        for (GeofenceData data: geofenceData) {
            list.add(data);
        }

        mView.onCardDataLoaded(list);
    }

    public void onGeofenceDataLoadError() {

    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------
}
