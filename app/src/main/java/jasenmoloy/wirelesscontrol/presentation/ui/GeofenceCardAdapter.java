package jasenmoloy.wirelesscontrol.presentation.ui;

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
import jasenmoloy.wirelesscontrol.application.data.GeofenceData;

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
            OnMapReadyCallback {

        /// ----------------------
        /// Class Fields
        /// ----------------------

        /// ----------------------
        /// Object Fields
        /// ----------------------

        private GeofenceMarker mMarker;

        //UI Elements
        private GeofenceCardView mCardView;
        private GoogleMap mMap;
        private MapView mMapView;
        private TextView mName;
        private TextView mLocation;
        private TextView mRadius;

        /// ----------------------
        /// Public Methods
        /// ----------------------

        public ViewHolderGoogleMap(GeofenceCardView v) {
            super(v);
            mCardView = v;
            mMap = null;

            mName = (TextView) mCardView.findViewById(R.id.card_savedgeofence_name);
            mLocation = (TextView) mCardView.findViewById(R.id.card_savedgeofence_location);
            mRadius = (TextView) mCardView.findViewById(R.id.card_savedgeofence_radius);
            mMapView = (MapView) mCardView.findViewById(R.id.card_savedgeofence_map);
        }

        /**
         * Remove any "intensive" resources from this view as we're being recycled.
         */
        @Override
        public void onViewRecycled() {
            mMapView.onPause();
        }

        @Override
        public void setCard(int position, GeofenceData data) {
            //Set the new data on the CardView
            mCardView.setData(position, data);

            mName.setText(data.displayName);

            mLocation.setText(String.format("Latitude:%1$.4f\nLongitude:%2$.4f", //JAM TODO: Need to get access to context to get resources
                    data.position.latitude,
                    data.position.longitude));
//
//            mRadius.setText(String.format("Radius: %1$d meters", //JAM TODO: Need to get access to context to get resources
//                    data.radius));

            //If we already have one created, just update the marker
            if(mMarker != null && mMap != null) {
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

            mMapView.onResume();
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            //Disable the map toolbar as we have no need for it in this context.
            mMap.getUiSettings().setMapToolbarEnabled(false);

            try {
                mMap.setMyLocationEnabled(true);
            }
            catch(SecurityException ex) {
                ex.printStackTrace();
            }

            //Set the marker we have now that the map has loaded
            displayMarkerOnMap();
        }

        /// ----------------------
        /// Protected Methods
        /// ----------------------

        /// ----------------------
        /// Private Methods
        /// ----------------------

        private void displayMarkerOnMap() {
            Assert.assertNotNull(mMap);

            mMarker.addToMap(mMap);
            mMarker.moveCameraOnMarker(mMap);
        }
    }

    public class ViewHolderBitmap extends GeofenceCardAdapter.ViewHolder {
        /// ----------------------
        /// Class Fields
        /// ----------------------

        /// ----------------------
        /// Object Fields
        /// ----------------------

        private GeofenceCardView mCardView;
        private ImageView mImageView;
        private TextView mName;
        private TextView mLocation;
        private TextView mRadius;

        /// ----------------------
        /// Public Methods
        /// ----------------------

        public ViewHolderBitmap(GeofenceCardView v) {
            super(v);
            mCardView = v;

            mName = (TextView) mCardView.findViewById(R.id.card_savedgeofence_name);
            mLocation = (TextView) mCardView.findViewById(R.id.card_savedgeofence_location);
            mRadius = (TextView) mCardView.findViewById(R.id.card_savedgeofence_radius);
            mImageView = (ImageView) mCardView.findViewById(R.id.card_savedgeofence_image);
        }

        @Override
        public void onViewRecycled() {
            mCardView.setData(-1, null);
        }

        @Override
        public void setCard(int position, GeofenceData data) {
            //Set the new data on the CardView
            mCardView.setData(position, data);

            mName.setText(data.displayName);

            mLocation.setText(String.format("Latitude:%1$.4f\nLongitude:%2$.4f", //JAM TODO: Need to get access to context to get resources
                    data.position.latitude,
                    data.position.longitude));

//            mRadius.setText(String.format("Radius: %1$d meters", //JAM TODO: Need to get access to context to get resources
//                    data.radius));

            mImageView.setImageBitmap(data.mapScreenshot);
        }

        /// ----------------------
        /// Protected Methods
        /// ----------------------

        /// ----------------------
        /// Private Methods
        /// ----------------------
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private List<GeofenceData> mDataset;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    /**
     * Providing a constructor to create this dataset
     * @param dataset
     */
    public GeofenceCardAdapter(List<GeofenceData> dataset) {
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

                //Set the viewHolder to be returned
                vh = new ViewHolderGoogleMap(cardView);
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
