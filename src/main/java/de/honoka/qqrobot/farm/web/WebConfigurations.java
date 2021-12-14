package de.honoka.qqrobot.farm.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebConfigurations {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public String getServerPort() {
        return serverPort;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationBaseUrl() {
        return "http://localhost:" + serverPort + contextPath;
    }

    public String getTextImageUrl() {
        return getApplicationBaseUrl() + "/text";
    }
}
