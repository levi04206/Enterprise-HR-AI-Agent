package com.example.enterprisehraiagent.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 轻量级企业网关鉴权过滤器。
 *
 * <p>真实企业系统通常会在网关或 SSO 层完成登录认证，再把员工身份通过可信请求头传给后端。
 * 这个过滤器模拟这种模式：启用 `app.security.enabled=true` 后，所有业务 API 都需要携带
 * 员工姓名请求头，管理类写接口还需要 `ADMIN` 角色。</p>
 *
 * <p>它不是完整 SSO 实现，但足够让 Demo 具备清晰的权限边界；后续切换 Spring Security
 * OAuth2/JWT 时，可以保留 Controller 和 Service，替换这一层即可。</p>
 */
@Component
@EnableConfigurationProperties(SecurityProperties.class)
public class LightweightSecurityWebFilter implements WebFilter {

    public static final String CURRENT_EMPLOYEE_ATTRIBUTE = "currentEmployeeName";
    public static final String CURRENT_ROLE_ATTRIBUTE = "currentRole";

    private final SecurityProperties properties;

    public LightweightSecurityWebFilter(SecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!properties.isEnabled() || isPermitAll(exchange)) {
            return chain.filter(exchange);
        }

        String employeeName = exchange.getRequest().getHeaders().getFirst(properties.getEmployeeHeader());
        if (!StringUtils.hasText(employeeName)) {
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }

        String role = exchange.getRequest().getHeaders().getFirst(properties.getRoleHeader());
        exchange.getAttributes().put(CURRENT_EMPLOYEE_ATTRIBUTE, employeeName.trim());
        exchange.getAttributes().put(CURRENT_ROLE_ATTRIBUTE, StringUtils.hasText(role) ? role.trim() : "EMPLOYEE");

        if (requiresAdmin(exchange) && !"ADMIN".equalsIgnoreCase(role)) {
            return reject(exchange, HttpStatus.FORBIDDEN);
        }

        return chain.filter(exchange);
    }

    private boolean isPermitAll(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        return properties.getPermitAllPrefixes().stream().anyMatch(path::startsWith);
    }

    private boolean requiresAdmin(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        HttpMethod method = exchange.getRequest().getMethod();

        if (path.startsWith("/api/v1/diagnostics")) {
            return true;
        }
        if (path.startsWith("/api/v1/observability")) {
            return true;
        }
        if (path.startsWith("/api/v1/knowledge")) {
            return method == HttpMethod.POST || method == HttpMethod.DELETE;
        }
        if (path.startsWith("/api/v1/employees")) {
            return method == HttpMethod.POST;
        }
        if (path.startsWith("/api/v1/leave-records")) {
            return method == HttpMethod.POST;
        }
        return false;
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }
}
