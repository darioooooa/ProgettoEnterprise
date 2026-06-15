package com.example.progettoenterprise.exception.ratelimiter;

public class ManyRequestException extends RuntimeException {
    public ManyRequestException(String message) {
        super(message);
    }
}
