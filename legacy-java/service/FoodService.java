package service;

import model.FoodItem;
import java.util.ArrayList;

public class FoodService {

    private ArrayList<FoodItem> foodList;

    // Constructor
    public FoodService() {
        foodList = new ArrayList<>();

        // Default Menu
        foodList.add(new FoodItem(1, "Veg Burger", 80, 20));
        foodList.add(new FoodItem(2, "Chicken Burger", 120, 15));
        foodList.add(new FoodItem(3, "French Fries", 60, 25));
        foodList.add(new FoodItem(4, "Pizza", 180, 10));
        foodList.add(new FoodItem(5, "Coke", 40, 30));
    }

    // Display all food items
    public void displayMenu() {

        System.out.println("\n========== MENU ==========");

        if (foodList.isEmpty()) {
            System.out.println("No food items available.");
            return;
        }

        for (FoodItem item : foodList) {
            System.out.println(item);
        }
    }

    // Add new food
    public void addFood(FoodItem item) {
        foodList.add(item);
        System.out.println("Food item added successfully.");
    }

    // Search food by ID
    public FoodItem searchFood(int id) {

        for (FoodItem item : foodList) {

            if (item.getId() == id) {
                return item;
            }

        }

        return null;
    }

    // Delete food
    public boolean deleteFood(int id) {

        FoodItem item = searchFood(id);

        if (item != null) {
            foodList.remove(item);
            return true;
        }

        return false;
    }

    // Update food
    public boolean updateFood(int id, String name, double price, int quantity) {

        FoodItem item = searchFood(id);

        if (item != null) {

            item.setName(name);
            item.setPrice(price);
            item.setQuantity(quantity);

            return true;
        }

        return false;
    }

    // Return all food items
    public ArrayList<FoodItem> getFoodList() {
        return foodList;
    }
}