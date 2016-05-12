package jasenmoloy.wirelesscontrol.managers;

import android.content.Context;
import android.os.AsyncTask;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataLoadFinishedListener;
import jasenmoloy.wirelesscontrol.io.OnGeofenceSaveFinishedListener;

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
            File f = new File(mContext.getFilesDir() + "/" + params[0]);

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
            if(mLoadListener == null)
                return;

            if(jsonData != null) {
                //JAM if we have a string, then we have data to serialize
                if(jsonData.length() > 0) {
                    try {
                        mGeofenceData = (ArrayList<GeofenceData>) LoganSquare.parseList(jsonData, GeofenceData.class);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                mLoadListener.onGeofenceDataLoadSuccess(mGeofenceData);
            }
            else {
                mLoadListener.onGeofenceDataLoadError();
            }

            mLoadListener = null;
        }
    }

    class SaveTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            try {
                FileOutputStream outputStream = mContext.openFileOutput(params[0], Context.MODE_PRIVATE);
                FileWriter writer = new FileWriter(outputStream.getFD());
                String jsonData = LoganSquare.serialize(mGeofenceData, GeofenceData.class);

                writer.write(jsonData);
                writer.flush();
                writer.close();

                return jsonData.length();
            }
            catch(Exception ex) {
                ex.printStackTrace();

            }

            return -1;
        }

        @Override
        protected void onPostExecute(Integer bytesSaved) {
            if(mSaveListener == null)
                return;

            if(bytesSaved > 0) {
                mSaveListener.onGeofenceSaveSuccess();
            }
            else {
                mSaveListener.onGeofenceSaveError();
            }

            mSaveListener = null;
        }
    }

    Context mContext;
    ArrayList<GeofenceData> mGeofenceData;

    OnGeofenceDataLoadFinishedListener mLoadListener;
    OnGeofenceSaveFinishedListener mSaveListener;

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

    public void addGeofence(GeofenceData data, OnGeofenceSaveFinishedListener listener) {
        mSaveListener = listener;
        mGeofenceData.add(data);
        new SaveTask().execute(getGeofenceDataFilename());
    }

    public void addGeofence(List<GeofenceData> data, OnGeofenceSaveFinishedListener listener) {
        mSaveListener = listener;
        for(GeofenceData geofence : data) {
            mGeofenceData.add(geofence);
        }
        new SaveTask().execute(getGeofenceDataFilename());
    }

    public void loadSavedGeofences(OnGeofenceDataLoadFinishedListener listener) {
        mLoadListener = listener;
        new LoadTask().execute(getGeofenceDataFilename());
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

    private String getGeofenceDataFilename() {
            return FILENAME + ".json";
    }

}