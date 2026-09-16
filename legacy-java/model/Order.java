package model;

import java.util.ArrayList;

public class Order {

    private int orderId;
    private String customerName;
    private ArrayList<CartItem> cartItems;

    // Constructor
    public Order(int orderId, String customerName) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.cartItems = new ArrayList<>();
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

    @Override
    public String toString() {

        StringBuilder bill = new StringBuilder();

        bill.append("\n========== ORDER ==========\n");
        bill.append("Order ID : ").append(orderId).append("\n");
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