package jasenmoloy.wirelesscontrol.presentation.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import jasenmoloy.wirelesscontrol.R;
import jasenmoloy.wirelesscontrol.application.data.GeofenceData;
import jasenmoloy.wirelesscontrol.application.debug.Debug;
import jasenmoloy.wirelesscontrol.adapters.MainPresenter;
import jasenmoloy.wirelesscontrol.adapters.MainPresenterImpl;
import jasenmoloy.wirelesscontrol.adapters.MainView;
import jasenmoloy.wirelesscontrol.presentation.service.GeofenceHandlerService;

public class MainActivity extends AppCompatActivity implements MainView {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    public static final int LOCATION_PERMISSIONS = 1;
    public static final int STARTING_DIALOG_VERISON = -5;
    public static final int INTRODUCTION_DIALOG_VERSION = -4;

    private static final String TAG = MainActivity.class.getSimpleName();

    private class InitTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            while(!mIsServiceConnected || !mIntroDialogSeen) {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //Now that the service is connected and the user has seen the intro dialog,
            //lets present the required permissions.
            checkPermissions();
        }
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private RecyclerView mRecyclerView;
    private TextView mEmptyMessage;
    private ProgressBar mProgressBar;

    private SharedPreferences mSharedPreferences;

    private MainPresenter mPresenter;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIsServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceConnected = false;
        }
    };

    private volatile boolean mIsServiceConnected = false;
    private volatile boolean mIntroDialogSeen = false;

    /// ----------------------
    /// Public Methods
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

        //JAM Disabled for now.
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                Intent intent = new Intent(this, SettingsActivity.class);
//                startActivity(intent);
//                return true;
//            default:
//                //JAM User action not recognized. Invoke the super class instead.
//                return super.onOptionsItemSelected(item);
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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
    }

    /**
     * Loads the recyclerView's cards with the loaded geofence data
     * @param geofenceData Saved Geofence data created by the user
     */
    @Override
    public void loadGeofenceCards(ArrayList<GeofenceData> geofenceData) {
        Debug.logVerbose(TAG, "cardData.length:" + geofenceData.size());

        if(geofenceData.size() == 0) { //Display a message to the user that we don't have any to load
            mProgressBar.setVisibility(View.GONE);
            mEmptyMessage.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
        else {
            mProgressBar.setVisibility(View.GONE);
            mEmptyMessage.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            //Specify target adapter to use to populate each card
            mRecyclerView.setAdapter(new GeofenceCardAdapter(geofenceData));
        }
    }

    @Override
    public void unloadGeofenceCards() {
        mEmptyMessage.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE); //Show the loading bar again when we reload our data
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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Grab the message and progress bar that are placeholders for the recyclerView
        mEmptyMessage = (TextView) findViewById(R.id.main_empty_geofence_container);
        mProgressBar = (ProgressBar) findViewById(R.id.main_geofence_container_progressbar);

        //Set the active toolbar
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        //Set the add button to open a new geofence card
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPresenter.allowNewGeofence()) {
                    Intent intent = new Intent(v.getContext(), AddGeofenceActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //Prevents reinstantiation if the activity already exists
                    startActivity(intent);
                }
                else {
                    //Display a toast informing the user they've reach the max allowablelimit
                    UIHelper.displayToast(MainActivity.this, Toast.LENGTH_SHORT, getString(R.string.mainactivity_toast_max_geofence_limit, MainPresenter.MAX_ALLOWABLE_GEOFENCES));
                }
            }
        });

        //Set up our presenter
        mPresenter = new MainPresenterImpl(this, this);
        mPresenter.onActivityCreated(this, savedInstanceState);



        //Create the loader thread which watches until the the service is up and the user has seen
        //the dialog screen
        new InitTask().execute();

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
        else {
            mIntroDialogSeen = true;
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

                        //Set the flag that the user has seen the intro dialog
                        mIntroDialogSeen = true;
                    }
                });
    }
}
