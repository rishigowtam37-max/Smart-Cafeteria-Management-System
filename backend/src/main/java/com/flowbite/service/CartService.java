package com.flowbite.service;

import com.flowbite.error.InvalidRequestException;
import com.flowbite.error.NotFoundException;
import com.flowbite.model.CartItem;
import com.flowbite.model.FoodItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * One customer's cart.
 *
 * <p>Unlike the other services this is <b>not</b> a singleton bean. Each browser
 * session gets its own instance, stored as a session attribute (see
 * {@code CartSessionListener}), because a cart belongs to one customer the same
 * way the console app's single cart belonged to the one person at the terminal.
 *
 * <p><b>Stock is reserved on add</b>, exactly as the console app did: the units
 * come off the counter when the item enters the cart and go back if it leaves.
 * That is what makes "two people cannot both take the last Pizza" true without
 * any checkout-time gymnastics - by the time you are holding it, it is yours.
 * The cost is that abandoned carts hold stock, which is why sessions expire
 * after ten minutes and release it.
 *
 * <p>Methods are {@code synchronized} because one customer can have several tabs
 * open, and each tab's request arrives on its own thread.
 */
public class CartService {

    private final FoodService foodService;
    private final List<CartItem> cart = new ArrayList<>();

    /** Set when reconcile() drops a line, consumed once by the next response. */
    private String pendingNotice;

    public CartService(FoodService foodService) {
        this.foodService = foodService;
    }

    /**
     * Adds units of a food item to the cart, reserving the stock.
     *
     * <p>Differs from the console version in one way: adding an item already in
     * the cart increases that line instead of appending a second line for the
     * same food. The console app appended, which the web cart cannot display
     * sensibly and which made removal ambiguous.
     *
     * @param foodItem the item being ordered.
     * @param quantity how many units, at least 1.
     * @throws NotFoundException if the item is null.
     * @throws InvalidRequestException if the quantity is zero or negative.
     * @throws com.flowbite.error.InsufficientStockException if stock is short.
     */
    public synchronized void addToCart(FoodItem foodItem, int quantity) {

        reconcile();

        if (foodItem == null) {
            throw new NotFoundException("Food item not found.");
        }

        if (quantity <= 0) {
            throw new InvalidRequestException("Invalid quantity.");
        }

        // Reserve first: if stock is short this throws and the cart is untouched.
        foodService.reserveStock(foodItem, quantity);

        CartItem existingLine = findLine(foodItem.getId());

        if (existingLine == null) {
            cart.add(new CartItem(foodItem, quantity));
        } else {
            existingLine.setQuantity(existingLine.getQuantity() + quantity);
        }
    }

    /**
     * Sets a line to an absolute quantity, reserving or releasing the difference.
     *
     * <p>New in the web app - the console app had no way to change a line, only
     * to add or remove it. Setting zero removes the line.
     *
     * @param foodId which line to change.
     * @param newQuantity the quantity the line should end up at.
     * @throws NotFoundException if that line is not in the cart.
     * @throws com.flowbite.error.InsufficientStockException if increasing beyond stock.
     */
    public synchronized void changeQuantity(int foodId, int newQuantity) {

        reconcile();

        CartItem line = findLine(foodId);

        if (line == null) {
            throw new NotFoundException("Food item not found.");
        }

        if (newQuantity < 0) {
            throw new InvalidRequestException("Invalid quantity.");
        }

        if (newQuantity == 0) {
            removeFromCart(foodId);
            return;
        }

        int difference = newQuantity - line.getQuantity();

        if (difference > 0) {
            foodService.reserveStock(line.getFoodItem(), difference);
        } else if (difference < 0) {
            foodService.releaseStock(line.getFoodItem(), -difference);
        }

        line.setQuantity(newQuantity);
    }

    // Remove item from cart
    public synchronized boolean removeFromCart(int foodId) {

        reconcile();

        for (CartItem item : cart) {

            if (item.getFoodItem().getId() == foodId) {

                // Restore stock
                foodService.releaseStock(item.getFoodItem(), item.getQuantity());

                cart.remove(item);
                return true;
            }
        }

        return false;
    }

    /** Empties the cart, releasing every reserved unit back to the menu. */
    public synchronized void clearCart() {

        for (CartItem item : cart) {
            foodService.releaseStock(item.getFoodItem(), item.getQuantity());
        }

        cart.clear();
    }

    /**
     * Empties the cart <i>without</i> releasing stock.
     *
     * <p>Used at checkout only: those units were sold, not abandoned, so they
     * must not go back on the counter.
     */
    synchronized void clearAfterCheckout() {
        cart.clear();
    }

    // Calculate total
    public synchronized double calculateTotal() {

        double total = 0;

        for (CartItem item : cart) {
            total += item.getTotalPrice();
        }

        return total;
    }

    /**
     * @return number of individual units, not lines.
     */
    public synchronized int getItemCount() {

        int count = 0;

        for (CartItem item : cart) {
            count += item.getQuantity();
        }

        return count;
    }

    /**
     * @return a snapshot copy of the cart lines.
     */
    public synchronized List<CartItem> getCart() {
        reconcile();
        return new ArrayList<>(cart);
    }

    public synchronized boolean isEmpty() {
        return cart.isEmpty();
    }

    /**
     * Returns and clears the pending notice, if any.
     *
     * @return a message such as "Pizza is no longer on the menu and was removed
     *     from your cart.", or null.
     */
    public synchronized String consumeNotice() {
        String notice = pendingNotice;
        pendingNotice = null;
        return notice;
    }

    /**
     * Drops lines whose food item is no longer on the menu.
     *
     * <p>An admin deleting an item cannot reach into other customers' sessions -
     * sessions are not enumerable - so each cart repairs itself the next time its
     * owner touches it. The reserved units disappeared with the item, so there is
     * nothing to release.
     */
    private void reconcile() {

        List<String> dropped = new ArrayList<>();
        Iterator<CartItem> lines = cart.iterator();

        while (lines.hasNext()) {

            CartItem line = lines.next();
            int foodId = line.getFoodItem().getId();

            // Identity, not id: an id reused by a newly created item is a
            // different product, and this line's reservation was against the old one.
            if (foodService.searchFood(foodId) != line.getFoodItem()) {
                dropped.add(line.getFoodItem().getName());
                lines.remove();
            }
        }

        if (!dropped.isEmpty()) {
            pendingNotice = String.join(", ", dropped)
                    + (dropped.size() == 1 ? " is" : " are")
                    + " no longer on the menu and left your cart.";
        }
    }

    private CartItem findLine(int foodId) {

        for (CartItem item : cart) {

            if (item.getFoodItem().getId() == foodId) {
                return item;
            }
        }

        return null;
    }

    // ------------------------------------------------------------- console

    /**
     * Prints the cart. Console app only - the web layer serialises the cart to
     * JSON instead.
     */
    public void viewCart() {

        List<CartItem> lines = getCart();

        if (lines.isEmpty()) {
            System.out.println("\nYour cart is empty.");
            return;
        }

        System.out.println("\n========== YOUR CART ==========");

        for (CartItem item : lines) {
            System.out.println(item);
        }

        System.out.println("-------------------------------");
        System.out.printf("Total Bill : ₹%.2f%n", calculateTotal());
    }
}
