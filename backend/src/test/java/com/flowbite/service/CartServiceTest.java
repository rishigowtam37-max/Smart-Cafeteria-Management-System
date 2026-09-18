package com.flowbite.service;

import com.flowbite.error.InsufficientStockException;
import com.flowbite.error.InvalidRequestException;
import com.flowbite.error.NotFoundException;
import com.flowbite.model.FoodItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The cart, including the reserve-on-add behaviour inherited from the console
 * app and the lazy reconciliation the web app needed.
 */
class CartServiceTest {

    private FoodService foodService;
    private CartService cart;
    private FoodItem burger;
    private FoodItem pizza;

    @BeforeEach
    void setUp() {
        List<FoodItem> seed = new ArrayList<>();
        seed.add(new FoodItem(1, "Veg Burger", 80, 20));
        seed.add(new FoodItem(2, "Pizza", 180, 2));
        foodService = new FoodService(seed);
        cart = new CartService(foodService);
        burger = foodService.requireFood(1);
        pizza = foodService.requireFood(2);
    }

    @Test
    @DisplayName("adding to the cart reserves the stock immediately")
    void addReservesStock() {
        cart.addToCart(burger, 3);

        assertEquals(17, burger.getQuantity(), "units in a cart are off the counter");
        assertEquals(1, cart.getCart().size());
        assertEquals(3, cart.getItemCount());
    }

    @Test
    @DisplayName("adding the same item again grows the existing line instead of adding a second")
    void addMergesIntoOneLine() {
        cart.addToCart(burger, 2);
        cart.addToCart(burger, 3);

        assertEquals(1, cart.getCart().size());
        assertEquals(5, cart.getCart().get(0).getQuantity());
        assertEquals(15, burger.getQuantity());
    }

    @Test
    @DisplayName("adding more than the stock is refused with the console app's message")
    void addBeyondStockIsRefused() {
        InsufficientStockException thrown = assertThrows(InsufficientStockException.class,
                () -> cart.addToCart(pizza, 3));

        assertEquals("Not enough stock available.", thrown.getMessage());
        assertTrue(cart.getCart().isEmpty(), "a refused add must leave the cart untouched");
        assertEquals(2, pizza.getQuantity());
    }

    @Test
    @DisplayName("a zero or negative quantity is rejected")
    void invalidQuantityIsRejected() {
        InvalidRequestException thrown =
                assertThrows(InvalidRequestException.class, () -> cart.addToCart(burger, 0));

        assertEquals("Invalid quantity.", thrown.getMessage());
        assertThrows(InvalidRequestException.class, () -> cart.addToCart(burger, -1));
    }

    @Test
    @DisplayName("a null item is reported as not found")
    void nullItemIsRejected() {
        assertThrows(NotFoundException.class, () -> cart.addToCart(null, 1));
    }

    @Test
    @DisplayName("removing a line puts its stock back on the counter")
    void removeRestoresStock() {
        cart.addToCart(burger, 4);

        assertTrue(cart.removeFromCart(1));

        assertEquals(20, burger.getQuantity());
        assertTrue(cart.getCart().isEmpty());
    }

    @Test
    @DisplayName("removing something that is not in the cart reports false")
    void removeUnknownLine() {
        assertFalse(cart.removeFromCart(99));
    }

    @Test
    @DisplayName("changeQuantity reserves the increase and releases the decrease")
    void changeQuantityAdjustsStock() {
        cart.addToCart(burger, 2);

        cart.changeQuantity(1, 5);
        assertEquals(15, burger.getQuantity());

        cart.changeQuantity(1, 1);
        assertEquals(19, burger.getQuantity());
    }

    @Test
    @DisplayName("changeQuantity to zero removes the line and returns all the stock")
    void changeQuantityToZeroRemovesLine() {
        cart.addToCart(burger, 3);

        cart.changeQuantity(1, 0);

        assertTrue(cart.getCart().isEmpty());
        assertEquals(20, burger.getQuantity());
    }

    @Test
    @DisplayName("changeQuantity cannot push a line past available stock")
    void changeQuantityRespectsStock() {
        cart.addToCart(pizza, 1);

        assertThrows(InsufficientStockException.class, () -> cart.changeQuantity(2, 5));
        assertEquals(1, cart.getCart().get(0).getQuantity(), "the line must be unchanged");
    }

    @Test
    @DisplayName("calculateTotal multiplies price by quantity across every line")
    void calculateTotal() {
        cart.addToCart(burger, 2);   // 80 x 2 = 160
        cart.addToCart(pizza, 1);    // 180 x 1 = 180

        assertEquals(340.0, cart.calculateTotal(), 0.001);
        assertEquals(3, cart.getItemCount());
    }

    @Test
    @DisplayName("clearing the cart releases every reserved unit")
    void clearCartReleasesEverything() {
        cart.addToCart(burger, 4);
        cart.addToCart(pizza, 2);

        cart.clearCart();

        assertEquals(20, burger.getQuantity());
        assertEquals(2, pizza.getQuantity());
        assertTrue(cart.getCart().isEmpty());
    }

    @Test
    @DisplayName("an admin price change reaches a cart that already holds the item")
    void priceEditsPropagateIntoTheCart() {
        cart.addToCart(burger, 2);

        foodService.updateFood(1, "Veg Burger", 100, 18, "Snacks", "", "🍔");

        assertEquals(200.0, cart.calculateTotal(), 0.001);
    }

    @Test
    @DisplayName("a deleted item's line disappears the next time the cart is touched")
    void reconcileDropsDeletedItems() {
        cart.addToCart(burger, 2);
        cart.addToCart(pizza, 1);

        foodService.deleteFood(2);

        assertEquals(1, cart.getCart().size(), "the Pizza line should be gone");
        assertEquals(1, cart.getCart().get(0).getFoodItem().getId());
        assertNotNull(cart.consumeNotice(), "the customer should be told why");
    }

    @Test
    @DisplayName("the notice is delivered once, then cleared")
    void noticeIsConsumedOnce() {
        cart.addToCart(pizza, 1);
        foodService.deleteFood(2);
        cart.getCart();

        assertNotNull(cart.consumeNotice());
        assertNull(cart.consumeNotice());
    }
}
