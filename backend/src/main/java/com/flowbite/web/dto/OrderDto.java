package com.flowbite.web.dto;

import com.flowbite.model.CartItem;
import com.flowbite.model.Order;

import java.util.List;

/**
 * A placed order as the browser sees it.
 *
 * <p>{@code placedAt} is epoch milliseconds so the existing JavaScript
 * {@code formatOrderTime} helper keeps working unchanged, and {@code orderCode}
 * is the {@code FB-0001} string - generated in {@link Order}, not in the browser.
 */
public record OrderDto(
        int orderId,
        String orderCode,
        String customerName,
        long placedAt,
        List<OrderLineDto> items,
        double totalAmount,
        int itemCount) {

    /** One line of a placed order - a snapshot, frozen at the price charged. */
    public record OrderLineDto(
            int foodId,
            String name,
            double price,
            int quantity,
            double lineTotal,
            String emoji) {

        static OrderLineDto from(CartItem line) {
            return new OrderLineDto(
                    line.getFoodItem().getId(),
                    line.getFoodItem().getName(),
                    line.getFoodItem().getPrice(),
                    line.getQuantity(),
                    line.getTotalPrice(),
                    line.getFoodItem().getEmoji());
        }
    }

    public static OrderDto from(Order order) {
        return new OrderDto(
                order.getOrderId(),
                order.getOrderCode(),
                order.getCustomerName(),
                order.getPlacedAt().toEpochMilli(),
                order.getCartItems().stream().map(OrderLineDto::from).toList(),
                order.getTotalAmount(),
                order.getItemCount());
    }

    public static List<OrderDto> from(List<Order> orders) {
        return orders.stream().map(OrderDto::from).toList();
    }
}
