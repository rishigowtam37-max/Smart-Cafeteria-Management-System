package com.flowbite.web;

import com.flowbite.model.FoodItem;
import com.flowbite.service.CartService;
import com.flowbite.service.FoodService;
import jakarta.servlet.http.HttpSessionEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * The safety net for reserve-on-add: a customer who fills a cart and walks away
 * must not hold stock forever.
 */
class CartSessionListenerTest {

    private final CartSessionListener listener = new CartSessionListener();

    @Test
    @DisplayName("an expiring session gives its reserved stock back")
    void expiredSessionReleasesStock() {
        List<FoodItem> seed = new ArrayList<>();
        seed.add(new FoodItem(1, "Pizza", 180, 10));
        FoodService foodService = new FoodService(seed);
        FoodItem pizza = foodService.requireFood(1);

        MockHttpSession session = new MockHttpSession();
        CartService cart = new CartService(foodService);
        cart.addToCart(pizza, 4);
        session.setAttribute(SessionKeys.CART, cart);

        assertEquals(6, pizza.getQuantity(), "four units should be reserved while the cart holds them");

        listener.sessionDestroyed(new HttpSessionEvent(session));

        assertEquals(10, pizza.getQuantity(), "expiry must put every reserved unit back");
    }

    @Test
    @DisplayName("a session that never had a cart is handled without complaint")
    void sessionWithoutCartIsIgnored() {
        assertDoesNotThrow(() ->
                listener.sessionDestroyed(new HttpSessionEvent(new MockHttpSession())));
    }

    @Test
    @DisplayName("an empty cart releases nothing and changes nothing")
    void emptyCartIsANoop() {
        List<FoodItem> seed = new ArrayList<>();
        seed.add(new FoodItem(1, "Pizza", 180, 10));
        FoodService foodService = new FoodService(seed);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.CART, new CartService(foodService));

        listener.sessionDestroyed(new HttpSessionEvent(session));

        assertEquals(10, foodService.requireFood(1).getQuantity());
    }
}
