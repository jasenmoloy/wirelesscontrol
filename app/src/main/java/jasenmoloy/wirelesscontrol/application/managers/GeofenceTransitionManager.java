package jasenmoloy.wirelesscontrol.application.managers;

import com.google.android.gms.location.Geofence;

import java.util.List;

/**
 * Created by jasenmoloy on 5/20/16.
 */
public abstract class GeofenceTransitionManager {
    private static GeofenceTransitionManager msInstance;

    public static GeofenceTransitionManager get() {
        if(msInstance == null) msInstance = getSync();
        return msInstance;
    }

    private static synchronized GeofenceTransitionManager getSync() {
        if(msInstance == null) msInstance = new GeofenceTransitionManagerImpl();
        return msInstance;
    }


    public abstract void onTransitionEnter(List<Geofence> triggeredGeofences);

    public abstract void onTransitionDwell(List<Geofence> triggeredGeofences);

    public abstract void onTransitionExit(List<Geofence> triggeredGeofences);


}
