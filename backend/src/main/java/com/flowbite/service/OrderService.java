package com.flowbite.service;

import com.flowbite.error.EmptyCartException;
import com.flowbite.model.CartItem;
import com.flowbite.model.Order;
import com.flowbite.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Every order ever placed.
 *
 * <p>A singleton shared by all sessions, so both the list and the id counter are
 * guarded: {@code synchronized} for the list, an {@link AtomicInteger} for the
 * counter. Without the latter two simultaneous checkouts could be handed the
 * same order number.
 *
 * <p><b>Write-through.</b> Orders are written to an {@link OrderRepository} as
 * they are placed and read back once, here in the constructor. Every read after
 * that - search, revenue, the admin list - is answered from the in-memory list,
 * which stays the working set. The repository is therefore a durable log rather
 * than the runtime source of truth: correct for one process, and the reason the
 * admin screen does not issue SQL every time it polls. A second instance of this
 * application against the same database would need that inverted.
 *
 * <p>Orders are the only state that outlives a restart. The menu is reloaded
 * from {@code menu.json} on every boot and carts live in the HTTP session, so
 * stock returns to its seeded values while placed orders do not.
 */
@Service
public class OrderService {

    private final OrderRepository repository;
    private final List<Order> orders = new ArrayList<>();
    private final AtomicInteger nextOrderId;

    /**
     * Loads everything previously placed, and resumes numbering after it.
     *
     * <p>Seeding the counter from the highest stored id is what stops a restart
     * handing {@code FB-0001} to a second, different order.
     *
     * @param repository where placed orders are kept.
     */
    public OrderService(OrderRepository repository) {

        this.repository = repository;
        this.orders.addAll(repository.findAll());

        int highest = 0;

        for (Order order : orders) {
            highest = Math.max(highest, order.getOrderId());
        }

        this.nextOrderId = new AtomicInteger(highest + 1);
    }

    /**
     * Turns a cart into an order.
     *
     * <p>Two differences from the console version. An empty cart throws rather
     * than printing and returning null, and the cart lines are <b>copied</b> into
     * the order so that later menu edits cannot rewrite what was charged.
     *
     * <p>Stock is not touched here: the units were already reserved when they
     * entered the cart, so checkout simply keeps them rather than releasing them.
     *
     * @param customerName who is ordering.
     * @param cartService that customer's cart.
     * @return the placed order.
     * @throws EmptyCartException if the cart has nothing in it.
     */
    public Order placeOrder(String customerName, CartService cartService) {

        List<CartItem> lines = cartService.getCart();

        if (lines.isEmpty()) {
            throw new EmptyCartException("Your cart is empty.");
        }

        Order order = new Order(nextOrderId.getAndIncrement(), customerName);

        for (CartItem item : lines) {
            order.addCartItem(new CartItem(item));
        }

        // Stored before it is acknowledged. If this throws, the exception reaches
        // the customer and the cart is left untouched - they still have their
        // items and their reserved stock - rather than the order being confirmed
        // on screen and then missing after a restart.
        repository.save(order);

        synchronized (this) {
            orders.add(order);
        }

        // Sold, not abandoned - the reserved units stay off the counter.
        cartService.clearAfterCheckout();

        return order;
    }

    // Search order by ID
    public synchronized Order searchOrder(int orderId) {

        for (Order order : orders) {

            if (order.getOrderId() == orderId) {
                return order;
            }

        }

        return null;
    }

    /**
     * @return a snapshot of every order, oldest first.
     */
    public synchronized List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    /**
     * @return a snapshot of every order, newest first - the order the admin
     *     screen lists them in.
     */
    public synchronized List<Order> getOrdersNewestFirst() {
        List<Order> snapshot = new ArrayList<>(orders);
        Collections.reverse(snapshot);
        return snapshot;
    }

    /**
     * @return the sum of every order placed, for the admin revenue tile.
     */
    public synchronized double getTotalRevenue() {

        double revenue = 0;

        for (Order order : orders) {
            revenue += order.getTotalAmount();
        }

        return revenue;
    }

    // ------------------------------------------------------------- console

    /**
     * Prints all orders. Console app only.
     */
    public void displayOrders() {

        List<Order> snapshot = getOrders();

        if (snapshot.isEmpty()) {
            System.out.println("\nNo orders placed yet.");
            return;
        }

        System.out.println("\n========== ALL ORDERS ==========");

        for (Order order : snapshot) {
            System.out.println(order);
        }
    }
}
