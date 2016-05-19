package jasenmoloy.wirelesscontrol.ui;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import junit.framework.Assert;

import java.util.List;

import jasenmoloy.wirelesscontrol.R;
import jasenmoloy.wirelesscontrol.data.GeofenceData;

/**
 * Created by jasenmoloy on 2/17/16.
 */
public class GeofenceCardAdapter extends RecyclerView.Adapter<GeofenceCardAdapter.ViewHolder> {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    private static final String TAG = GeofenceCardAdapter.class.getSimpleName();

    private static final int VIEWTYPE_GOOGLEMAP = 0;
    private static final int VIEWTYPE_BITMAP = 1;

    protected abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(CardView v) {
            super(v);
        }

        abstract void setCard(int position, GeofenceData data);

        abstract void onViewRecycled();
    }

    //Providing a reference to the views that are contained within each card
    public class ViewHolderGoogleMap extends GeofenceCardAdapter.ViewHolder implements
            OnMapReadyCallback, Application.ActivityLifecycleCallbacks, ComponentCallbacks {

        private GeofenceMarker mMarker;
        boolean mIsCardRecycled;

        //UI Elements
        private GeofenceCardView mCardView;
        private GoogleMap mMap;
        private MapView mMapView;
        private TextView mName;
        private TextView mLocation;
        private TextView mRadius;

        public ViewHolderGoogleMap(GeofenceCardView v) {
            super(v);
            mCardView = v;
            mMap = null;
            mIsCardRecycled = false;

            mName = (TextView) mCardView.findViewById(R.id.card_savedgeofence_name);
            mLocation = (TextView) mCardView.findViewById(R.id.card_savedgeofence_location);
            mRadius = (TextView) mCardView.findViewById(R.id.card_savedgeofence_radius);
            mMapView = (MapView) mCardView.findViewById(R.id.card_savedgeofence_map);
        }

        /**
         * Remove any "intensive" resources from this view as we're being recycled.
         */
        public void onViewRecycled() {
            mMapView.onPause();
            mIsCardRecycled = true;
        }

        public void setCard(int position, GeofenceData data) {
            mName.setText(data.name);

            mLocation.setText(String.format("Lat:%1$.4f Long:%2$.4f", //JAM TODO: Need to get access to context to get resources
                    data.position.latitude,
                    data.position.longitude));

            mRadius.setText(String.format("Radius: %1$.0f meters", //JAM TODO: Need to get access to context to get resources
                    data.radius));

            //If we already have one created, just update the marker
            if(mMarker != null && mMap != null) {
                if( mIsCardRecycled ) {
                    mMapView.onResume();
                    mIsCardRecycled = false;
                }

                //reset our existing marker and add it to the map.
                mMarker.reset(data.position, data.radius);
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

            mMarker.addToMap(mMap);
            mMarker.moveCameraOnMarker(mMap);
        }
    }

    public class ViewHolderBitmap extends GeofenceCardAdapter.ViewHolder {
        private GeofenceCardView mCardView;
        private ImageView mImageView;
        private TextView mName;
        private TextView mLocation;
        private TextView mRadius;

        public ViewHolderBitmap(GeofenceCardView v) {
            super(v);
            mCardView = v;

            mName = (TextView) mCardView.findViewById(R.id.card_savedgeofence_name);
            mLocation = (TextView) mCardView.findViewById(R.id.card_savedgeofence_location);
            mRadius = (TextView) mCardView.findViewById(R.id.card_savedgeofence_radius);
            mImageView = (ImageView) mCardView.findViewById(R.id.card_savedgeofence_image);
        }

        public void onViewRecycled() {
            mCardView.setData(-1, null);
        }

        public void setCard(int position, GeofenceData data) {
            //Set the new data on the CardView
            mCardView.setData(position, data);

            mName.setText(data.name);

            mLocation.setText(String.format("Lat:%1$.4f Long:%2$.4f", //JAM TODO: Need to get access to context to get resources
                    data.position.latitude,
                    data.position.longitude));

            mRadius.setText(String.format("Radius: %1$d meters", //JAM TODO: Need to get access to context to get resources
                    data.radius));

            mImageView.setImageBitmap(data.mapScreenshot);
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

    /**
     * Providing a constructor to create this dataset
     * @param globalApplication
     * @param dataset
     */
    public GeofenceCardAdapter(Application globalApplication, List<GeofenceData> dataset) {
        mApplication = globalApplication;
        mDataset = dataset;
    }

    /**
     * Return the size of the dataset (invoked by the layout manager)
     * @return returns the size of the given dataset
     */
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    /**
     * Determines which viewHolder to use depending on the position's data.
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if(mDataset.get(position).mapScreenshot != null)
            return VIEWTYPE_BITMAP;
        else
            return VIEWTYPE_GOOGLEMAP;
    }


    /// ----------------------
    /// Callback Methods
    /// ----------------------

    /**
     * Create new views (this is invoked by the layout manager)
     * @param parent
     * @param viewType
     * @return Newly created view holder with the initialized CardView
     */
    @Override
    public GeofenceCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
        GeofenceCardView cardView;
        ViewHolder vh;

        switch(viewType) {
            case VIEWTYPE_BITMAP:
                //create a new view
                cardView = (GeofenceCardView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_geofenceimage, parent, false);

                //Create and set the view holder container
                vh = new ViewHolderBitmap(cardView);
                break;
            case VIEWTYPE_GOOGLEMAP:
            default:
                //create a new view
                cardView = (GeofenceCardView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_geofence, parent, false);

                //Create the view holder container
                ViewHolderGoogleMap vhGoogleMap = new ViewHolderGoogleMap(cardView);

                //Set callbacks for this holder as MapView requires knowledge of the Activity's lifecycle
                mApplication.registerActivityLifecycleCallbacks(vhGoogleMap);
                mApplication.registerComponentCallbacks(vhGoogleMap);

                //Set the viewHolder to be returned
                vh = vhGoogleMap;
                break;
        }

        return vh;
    }

    /**
     * Sets the contents of the view (invoked by layout manager)
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setCard(position, mDataset.get(position));
    }

    /**
     * Called when a view is need to be prepped for reuse. Unload any heavy resources.
     * @param holder
     */
    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.onViewRecycled();
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

}
