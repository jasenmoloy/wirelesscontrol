package jasenmoloy.wirelesscontrol.ui;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

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
    public static class ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
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


            mName = (TextView) mCardView.findViewById(R.id.card_savedgeofence_name);
            mLocation = (TextView) mCardView.findViewById(R.id.card_savedgeofence_location);
            mRadius = (TextView) mCardView.findViewById(R.id.card_savedgeofence_radius);
            mMapView = (MapView) mCardView.findViewById(R.id.card_savedgeofence_map);
        }

        public void SetCard(GeofenceData data) {
            //JAM TODO: Set the Google Lite Map given the location, and a geofence marker.
            mName.setText(data.name);
            mLocation.setText("Lat:" + Double.toString(data.position.latitude) + " Long:" + Double.toString(data.position.longitude));
            mRadius.setText("Radius:" + Double.toString(data.radius) + " meters");

            //Create a Geofence Marker
            mMarker = new GeofenceMarker(data.position, data.radius);

            //Acquire the map
            mMapView.onCreate(null);
            mMapView.getMapAsync(this);

            //If the maps is not ready yet, show a progress bar where it should be and wait.
            if(mMap != null) {
                InitMarkerOnMap();
            }
            else {
                //JAM TODO: Show a progress bar and wait until the map is ready.
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            //Disable the map toolbar as we have no need for it in this context.
            mMap.getUiSettings().setMapToolbarEnabled(false);

            //Set the marker we have now that the map has loaded if it's ready.
            if(mMarker != null) {
                InitMarkerOnMap();
            }
            else {
                //JAM TODO: Show a progress bar and wait until the data is ready.
            }
        }

        private void InitMarkerOnMap() {
            mMarker.AddToMap(mMap);
            mMarker.MoveCameraOnMarker(mMap);
        }
    }

    /// ----------------------
    /// Object Fields
    /// ----------------------

    private List<GeofenceData> mDataset;

    /// ----------------------
    /// Public Methods
    /// ----------------------

    //Providing a constructor to create this dataset
    public GeofenceCardAdapter(List<GeofenceData> dataset) {
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
        Debug.LogVerbose(TAG, "BOM onCreateViewHolder()");

        //create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_geofence, parent, false);

        //JAM TODO: set view size, margins, paddings, etc. parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    //Replace contents of a view (invoked by layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.SetCard(mDataset.get(position));
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

}
