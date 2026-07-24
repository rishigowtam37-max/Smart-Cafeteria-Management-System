package model;

public class FoodItem {

    private int id;
    private String name;
    private double price;
    private int quantity;

    // Constructor
    public FoodItem(int id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Display Food Item
    @Override
    public String toString() {
        return String.format(
                "ID: %d | Name: %s | Price: ₹%.2f | Quantity: %d",
                id, name, price, quantity);
    }
}