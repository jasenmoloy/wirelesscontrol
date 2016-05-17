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
    /// ----------------------
    /// Class Fields
    /// ----------------------

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

    private static final String TAG = "GeofenceData";

    /// ----------------------
    /// Object Fields
    /// ----------------------

    @JsonField
    public String name;

    @JsonField(typeConverter = LatLngTypeConverter.class)
    public LatLng position;

    @JsonField
    public double radius;

    @JsonField
    public String screenshotFileName;

    public Bitmap mapScreenshot;

    /// ----------------------
    /// Getters / Setters
    /// ----------------------

    /// ----------------------
    /// Public Methods
    /// ----------------------

    public GeofenceData(String name, LatLng pos, double radius) {
        this.name = name;
        this.position = pos;
        this.radius = radius;

        //Generate filename for bitmap
        int hashCode = (name + position.toString()).hashCode();

        //Set the filename for JSON output
        screenshotFileName = String.valueOf(hashCode) + ".png";
    }

    public void addBitmap(Bitmap mapScreenshot) {
        this.mapScreenshot = mapScreenshot;
    }

    public GeofenceData(Parcel in) {
        Debug.logDebug(TAG, "GeofenceData()");


        name = in.readString();
        position = new LatLng(in.readDouble(), in.readDouble());
        radius = in.readDouble();
        screenshotFileName = in.readString();

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
        out.writeString(screenshotFileName);
    }

    /// ----------------------
    /// Callback Methods
    /// ----------------------

    /// ----------------------
    /// Protected Methods
    /// ----------------------

    /// ----------------------
    /// Private Methods
    /// ----------------------

    //Created primarily for LoganSquare Serialization
    //JAM TODO: Create a GeofenceDataTypeConverter class to write our own implementation
    protected GeofenceData() {
        this.name = "";
        this.position = new LatLng(0d, 0d);
        this.radius = 0;
        this.screenshotFileName = "";
    }
}
