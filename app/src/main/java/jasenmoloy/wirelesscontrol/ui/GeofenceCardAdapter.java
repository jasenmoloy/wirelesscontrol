package jasenmoloy.wirelesscontrol.ui;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

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

    //Providing a reference to the views that are contained within each card
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView mCardView;

        private TextView mName;
        private TextView mLocation;
        private TextView mRadius;

        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;

            mName = (TextView) mCardView.findViewById(R.id.card_savedgeofence_name);
            mLocation = (TextView) mCardView.findViewById(R.id.card_savedgeofence_location);
            mRadius = (TextView) mCardView.findViewById(R.id.card_savedgeofence_radius);
        }

        public void SetCard(GeofenceData data) {
            //JAM TODO: Set the Google Lite Map given the location, and a geofence marker.
            mName.setText(data.name);
            mLocation.setText("Lat:" + Double.toString(data.position.latitude) + " Long:" + Double.toString(data.position.longitude));
            mRadius.setText("Radius:" + Double.toString(data.radius) + " meters");
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
