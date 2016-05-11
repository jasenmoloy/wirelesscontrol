package jasenmoloy.wirelesscontrol.managers;

import android.content.Context;
import android.os.AsyncTask;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataLoadFinishedListener;

/**
 * Created by jasenmoloy on 5/10/16.
 */
public class GeofenceDataManager {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    //JAM TODO Move to resources file
    private static final String FILENAME = "GeofenceData";


    /// ----------------------
    /// Object Fields
    /// ----------------------

    class LoadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            File f = new File(params[0]);

            //If the file does not exist, send an empty string as we have no data to load yet.
            if(!f.exists()) {
                return "";
            }

            try {
                StringBuffer output = new StringBuffer();
                FileInputStream is = mContext.openFileInput(params[0]);
                InputStreamReader inputStreamReader = new InputStreamReader(is);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;

                while((line = bufferedReader.readLine()) != null) {
                    output.append(line);
                }

                bufferedReader.close();
                return output.toString();
            }
            catch(Exception ex) {
                ex.printStackTrace();

            }

            return null;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if(jsonData != null) {
                //JAM if we have a string, then we have data to serialize
                if(jsonData.length() > 0) {
                    try {
                        LoganSquare.parseList(jsonData, mGeofenceData.getClass());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                mListener.onGeofenceDataLoadSuccess(mGeofenceData);
            }
            else {
                mListener.onGeofenceDataLoadError();
            }

            mListener = null;
        }
    }

    Context mContext;
    ArrayList<GeofenceData> mGeofenceData;

    OnGeofenceDataLoadFinishedListener mListener;

    /// ----------------------
    /// Getters / Setters
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public GeofenceDataManager(Context context) {
        mContext = context;
        mGeofenceData = new ArrayList<>();
    }

    public void addGeofence(GeofenceData data) {
        mGeofenceData.add(data);
    }

    public void addGeofence(List<GeofenceData> data) {
        for(GeofenceData geofence : data) {
            mGeofenceData.add(geofence);
        }
    }

    public void save() {
        try {
            FileOutputStream outputStream = mContext.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            LoganSquare.serialize(mGeofenceData, outputStream);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void load(OnGeofenceDataLoadFinishedListener listener) {
        mListener = listener;
        new LoadTask().execute(getGeofenceDataFilePath());
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

    private String getGeofenceDataFilePath() {
        return mContext.getFilesDir() + "/" +  FILENAME + ".json";
    }

}