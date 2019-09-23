package org.liws.framework.vo.json;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CustomLocalTimeSerializer1 extends JsonSerializer<LocalTime> {
    @Override
    public void serialize(LocalTime dateTime, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
        String formattedDateTime = dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        generator.writeString( formattedDateTime);
    }
}
