package jasenmoloy.wirelesscontrol;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by jasenmoloy on 2/17/16.
 */
public class SavedGeofenceCardAdapter extends RecyclerView.Adapter<SavedGeofenceCardAdapter.ViewHolder> {
    private String[] mDataset;

    //Providing a reference to the views that are contained within each card
    public static class ViewHolder extends RecyclerView.ViewHolder {
        //JAM TEMP: each data item is just a string for now.
        public CardView mCardView;
        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
        }
    }

    //Providing a constructor to create this dataset
    public SavedGeofenceCardAdapter(String[] dataset) {
        mDataset = dataset;
    }

    //Create new views (this is invoked by the layout manager)
    @Override
    public SavedGeofenceCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {
        //create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_savedgeofence, parent, false);

        //JAM TODO: set view size, margins, paddings, etc. parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    //Replace contents of a view (invoked by layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        TextView textView = (TextView) holder.mCardView.findViewById(R.id.card_savedgeofence_name);
        textView.setText(mDataset[position]);
    }

    //Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }

}
