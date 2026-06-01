package com.example.enterprisehraiagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private List<String> allowedOrigins = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:3000",
            "http://127.0.0.1:3000"
    );

    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

    private List<String> allowedHeaders = List.of("*");

    private List<String> exposedHeaders = List.of("Content-Type");

    private boolean allowCredentials = true;

    private long maxAge = 3600;

    /**
     * 获取允许跨域访问的前端来源列表。
     */
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * 设置允许跨域访问的前端来源列表。
     */
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * 获取允许的 HTTP 方法列表。
     */
    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    /**
     * 设置允许的 HTTP 方法列表。
     */
    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * 获取允许的请求头列表。
     */
    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    /**
     * 设置允许的请求头列表。
     */
    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    /**
     * 获取允许前端读取的响应头列表。
     */
    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    /**
     * 设置允许前端读取的响应头列表。
     */
    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    /**
     * 判断是否允许携带 Cookie 或认证信息。
     */
    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    /**
     * 设置是否允许携带 Cookie 或认证信息。
     */
    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    /**
     * 获取预检请求缓存时间。
     */
    public long getMaxAge() {
        return maxAge;
    }

    /**
     * 设置预检请求缓存时间。
     */
    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }
}
