package jasenmoloy.wirelesscontrol.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.List;

import jasenmoloy.wirelesscontrol.R;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.managers.LocationServicesManager;
import jasenmoloy.wirelesscontrol.mvp.MainPresenter;
import jasenmoloy.wirelesscontrol.mvp.MainPresenterImpl;
import jasenmoloy.wirelesscontrol.mvp.MainView;
import jasenmoloy.wirelesscontrol.service.InitializeGeofenceDataService;

public class MainActivity extends AppCompatActivity implements MainView {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    public static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;
    public static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    public static final String TAG = "MainActivity";
    public static final String msGoogleMapsApiKey = "AIzaSyCwLIuxHEE5Dly9nrkyxl_8kiGjgJ8jmDk";

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private MainPresenter mPresenter;

    //Services
    Intent mInitializationIntent;

    //TODO: Move these to the service and lay it out MVP
    LocationServicesManager mLocationServices;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Init fields
        mPresenter = new MainPresenterImpl(this);
        mLocationServices = new LocationServicesManager(this);

        //Set up the activity's view
        setContentView(R.layout.activity_main);

        //Init any UI related related fields
        mRecyclerView = (RecyclerView) findViewById(R.id.list_geofences);
        mRecyclerView.setHasFixedSize(true); //Improves performance if you know the layout size does not change.

        //Use a linear layout for geofence cards
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Set default parameters for all preferences.
        PreferenceManager.setDefaultValues(this, R.xml.settings_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.settings_wifi, false);
        PreferenceManager.setDefaultValues(this, R.xml.settings_bluetooth, false);

        //Set the active toolbar
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        //Set the add button to open a new geofence card
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddGeofenceActivity.class);
                startActivity(intent);
            }
        });

        //Create an intent service to start initializing geofence data
        mInitializationIntent = new Intent(this, InitializeGeofenceDataService.class);
        startService(mInitializationIntent);
    }

    @Override
    protected void onStart() {
        mLocationServices.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    protected void onStop() {
        mLocationServices.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDestroy();
        super.onDestroy();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case MY_PERMISSION_ACCESS_COARSE_LOCATION:
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //User has granted permission, inform the location manager we're good to go.
                    mLocationServices.performLocationServices();
                }
                else {
                    //JAM TODO: User has denied permissions. Should present a dialog explaining that this is required for this app to work properly.
                }
                break;
            default:
                break;
        }
        return;
    }

    public void onCardDataLoaded(List<GeofenceData> geofenceData) {

        Debug.LogVerbose(TAG, "cardData.length:" + geofenceData.size());

        //JAM TEMP TODO: Implement this elsewhere outside of activity
        mLocationServices.sendGeofenceData(geofenceData);

        //Specify target adapter to use to populate each card
        mAdapter = new GeofenceCardAdapter(geofenceData);
        mRecyclerView.setAdapter(mAdapter);
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

}
