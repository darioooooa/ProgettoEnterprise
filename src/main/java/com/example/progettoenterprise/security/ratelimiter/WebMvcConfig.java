package com.example.progettoenterprise.security.ratelimiter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registra l'interceptor applicandolo a tutte le rotte delle API
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
    }
}