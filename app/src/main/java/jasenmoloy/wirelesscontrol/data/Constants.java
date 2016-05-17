package jasenmoloy.wirelesscontrol.data;

/**
 * Created by jasenmoloy on 3/15/16.
 */
public final class Constants {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    /**
     * Broadcast actions
     */
    public static final String BROADCAST_ACTION_LOCATIONSERVICES_CONNECTED = "com.jasenmoloy.wirelesscontrol.MainActivity.LOCATIONSERVICES_CONNECTED";
    public static final String BROADCAST_ACTION_PERMISSION_REQUESTED = "com.jasenmoloy.wirelesscontrol.MainActivity.PERMISSION_REQUESTED";

    public static final String BROADCAST_ACTION_PERMISSIONS_GRANTED = "com.jasenmoloy.wirelesscontrol.service.AutonomousGeofenceHandlerService.PERMISSIONS_GRANTED";
    public static final String BROADCAST_ACTION_GEODATA_LOADED = "com.jasenmoloy.wirelesscontrol.service.AutonomousGeofenceHandlerService.GEODATA_LOADED";
    public static final String BROADCAST_ACTION_SAVE_GEOFENCE = "com.jasenmoloy.wirelesscontrol.MainActivity.SAVE_GEOFENCE";
    public static final String BROADCAST_ACTION_UPDATE_GEOFENCE = "com.jasenmoloy.wirelesscontrol.MainActivity.UPDATE_GEOFENCE";
    public static final String BROADCAST_ACTION_DELETE_GEOFENCE = "com.jasenmoloy.wirelesscontrol.MainActivity.DELETE_GEOFENCE";
    public static final String BROADCAST_ACTION_GEOFENCE_SAVED = "com.jasenmoloy.wirelesscontrol.MainActivity.GEOFENCE_SAVED";
    public static final String BROADCAST_ACTION_GEOFENCE_UPDATED = "com.jasenmoloy.wirelesscontrol.MainActivity.GEOFENCE_UPDATED";
    public static final String BROADCAST_ACTION_GEOFENCE_DELETED = "com.jasenmoloy.wirelesscontrol.MainActivity.GEOFENCE_DELETED";

    /**
     * Broadcast extras
     */
    public static final String BROADCAST_EXTRA_KEY_PERMISSION_REQUEST = "PERMISSION_REQUEST";
    public static final String BROADCAST_EXTRA_KEY_GEOFENCE_ID = "GEOFENCE_ID";
    public static final String BROADCAST_EXTRA_KEY_GEODATA = "GEODATA";
    public static final String BROADCAST_EXTRA_KEY_GEODATALIST = "GEODATALIST";
    public static final String BROADCAST_EXTRA_KEY_BOOLEAN = "BOOLEAN";

    /**
     * Prevents instantiation
     */
    private Constants() {

    }
}
