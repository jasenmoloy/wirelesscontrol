package jasenmoloy.wirelesscontrol.ui;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jasenmoloy.wirelesscontrol.R;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.helpers.UIHelper;
import jasenmoloy.wirelesscontrol.mvp.MainPresenter;
import jasenmoloy.wirelesscontrol.mvp.MainPresenterImpl;
import jasenmoloy.wirelesscontrol.mvp.MainView;
import jasenmoloy.wirelesscontrol.service.GeofenceHandlerService;

public class MainActivity extends AppCompatActivity implements MainView {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    public static final int LOCATION_PERMISSIONS = 1;

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String msGoogleMapsApiKey = "AIzaSyCwLIuxHEE5Dly9nrkyxl_8kiGjgJ8jmDk";
    public static final int STARTING_DIALOG_VERISON = -5;
    public static final int INTRODUCTION_DIALOG_VERSION = -4;

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView mEmptyMessage;

    private MainPresenter mPresenter;

    private SharedPreferences mSharedPreferences;

    //Services
    private Service mService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Once we're connected to our service, ask the user about permissions
            checkPermissions();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Stubbed
        }
    };

    /// ----------------------
    /// Public Methods
    /// ----------------------

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
        switch (item.getItemId()) {
            case R.id.action_settings:
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
    public void loadGeofenceCards(ArrayList<GeofenceData> geofenceData) {
        Debug.logVerbose(TAG, "cardData.length:" + geofenceData.size());

        if(geofenceData.size() == 0) { //Display a message to the user that we don't have any to load
            mEmptyMessage.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
        else {
            mEmptyMessage.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            //Specify target adapter to use to populate each card
            mAdapter = new GeofenceCardAdapter(getApplication(), geofenceData);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void unloadGeofenceCards() {
        mRecyclerView.setAdapter(null);
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Debug.logDebug(TAG, "JAM - onCreate()");

        super.onCreate(savedInstanceState);

        //Set default parameters for all preferences.
        PreferenceManager.setDefaultValues(this, R.xml.settings_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.settings_wifi, false);
        PreferenceManager.setDefaultValues(this, R.xml.settings_bluetooth, false);

        //Get preferences
        mSharedPreferences = getPreferences(MODE_PRIVATE);

        //Set up the activity's view
        setContentView(R.layout.activity_main);

        //Init any UI related related fields
        mRecyclerView = (RecyclerView) findViewById(R.id.list_geofences);
        mRecyclerView.setHasFixedSize(true); //Improves performance if you know the layout size does not change.

        //Use a linear layout for geofence cards
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mEmptyMessage = (TextView) findViewById(R.id.main_empty_geofence_container);

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

        //Set up our presenter
        mPresenter = new MainPresenterImpl(this, this);
        mPresenter.onActivityCreated(this, savedInstanceState);

        //Bind to our background service
        bindService(new Intent(this, GeofenceHandlerService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Debug.logDebug(TAG, "JAM - onStart()");

        mPresenter.onActivityStarted(this);

        //Determine if we should show the user the intro dialog.
        //Hardcoded values are used to represent the app's "version". Rudimentary way display different introduction/updates.
        if(mSharedPreferences.getInt(getString(R.string.mainactivity_pref_key_introduction_flag), STARTING_DIALOG_VERISON) < INTRODUCTION_DIALOG_VERSION) {
            showIntroDialog();
        }
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

    private void checkPermissions() {
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

    private void showIntroDialog() {
        UIHelper.displayOkDialog(this,
                R.string.introduction_dialog_title,
                R.string.introduction_dialog_message,
                R.string.introduction_dialog_button_text,
                false,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putInt(getString(R.string.mainactivity_pref_key_introduction_flag), INTRODUCTION_DIALOG_VERSION); //INTRODUCTION_DIALOG_VERSION (-4) is greater than STARTING_DIALOG_VERISON (-5)
                        editor.apply();
                    }
                });
    }
}
