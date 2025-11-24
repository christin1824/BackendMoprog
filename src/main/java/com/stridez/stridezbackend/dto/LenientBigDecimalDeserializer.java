package com.stridez.stridezbackend.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Deserializer yang toleran: menerima angka atau string, mengganti koma dengan titik,
 * dan mengembalikan null untuk string kosong.
 */
public class LenientBigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return p.getDecimalValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            String text = p.getText().trim();
            if (text.length() == 0) return null;
            // replace comma decimal separator
            text = text.replace(',', '.');
            try {
                return new BigDecimal(text);
            } catch (NumberFormatException ex) {
                // let Jackson handle problem by returning null
                return null;
            }
        }
        // other tokens -> null
        return null;
    }
}
