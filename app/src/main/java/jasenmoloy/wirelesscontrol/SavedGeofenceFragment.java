package jasenmoloy.wirelesscontrol;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jasenmoloy on 2/8/16.
 */
public class SavedGeofenceFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedBundleState) {
        super.onCreate(savedBundleState);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_savedgeofence, container, false);
    }
}
