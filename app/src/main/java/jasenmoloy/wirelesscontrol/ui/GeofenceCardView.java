package jasenmoloy.wirelesscontrol.ui;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.MotionEvent;

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
        //JAM TODO: Implement "EditGeofence" functionality here!
        return false;
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
