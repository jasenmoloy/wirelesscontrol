package jasenmoloy.wirelesscontrol.ui;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import junit.framework.Assert;

import java.util.List;

import jasenmoloy.wirelesscontrol.R;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;

/**
 * Created by jasenmoloy on 2/17/16.
 */
public class GeofenceCardAdapter extends RecyclerView.Adapter<GeofenceCardAdapter.ViewHolder> {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = "GeofenceCardAdapter";

    //Providing a reference to the views that are contained within each card
    public static class ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback, Application.ActivityLifecycleCallbacks, ComponentCallbacks {
        private GeofenceMarker mMarker;

        //UI Elements
        private CardView mCardView;
        private GoogleMap mMap;
        private MapView mMapView;
        private TextView mName;
        private TextView mLocation;
        private TextView mRadius;

        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
            mMap = null;

            mName = (TextView) mCardView.findViewById(R.id.card_savedgeofence_name);
            mLocation = (TextView) mCardView.findViewById(R.id.card_savedgeofence_location);
            mRadius = (TextView) mCardView.findViewById(R.id.card_savedgeofence_radius);
            mMapView = (MapView) mCardView.findViewById(R.id.card_savedgeofence_map);
        }

        public void setCard(GeofenceData data) {
            mName.setText(data.name);
            mLocation.setText("Lat:" + Double.toString(data.position.latitude) + " Long:" + Double.toString(data.position.longitude)); //JAM TODO: Move this to resoruces file
            mRadius.setText("Radius:" + Double.toString(data.radius) + " meters"); //JAM TODO: Move this to resoruces file

            //If we already have one created, just update the marker
            if(mMarker != null && mMap != null) {
                //Reset our existing marker and add it to the map.
                mMarker.Reset(data.position, data.radius);
                displayMarkerOnMap();
            }
            else {
                //Create a Geofence Marker
                mMarker = new GeofenceMarker(data.position, data.radius);

                //Acquire the map
                mMapView.onCreate(null);
                mMapView.getMapAsync(this);
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            //Disable the map toolbar as we have no need for it in this context.
            mMap.getUiSettings().setMapToolbarEnabled(false);

            //Set the marker we have now that the map has loaded
            displayMarkerOnMap();
        }

        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            mMapView.onCreate(savedInstanceState);
        }

        public void onActivityStarted(Activity activity) {
            //Stubbed
        }

        public void onActivityResumed(Activity activity) {
            mMapView.onResume();
        }

        public void onActivityPaused(Activity activity) {
            mMapView.onPause();
        }

        public void onActivityStopped(Activity activity) {
            //Stubbed
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            mMapView.onSaveInstanceState(outState);
        }

        public void onActivityDestroyed(Activity activity) {
            mMapView.onDestroy();
        }

        public void onLowMemory() {
            mMapView.onLowMemory();
        }

        public void onConfigurationChanged(Configuration config) {
            //Stubbed
        }

        private void displayMarkerOnMap() {
            Assert.assertNotNull(mMap);

            mMarker.AddToMap(mMap);
            mMarker.MoveCameraOnMarker(mMap);
        }
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private List<GeofenceData> mDataset;
    private Application mApplication;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    //Providing a constructor to create this dataset
    public GeofenceCardAdapter(Application globalApplication, List<GeofenceData> dataset) {
        mApplication = globalApplication;
        mDataset = dataset;
    }

    //Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    //Create new views (this is invoked by the layout manager)
    @Override
    public GeofenceCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
        //create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_geofence, parent, false);

        //JAM TODO: set view size, margins, paddings, etc. parameters

        //Creat the view holder container
        ViewHolder vh = new ViewHolder(v);

        //Set callbacks for each cardView as they are required for Google Maps
        mApplication.registerActivityLifecycleCallbacks(vh);
        mApplication.registerComponentCallbacks(vh);

        return vh;
    }

    //Replace contents of a view (invoked by layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setCard(mDataset.get(position));
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

}
