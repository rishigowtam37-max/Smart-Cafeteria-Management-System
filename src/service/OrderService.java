package service;

import model.CartItem;
import model.Order;

import java.util.ArrayList;

public class OrderService {

    private ArrayList<Order> orders;
    private int nextOrderId;

    // Constructor
    public OrderService() {
        orders = new ArrayList<>();
        nextOrderId = 1;
    }

    // Place a new order
    public Order placeOrder(String customerName, CartService cartService) {

        if (cartService.getCart().isEmpty()) {
            System.out.println("Your cart is empty.");
            return null;
        }

        Order order = new Order(nextOrderId++, customerName);

        for (CartItem item : cartService.getCart()) {
            order.addCartItem(item);
        }

        orders.add(order);

        cartService.clearCart();

        System.out.println("Order placed successfully!");

        return order;
    }

    // Display all orders
    public void displayOrders() {

        if (orders.isEmpty()) {
            System.out.println("\nNo orders placed yet.");
            return;
        }

        System.out.println("\n========== ALL ORDERS ==========");

        for (Order order : orders) {
            System.out.println(order);
        }
    }

    // Search order by ID
    public Order searchOrder(int orderId) {

        for (Order order : orders) {

            if (order.getOrderId() == orderId) {
                return order;
            }

        }

        return null;
    }

    // Getter
    public ArrayList<Order> getOrders() {
        return orders;
    }
}