package com.flowbite.web;

import com.flowbite.service.FoodService;
import com.flowbite.web.dto.FoodItemDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The menu, as both roles see it.
 *
 * <p>Read-only and open to anyone signed in. Changing the menu lives in
 * {@link AdminMenuController} under {@code /api/admin}, which is the path
 * {@link RoleInterceptor} guards.
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final FoodService foodService;

    public MenuController(FoodService foodService) {
        this.foodService = foodService;
    }

    /**
     * @return every menu item, including sold-out ones - the customer screen
     *     shows those greyed out rather than hiding them.
     */
    @GetMapping
    public List<FoodItemDto> menu() {
        return FoodItemDto.from(foodService.getFoodList());
    }
}
