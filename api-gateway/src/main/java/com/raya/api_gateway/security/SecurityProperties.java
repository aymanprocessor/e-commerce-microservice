package com.raya.api_gateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "gateway")
public class SecurityProperties {

    private List<PublicRoute> publicRoutes = new ArrayList<>();

    public List<PublicRoute> getPublicRoutes() {
        return publicRoutes;
    }

    public void setPublicRoutes(List<PublicRoute> publicRoutes) {
        this.publicRoutes = publicRoutes;
    }

    public static class PublicRoute {

        private HttpMethod method;
        private String path;

        public HttpMethod getMethod() {
            return method;
        }

        public void setMethod(HttpMethod method) {
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}