package com.flowbite.model;

/**
 * One line in a cart: a food item and how many of it.
 *
 * <p>The {@code foodItem} reference is <b>live</b> - it points at the same
 * object the menu holds. That is deliberate and is what the console app always
 * did: when an admin edits a price, every cart holding that item sees the new
 * price immediately, and {@link #getTotalPrice()} recalculates.
 *
 * <p>The exception is a placed order, which must not change afterwards. That is
 * what {@link #CartItem(CartItem)} is for.
 */
public class CartItem {

    private FoodItem foodItem;
    private int quantity;

    // Constructor
    public CartItem(FoodItem foodItem, int quantity) {
        this.foodItem = foodItem;
        this.quantity = quantity;
    }

    /**
     * Deep copy, taking a snapshot of the food item as well as the quantity.
     *
     * <p>Used by {@code OrderService.placeOrder}. Without it an order would keep
     * pointing at the live menu item, so an admin changing the price of Pizza
     * would retroactively rewrite the total of every Pizza order ever placed.
     *
     * @param other the cart line to snapshot.
     */
    public CartItem(CartItem other) {
        this(new FoodItem(other.foodItem), other.quantity);
    }

    // Getters
    public FoodItem getFoodItem() {
        return foodItem;
    }

    public int getQuantity() {
        return quantity;
    }

    // Setters
    public void setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Calculate total price
    public double getTotalPrice() {
        return foodItem.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return foodItem.getName()
                + " | Qty: " + quantity
                + " | Total: ₹" + String.format("%.2f", getTotalPrice());
    }
}
