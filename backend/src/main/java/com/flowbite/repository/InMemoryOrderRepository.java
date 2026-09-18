package com.flowbite.repository;

import com.flowbite.model.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link OrderRepository} that forgets everything when the process ends.
 *
 * <p>This is what the console app uses: it is a single-user program run from a
 * terminal, and giving it a database file would mean opening one from
 * {@code main} outside any Spring context, plus a second process contending for
 * the same file while the web app is running. The console app has never
 * remembered orders between runs, and it still does not.
 *
 * <p>The unit tests use it for the same reason - they are testing checkout
 * rules, not SQL.
 *
 * <p>Not a Spring bean. The web application always gets
 * {@link JdbcOrderRepository}; if this were annotated, the two would compete.
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final List<Order> orders = new ArrayList<>();

    @Override
    public synchronized void save(Order order) {
        orders.add(order);
    }

    @Override
    public synchronized List<Order> findAll() {
        return new ArrayList<>(orders);
    }
}
