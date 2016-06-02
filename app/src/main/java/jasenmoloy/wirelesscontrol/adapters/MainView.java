package jasenmoloy.wirelesscontrol.adapters;

import java.util.ArrayList;

import jasenmoloy.wirelesscontrol.application.data.GeofenceData;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface MainView {

    void loadGeofenceCards(ArrayList<GeofenceData> geofenceData);

    void unloadGeofenceCards();
}
