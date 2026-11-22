package com.mariuszilinskas.streamix.infra.gateway.handler;

import com.mariuszilinskas.streamix.web.response.error.ErrorResponse;
import com.mariuszilinskas.streamix.infra.gateway.exception.JwtTokenValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates gateway-level exceptions into structured HTTP error responses.
 */
@RestControllerAdvice
public class GatewayExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    @ExceptionHandler(JwtTokenValidationException.class)
    public ResponseEntity<ErrorResponse> handleJwtTokenValidationException(JwtTokenValidationException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // -----------------------------------------------------------

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
        logger.info("Status: {}, Message: '{}'", status.value(), message);
        ErrorResponse errorResponse = new ErrorResponse(message, status.value(), status.getReasonPhrase());
        return ResponseEntity.status(status).body(errorResponse);
    }

}
