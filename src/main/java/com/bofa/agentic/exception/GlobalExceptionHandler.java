package com.bofa.agentic.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for reactive WebFlux endpoints
 * 
 * Catches exceptions and converts them to proper HTTP error responses
 * instead of generic 500 errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle AgentException (business logic errors like guardrail violations)
     * 
     * These include:
     * - INPUT_SAFETY_VIOLATION: User input violates content policy
     * - SAFETY_GUARDRAIL_BLOCKED: AI response violates safety rules
     * - GROUNDING_GUARDRAIL_BLOCKED: AI response not grounded in evidence
     */
    @ExceptionHandler(AgentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleAgentException(
            AgentException ex, 
            ServerWebExchange exchange) {
        
        log.warn("AgentException caught: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        HttpStatus status = determineHttpStatus(ex.getErrorCode());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("errorCode", ex.getErrorCode());
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("retryable", ex.isRetryable());
        
        if (ex.getMetadata() != null) {
            errorResponse.put("metadata", ex.getMetadata());
        }
        
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }
    
    /**
     * Handle generic exceptions as fallback
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(
            Exception ex, 
            ServerWebExchange exchange) {
        
        log.error("Unexpected exception caught", ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("status", 500);
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An unexpected error occurred. Please try again later.");
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }
    
    /**
     * Map error codes to HTTP status codes
     */
    private HttpStatus determineHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "INPUT_SAFETY_VIOLATION" -> HttpStatus.BAD_REQUEST;
            case "SAFETY_GUARDRAIL_BLOCKED" -> HttpStatus.FORBIDDEN;
            case "GROUNDING_GUARDRAIL_BLOCKED" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "MODERATION_ERROR" -> HttpStatus.SERVICE_UNAVAILABLE;
            case "TOOL_EXECUTION_FAILED" -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
