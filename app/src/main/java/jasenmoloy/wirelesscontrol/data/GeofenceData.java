package jasenmoloy.wirelesscontrol.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.google.android.gms.maps.model.LatLng;

import jasenmoloy.wirelesscontrol.debug.Debug;

/**
 * Created by jasenmoloy on 2/25/16.
 */
@JsonObject
public class GeofenceData implements Parcelable {
    private static final String TAG = "GeofenceData";

    public static final Parcelable.Creator<GeofenceData> CREATOR =
            new Parcelable.Creator<GeofenceData>() {
                @Override
                public GeofenceData createFromParcel(Parcel in) {
                    return new GeofenceData(in);
                }

                @Override
                public GeofenceData[] newArray(int size) {
                    return new GeofenceData[size];
                }
            };

    @JsonField
    public String name;

    @JsonField(typeConverter = LatLngTypeConverter.class)
    public LatLng position;

    @JsonField
    public double radius;

    public Bitmap mapScreenshot;

    public GeofenceData(String name, LatLng pos, double radius) {
        this.name = name;
        this.position = pos;
        this.radius = radius;
    }

    public void addBitmap(Bitmap mapScreenshot) {
        this.mapScreenshot = mapScreenshot;
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

    @Override
    public int describeContents() {
        Debug.logDebug(TAG, "describeContents()");
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        Debug.logDebug(TAG, "writeToParcel()");

        out.writeString(name);
        out.writeDouble(position.latitude);
        out.writeDouble(position.longitude);
        out.writeDouble(radius);
    }

    //Created primarily for LoganSquare Serialization
    //JAM TODO: Create a GeofenceDataTypeConverter class to write our own implementation
    protected GeofenceData() {
        this.name = "";
        this.position = new LatLng(0d, 0d);
        this.radius = 0;
    }
}
