package com.dalbitlive.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class NullToZeroValueSerializer extends JsonSerializer<Number> {

    @Override
    public void serialize(Number value, JsonGenerator generator, SerializerProvider provider) throws IOException{
        if(value == null){
            generator.writeNumber(0);
        }else{
            if(value instanceof Integer) {
                generator.writeNumber(value.intValue());
            }else if(value instanceof Long){
                generator.writeNumber(value.longValue());
            }else if(value instanceof Short){
                generator.writeNumber(value.shortValue());
            }else if(value instanceof Float){
                generator.writeNumber(value.floatValue());
            }else if(value instanceof Double){
                generator.writeNumber(value.doubleValue());
            }else{
                generator.writeNumber(value.toString());
            }
        }
    }
}
