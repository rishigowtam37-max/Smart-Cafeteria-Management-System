package com.flowbite.web;

import com.flowbite.error.NotFoundException;
import com.flowbite.model.FoodItem;
import com.flowbite.service.FoodService;
import com.flowbite.service.OrderService;
import com.flowbite.web.dto.AdminStatsDto;
import com.flowbite.web.dto.FoodItemDto;
import com.flowbite.web.dto.OrderDto;
import com.flowbite.web.dto.Requests.FoodItemRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Everything the admin screen does: the five options of the console admin menu.
 *
 * <p>Sitting under {@code /api/admin} is what makes these staff-only -
 * {@link RoleInterceptor} refuses the whole prefix to anyone signed in as a
 * customer, so the protection does not depend on the browser hiding a button.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminMenuController {

    private final FoodService foodService;
    private final OrderService orderService;

    public AdminMenuController(FoodService foodService, OrderService orderService) {
        this.foodService = foodService;
        this.orderService = orderService;
    }

    /** Add Food Item. The id is assigned here, so two items cannot collide. */
    @PostMapping("/menu")
    @ResponseStatus(HttpStatus.CREATED)
    public FoodItemDto create(@Valid @RequestBody FoodItemRequest request) {

        FoodItem created = foodService.createFood(
                request.name(),
                request.price(),
                request.quantity(),
                request.category(),
                request.descriptionOrEmpty(),
                request.emojiOrDefault());

        return FoodItemDto.from(created);
    }

    /**
     * Update Food Item.
     *
     * <p>Edits the existing object rather than replacing it, which is why a price
     * change is immediately visible in carts that already hold the item. Already
     * placed orders are unaffected - those hold snapshots.
     */
    @PutMapping("/menu/{id}")
    public FoodItemDto update(@PathVariable int id, @Valid @RequestBody FoodItemRequest request) {

        FoodItem updated = foodService.updateFood(
                id,
                request.name(),
                request.price(),
                request.quantity(),
                request.category(),
                request.descriptionOrEmpty(),
                request.emojiOrDefault());

        return FoodItemDto.from(updated);
    }

    /**
     * Delete Food Item.
     *
     * <p>Carts elsewhere cannot be reached from here - sessions are not
     * enumerable - so each one drops the line itself the next time its owner
     * touches it. See {@code CartService.reconcile()}.
     */
    @DeleteMapping("/menu/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {

        if (!foodService.deleteFood(id)) {
            throw new NotFoundException("Food item not found.");
        }

        return ResponseEntity.noContent().build();
    }

    /** View Orders, newest first. */
    @GetMapping("/orders")
    public List<OrderDto> orders() {
        return OrderDto.from(orderService.getOrdersNewestFirst());
    }

    /**
     * The dashboard tiles: menu size, sold-out count, orders placed and revenue.
     *
     * <p>Revenue comes from {@code OrderService.getTotalRevenue()} rather than
     * being summed in the browser, so it covers every order rather than only the
     * page the admin happens to be looking at.
     */
    @GetMapping("/stats")
    public AdminStatsDto stats() {
        return AdminStatsDto.from(
                foodService.getFoodList(),
                orderService.getOrders(),
                orderService.getTotalRevenue());
    }
}
