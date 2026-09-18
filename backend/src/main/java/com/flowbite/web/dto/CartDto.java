package com.flowbite.web.dto;

import com.flowbite.model.CartItem;
import com.flowbite.service.CartService;

import java.util.List;

/**
 * A whole cart, including the totals.
 *
 * <p>Every cart endpoint returns this - not just the part that changed - so the
 * browser never has to recompute a total or guess at the new state. The maths
 * comes from {@link CartService} and {@link CartItem}, the same methods the
 * console app used.
 *
 * @param notice set when {@code reconcile()} dropped a line because an admin
 *     deleted the item; null the rest of the time, and omitted from the JSON.
 */
public record CartDto(
        List<CartLineDto> lines,
        double total,
        int itemCount,
        String notice) {

    /**
     * One cart line.
     *
     * @param availableQuantity units still on the counter <i>beyond</i> the ones
     *     already held in this line, so the browser knows when to stop the
     *     quantity stepper without doing arithmetic of its own.
     */
    public record CartLineDto(
            int foodId,
            String name,
            double price,
            int quantity,
            double lineTotal,
            String emoji,
            String category,
            int availableQuantity) {

        static CartLineDto from(CartItem line) {
            return new CartLineDto(
                    line.getFoodItem().getId(),
                    line.getFoodItem().getName(),
                    line.getFoodItem().getPrice(),
                    line.getQuantity(),
                    line.getTotalPrice(),
                    line.getFoodItem().getEmoji(),
                    line.getFoodItem().getCategory(),
                    line.getFoodItem().getQuantity());
        }
    }

    /**
     * Snapshots a cart for the wire, consuming any pending notice.
     *
     * @param cartService the session's cart.
     * @return the cart as the browser should see it.
     */
    public static CartDto from(CartService cartService) {
        List<CartLineDto> lines = cartService.getCart().stream()
                .map(CartLineDto::from)
                .toList();

        return new CartDto(
                lines,
                cartService.calculateTotal(),
                cartService.getItemCount(),
                cartService.consumeNotice());
    }
}
