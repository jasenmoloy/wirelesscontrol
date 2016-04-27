package jasenmoloy.wirelesscontrol.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import jasenmoloy.wirelesscontrol.debug.Debug;

/**
 * Created by jasenmoloy on 2/25/16.
 */
public class GeofenceData implements Parcelable {
    private static final String TAG = "GeofenceData";

    public String name;
    public LatLng position;
    public double radius;
    public Bitmap mMapScreenshot;

    public GeofenceData(String name, LatLng pos, double radius) {
        this.name = name;
        this.position = pos;
        this.radius = radius;
    }

    public void addBitmap(Bitmap mapScreenshot) {
        mMapScreenshot = mapScreenshot;
    }

    public GeofenceData(Parcel in) {
        Debug.logDebug(TAG, "GeofenceData()");


        name = in.readString();
        position = new LatLng(in.readDouble(), in.readDouble());
        radius = in.readDouble();

        Debug.logDebug(TAG, "GeofenceData() - name: " + name);
        Debug.logDebug(TAG, "GeofenceData() - position: " + position);
        Debug.logDebug(TAG, "GeofenceData() - radius: " + radius);
    }

    public int describeContents() {
        Debug.logDebug(TAG, "describeContents()");
        return 0;
    }

    public static final Parcelable.Creator<GeofenceData> CREATOR =
            new Parcelable.Creator<GeofenceData>() {
                public GeofenceData createFromParcel(Parcel in) {
                    return new GeofenceData(in);
                }

                public GeofenceData[] newArray(int size) {
                    return new GeofenceData[size];
                }
            };

    public void writeToParcel(Parcel out, int flags) {
        Debug.logDebug(TAG, "writeToParcel()");

        out.writeString(name);
        out.writeDouble(position.latitude);
        out.writeDouble(position.longitude);
        out.writeDouble(radius);
    }
}
