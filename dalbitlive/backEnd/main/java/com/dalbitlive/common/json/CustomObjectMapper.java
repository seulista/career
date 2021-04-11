package com.dalbitlive.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class CustomObjectMapper extends ObjectMapper {
    private static final long serialVersionUID = -2148669317097583174L;

    public CustomObjectMapper(){
        super();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Number.class, new NullToZeroValueSerializer());
        registerModule(simpleModule);
        setPropertyNamingStrategy(new PropertyNamingStrategy(){
            @Override
            public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName){
                if(method.hasReturnType()
                        && (method.getRawReturnType() == Boolean.class || method.getRawReturnType() == boolean.class)
                        && (method.getName().startsWith("is") || method.getName().startsWith("was") || method.getName().startsWith("has"))){
                    return method.getName();
                }
                return super.nameForGetterMethod(config, method, defaultName);
            }
        });
        getSerializerProvider().setNullValueSerializer(new NullToEmptyValueSerializer());
    }
}
