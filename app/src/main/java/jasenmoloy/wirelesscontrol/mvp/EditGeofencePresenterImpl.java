package jasenmoloy.wirelesscontrol.mvp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;

/**
 * Created by jasenmoloy on 5/13/16.
 */
public class EditGeofencePresenterImpl implements
        EditGeofencePresenter {

    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = EditGeofencePresenterImpl.class.getSimpleName();

    private class ResponseReceiver extends BroadcastReceiver {
        public IntentFilter buildIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEOFENCE_UPDATED);
            intentFilter.addAction(Constants.BROADCAST_ACTION_GEOFENCE_DELETED);
            return intentFilter;
        }

        public void onReceive(Context context, Intent intent) {
            Debug.logDebug(TAG, "onReceive() - action:" + intent.getAction());

            switch(intent.getAction()) {
                case Constants.BROADCAST_ACTION_GEOFENCE_UPDATED:
                    if( intent.getBooleanExtra(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, false) )
                        EditGeofencePresenterImpl.this.onGeofenceUpdateSuccess();
                    else
                        EditGeofencePresenterImpl.this.onGeofenceUpdateError();
                    break;
                case Constants.BROADCAST_ACTION_GEOFENCE_DELETED:
                    if( intent.getBooleanExtra(Constants.BROADCAST_EXTRA_KEY_BOOLEAN, false) )
                        EditGeofencePresenterImpl.this.onGeofenceDeleteSuccess();
                    else
                        EditGeofencePresenterImpl.this.onGeofenceDeleteError();
                    break;
            }
        }

        // Prevents instantiation
        private ResponseReceiver() {

        }
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private EditGeofenceModel mModel;
    private EditGeofenceView mView;

    private LocalBroadcastManager mBroadcastManager;
    private ResponseReceiver mReceiver;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public EditGeofencePresenterImpl(EditGeofenceView view, Context context) {
        mView = view;
        mModel = new EditGeofenceModelImpl(context);
        mReceiver = new ResponseReceiver();
    }

    @Override
    public void saveGeofence(int id, GeofenceData data) {
        mModel.updateGeofence(id, data);
        //JAM TODO: Tell the model to save out the data and let me know when it's done.
        //JAM TODO: Once the model is done saving, let the view know to send the user back to the main screen.
    }

    @Override
    public void deleteGeofence(int id) {
        mModel.deleteGeofence(id);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        registerReceiver(LocalBroadcastManager.getInstance(activity));
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mBroadcastManager.unregisterReceiver(mReceiver);
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    protected void onGeofenceUpdateSuccess() {
        mView.onEditSuccess();
    }

    protected void onGeofenceUpdateError() {
        //JAM TODO: Determine the issue and notify the view with the appropriate action.
        mView.onEditFailure();
    }

    protected void onGeofenceDeleteSuccess() {
        mView.onDeleteSuccess();
    }

    protected void onGeofenceDeleteError() {
        //JAM TODO: Determine the issue and notify the view with the appropriate action.
        mView.onDeleteFailure();
    }

    /// ----------------------
    /// Private Methods
    /// ----------------------

    private void registerReceiver(LocalBroadcastManager broadcastManager) {
        mBroadcastManager = broadcastManager;
        mBroadcastManager.registerReceiver(mReceiver, mReceiver.buildIntentFilter());
    }
}
