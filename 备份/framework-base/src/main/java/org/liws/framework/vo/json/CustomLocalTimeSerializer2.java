package org.liws.framework.vo.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CustomLocalTimeSerializer2 extends JsonSerializer<LocalTime>{
    @Override
    public void serialize(LocalTime dateTime, JsonGenerator generator, SerializerProvider serializers) throws IOException, JsonProcessingException {
        String formattedDateTime = dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        generator.writeString( formattedDateTime);
    }
}
