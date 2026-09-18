package com.flowbite.web.dto;

import com.flowbite.model.FoodItem;

import java.util.List;

/**
 * A menu item as the browser sees it.
 *
 * <p>Separate from {@link FoodItem} on purpose: the model is free to change
 * shape without silently changing the API, and {@code available} is computed
 * here rather than being a field anyone could set inconsistently.
 *
 * @param quantity units still available to order (see {@link FoodItem}).
 */
public record FoodItemDto(
        int id,
        String name,
        double price,
        int quantity,
        String category,
        String description,
        String emoji,
        boolean available) {

    public static FoodItemDto from(FoodItem item) {
        return new FoodItemDto(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getQuantity(),
                item.getCategory(),
                item.getDescription(),
                item.getEmoji(),
                item.isAvailable());
    }

    public static List<FoodItemDto> from(List<FoodItem> items) {
        return items.stream().map(FoodItemDto::from).toList();
    }
}
