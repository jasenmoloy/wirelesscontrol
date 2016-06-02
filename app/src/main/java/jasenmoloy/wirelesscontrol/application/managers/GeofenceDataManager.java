package jasenmoloy.wirelesscontrol.application.managers;

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

import jasenmoloy.wirelesscontrol.R;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;
import jasenmoloy.wirelesscontrol.adapters.io.OnGeofenceDataDeleteFinishedListener;
import jasenmoloy.wirelesscontrol.adapters.io.OnGeofenceDataLoadFinishedListener;
import jasenmoloy.wirelesscontrol.adapters.io.OnGeofenceDataUpdateFinishedListener;
import jasenmoloy.wirelesscontrol.adapters.io.OnGeofenceSaveFinishedListener;

/**
 * Created by jasenmoloy on 5/10/16.
 */
public class GeofenceDataManager {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = GeofenceDataManager.class.getSimpleName();

    private class LoadTask extends AsyncTask<String, Void, ArrayList<GeofenceData>> {
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
                        mIsDataLoaded = true;
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

    private class SaveTask extends AsyncTask<String, Void, Integer> {
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

            if(mDeleteListener != null) {
                if (bytesSaved > 0) {
                    mDeleteListener.onGeofenceDataDeleteSuccess();
                } else {
                    mDeleteListener.onGeofenceDataDeleteError();
                }

                mDeleteListener = null;
            }
        }
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private Context mContext;
    private ArrayList<GeofenceData> mGeofenceData;
    private boolean mIsDataLoaded;


    private OnGeofenceDataLoadFinishedListener mLoadListener;
    private OnGeofenceSaveFinishedListener mSaveListener;

    private OnGeofenceDataUpdateFinishedListener mUpdateListener;
    private int mUpdatePosition;

    private OnGeofenceDataDeleteFinishedListener mDeleteListener;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public GeofenceDataManager(Context context) {
        mContext = context;
        mGeofenceData = new ArrayList<>();
        mIsDataLoaded = false;
    }

    public boolean isDataLoaded() {
        return mIsDataLoaded;
    }

    public ArrayList<GeofenceData> getGeofenceData() {
        return mGeofenceData;
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

    public void deleteGeofence(int id, OnGeofenceDataDeleteFinishedListener listener) {
        mDeleteListener = listener;
        mUpdatePosition = id;
        mGeofenceData.remove(id);
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
            return mContext.getString(R.string.geofencedata_filename);
    }

}