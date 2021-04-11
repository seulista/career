package com.dalbitlive.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletConfig {
    @Value("${server.http.port}")
    private int serverHttpPort;

    @Bean
    public ServletWebServerFactory servletWebServerFactory(){
        TomcatServletWebServerFactory tomcatServletWebServerFactory = new TomcatServletWebServerFactory();
        tomcatServletWebServerFactory.addAdditionalTomcatConnectors(createStandardConnector());
        return tomcatServletWebServerFactory;
    }

    private Connector createStandardConnector(){
        Connector connector = new Connector();
        connector.setPort(serverHttpPort);
        return connector;
    }
}
