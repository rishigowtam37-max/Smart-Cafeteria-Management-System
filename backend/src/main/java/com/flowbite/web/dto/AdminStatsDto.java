package com.flowbite.web.dto;

import com.flowbite.model.FoodItem;
import com.flowbite.model.Order;

import java.util.List;

/**
 * The four numbers on the admin dashboard.
 *
 * <p>These used to be worked out in the browser - revenue in particular was a
 * {@code reduce} over the order list in AdminView.jsx. Summing money is the
 * server's job: the browser only ever sees the orders it happens to have
 * fetched, while {@code OrderService} knows about all of them.
 */
public record AdminStatsDto(
        int itemsOnMenu,
        int soldOut,
        int ordersPlaced,
        double revenue) {

    public static AdminStatsDto from(List<FoodItem> menu, List<Order> orders, double revenue) {
        int soldOut = (int) menu.stream().filter(item -> !item.isAvailable()).count();

        return new AdminStatsDto(menu.size(), soldOut, orders.size(), revenue);
    }
}
