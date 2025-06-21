package org.example.postsservice.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import org.locationtech.jts.geom.Point;
import java.io.IOException;

public class PointSerializer extends JsonSerializer<Point> {
    @Override
    public void serialize(Point point, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("latitude", point.getY());
        gen.writeNumberField("longitude", point.getX());
        gen.writeEndObject();
    }

    @Override
    public void serializeWithType(Point point, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        // Use the new WritableTypeId mechanism
        WritableTypeId typeId = typeSer.writeTypePrefix(gen, typeSer.typeId(point, JsonToken.START_OBJECT));

        gen.writeNumberField("latitude", point.getY());
        gen.writeNumberField("longitude", point.getX());

        typeSer.writeTypeSuffix(gen, typeId);
    }
}
