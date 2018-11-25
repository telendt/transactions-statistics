package com.n26;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Custom JsonComponent. Separated from ApplicationConfig so that JsonTest(s) can load it.
 */
@JsonComponent
public class CustomJsonComponent {

    public static class BigDecimalSerializer extends JsonSerializer<BigDecimal> {
        @Override
        public void serialize(BigDecimal value, JsonGenerator jGen, SerializerProvider provider) throws IOException {
            jGen.writeString(value.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        }
    }
}
