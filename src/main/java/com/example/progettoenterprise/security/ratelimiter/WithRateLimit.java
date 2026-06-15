package com.example.progettoenterprise.security.ratelimiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // Viene applicata solo ai metodi del controller
@Retention(RetentionPolicy.RUNTIME) // Visibile in fase di runtime
public @interface WithRateLimit {
    RateLimitPolicy value() default RateLimitPolicy.DEFAULT; // Di default applica la politica base
}