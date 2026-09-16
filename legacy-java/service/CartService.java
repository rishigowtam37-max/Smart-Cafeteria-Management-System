package service;

import model.CartItem;
import model.FoodItem;

import java.util.ArrayList;

public class CartService {

    private ArrayList<CartItem> cart;

    // Constructor
    public CartService() {
        cart = new ArrayList<>();
    }

    // Add item to cart
    public void addToCart(FoodItem foodItem, int quantity) {

        if (foodItem == null) {
            System.out.println("Food item not found.");
            return;
        }

        if (quantity <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }

        if (quantity > foodItem.getQuantity()) {
            System.out.println("Not enough stock available.");
            return;
        }

        cart.add(new CartItem(foodItem, quantity));

        // Reduce stock
        foodItem.setQuantity(foodItem.getQuantity() - quantity);

        System.out.println("Item added to cart successfully.");
    }

    // View cart
    public void viewCart() {

        if (cart.isEmpty()) {
            System.out.println("\nYour cart is empty.");
            return;
        }

        System.out.println("\n========== YOUR CART ==========");

        for (CartItem item : cart) {
            System.out.println(item);
        }

        System.out.println("-------------------------------");
        System.out.printf("Total Bill : ₹%.2f%n", calculateTotal());
    }

    // Calculate total
    public double calculateTotal() {

        double total = 0;

        for (CartItem item : cart) {
            total += item.getTotalPrice();
        }

        return total;
    }

    // Remove item from cart
    public boolean removeFromCart(int foodId) {

        for (CartItem item : cart) {

            if (item.getFoodItem().getId() == foodId) {

                // Restore stock
                FoodItem food = item.getFoodItem();
                food.setQuantity(food.getQuantity() + item.getQuantity());

                cart.remove(item);
                return true;
            }
        }

        return false;
    }

    // Clear cart
    public void clearCart() {
        cart.clear();
    }

    // Getter
    public ArrayList<CartItem> getCart() {
        return cart;
    }
}