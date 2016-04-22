package jasenmoloy.wirelesscontrol.mvp;

import java.util.List;

import jasenmoloy.wirelesscontrol.data.GeofenceData;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public interface MainView {

    void checkPermissions();

    void onCardDataLoaded(List<GeofenceData> cardData);
}
