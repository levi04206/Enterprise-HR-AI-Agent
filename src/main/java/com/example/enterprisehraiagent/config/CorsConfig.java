package com.example.enterprisehraiagent.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.WebFilter;

import java.util.List;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    /**
     * 创建前端跨域过滤器，统一处理 CORS 响应头和预检请求。
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter frontendCorsWebFilter(CorsProperties properties) {
        return (exchange, chain) -> {
            String origin = exchange.getRequest().getHeaders().getOrigin();
            if (!StringUtils.hasText(origin) || !isAllowedOrigin(origin, properties.getAllowedOrigins())) {
                return chain.filter(exchange);
            }

            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders responseHeaders = response.getHeaders();
            responseHeaders.setAccessControlAllowOrigin(origin);
            responseHeaders.setAccessControlAllowCredentials(properties.isAllowCredentials());
            responseHeaders.setAccessControlAllowMethods(properties.getAllowedMethods().stream().map(HttpMethod::valueOf).toList());
            responseHeaders.setAccessControlMaxAge(properties.getMaxAge());
            responseHeaders.add(HttpHeaders.VARY, HttpHeaders.ORIGIN);
            responseHeaders.add(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
            responseHeaders.add(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);

            List<String> requestHeaders = exchange.getRequest().getHeaders().getAccessControlRequestHeaders();
            if (!CollectionUtils.isEmpty(requestHeaders)) {
                responseHeaders.setAccessControlAllowHeaders(requestHeaders);
            } else {
                responseHeaders.setAccessControlAllowHeaders(properties.getAllowedHeaders());
            }
            responseHeaders.setAccessControlExposeHeaders(properties.getExposedHeaders());

            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS
                    && StringUtils.hasText(exchange.getRequest().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD))) {
                response.setStatusCode(HttpStatus.OK);
                return response.setComplete();
            }

            return chain.filter(exchange);
        };
    }

    /**
     * 判断请求来源是否在允许的前端来源列表中。
     */
    private boolean isAllowedOrigin(String origin, List<String> allowedOrigins) {
        return allowedOrigins.contains("*") || allowedOrigins.contains(origin);
    }
}
