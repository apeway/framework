package org.liws.framework.vo.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateSerializer2 extends JsonSerializer<LocalDate>{
    @Override
    public void serialize(LocalDate dateTime, JsonGenerator generator, SerializerProvider serializers) throws IOException, JsonProcessingException {
        String formattedDateTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        generator.writeString( formattedDateTime);
    }
}
