package com.flowbite.service;

import com.flowbite.error.InsufficientStockException;
import com.flowbite.error.InvalidRequestException;
import com.flowbite.error.NotFoundException;
import com.flowbite.model.FoodItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The menu, and the only place stock is allowed to change.
 *
 * <p>This is the console app's {@code FoodService} with two differences. The
 * hard-coded seed list is now {@code menu.json}, and stock changes go through
 * {@link #reserveStock} / {@link #releaseStock}.
 *
 * <p><b>Why the locking.</b> In the console app one person typed one command at
 * a time, so "check there is enough, then subtract" could never interleave. On a
 * server every request is a different thread against this one shared object, and
 * two customers can read "1 Pizza left" at the same instant and both subtract,
 * overselling. Every method that touches the list or a quantity is therefore
 * {@code synchronized} on this service, which makes check-and-subtract atomic.
 *
 * <p>Locking on the service rather than on each item is the simple choice and it
 * is fast enough here - operations are a few array scans over a dozen items.
 */
@Service
public class FoodService {

    private final List<FoodItem> foodList = new ArrayList<>();

    /**
     * Spring's constructor: seeds the menu from menu.json.
     *
     * @param menuLoader reads the seed file.
     */
    @Autowired
    public FoodService(MenuLoader menuLoader) {
        this(menuLoader.loadSeedMenu());
    }

    /**
     * Seeds the menu from an explicit list. Used by tests and by the console app.
     *
     * @param seedItems the items to start with.
     */
    public FoodService(List<FoodItem> seedItems) {
        foodList.addAll(seedItems);
    }

    // ---------------------------------------------------------------- reads

    /**
     * @return a snapshot copy of the menu. A copy, so callers can iterate it
     *     without holding the lock and without seeing it change underneath them.
     */
    public synchronized List<FoodItem> getFoodList() {
        return new ArrayList<>(foodList);
    }

    // Search food by ID
    public synchronized FoodItem searchFood(int id) {

        for (FoodItem item : foodList) {

            if (item.getId() == id) {
                return item;
            }

        }

        return null;
    }

    /**
     * Looks up an item, refusing rather than returning null.
     *
     * @param id the food id.
     * @return the item.
     * @throws NotFoundException if no item has that id.
     */
    public synchronized FoodItem requireFood(int id) {

        FoodItem item = searchFood(id);

        if (item == null) {
            throw new NotFoundException("Food item not found.");
        }

        return item;
    }

    /**
     * @return one higher than the largest id in use, or 1 for an empty menu.
     *     The console app made the admin type an id and let them collide; the
     *     server assigns it instead.
     */
    public synchronized int nextId() {

        int highest = 0;

        for (FoodItem item : foodList) {
            highest = Math.max(highest, item.getId());
        }

        return highest + 1;
    }

    // --------------------------------------------------------------- writes

    // Add new food
    public synchronized void addFood(FoodItem item) {
        foodList.add(item);
    }

    // Delete food
    public synchronized boolean deleteFood(int id) {

        FoodItem item = searchFood(id);

        if (item != null) {
            foodList.remove(item);
            return true;
        }

        return false;
    }

    // Update food
    public synchronized boolean updateFood(int id, String name, double price, int quantity) {

        FoodItem item = searchFood(id);

        if (item != null) {

            item.setName(name);
            item.setPrice(price);
            item.setQuantity(quantity);

            return true;
        }

        return false;
    }

    /**
     * Full update including the web-only fields.
     *
     * <p>Mutating the existing object rather than replacing it is what makes an
     * admin's price change show up instantly in carts that already hold the
     * item - they hold a reference to this same object.
     *
     * @param id the item to update.
     * @param name new name.
     * @param price new price.
     * @param quantity new <i>available</i> quantity (see {@link FoodItem}).
     * @param category new category.
     * @param description new description.
     * @param emoji new icon.
     * @return the updated item.
     * @throws NotFoundException if no item has that id.
     * @throws InvalidRequestException if a field is unusable.
     */
    public synchronized FoodItem updateFood(int id, String name, double price, int quantity,
                                            String category, String description, String emoji) {

        FoodItem item = requireFood(id);

        validate(name, price, quantity);

        item.setName(name.trim());
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setCategory(category);
        item.setDescription(description);
        item.setEmoji(emoji);

        return item;
    }

    /**
     * Creates a menu item, assigning the id itself.
     *
     * @return the newly created item.
     * @throws InvalidRequestException if a field is unusable.
     */
    public synchronized FoodItem createFood(String name, double price, int quantity,
                                            String category, String description, String emoji) {

        validate(name, price, quantity);

        FoodItem item = new FoodItem(nextId(), name.trim(), price, quantity,
                                     category, description, emoji);
        foodList.add(item);

        return item;
    }

    // ---------------------------------------------------------------- stock

    /**
     * Takes units off the counter for a cart, atomically.
     *
     * <p>Check and subtract happen under one lock, so of two customers racing
     * for the last unit exactly one succeeds and the other is refused.
     *
     * @param item the item to reserve.
     * @param quantity how many units.
     * @throws InsufficientStockException if fewer units are available.
     */
    public synchronized void reserveStock(FoodItem item, int quantity) {

        if (quantity > item.getQuantity()) {
            throw new InsufficientStockException("Not enough stock available.");
        }

        item.setQuantity(item.getQuantity() - quantity);
    }

    /**
     * Puts units back on the counter when a cart gives them up.
     *
     * @param item the item to release.
     * @param quantity how many units.
     */
    public synchronized void releaseStock(FoodItem item, int quantity) {
        item.setQuantity(item.getQuantity() + quantity);
    }

    // ------------------------------------------------------------- console

    /**
     * Prints the menu. Console app only - the web layer serialises
     * {@link #getFoodList()} to JSON instead.
     */
    public void displayMenu() {

        System.out.println("\n========== MENU ==========");

        List<FoodItem> items = getFoodList();

        if (items.isEmpty()) {
            System.out.println("No food items available.");
            return;
        }

        for (FoodItem item : items) {
            System.out.println(item);
        }
    }

    // ------------------------------------------------------------- internal

    private void validate(String name, double price, int quantity) {

        if (name == null || name.trim().isEmpty()) {
            throw new InvalidRequestException("Food name is required.");
        }

        if (price <= 0) {
            throw new InvalidRequestException("Price must be greater than zero.");
        }

        if (quantity < 0) {
            throw new InvalidRequestException("Quantity cannot be negative.");
        }
    }
}
