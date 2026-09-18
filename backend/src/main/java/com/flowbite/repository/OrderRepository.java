package com.flowbite.repository;

import com.flowbite.model.Order;

import java.util.List;

/**
 * Durable storage for placed orders.
 *
 * <p>Two methods, because that is all {@link com.flowbite.service.OrderService}
 * does: it appends an order when one is placed, and reads everything back once
 * at startup. Searching, revenue and newest-first ordering are answered from
 * memory, so they are deliberately absent here.
 *
 * <p>The interface exists so the console app and the unit tests can run without
 * a database - see {@link InMemoryOrderRepository}.
 */
public interface OrderRepository {

    /**
     * Stores an order and every line of its bill.
     *
     * <p>Implementations must be all-or-nothing: an order with half its lines
     * written would come back as a bill charging less than the customer paid.
     *
     * @param order the order to store.
     */
    void save(Order order);

    /**
     * @return every stored order, <b>oldest first</b>, so that rebuilding a list
     *     from it preserves the sequence orders were placed in.
     */
    List<Order> findAll();
}
