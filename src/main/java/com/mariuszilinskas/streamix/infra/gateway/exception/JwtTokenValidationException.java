package com.mariuszilinskas.streamix.infra.gateway.exception;

public class JwtTokenValidationException extends RuntimeException {

    public JwtTokenValidationException() {
        super("Invalid JWT Token");
    }

}
