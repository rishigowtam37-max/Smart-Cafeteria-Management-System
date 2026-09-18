package com.flowbite.web;

import com.flowbite.error.ForbiddenException;
import com.flowbite.error.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Checks, on every API request, that somebody is signed in and that their role
 * covers what they are asking for.
 *
 * <p>The console app got this for free: you logged in, and it only ever showed
 * you your own menu. A browser is not so polite - it can send any request to any
 * path - so the rule has to be enforced when the request arrives rather than
 * implied by the UI. That is the difference between the React app "hiding" the
 * admin screen and the admin screen actually being closed to customers.
 *
 * <p>This is deliberately not Spring Security: two fixed accounts and two roles
 * do not justify a filter chain.
 */
public class RoleInterceptor implements HandlerInterceptor {

    private static final String ADMIN = "admin";
    private static final String CUSTOMER = "customer";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // CORS preflight carries no cookie by design; let it through.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        String role = SessionKeys.role(request.getSession());

        if (role == null) {
            throw new UnauthorizedException("Please sign in first.");
        }

        if (path.startsWith("/api/admin/") && !ADMIN.equals(role)) {
            throw new ForbiddenException("This area is for cafeteria staff only.");
        }

        if ((path.startsWith("/api/cart") || path.startsWith("/api/orders") || path.startsWith("/api/craving"))
                && !CUSTOMER.equals(role)) {
            throw new ForbiddenException("Only a customer can order food.");
        }

        return true;
    }
}
