package org.example.authentication.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.JsonSerializer;
import org.locationtech.jts.geom.Point;
import java.io.IOException;

public class PointSerializer extends JsonSerializer<Point> {
    @Override
    public void serialize(Point point, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("latitude", point.getY()); // Y is latitude
        jsonGenerator.writeNumberField("longitude", point.getX()); // X is longitude
        jsonGenerator.writeEndObject();
    }
}
