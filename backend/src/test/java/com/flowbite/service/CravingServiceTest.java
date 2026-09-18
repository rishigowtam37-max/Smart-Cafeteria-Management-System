package com.flowbite.service;

import com.flowbite.error.InvalidRequestException;
import com.flowbite.model.FoodItem;
import com.flowbite.service.CravingService.Pick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The part of Smart Craving that has to be right even when the model is not.
 *
 * <p>No network here. These exercise {@code resolvePicks}, which is the step
 * that decides what a model's answer is allowed to put on a customer's screen -
 * and therefore the step that makes a hallucinated id or a prompt injection
 * harmless rather than dangerous.
 */
class CravingServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CravingService cravingService;
    private FoodService foodService;
    private List<FoodItem> menu;

    @BeforeEach
    void setUp() {
        List<FoodItem> seed = new ArrayList<>();
        seed.add(new FoodItem(1, "Veg Burger", 80, 20, "Snacks", "Crisp patty", "🍔"));
        seed.add(new FoodItem(4, "Pizza", 180, 10, "Main Course", "Wood-fired", "🍕"));
        seed.add(new FoodItem(6, "Masala Dosa", 70, 18, "South Indian", "Crisp", "🥞"));
        seed.add(new FoodItem(12, "Gulab Jamun", 40, 0, "Desserts", "Sold out", "🍮"));

        foodService = new FoodService(seed);
        menu = foodService.getFoodList();

        cravingService = new CravingService(
                foodService, objectMapper, "test-key", "gemini-3.6-flash",
                "https://generativelanguage.googleapis.com/v1beta/models");
    }

    private JsonNode picks(String json) {
        return objectMapper.readTree(json);
    }

    @Test
    @DisplayName("real ids resolve to real menu items, in the model's order")
    void resolvesRealIds() {
        List<Pick> resolved = cravingService.resolvePicks(
                picks("""
                        [{"id":6,"reason":"Crisp and spicy."},{"id":1,"reason":"Very filling."}]
                        """), menu);

        assertEquals(2, resolved.size());
        assertEquals("Masala Dosa", resolved.get(0).item().getName());
        assertEquals("Veg Burger", resolved.get(1).item().getName());
        assertEquals("Crisp and spicy.", resolved.get(0).reason());
    }

    @Test
    @DisplayName("an invented id is dropped - this is what makes a hallucination harmless")
    void dropsInventedIds() {
        List<Pick> resolved = cravingService.resolvePicks(
                picks("""
                        [{"id":999,"reason":"Our famous lobster thermidor."},{"id":4,"reason":"Real."}]
                        """), menu);

        assertEquals(1, resolved.size());
        assertEquals("Pizza", resolved.get(0).item().getName());
    }

    @Test
    @DisplayName("a sold-out item is never suggested")
    void dropsSoldOutItems() {
        List<Pick> resolved = cravingService.resolvePicks(
                picks("""
                        [{"id":12,"reason":"Sweet and syrupy."}]
                        """), menu);

        assertTrue(resolved.isEmpty());
    }

    @Test
    @DisplayName("an item that sold out while the model was thinking is dropped too")
    void reflectsStockChangedDuringTheCall() {
        FoodItem pizza = foodService.requireFood(4);
        foodService.reserveStock(pizza, 10);   // a customer took the last of it

        List<Pick> resolved = cravingService.resolvePicks(
                picks("""
                        [{"id":4,"reason":"Still warm."}]
                        """), menu);

        assertTrue(resolved.isEmpty(), "the menu is re-checked, not trusted from prompt time");
    }

    @Test
    @DisplayName("a repeated id appears once")
    void dropsDuplicates() {
        List<Pick> resolved = cravingService.resolvePicks(
                picks("""
                        [{"id":1,"reason":"First."},{"id":1,"reason":"Again."},{"id":4,"reason":"Other."}]
                        """), menu);

        assertEquals(2, resolved.size());
        assertEquals(1, resolved.get(0).item().getId());
        assertEquals(4, resolved.get(1).item().getId());
    }

    @Test
    @DisplayName("never more than three picks, however many come back")
    void capsAtThree() {
        List<Pick> resolved = cravingService.resolvePicks(
                picks("""
                        [{"id":1,"reason":"a"},{"id":4,"reason":"b"},{"id":6,"reason":"c"},{"id":1,"reason":"d"}]
                        """), menu);

        assertEquals(3, resolved.size());
    }

    @Test
    @DisplayName("an over-long reason is truncated rather than wrecking the layout")
    void truncatesLongReasons() {
        String essay = "x".repeat(500);

        List<Pick> resolved = cravingService.resolvePicks(
                picks("[{\"id\":1,\"reason\":\"" + essay + "\"}]"), menu);

        assertEquals(160, resolved.get(0).reason().length());
    }

    @Test
    @DisplayName("junk in place of a picks array yields nothing, not an exception")
    void handlesMalformedPicks() {
        assertTrue(cravingService.resolvePicks(picks("{}"), menu).isEmpty());
        assertTrue(cravingService.resolvePicks(picks("\"nonsense\""), menu).isEmpty());
        assertTrue(cravingService.resolvePicks(picks("[{\"noId\":true}]"), menu).isEmpty());
        assertTrue(cravingService.resolvePicks(null, menu).isEmpty());
    }

    @Test
    @DisplayName("a blank craving is refused before any API call is made")
    void refusesBlankCraving() {
        assertThrows(InvalidRequestException.class, () -> cravingService.suggest("   "));
        assertThrows(InvalidRequestException.class, () -> cravingService.suggest(null));
    }

    @Test
    @DisplayName("with nothing in stock there is nothing to suggest, and no call is made")
    void refusesWhenEverythingIsSoldOut() {
        List<FoodItem> soldOut = new ArrayList<>();
        soldOut.add(new FoodItem(1, "Veg Burger", 80, 0));

        CravingService service = new CravingService(
                new FoodService(soldOut), objectMapper,
                "test-key", "gemini-3.6-flash", "https://example.invalid");

        InvalidRequestException thrown = assertThrows(InvalidRequestException.class,
                () -> service.suggest("anything"));

        assertTrue(thrown.getMessage().contains("Nothing is in stock"));
    }

    @Test
    @DisplayName("with no API key the feature reports itself off and refuses to run")
    void disabledWithoutAKey() {
        CravingService service = new CravingService(
                foodService, objectMapper,
                "  ", "gemini-3.6-flash", "https://example.invalid");

        assertFalse(service.isEnabled());
        assertThrows(InvalidRequestException.class, () -> service.suggest("something spicy"));
    }

    @Test
    @DisplayName("a configured key switches the feature on")
    void enabledWithAKey() {
        assertTrue(cravingService.isEnabled());
    }
}
