package com.example.enterprisehraiagent.config;

import com.example.enterprisehraiagent.dto.ApiErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

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
    private final ObjectMapper objectMapper;

    /**
     * 注入安全配置和 JSON 序列化器。
     */
    public LightweightSecurityWebFilter(SecurityProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 对业务 API 执行轻量身份识别和管理员权限校验。
     */
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

    /**
     * 判断当前请求是否属于免鉴权路径。
     */
    private boolean isPermitAll(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        return properties.getPermitAllPrefixes().stream().anyMatch(path::startsWith);
    }

    /**
     * 判断当前请求是否需要管理员角色。
     */
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
            return method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.DELETE;
        }
        if (path.equals("/api/v1/leave-records")) {
            return method == HttpMethod.GET;
        }
        if (path.startsWith("/api/v1/leave-records/") && path.endsWith("/status")) {
            return method == HttpMethod.PATCH;
        }
        return false;
    }

    /**
     * 返回结构化的未认证或无权限错误响应。
     */
    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiErrorResponse body = ApiErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                status == HttpStatus.UNAUTHORIZED ? "UNAUTHORIZED" : "FORBIDDEN",
                status == HttpStatus.UNAUTHORIZED ? "缺少员工身份请求头" : "当前角色无权访问该接口",
                exchange.getRequest().getPath().value(),
                Map.of()
        );
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException error) {
            byte[] fallback = "{\"message\":\"请求未通过权限校验\"}".getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(fallback)));
        }
    }
}
