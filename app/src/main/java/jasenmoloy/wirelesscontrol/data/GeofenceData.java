package jasenmoloy.wirelesscontrol.data;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class GeofenceData {
    public String name;
    public LatLng position;
    public double radius;

    public GeofenceData(String name, LatLng pos, double radius) {
        this.name = name;
        this.position = pos;
        this.radius = radius;
    }
}
