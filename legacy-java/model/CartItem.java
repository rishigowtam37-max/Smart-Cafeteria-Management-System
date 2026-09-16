package model;

public class CartItem {

    private FoodItem foodItem;
    private int quantity;

    // Constructor
    public CartItem(FoodItem foodItem, int quantity) {
        this.foodItem = foodItem;
        this.quantity = quantity;
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