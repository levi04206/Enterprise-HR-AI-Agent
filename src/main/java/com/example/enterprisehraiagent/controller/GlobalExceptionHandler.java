package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务参数错误。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException error,
                                                                  ServerWebExchange exchange) {
        return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", error.getMessage(), exchange, Map.of());
    }

    /**
     * 处理资源不存在错误。
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NoSuchElementException error,
                                                           ServerWebExchange exchange) {
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", error.getMessage(), exchange, Map.of());
    }

    /**
     * 处理 MVC 参数校验失败。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidation(MethodArgumentNotValidException error,
                                                                   ServerWebExchange exchange) {
        return validationError(exchange, collectFieldErrors(error.getBindingResult().getFieldErrors()));
    }

    /**
     * 处理 WebFlux 参数绑定校验失败。
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiErrorResponse> handleWebFluxValidation(WebExchangeBindException error,
                                                                    ServerWebExchange exchange) {
        return validationError(exchange, collectFieldErrors(error.getFieldErrors()));
    }

    /**
     * 处理通用参数绑定校验失败。
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindValidation(BindException error,
                                                                 ServerWebExchange exchange) {
        return validationError(exchange, collectFieldErrors(error.getFieldErrors()));
    }

    /**
     * 处理请求体格式错误或路径参数转换失败。
     */
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(ServerWebInputException error,
                                                             ServerWebExchange exchange) {
        return error(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST",
                "请求参数格式不正确：" + nullToDefault(error.getReason(), "请检查请求体或路径参数"),
                exchange,
                Map.of()
        );
    }

    /**
     * 处理唯一键重复等数据库冲突。
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateKey(DuplicateKeyException error,
                                                               ServerWebExchange exchange) {
        return error(
                HttpStatus.CONFLICT,
                "DUPLICATE_RESOURCE",
                "数据已存在，请检查唯一字段是否重复",
                exchange,
                Map.of("constraint", readableDatabaseMessage(error))
        );
    }

    /**
     * 处理数据库约束违反错误。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException error,
                                                                ServerWebExchange exchange) {
        return error(
                HttpStatus.CONFLICT,
                "DATA_INTEGRITY_VIOLATION",
                "数据约束校验失败，请检查关联数据或唯一字段",
                exchange,
                Map.of("constraint", readableDatabaseMessage(error))
        );
    }

    /**
     * 处理外部调用超时。
     */
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ApiErrorResponse> handleTimeout(TimeoutException error,
                                                          ServerWebExchange exchange) {
        return error(
                HttpStatus.GATEWAY_TIMEOUT,
                "REQUEST_TIMEOUT",
                "访问超时，请稍后重试",
                exchange,
                Map.of()
        );
    }

    /**
     * 处理 Controller 主动抛出的 HTTP 状态异常。
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException error,
                                                                 ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.resolve(error.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return error(
                status,
                status.name(),
                nullToDefault(error.getReason(), status.getReasonPhrase()),
                exchange,
                Map.of()
        );
    }

    /**
     * 兜底处理未预料的系统异常。
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Throwable error,
                                                            ServerWebExchange exchange) {
        log.error("Unhandled API exception. path={}", path(exchange), error);
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "服务内部异常，请稍后重试",
                exchange,
                Map.of()
        );
    }

    /**
     * 统一构造参数校验错误响应。
     */
    private ResponseEntity<ApiErrorResponse> validationError(ServerWebExchange exchange,
                                                             Map<String, String> details) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "请求参数校验失败", exchange, details);
    }

    /**
     * 统一构造 API 错误响应体。
     */
    private ResponseEntity<ApiErrorResponse> error(HttpStatus status,
                                                   String code,
                                                   String message,
                                                   ServerWebExchange exchange,
                                                   Map<String, String> details) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(
                        status.value(),
                        status.getReasonPhrase(),
                        code,
                        message,
                        path(exchange),
                        details
                ));
    }

    /**
     * 收集字段级校验错误并转换为前端可读详情。
     */
    private Map<String, String> collectFieldErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() == null ? "参数不合法" : fieldError.getDefaultMessage(),
                        (left, right) -> left
                ));
    }

    /**
     * 读取当前请求路径。
     */
    private String path(ServerWebExchange exchange) {
        return exchange.getRequest().getPath().value();
    }

    /**
     * 提取底层数据库异常消息。
     */
    private String readableDatabaseMessage(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return nullToDefault(message, "数据库约束冲突");
    }

    /**
     * 为空字符串提供默认提示。
     */
    private String nullToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
