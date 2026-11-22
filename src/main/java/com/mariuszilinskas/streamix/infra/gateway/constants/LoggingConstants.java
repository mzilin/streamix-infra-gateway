package com.mariuszilinskas.streamix.infra.gateway.constants;

public final class LoggingConstants {

    private LoggingConstants() {}

    public static final String MDC_CONTEXT_KEY = "slf4j.mdc";

    public static final String CORRELATION_ID = "correlation_id";
    public static final String USER_ID = "user_id";
    public static final String SERVICE = "service";
    public static final String ENVIRONMENT = "environment";

}
