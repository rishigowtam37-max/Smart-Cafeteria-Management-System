package com.flowbite.service;

import com.flowbite.error.InsufficientStockException;
import com.flowbite.error.InvalidRequestException;
import com.flowbite.error.NotFoundException;
import com.flowbite.model.FoodItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the menu CRUD carried over from the console app plus the stock
 * reservation that the web app added.
 */
class FoodServiceTest {

    private FoodService foodService;

    @BeforeEach
    void setUp() {
        List<FoodItem> seed = new ArrayList<>();
        seed.add(new FoodItem(1, "Veg Burger", 80, 20));
        seed.add(new FoodItem(2, "Pizza", 180, 1));
        seed.add(new FoodItem(3, "Gulab Jamun", 40, 0));
        foodService = new FoodService(seed);
    }

    @Test
    @DisplayName("searchFood finds an item by id and returns null for an unknown one")
    void searchFood() {
        assertEquals("Pizza", foodService.searchFood(2).getName());
        assertNull(foodService.searchFood(99));
    }

    @Test
    @DisplayName("requireFood throws instead of returning null")
    void requireFoodThrows() {
        NotFoundException thrown =
                assertThrows(NotFoundException.class, () -> foodService.requireFood(99));
        assertEquals("Food item not found.", thrown.getMessage());
    }

    @Test
    @DisplayName("createFood assigns the next free id")
    void createFoodAssignsId() {
        FoodItem created = foodService.createFood("Samosa", 25, 40, "Snacks", "Crisp", "🥟");

        assertEquals(4, created.getId());
        assertEquals(4, foodService.getFoodList().size());
    }

    @Test
    @DisplayName("nextId is one past the highest id, and 1 for an empty menu")
    void nextId() {
        assertEquals(4, foodService.nextId());
        assertEquals(1, new FoodService(new ArrayList<>()).nextId());
    }

    @Test
    @DisplayName("createFood rejects a blank name, a zero price and negative stock")
    void createFoodValidates() {
        assertThrows(InvalidRequestException.class,
                () -> foodService.createFood("  ", 25, 5, "Snacks", "", "🥟"));
        assertThrows(InvalidRequestException.class,
                () -> foodService.createFood("Samosa", 0, 5, "Snacks", "", "🥟"));
        assertThrows(InvalidRequestException.class,
                () -> foodService.createFood("Samosa", 25, -1, "Snacks", "", "🥟"));
    }

    @Test
    @DisplayName("updateFood mutates the existing object so carts holding it see the change")
    void updateFoodMutatesInPlace() {
        FoodItem heldByACart = foodService.searchFood(1);

        foodService.updateFood(1, "Veg Burger Deluxe", 95, 18, "Snacks", "Now with cheese", "🍔");

        assertEquals("Veg Burger Deluxe", heldByACart.getName());
        assertEquals(95, heldByACart.getPrice());
    }

    @Test
    @DisplayName("deleteFood removes a real item and reports an unknown one")
    void deleteFood() {
        assertTrue(foodService.deleteFood(1));
        assertFalse(foodService.deleteFood(1));
        assertNull(foodService.searchFood(1));
    }

    @Test
    @DisplayName("reserveStock subtracts the units it took")
    void reserveStock() {
        FoodItem burger = foodService.requireFood(1);

        foodService.reserveStock(burger, 3);

        assertEquals(17, burger.getQuantity());
    }

    @Test
    @DisplayName("reserveStock refuses to oversell, with the console app's message")
    void reserveStockRefusesOverselling() {
        FoodItem pizza = foodService.requireFood(2);

        InsufficientStockException thrown = assertThrows(InsufficientStockException.class,
                () -> foodService.reserveStock(pizza, 2));

        assertEquals("Not enough stock available.", thrown.getMessage());
        assertEquals(1, pizza.getQuantity(), "a refused reservation must not change stock");
    }

    @Test
    @DisplayName("a sold-out item cannot be reserved at all")
    void soldOutCannotBeReserved() {
        FoodItem soldOut = foodService.requireFood(3);

        assertFalse(soldOut.isAvailable());
        assertThrows(InsufficientStockException.class, () -> foodService.reserveStock(soldOut, 1));
    }

    @Test
    @DisplayName("releaseStock puts units back")
    void releaseStock() {
        FoodItem burger = foodService.requireFood(1);

        foodService.reserveStock(burger, 5);
        foodService.releaseStock(burger, 5);

        assertEquals(20, burger.getQuantity());
    }

    @Test
    @DisplayName("getFoodList hands back a copy, so callers cannot edit the menu behind its back")
    void getFoodListIsACopy() {
        List<FoodItem> snapshot = foodService.getFoodList();

        snapshot.clear();

        assertEquals(3, foodService.getFoodList().size());
        assertNotNull(foodService.searchFood(1));
    }
}
