package jasenmoloy.wirelesscontrol;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    public static final String msGoogleMapsApiKey = "AIzaSyCwLIuxHEE5Dly9nrkyxl_8kiGjgJ8jmDk";

    public static final String[] mTestDataset = {
            "hello1", "hello2", "hello3", "hello4", "hello5", "hello6",
            "hello7", "hello8", "hello9", "hello10", "hello11", "hello12",
            "hello13", "hello14", "hello15", "hello16", "hello17", "hello18",
            "hello1", "hello2", "hello3", "hello4", "hello5", "hello6",
            "hello7", "hello8", "hello9", "hello10", "hello11", "hello12",
            "hello13", "hello14", "hello15", "hello16", "hello17", "hello18",
            "hello1", "hello2", "hello3", "hello4", "hello5", "hello6",
            "hello7", "hello8", "hello9", "hello10", "hello11", "hello12",
            "hello13", "hello14", "hello15", "hello16", "hello17", "hello18",
            "hello1", "hello2", "hello3", "hello4", "hello5", "hello6",
            "hello7", "hello8", "hello9", "hello10", "hello11", "hello12",
            "hello13", "hello14", "hello15", "hello16", "hello17", "hello18",
            "hello1", "hello2", "hello3", "hello4", "hello5", "hello6",
            "hello7", "hello8", "hello9", "hello10", "hello11", "hello12",
            "hello13", "hello14", "hello15", "hello16", "hello17", "hello18",
            "hello1", "hello2", "hello3", "hello4", "hello5", "hello6",
            "hello7", "hello8", "hello9", "hello10", "hello11", "hello12",
            "hello13", "hello14", "hello15", "hello16", "hello17", "hello18",
            "hello1", "hello2", "hello3", "hello4", "hello5", "hello6",
            "hello7", "hello8", "hello9", "hello10", "hello11", "hello12",
            "hello13", "hello14", "hello15", "hello16", "hello17", "hello18",
            "hello1", "hello2", "hello3", "hello4", "hello5", "hello6",
            "hello7", "hello8", "hello9", "hello10", "hello11", "hello12",
            "hello13", "hello14", "hello15", "hello16", "hello17", "hello18",
            "hello1", "hello2", "hello3", "hello4", "hello5", "hello6",
            "hello7", "hello8", "hello9", "hello10", "hello11", "hello12",
            "hello13", "hello14", "hello15", "hello16", "hello17", "hello18",
            "hello1", "hello2", "hello3", "hello4", "hello5", "hello6",
            "hello7", "hello8", "hello9", "hello10", "hello11", "hello12",
            "hello13", "hello14", "hello15", "hello16", "hello17", "hello18",
            "hello1", "hello2", "hello3", "hello4", "hello5", "hello6",
            "hello7", "hello8", "hello9", "hello10", "hello11", "hello12",
            "hello13", "hello14", "hello15", "hello16", "hello17", "hello18",
            "hello1", "hello2", "hello3", "hello4", "hello5", "hello6",
            "hello7", "hello8", "hello9", "hello10", "hello11", "hello12",
            "hello13", "hello14", "hello15", "hello16", "hello17", "hello18",
    };

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the active view
        setContentView(R.layout.activity_main);

        //Set the active toolbar
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        //Set default parameters for all preferences.
        PreferenceManager.setDefaultValues(this, R.xml.settings_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.settings_wifi, false);
        PreferenceManager.setDefaultValues(this, R.xml.settings_bluetooth, false);

        //Grab Recycler information
        mRecyclerView = (RecyclerView) findViewById(R.id.list_geofences);
        mRecyclerView.setHasFixedSize(true); //Improves performance if you know the layout size does not change.

        //Use a linear layout for geofence cards
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Specify target adapter to use to populate each card
        mAdapter = new SavedGeofenceCardAdapter(mTestDataset);
        mRecyclerView.setAdapter(mAdapter);

        //Set the add button to open a new geofence card
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddGeofenceActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId())
        {
            case R.id.action_addgeofence:
                //JAM TODO Invoke add geo fence flow.
                return true;
            case R.id.action_settings:
                //JAM TODO Invoke the settings fragments.
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                //JAM User action not recognized. Invoke the super class instead.
                return super.onOptionsItemSelected(item);
        }
    }
}
