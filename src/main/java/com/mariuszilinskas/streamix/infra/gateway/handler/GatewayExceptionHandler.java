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
 * This class is a global exception handler that handles exceptions thrown across the whole application.
 *
 * @author Marius Zilinskas
 */
@RestControllerAdvice
public class GatewayExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    @ExceptionHandler(JwtTokenValidationException.class)
    public ResponseEntity<ErrorResponse> handleJwtTokenValidationException(JwtTokenValidationException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // -----------------------------------------------------------

    /**
     * This method builds the error response for a given exception.
     *
     * @param message the exception message
     * @param status the HTTP status
     * @return a ResponseEntity that includes the error response and the given HTTP status
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
        logger.info("Status: {}, Message: '{}'", status.value(), message);
        ErrorResponse errorResponse = new ErrorResponse(message, status.value(), status.getReasonPhrase());
        return new ResponseEntity<>(errorResponse, status);
    }

}
