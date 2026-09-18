package com.flowbite.model;

import java.time.Instant;
import java.util.ArrayList;

/**
 * A placed order: who ordered, what, and when.
 *
 * <p>An order is a record of what was actually charged, so the cart lines it
 * holds are snapshots (see {@link CartItem#CartItem(CartItem)}), not live menu
 * references. Editing a menu item afterwards leaves placed orders untouched.
 */
public class Order {

    private int orderId;
    private String customerName;
    private ArrayList<CartItem> cartItems;
    private Instant placedAt;

    // Constructor - a brand new order, placed right now.
    public Order(int orderId, String customerName) {
        this(orderId, customerName, Instant.now());
    }

    /**
     * Rebuilds an order that was placed earlier.
     *
     * <p>Used when loading from storage, where the time it was placed is a fact
     * read off disk rather than "now". The two-argument constructor above is
     * still the one checkout uses.
     *
     * @param orderId the id it was given when it was placed.
     * @param customerName who placed it.
     * @param placedAt when it was placed.
     */
    public Order(int orderId, String customerName, Instant placedAt) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.cartItems = new ArrayList<>();
        this.placedAt = placedAt;
    }

    // Add item to order
    public void addCartItem(CartItem item) {
        cartItems.add(item);
    }

    // Calculate total bill
    public double getTotalAmount() {
        double total = 0;

        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }

        return total;
    }

    /**
     * Counts individual units rather than lines, for the "N items" summary.
     *
     * @return total number of units in the order.
     */
    public int getItemCount() {
        int count = 0;

        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }

        return count;
    }

    /**
     * The order's public identifier.
     *
     * <p>The console app showed a bare incrementing integer. The web app shows
     * this padded form, which used to be generated in JavaScript; it lives here
     * now so both front ends print the same code for the same order.
     *
     * @return an identifier such as {@code FB-0001}.
     */
    public String getOrderCode() {
        return String.format("FB-%04d", orderId);
    }

    // Getters
    public int getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public ArrayList<CartItem> getCartItems() {
        return cartItems;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    @Override
    public String toString() {

        StringBuilder bill = new StringBuilder();

        bill.append("\n========== ORDER ==========\n");
        bill.append("Order ID : ").append(getOrderCode()).append("\n");
        bill.append("Customer : ").append(customerName).append("\n\n");

        for (CartItem item : cartItems) {
            bill.append(item).append("\n");
        }

        bill.append("\n----------------------------\n");
        bill.append("Total Amount : ₹")
            .append(String.format("%.2f", getTotalAmount()))
            .append("\n");

        return bill.toString();
    }
}
