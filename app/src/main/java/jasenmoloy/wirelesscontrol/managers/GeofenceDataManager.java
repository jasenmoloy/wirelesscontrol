package jasenmoloy.wirelesscontrol.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataLoadFinishedListener;
import jasenmoloy.wirelesscontrol.io.OnGeofenceDataUpdateFinishedListener;
import jasenmoloy.wirelesscontrol.io.OnGeofenceSaveFinishedListener;

/**
 * Created by jasenmoloy on 5/10/16.
 */
public class GeofenceDataManager {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = "GeofenceDataManager";

    //JAM TODO Move to resources file
    private static final String FILENAME = "GeofenceData";


    /// ----------------------
    /// Object Fields
    /// ----------------------

    class LoadTask extends AsyncTask<String, Void, ArrayList<GeofenceData>> {
        @Override
        protected ArrayList<GeofenceData> doInBackground(String... params) {
            File f = new File(mContext.getFilesDir() + "/" + params[0]);

            //If the file does not exist, send an empty list as we have no data to load yet.
            if(!f.exists()) {
                return new ArrayList<>();
            }

            try {
                StringBuffer output = new StringBuffer();
                FileInputStream inputStream = mContext.openFileInput(params[0]);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                ArrayList<GeofenceData> geoData;
                Bitmap bitmap;

                //Grab all JSON data
                while((line = bufferedReader.readLine()) != null) {
                    output.append(line);
                }

                //Parse the new JSON data and assign it to a new list of GeofenceData
                bufferedReader.close();
                geoData = (ArrayList<GeofenceData>) LoganSquare.parseList(output.toString(), GeofenceData.class);

                //Load all bitmaps given the filenames within the JSON data
                for(GeofenceData data : geoData) {
                    if(data.screenshotFileName == null) { //JAM TODO: Skip this screenshot if it doesn't exist...for now
                        Debug.logError(TAG, "screenshotFileName is NULL for " + data.name + "! Can't load an approparite bitmap.");
                        continue;
                    }

                    inputStream = mContext.openFileInput(data.screenshotFileName);
                    if((bitmap = BitmapFactory.decodeStream(inputStream)) != null) {
                        data.addBitmap(bitmap);
                        //Bitmap loaded successfully!
                    }
                    else {
                        //Bitmap failed to load!
                    }
                    inputStream.close();
                }

                return geoData;
            }
            catch(Exception ex) {
                ex.printStackTrace();

            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<GeofenceData> geofenceData) {
            if(mLoadListener == null)
                return;

            if(geofenceData != null) {
                //JAM if we have a string, then we have data to serialize
                if(geofenceData.size() > 0) {
                    try {
                        mGeofenceData = geofenceData;
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

                //Save out all bitmaps to files for future retrieving
                for(GeofenceData data : mGeofenceData) {
                    if(data.screenshotFileName == null) { //JAM TODO: Skip this screenshot save if it doesn't exist...for now.
                        Debug.logError(TAG, "screenshotFileName is NULL for " + data.name + "! Can't save an approparite bitmap!");
                        continue;
                    }

                    outputStream = mContext.openFileOutput(data.screenshotFileName, Context.MODE_PRIVATE);
                    if(data.mapScreenshot.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                        //JAM TODO: Report Success!
                    }
                    else {
                        //JAM TODO: Report Failure!
                    }
                    outputStream.close();
                }


                return jsonData.length();
            }
            catch(Exception ex) {
                ex.printStackTrace();

            }

            return -1;
        }

        @Override
        protected void onPostExecute(Integer bytesSaved) {
            if(mSaveListener != null) {
                if (bytesSaved > 0) {
                    mSaveListener.onGeofenceSaveSuccess();
                } else {
                    mSaveListener.onGeofenceSaveError();
                }

                mSaveListener = null;
            }

            if(mUpdateListener != null) {
                if (bytesSaved > 0) {
                    mUpdateListener.onGeofenceDataUpdateSuccess(mUpdatePosition, mGeofenceData.get(mUpdatePosition));
                } else {
                    mUpdateListener.onGeofenceDataUpdateError();
                }

                mUpdateListener = null;
                mUpdatePosition = -1;
            }
        }
    }

    Context mContext;
    ArrayList<GeofenceData> mGeofenceData;

    OnGeofenceDataLoadFinishedListener mLoadListener;
    OnGeofenceSaveFinishedListener mSaveListener;

    OnGeofenceDataUpdateFinishedListener mUpdateListener;
    int mUpdatePosition;

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

    public void updateGeofence(int id, GeofenceData updateData, OnGeofenceDataUpdateFinishedListener listener) {
        mUpdateListener = listener;
        mUpdatePosition = id;
        mGeofenceData.set(id, updateData);
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