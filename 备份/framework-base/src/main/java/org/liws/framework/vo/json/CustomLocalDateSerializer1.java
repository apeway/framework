package org.liws.framework.vo.json;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateSerializer1 extends JsonSerializer<LocalDate> {
    @Override
    public void serialize(LocalDate dateTime, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
        String formattedDateTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        generator.writeString( formattedDateTime);
    }
}
