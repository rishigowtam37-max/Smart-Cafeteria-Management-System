package com.flowbite.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Every request body the API accepts, in one file because each is two or three
 * fields and they are easier to read together than scattered.
 *
 * <p>The constraints here are the first gate: malformed input is rejected before
 * a service ever sees it. They do not replace the service's own rules - a
 * quantity can pass {@code @Min(1)} and still be refused for want of stock, and
 * that second check is the one that actually protects the counter.
 */
public final class Requests {

    private Requests() {
    }

    /** Sign-in. */
    public record LoginRequest(
            @NotBlank(message = "Role is required.") String role,
            @NotBlank(message = "Username is required.") String username,
            @NotBlank(message = "Password is required.") String password) {
    }

    /** Add units of a menu item to the cart. */
    public record AddToCartRequest(
            @NotNull(message = "Food item is required.") Integer foodId,
            @NotNull(message = "Invalid quantity.")
            @Min(value = 1, message = "Invalid quantity.") Integer quantity) {
    }

    /** Set a cart line to an absolute quantity; zero removes the line. */
    public record ChangeQuantityRequest(
            @NotNull(message = "Invalid quantity.")
            @PositiveOrZero(message = "Invalid quantity.") Integer quantity) {
    }

    /** Ask Smart Craving for suggestions. */
    public record CravingRequest(
            @NotBlank(message = "Tell us what you are in the mood for first.")
            @Size(max = 200, message = "That craving is a bit long - keep it under 200 characters.")
            String craving) {
    }

    /** Create or update a menu item (admin). */
    public record FoodItemRequest(
            @NotBlank(message = "Food name is required.")
            @Size(max = 60, message = "Food name is too long.") String name,

            @NotNull(message = "Price is required.")
            @Positive(message = "Price must be greater than zero.") Double price,

            @NotNull(message = "Quantity is required.")
            @PositiveOrZero(message = "Quantity cannot be negative.") Integer quantity,

            @NotBlank(message = "Category is required.") String category,

            @Size(max = 200, message = "Description is too long.") String description,

            @Size(max = 8, message = "Emoji is too long.") String emoji) {

        /** @return the description, never null. */
        public String descriptionOrEmpty() {
            return description == null ? "" : description;
        }

        /** @return the emoji, falling back to a neutral plate. */
        public String emojiOrDefault() {
            return emoji == null || emoji.isBlank() ? "🍽️" : emoji;
        }
    }
}
