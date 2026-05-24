package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException error,
                                                                  ServerWebExchange exchange) {
        return badRequest(error.getMessage(), exchange, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidation(MethodArgumentNotValidException error,
                                                                   ServerWebExchange exchange) {
        return badRequest("请求参数校验失败", exchange, collectFieldErrors(error.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiErrorResponse> handleWebFluxValidation(WebExchangeBindException error,
                                                                    ServerWebExchange exchange) {
        return badRequest("请求参数校验失败", exchange, collectFieldErrors(error.getFieldErrors()));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(ServerWebInputException error,
                                                             ServerWebExchange exchange) {
        return badRequest("请求参数格式不正确：" + error.getReason(), exchange, Map.of());
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Throwable error,
                                                            ServerWebExchange exchange) {
        log.error("Unhandled API exception. path={}", path(exchange), error);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(
                        status.value(),
                        status.getReasonPhrase(),
                        "服务内部异常，请稍后重试",
                        path(exchange),
                        Map.of()
                ));
    }

    private ResponseEntity<ApiErrorResponse> badRequest(String message,
                                                        ServerWebExchange exchange,
                                                        Map<String, String> details) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        path(exchange),
                        details
                ));
    }

    private Map<String, String> collectFieldErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() == null ? "参数不合法" : fieldError.getDefaultMessage(),
                        (left, right) -> left
                ));
    }

    private String path(ServerWebExchange exchange) {
        return exchange.getRequest().getPath().value();
    }
}
