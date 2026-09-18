package com.flowbite.web;

import com.flowbite.error.UnauthorizedException;
import com.flowbite.model.User;
import com.flowbite.service.CartService;
import com.flowbite.service.LoginService;
import com.flowbite.web.dto.Requests.LoginRequest;
import com.flowbite.web.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sign in, sign out, and "who am I".
 *
 * <p>Authentication itself is entirely {@link LoginService} - the unchanged
 * console class, including its polymorphic {@code login} that hands back an
 * {@code Admin} or a {@code Customer}. This controller only decides what to keep
 * in the session afterwards.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginService loginService;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * Signs in and starts a session.
     *
     * <p>The old session is discarded first. That is standard practice against
     * session fixation, and it also guarantees a signed-in user never inherits a
     * cart - and its reserved stock - from whoever used the browser before.
     */
    @PostMapping("/login")
    public UserDto login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {

        User user = loginService.login(request.role(), request.username().trim(), request.password());

        if (user == null) {
            throw new UnauthorizedException(
                    "Invalid " + request.role() + " credentials. Check the demo hints below.");
        }

        HttpSession previous = httpRequest.getSession(false);

        if (previous != null) {
            previous.invalidate();   // CartSessionListener releases any held stock
        }

        String role = request.role().toLowerCase();

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(SessionKeys.USER, user);
        session.setAttribute(SessionKeys.ROLE, role);

        return UserDto.from(user, role);
    }

    /**
     * Ends the session, returning any reserved stock to the counter.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {

        // Explicitly, rather than relying on the listener, so the stock is back
        // before this response returns and the next menu fetch is accurate.
        CartService cart = (CartService) session.getAttribute(SessionKeys.CART);

        if (cart != null) {
            cart.clearCart();
        }

        session.invalidate();

        return ResponseEntity.noContent().build();
    }

    /**
     * @return the signed-in user; 401 when there is no session, which is how the
     *     browser decides whether to show the login screen on load.
     */
    @GetMapping("/me")
    public UserDto me(HttpSession session) {
        User user = SessionKeys.requireUser(session);
        return UserDto.from(user, SessionKeys.role(session));
    }
}
