package com.flowbite.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Wires {@link RoleInterceptor} across the API.
 *
 * <p>Everything under {@code /api/**} needs a session except the two things you
 * necessarily reach before you have one: signing in, and asking what the server
 * supports. {@code /api/auth/me} is <i>not</i> excluded - the browser calls it on
 * load to find out whether a session already exists, and a 401 is the answer.
 *
 * <p>No CORS configuration: in development Vite proxies {@code /api} to this
 * server, and in production the built React app is served from it. Either way
 * the browser sees one origin, so the session cookie travels without any
 * cross-origin rules to relax.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RoleInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/config");
    }
}
