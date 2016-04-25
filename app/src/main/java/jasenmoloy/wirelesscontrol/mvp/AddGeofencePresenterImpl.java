package jasenmoloy.wirelesscontrol.mvp;

import jasenmoloy.wirelesscontrol.data.GeofenceData;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class AddGeofencePresenterImpl implements AddGeofencePresenter {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = "AddGeofencePresenterImpl";

    /// ----------------------
    /// Object Fields
    /// ----------------------

    AddGeofenceModel mModel;
    AddGeofenceView mView;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public AddGeofencePresenterImpl(AddGeofenceView view) {
        mView = view;
        mModel = new AddGeofenceModelImpl();
    }

    public void saveGeofence(GeofenceData data) {
        //
        mModel.Save(data, this);
        //JAM TODO: Tell the model to save out the data and let me know when it's done.
        //JAM TODO: Once the model is done saving, let the view know to send the user back to the main screen.
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    public void onGeofenceSaveSuccess() {
        mView.onGeofenceSaveSuccess();
    }

    public void onGeofenceSaveError() {
        //JAM TODO: Determine the issue and notify the view with the appropriate action.
        mView.onGeofenceSaveError();
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

}
