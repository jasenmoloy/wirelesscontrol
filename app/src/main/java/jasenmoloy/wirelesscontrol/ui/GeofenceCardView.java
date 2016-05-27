package jasenmoloy.wirelesscontrol.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import jasenmoloy.wirelesscontrol.data.Constants;
import jasenmoloy.wirelesscontrol.data.GeofenceData;

/**
 * Created by jasenmoloy on 4/26/16.
 */
public class GeofenceCardView extends CardView implements GestureDetector.OnGestureListener {
    /// ----------------------
    /// Class Fields
    /// ----------------------

    /// ----------------------
    /// Object Fields
    /// ----------------------

    GestureDetector mGestureDectector;

    int mPosition;
    GeofenceData mData;

    /// ----------------------
    /// Getters / Setters
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public GeofenceCardView(Context context) {
        super(context);
        init();
    }

    public GeofenceCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GeofenceCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setData(int position, GeofenceData data) {
        mPosition = position;
        mData = data;
    }

    /**
     * Handle an user's touch response to open an edit geofence activity.
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mGestureDectector.onTouchEvent(ev);
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

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        //Stubbed
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Context context = getContext();
        Intent intent = new Intent(context, EditGeofenceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //Prevents reinstantiation if the activity already exists
        intent.putExtra(Constants.BROADCAST_EXTRA_KEY_GEOFENCE_ID, mPosition);
        intent.putExtra(Constants.BROADCAST_EXTRA_KEY_GEODATA, mData);
        context.startActivity(intent);
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        //Stubbed
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

    private void init() {
        mGestureDectector = new GestureDetector(getContext(), this);
        mGestureDectector.setIsLongpressEnabled(false);
    }
}
