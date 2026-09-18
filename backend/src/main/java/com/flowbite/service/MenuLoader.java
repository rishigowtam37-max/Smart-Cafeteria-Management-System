package com.flowbite.service;

import com.flowbite.model.FoodItem;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the seed menu from {@code resources/data/menu.json}.
 *
 * <p>The console app hard-coded five items in {@code FoodService}'s
 * constructor. Keeping the menu in a JSON file instead means it can be edited
 * without recompiling, and it is the same file the web app used to fetch - the
 * five original items keep their exact ids, names, prices and quantities.
 *
 * <p>The JSON is mapped by hand rather than by binding Jackson directly to
 * {@link FoodItem}. That keeps the model free of a no-argument constructor and
 * annotations it would only need to satisfy a library.
 */
@Component
public class MenuLoader {

    private static final String MENU_RESOURCE = "data/menu.json";

    private final ObjectMapper objectMapper;

    public MenuLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Loads the seed menu.
     *
     * @return the food items described by menu.json, in file order.
     * @throws IllegalStateException if the file is missing or malformed - the
     *     application cannot serve a cafeteria with no menu, so this is fatal
     *     at startup rather than a per-request failure.
     */
    public List<FoodItem> loadSeedMenu() {
        try (InputStream stream = new ClassPathResource(MENU_RESOURCE).getInputStream()) {

            JsonNode root = objectMapper.readTree(stream);

            if (!root.isArray()) {
                throw new IllegalStateException(MENU_RESOURCE + " must contain an array of food items.");
            }

            List<FoodItem> items = new ArrayList<>();

            for (JsonNode node : root) {
                items.add(new FoodItem(
                        node.path("id").asInt(),
                        node.path("name").asText(),
                        node.path("price").asDouble(),
                        node.path("quantity").asInt(),
                        node.path("category").asString("Snacks"),
                        node.path("description").asString(""),
                        node.path("emoji").asString("🍽️")));
            }

            return items;

        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + MENU_RESOURCE, e);
        }
    }
}
