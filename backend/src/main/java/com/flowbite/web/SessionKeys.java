package com.flowbite.web;

import com.flowbite.error.UnauthorizedException;
import com.flowbite.model.User;
import com.flowbite.service.CartService;
import com.flowbite.service.FoodService;
import jakarta.servlet.http.HttpSession;

/**
 * The three things FlowBite keeps in an {@link HttpSession}, and the only code
 * that reads or writes them.
 *
 * <p><b>Why the cart is a plain session attribute.</b> The obvious Spring answer
 * is a {@code @SessionScope} bean, but those are proxies that resolve the real
 * object through the current request. When a session expires there is no
 * request - nobody is knocking, the session simply aged out - so
 * {@code HttpSessionListener} could not reach the cart to release its reserved
 * stock. Storing the cart directly sidesteps that entirely: the listener reads
 * the attribute off the expiring session and calls {@code clearCart()} on it.
 */
public final class SessionKeys {

    /** The signed-in {@link User}. */
    public static final String USER = "flowbite.user";

    /** Their role, "admin" or "customer". */
    public static final String ROLE = "flowbite.role";

    /** That session's {@link CartService}. */
    public static final String CART = "flowbite.cart";

    private SessionKeys() {
    }

    /**
     * @return the signed-in user.
     * @throws UnauthorizedException if nobody is signed in.
     */
    public static User requireUser(HttpSession session) {

        User user = (User) session.getAttribute(USER);

        if (user == null) {
            throw new UnauthorizedException("Please sign in first.");
        }

        return user;
    }

    /**
     * @return the role of the signed-in user, or null if nobody is.
     */
    public static String role(HttpSession session) {
        return (String) session.getAttribute(ROLE);
    }

    /**
     * Returns this session's cart, creating it on first use.
     *
     * <p>Synchronized on the session because a customer with several tabs open
     * can fire two requests at once, and both would otherwise create a cart -
     * with the loser's reserved stock stranded in an object nobody can reach.
     *
     * @param session the browser session.
     * @param foodService the shared menu, which the cart reserves stock from.
     * @return the cart belonging to this session.
     */
    public static CartService cart(HttpSession session, FoodService foodService) {

        synchronized (session) {

            CartService cart = (CartService) session.getAttribute(CART);

            if (cart == null) {
                cart = new CartService(foodService);
                session.setAttribute(CART, cart);
            }

            return cart;
        }
    }
}
