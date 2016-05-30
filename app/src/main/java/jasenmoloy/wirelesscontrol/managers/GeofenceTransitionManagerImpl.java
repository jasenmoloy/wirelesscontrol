package jasenmoloy.wirelesscontrol.managers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.List;

import jasenmoloy.wirelesscontrol.R;
import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.presentation.ui.BaseApp;
import jasenmoloy.wirelesscontrol.presentation.ui.MainActivity;

/**
 * Created by jasenmoloy on 5/20/16.
 */
public class GeofenceTransitionManagerImpl extends GeofenceTransitionManager {
    /// ----------------------
    /// Class Fields
    /// ----------------------
    private static final String TAG = GeofenceTransitionManagerImpl.class.getSimpleName();

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private Context mContext;
    private WifiManager mWifiManager;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    @Override
    public void onTransitionEnter(List<Geofence> triggeredGeofences) {
        // Get the transition details as a String.
        String geofenceTransitionDetails = getGeofenceTransitionDetails(
                mContext.getString(R.string.geofence_transition_entered),
                triggeredGeofences
        );

        //Turn on Wifi
        switchWifi(true);

        // Send notification and log the transition details.
        updateForegroundNotification(geofenceTransitionDetails);
        Debug.logVerbose(TAG, geofenceTransitionDetails);
    }

    @Override
    public void onTransitionDwell(List<Geofence> triggeredGeofences) {
        //JAM TODO: Implement me if needed
    }

    @Override
    public void onTransitionExit(List<Geofence> triggeredGeofences) {
        // Get the transition details as a String.
        String geofenceTransitionDetails = getGeofenceTransitionDetails(
                mContext.getString(R.string.geofence_transition_exited),
                triggeredGeofences
        );

        //JAM TODO: We should get the associated geofenceData with the triggered geofences and react according to the user's settings

        //Turn off Wifi
        switchWifi(false);

        // Send notification and log the transition details.
        updateForegroundNotification(geofenceTransitionDetails);
        Debug.logVerbose(TAG, geofenceTransitionDetails);
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    protected GeofenceTransitionManagerImpl() {
        mContext = BaseApp.get().getApplicationContext();
        mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
    }

    /// ----------------------
    /// Private Methods
    /// ----------------------

    private void switchWifi(boolean turnOn) {
        boolean isWifiEnabled = mWifiManager.isWifiEnabled();

        if(turnOn && !isWifiEnabled) { //Turn on WiFi only if it's actually off
            Debug.logDebug(TAG, "Turning on Wifi!");
            mWifiManager.setWifiEnabled(true);
        }
        else if(!turnOn && isWifiEnabled) { //Turn off WiFi only if it's actually on
            Debug.logDebug(TAG, "Turning off Wifi!");
            mWifiManager.setWifiEnabled(false);
        }
    }

    private void updateForegroundNotification(String details) {
        Intent intent = new Intent(Constants.ACTION_NOTIFICATION_UPDATE);
        intent.putExtra(Constants.EXTRA_NOTIFICATION_CONTENT, details);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(mContext, MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_add) //JAM TODO: Use real notification icon
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.ic_add)) //JAM TODO: Use real notification icon
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(mContext.getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransitionText    The prefix to display to the user (typically Entered, Dwelling, Exited, etc.)
     * @param triggeringGeofences       The geofence(s) triggered.
     * @return                          The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            String geofenceTransitionText,
            List<Geofence> triggeringGeofences) {

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionText + ": " + triggeringGeofencesIdsString;
    }
}
