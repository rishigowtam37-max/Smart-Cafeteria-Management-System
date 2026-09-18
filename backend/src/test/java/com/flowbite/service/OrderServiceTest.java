package com.flowbite.service;

import com.flowbite.error.EmptyCartException;
import com.flowbite.model.FoodItem;
import com.flowbite.model.Order;
import com.flowbite.repository.InMemoryOrderRepository;
import com.flowbite.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checkout: turning a cart into an immutable order.
 */
class OrderServiceTest {

    private FoodService foodService;
    private OrderRepository repository;
    private OrderService orderService;
    private CartService cart;
    private FoodItem burger;

    @BeforeEach
    void setUp() {
        List<FoodItem> seed = new ArrayList<>();
        seed.add(new FoodItem(1, "Veg Burger", 80, 20));
        seed.add(new FoodItem(2, "Pizza", 180, 10));
        foodService = new FoodService(seed);
        repository = new InMemoryOrderRepository();
        orderService = new OrderService(repository);
        cart = new CartService(foodService);
        burger = foodService.requireFood(1);
    }

    @Test
    @DisplayName("placing an order moves the cart into it and empties the cart")
    void placeOrderEmptiesCart() {
        cart.addToCart(burger, 2);

        Order order = orderService.placeOrder("Rishi", cart);

        assertNotNull(order);
        assertEquals("Rishi", order.getCustomerName());
        assertEquals(1, order.getCartItems().size());
        assertEquals(2, order.getItemCount());
        assertTrue(cart.getCart().isEmpty());
    }

    @Test
    @DisplayName("checkout keeps the stock it reserved - the units were sold, not abandoned")
    void checkoutDoesNotReturnStock() {
        cart.addToCart(burger, 3);
        assertEquals(17, burger.getQuantity());

        orderService.placeOrder("Rishi", cart);

        assertEquals(17, burger.getQuantity(), "sold units must not go back on the counter");
    }

    @Test
    @DisplayName("an empty cart is refused with the console app's message")
    void emptyCartIsRefused() {
        EmptyCartException thrown = assertThrows(EmptyCartException.class,
                () -> orderService.placeOrder("Rishi", cart));

        assertEquals("Your cart is empty.", thrown.getMessage());
        assertTrue(orderService.getOrders().isEmpty());
    }

    @Test
    @DisplayName("order codes run FB-0001, FB-0002, ...")
    void orderCodesAreSequentialAndPadded() {
        cart.addToCart(burger, 1);
        Order first = orderService.placeOrder("Rishi", cart);

        cart.addToCart(burger, 1);
        Order second = orderService.placeOrder("Rishi", cart);

        assertEquals("FB-0001", first.getOrderCode());
        assertEquals("FB-0002", second.getOrderCode());
        assertEquals(1, first.getOrderId());
    }

    @Test
    @DisplayName("getTotalAmount sums the lines")
    void totalAmount() {
        cart.addToCart(burger, 2);                       // 160
        cart.addToCart(foodService.requireFood(2), 1);   // 180

        Order order = orderService.placeOrder("Rishi", cart);

        assertEquals(340.0, order.getTotalAmount(), 0.001);
        assertEquals(3, order.getItemCount());
    }

    @Test
    @DisplayName("editing a menu item after checkout does not rewrite the placed order")
    void placedOrdersAreImmutable() {
        cart.addToCart(burger, 2);
        Order order = orderService.placeOrder("Rishi", cart);

        assertEquals(160.0, order.getTotalAmount(), 0.001);

        // An admin doubles the price the next day.
        foodService.updateFood(1, "Veg Burger", 160, 18, "Snacks", "", "🍔");

        assertEquals(160.0, order.getTotalAmount(), 0.001,
                "the order must record what was actually charged");
        assertEquals("Veg Burger", order.getCartItems().get(0).getFoodItem().getName());
    }

    @Test
    @DisplayName("deleting a menu item does not erase it from a placed order")
    void deletedItemsSurviveInOrders() {
        cart.addToCart(burger, 1);
        Order order = orderService.placeOrder("Rishi", cart);

        foodService.deleteFood(1);

        assertEquals(1, order.getCartItems().size());
        assertEquals("Veg Burger", order.getCartItems().get(0).getFoodItem().getName());
    }

    @Test
    @DisplayName("searchOrder finds a placed order by its numeric id")
    void searchOrder() {
        cart.addToCart(burger, 1);
        orderService.placeOrder("Rishi", cart);

        assertNotNull(orderService.searchOrder(1));
        assertEquals("FB-0001", orderService.searchOrder(1).getOrderCode());
    }

    @Test
    @DisplayName("the admin list is newest first and revenue is the sum of every order")
    void newestFirstAndRevenue() {
        cart.addToCart(burger, 1);                        // 80
        orderService.placeOrder("Rishi", cart);

        cart.addToCart(foodService.requireFood(2), 1);     // 180
        orderService.placeOrder("Rishi", cart);

        List<Order> newestFirst = orderService.getOrdersNewestFirst();

        assertEquals("FB-0002", newestFirst.get(0).getOrderCode());
        assertEquals("FB-0001", newestFirst.get(1).getOrderCode());
        assertEquals(260.0, orderService.getTotalRevenue(), 0.001);
    }

    // ------------------------------------------------------- surviving restart

    @Test
    @DisplayName("a restart reloads placed orders and carries on numbering after them")
    void restartResumesFromStoredOrders() {
        cart.addToCart(burger, 1);                         // 80
        orderService.placeOrder("Rishi", cart);
        cart.addToCart(foodService.requireFood(2), 1);     // 180
        orderService.placeOrder("Asha", cart);

        // The process ends and starts again: a new service over the same store.
        OrderService afterRestart = new OrderService(repository);

        assertEquals(2, afterRestart.getOrders().size());
        assertEquals("FB-0001", afterRestart.getOrders().get(0).getOrderCode());
        assertEquals("Asha", afterRestart.getOrders().get(1).getCustomerName());
        assertEquals(260.0, afterRestart.getTotalRevenue(), 0.001,
                "revenue must include what was earned before the restart");

        cart.addToCart(burger, 1);
        Order next = afterRestart.placeOrder("Rishi", cart);

        assertEquals("FB-0003", next.getOrderCode(),
                "numbering must resume after the stored orders, not restart at FB-0001");
    }

    @Test
    @DisplayName("an empty store starts at FB-0001")
    void emptyStoreStartsAtOne() {
        OrderService fresh = new OrderService(new InMemoryOrderRepository());

        cart.addToCart(burger, 1);

        assertEquals("FB-0001", fresh.placeOrder("Rishi", cart).getOrderCode());
    }

    @Test
    @DisplayName("an order that cannot be stored is not confirmed, and the cart is left intact")
    void aFailedWriteLeavesTheCartAlone() {
        OrderRepository broken = new InMemoryOrderRepository() {
            @Override
            public void save(Order order) {
                throw new IllegalStateException("database is down");
            }
        };
        OrderService failing = new OrderService(broken);

        cart.addToCart(burger, 2);

        assertThrows(IllegalStateException.class, () -> failing.placeOrder("Rishi", cart));

        assertTrue(failing.getOrders().isEmpty(),
                "an order that was never stored must not appear in the admin list");
        assertEquals(1, cart.getCart().size(),
                "the customer still has their items, and the stock is still reserved for them");
        assertEquals(18, burger.getQuantity());
    }
}
