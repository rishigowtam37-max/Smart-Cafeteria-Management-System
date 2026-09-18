package com.flowbite.web;

import com.flowbite.service.CartService;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Returns reserved stock to the counter when a session ends.
 *
 * <p>Stock is reserved the moment an item enters a cart, which is what stops two
 * customers taking the last Pizza. The flip side is that a customer who fills a
 * cart and closes the tab would hold that stock forever. Sessions expire after
 * ten minutes ({@code server.servlet.session.timeout}), and this listener empties
 * whatever cart the expiring session was holding.
 *
 * <p>It fires for both endings: an explicit {@code invalidate()} on sign-out, and
 * the quiet timeout of an abandoned session.
 */
@Component
public class CartSessionListener implements HttpSessionListener {

    private static final Logger log = LoggerFactory.getLogger(CartSessionListener.class);

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {

        CartService cart = (CartService) event.getSession().getAttribute(SessionKeys.CART);

        if (cart == null || cart.isEmpty()) {
            return;
        }

        int released = cart.getItemCount();
        cart.clearCart();

        log.info("Session ended with {} unit(s) still in the cart - stock released.", released);
    }
}
