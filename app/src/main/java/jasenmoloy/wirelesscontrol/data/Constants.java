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

    /**
     * Broadcast extras
     */
    public static final String BROADCAST_EXTRA_KEY_PERMISSION_REQUEST = "PERMISSION_REQUEST";
    public static final String BROADCAST_EXTRA_KEY_GEODATA = "GEODATA";

    /**
     * Prevents instantiation
     */
    private Constants() {

    }
}
