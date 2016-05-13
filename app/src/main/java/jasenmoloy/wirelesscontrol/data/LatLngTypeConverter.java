package jasenmoloy.wirelesscontrol.data;

import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

/**
 * Created by jasenmoloy on 5/11/16.
 */
public class LatLngTypeConverter implements TypeConverter<LatLng> {
    @Override
    public LatLng parse(JsonParser jsonParser) throws IOException {
        double lat;
        double lng;

        //parser should start at START_OBJECT
        jsonParser.nextToken(); //FIELD_NAME
        jsonParser.nextToken(); //VALUE
        lat = jsonParser.getDoubleValue();
        jsonParser.nextToken(); //FIELD_NAME
        jsonParser.nextToken(); //VALUE
        lng = jsonParser.getDoubleValue();
        jsonParser.nextToken(); //END_OBJECT

        return new LatLng(lat, lng);
    }

    @Override
    public void serialize(LatLng object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) throws IOException {
        if(writeFieldNameForObject)
            jsonGenerator.writeFieldName(fieldName);

        jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("lat", object.latitude);
            jsonGenerator.writeObjectField("lng", object.longitude);
        jsonGenerator.writeEndObject();
    }
}
