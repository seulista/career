package com.dalbitlive.config;

import com.dalbitlive.common.json.CustomObjectMapper;
import com.fasterxml.jackson.databind.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class JsonCustomConfig  extends WebMvcConfigurationSupport {
    public JsonCustomConfig(){
        super();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters){
        converters.add(new MappingJackson2HttpMessageConverter(customObjectMapper()));
    }

    @Bean
    public ObjectMapper customObjectMapper(){
        return new CustomObjectMapper();
    }
}
