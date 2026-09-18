package com.flowbite.service;

import com.flowbite.model.FoodItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The seed menu, which replaced the five items hard-coded in the console app's
 * {@code FoodService} constructor.
 */
class MenuLoaderTest {

    private final MenuLoader menuLoader = new MenuLoader(new ObjectMapper());

    @Test
    @DisplayName("menu.json loads all twelve items with their web fields")
    void loadsTheSeedMenu() {
        List<FoodItem> menu = menuLoader.loadSeedMenu();

        assertEquals(12, menu.size());

        FoodItem first = menu.get(0);
        assertEquals(1, first.getId());
        assertEquals("Veg Burger", first.getName());
        assertEquals(80.0, first.getPrice(), 0.001);
        assertEquals(20, first.getQuantity());
        assertEquals("Snacks", first.getCategory());
        assertFalse(first.getDescription().isEmpty());
        assertNotNull(first.getEmoji());
    }

    @Test
    @DisplayName("the five original console items keep their exact ids, prices and quantities")
    void originalItemsAreUnchanged() {
        List<FoodItem> menu = menuLoader.loadSeedMenu();
        FoodService foodService = new FoodService(menu);

        assertItem(foodService, 1, "Veg Burger", 80, 20);
        assertItem(foodService, 2, "Chicken Burger", 120, 15);
        assertItem(foodService, 3, "French Fries", 60, 25);
        assertItem(foodService, 4, "Pizza", 180, 10);
        assertItem(foodService, 5, "Coke", 40, 30);
    }

    @Test
    @DisplayName("one item is deliberately sold out so the empty state is reachable")
    void oneItemIsSoldOut() {
        List<FoodItem> menu = menuLoader.loadSeedMenu();

        long soldOut = menu.stream().filter(item -> !item.isAvailable()).count();

        assertEquals(1, soldOut);
    }

    @Test
    @DisplayName("every item has a unique id")
    void idsAreUnique() {
        List<FoodItem> menu = menuLoader.loadSeedMenu();

        long distinct = menu.stream().map(FoodItem::getId).distinct().count();

        assertEquals(menu.size(), distinct);
    }

    @Test
    @DisplayName("loading twice gives independent objects, not a shared menu")
    void eachLoadIsIndependent() {
        FoodItem fromFirstLoad = menuLoader.loadSeedMenu().get(0);
        FoodItem fromSecondLoad = menuLoader.loadSeedMenu().get(0);

        fromFirstLoad.setQuantity(0);

        assertTrue(fromSecondLoad.isAvailable());
    }

    private void assertItem(FoodService foodService, int id, String name, double price, int quantity) {
        FoodItem item = foodService.requireFood(id);
        assertEquals(name, item.getName());
        assertEquals(price, item.getPrice(), 0.001);
        assertEquals(quantity, item.getQuantity());
    }
}
