package jasenmoloy.wirelesscontrol.ui;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import jasenmoloy.wirelesscontrol.R;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.mvp.MainPresenter;
import jasenmoloy.wirelesscontrol.mvp.MainPresenterImpl;
import jasenmoloy.wirelesscontrol.mvp.MainView;
import jasenmoloy.wirelesscontrol.service.AutonomousGeofenceHandlerService;

public class MainActivity extends AppCompatActivity implements MainView {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    public static final int LOCATION_PERMISSIONS = 1;

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
    private Service mService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((AutonomousGeofenceHandlerService.ServiceBinder)service).getService();

            //Check for permissions now that the service is connected.
            //This will inform the service "down the road" "we're good to go in initializing location services.
            checkPermissions();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public void checkPermissions() {
        //Request the permissions needed from the user for this application from the start
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {

            //If we have all the permissions we need, then we're good to go!
            mPresenter.onAllPermissionsGranted();
        }
        else {
            requestPermissions();
        }
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

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
            case LOCATION_PERMISSIONS:
                if(grantResults.length == 0) {
                    //user has denied/canceled permissions. JAM TODO: Should present a dialog explaining that this is required for this app to work properly.
                    requestPermissions();
                }

                if( grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //User has granted permission, inform the location manager we're good to go.
                    //JAM TODO: Save a flag to know the user has already given permission
                    mPresenter.onAllPermissionsGranted();
                }
                else {
                    //JAM TODO: User has denied one permissions. Inform the user that both are required for this app to work properly.
                    requestPermissions();
                }
                break;
            default:
                break;
        }
        return;
    }

    /**
     * Loads the recyclerView's cards with the loaded geofence data
     * @param geofenceData Saved Geofence data created by the user
     */
    @Override
    public void onCardDataLoaded(List<GeofenceData> geofenceData) {
        Debug.logVerbose(TAG, "cardData.length:" + geofenceData.size());

        //Specify target adapter to use to populate each card
        mAdapter = new GeofenceCardAdapter(getApplication(), geofenceData);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void loadGeofenceCards(ArrayList<GeofenceData> geofenceData) {
        //JAM TODO: Load all geofence cards
    }

    @Override
    public void unloadGeofenceCards() {
        //JAM TODO: Unload the adapter to relieve resources
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Debug.logDebug(TAG, "JAM - onCreate()");

        super.onCreate(savedInstanceState);

        //Init fields
        mPresenter = new MainPresenterImpl(this, this);

        //Set default parameters for all preferences.
        PreferenceManager.setDefaultValues(this, R.xml.settings_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.settings_wifi, false);
        PreferenceManager.setDefaultValues(this, R.xml.settings_bluetooth, false);

        //Set up the activity's view
        setContentView(R.layout.activity_main);

        //Init any UI related related fields
        mRecyclerView = (RecyclerView) findViewById(R.id.list_geofences);
        mRecyclerView.setHasFixedSize(true); //Improves performance if you know the layout size does not change.

        //Use a linear layout for geofence cards
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Set the active toolbar
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        //Set the add button to open a new geofence card
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddGeofenceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //Prevents reinstantiation if the activity already exists
                startActivity(intent);
            }
        });

        //Start the background service that will maintain
        initializeBackgroundService();

        mPresenter.registerReceiver(LocalBroadcastManager.getInstance(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Debug.logDebug(TAG, "JAM - onStart()");
        mPresenter.onActivityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Debug.logDebug(TAG, "JAM - onResume()");
        mPresenter.onActivityResumed(this);
    }

    @Override
    protected void onPause() {
        mPresenter.onActivityPaused(this);
        Debug.logDebug(TAG, "JAM - onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        mPresenter.onActivityStopped(this);
        Debug.logDebug(TAG, "JAM - onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        Debug.logDebug(TAG, "JAM - onDestroy()");
        mPresenter.onActivityDestroyed(this);
        super.onDestroy();
    }

    /// ----------------------
    /// Private Methods
    /// ----------------------

    /**
     * Start the autonomous geofence handler service which loads and tracks geofences
     */
    private void initializeBackgroundService() {
        Intent intent = new Intent(this, AutonomousGeofenceHandlerService.class);
        startService(intent);

        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        //JAM TODO: Communication should be handled by the presenter
    }

    /**
     * Prompts the Android permission request to the user.
     */
    private void requestPermissions() {
        ArrayList<String> permissionRequests = new ArrayList<>(2); //Increase this value as more permissions are added below

        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            permissionRequests.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            permissionRequests.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if(permissionRequests.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionRequests.toArray(new String[permissionRequests.size()]),
                    MainActivity.LOCATION_PERMISSIONS);
        }
    }
}
