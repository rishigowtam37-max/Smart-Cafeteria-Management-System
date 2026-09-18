package com.flowbite.web;

import com.flowbite.model.User;
import com.flowbite.service.FoodService;
import com.flowbite.service.OrderService;
import com.flowbite.web.dto.OrderDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Checkout.
 *
 * <p>The customer name comes from the session, never from the request body - a
 * browser does not get to say who it is ordering as.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final FoodService foodService;

    public OrderController(OrderService orderService, FoodService foodService) {
        this.orderService = orderService;
        this.foodService = foodService;
    }

    /**
     * Turns the session's cart into an order.
     *
     * @return the placed order, including its {@code FB-0001} code.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto place(HttpSession session) {

        User customer = SessionKeys.requireUser(session);

        return OrderDto.from(orderService.placeOrder(
                customer.getName(),
                SessionKeys.cart(session, foodService)));
    }
}
