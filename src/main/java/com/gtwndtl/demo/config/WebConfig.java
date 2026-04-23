package com.gtwndtl.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gtwndtl.demo.interceptor.AuthInterceptor;

import jakarta.validation.constraints.NotNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // Need barer token to access
                .addPathPatterns("/**")
                // No need barer token to access
                .excludePathPatterns("/login", "/register");
    }
}
