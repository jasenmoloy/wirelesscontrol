package jasenmoloy.wirelesscontrol.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;
import jasenmoloy.wirelesscontrol.debug.Debug;

/**
 * Created by jasenmoloy on 4/26/16.
 */
public class GeofenceCardView extends CardView {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    /// ----------------------
    /// Object Fields
    /// ----------------------

    int mPosition;
    GeofenceData mData;
    int mLastEvent;

    /// ----------------------
    /// Getters / Setters
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public GeofenceCardView(Context context) {
        super(context);
    }

    public GeofenceCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GeofenceCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setData(int position, GeofenceData data) {
        mPosition = position;
        mData = data;
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    /**
     * Handle an user's touch response to open an edit geofence activity.
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Debug.logDebug("GeofenceCardView", "position: " + mPosition + " ev.getAction(): " + ev.getAction());
        int action = ev.getAction();

        switch(action) {
            case MotionEvent.ACTION_UP:
                //If the user's last action was pressing down, then we're attempting to "tap"
                // the button rather than scroll.
                if(mLastEvent == MotionEvent.ACTION_DOWN) {
                    Context context = getContext();
                    Intent intent = new Intent(context, EditGeofenceActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //Prevents reinstantiation if the activity already exists
                    intent.putExtra(Constants.BROADCAST_EXTRA_KEY_GEOFENCE_ID, mPosition);
                    intent.putExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA, mData);
                    context.startActivity(intent);
                }
                break;
        }

        mLastEvent = action;
        return true;
    }

    /**
     * Intecept all touch events heading to the children to be handled here.
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true; //JAM Intercept all touch events within this view group
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------
}
