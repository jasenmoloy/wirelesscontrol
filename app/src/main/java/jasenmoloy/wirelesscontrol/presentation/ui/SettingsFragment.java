package jasenmoloy.wirelesscontrol.presentation.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import jasenmoloy.wirelesscontrol.R;

/**
 * Created by jasenmoloy on 1/28/16.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //JAM load all preferences from each XML resource.
        addPreferencesFromResource(R.xml.settings_general);
        addPreferencesFromResource(R.xml.settings_wifi);
        addPreferencesFromResource(R.xml.settings_bluetooth);
    }
}
