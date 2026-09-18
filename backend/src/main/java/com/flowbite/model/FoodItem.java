package com.flowbite.model;

/**
 * One item on the cafeteria menu.
 *
 * <p>The four original fields - id, name, price, quantity - are unchanged from
 * the console app. Three were added for the web menu: category (the customer
 * filter bar), description (one line of copy on the card) and emoji (the icon
 * tile, which avoids any external image dependency).
 *
 * <p><b>quantity means units available to order right now</b>, not total stock
 * ever held. A unit sitting in someone's cart has already been subtracted,
 * because {@code CartService} reserves stock the moment an item is added - the
 * behaviour the console app has always had. An admin setting quantity to 20 is
 * therefore saying "twenty more can be ordered", and units already reserved in
 * carts are left alone.
 */
public class FoodItem {

    private int id;
    private String name;
    private double price;
    private int quantity;
    private String category;
    private String description;
    private String emoji;

    // Original constructor - still used by the console app.
    public FoodItem(int id, String name, double price, int quantity) {
        this(id, name, price, quantity, "Snacks", "", "🍽️");
    }

    // Full constructor, used when loading menu.json and by the admin form.
    public FoodItem(int id, String name, double price, int quantity,
                    String category, String description, String emoji) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.description = description;
        this.emoji = emoji;
    }

    /**
     * Copy constructor. Used when an order is placed, so the order keeps the
     * name and price that were actually charged even if an admin edits the menu
     * item afterwards.
     *
     * @param other the item to copy.
     */
    public FoodItem(FoodItem other) {
        this(other.id, other.name, other.price, other.quantity,
             other.category, other.description, other.emoji);
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

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getEmoji() {
        return emoji;
    }

    /**
     * @return true when at least one unit can still be ordered. The console app
     *     derived availability the same way, inside CartService.
     */
    public boolean isAvailable() {
        return quantity > 0;
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

    /**
     * Sets available stock.
     *
     * <p>Package-visible mutation is deliberate elsewhere: everything that
     * changes stock during ordering goes through
     * {@code FoodService.reserveStock} / {@code releaseStock}, which hold a lock
     * so two requests cannot both take the last unit.
     *
     * @param quantity units available to order.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    // Display Food Item
    @Override
    public String toString() {
        return String.format(
                "ID: %d | Name: %s | Price: ₹%.2f | Quantity: %d",
                id, name, price, quantity);
    }
}
