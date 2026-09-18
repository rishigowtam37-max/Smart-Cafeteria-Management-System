package com.flowbite.web;

import com.flowbite.error.NotFoundException;
import com.flowbite.model.FoodItem;
import com.flowbite.service.CartService;
import com.flowbite.service.FoodService;
import com.flowbite.web.dto.CartDto;
import com.flowbite.web.dto.Requests.AddToCartRequest;
import com.flowbite.web.dto.Requests.ChangeQuantityRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The customer's cart, one per session.
 *
 * <p>Every method returns the whole cart rather than just what changed, so the
 * browser can replace its state wholesale and never has to keep a running total
 * of its own. That is what makes the React side a view: it renders
 * {@link CartDto} and nothing more.
 *
 * <p>Adding to the cart reserves stock immediately, so a refusal here is a real
 * 409 from the counter, not a disabled button.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final FoodService foodService;

    public CartController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping
    public CartDto cart(HttpSession session) {
        return CartDto.from(cart(session, foodService));
    }

    /** Add units of an item, reserving the stock. 409 if there is not enough. */
    @PostMapping
    public CartDto add(@Valid @RequestBody AddToCartRequest request, HttpSession session) {

        CartService cart = cart(session, foodService);
        FoodItem food = foodService.requireFood(request.foodId());

        cart.addToCart(food, request.quantity());

        return CartDto.from(cart);
    }

    /** Set a line to an absolute quantity. Zero removes it. */
    @PatchMapping("/{foodId}")
    public CartDto changeQuantity(@PathVariable int foodId,
                                  @Valid @RequestBody ChangeQuantityRequest request,
                                  HttpSession session) {

        CartService cart = cart(session, foodService);

        cart.changeQuantity(foodId, request.quantity());

        return CartDto.from(cart);
    }

    /** Remove a line, returning its units to the counter. */
    @DeleteMapping("/{foodId}")
    public CartDto remove(@PathVariable int foodId, HttpSession session) {

        CartService cart = cart(session, foodService);

        if (!cart.removeFromCart(foodId)) {
            throw new NotFoundException("Food item not found.");
        }

        return CartDto.from(cart);
    }

    /** Empty the cart, returning everything. */
    @DeleteMapping
    public CartDto clear(HttpSession session) {

        CartService cart = cart(session, foodService);
        cart.clearCart();

        return CartDto.from(cart);
    }

    private static CartService cart(HttpSession session, FoodService foodService) {
        return SessionKeys.cart(session, foodService);
    }
}
