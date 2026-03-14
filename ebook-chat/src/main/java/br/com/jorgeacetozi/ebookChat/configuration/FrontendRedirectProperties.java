package br.com.jorgeacetozi.ebookChat.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class FrontendRedirectProperties {

    private boolean redirectToFrontend = false;
    private String frontendUrl = "http://localhost:3000";

    public boolean isRedirectToFrontend() {
        return redirectToFrontend;
    }

    public void setRedirectToFrontend(boolean redirectToFrontend) {
        this.redirectToFrontend = redirectToFrontend;
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }
}
