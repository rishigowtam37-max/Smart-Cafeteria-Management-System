package com.flowbite.repository;

import com.flowbite.model.CartItem;
import com.flowbite.model.FoodItem;
import com.flowbite.model.Order;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The round trip: an order written to H2 and read back as the same bill.
 *
 * <p>Built against a real embedded database with the real {@code schema.sql}
 * rather than a mocked {@code JdbcTemplate} - a mock would happily accept SQL
 * that does not match the schema, which is most of what can go wrong here.
 *
 * <p>No Spring context: this needs a {@code DataSource} and nothing else.
 */
class JdbcOrderRepositoryTest {

    private EmbeddedDatabase database;
    private JdbcOrderRepository repository;

    @BeforeEach
    void setUp() {
        database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("schema.sql")
                .build();

        repository = new JdbcOrderRepository(new JdbcTemplate(database));
    }

    @AfterEach
    void tearDown() {
        database.shutdown();
    }

    /** An order with the given lines, as {@code OrderService} would build it. */
    private static Order orderOf(int id, String customer, CartItem... lines) {
        Order order = new Order(id, customer);
        for (CartItem line : lines) {
            order.addCartItem(line);
        }
        return order;
    }

    private static CartItem line(int foodId, String name, double price, int quantity) {
        return new CartItem(
                new FoodItem(foodId, name, price, 0, "Snacks", "A description", "🍔"),
                quantity);
    }

    @Test
    @DisplayName("an order comes back as the same bill")
    void roundTrip() {
        repository.save(orderOf(1, "Rishi", line(1, "Veg Burger", 80, 2)));

        List<Order> loaded = repository.findAll();

        assertEquals(1, loaded.size());

        Order order = loaded.get(0);
        assertEquals("FB-0001", order.getOrderCode());
        assertEquals("Rishi", order.getCustomerName());
        assertEquals(1, order.getCartItems().size());
        assertEquals(2, order.getItemCount());
        assertEquals(160.0, order.getTotalAmount(), 0.001);

        FoodItem item = order.getCartItems().get(0).getFoodItem();
        assertEquals("Veg Burger", item.getName());
        assertEquals("Snacks", item.getCategory());
        assertEquals("🍔", item.getEmoji());
    }

    @Test
    @DisplayName("the bill keeps the order its lines were rung up in")
    void lineOrderIsPreserved() {
        repository.save(orderOf(1, "Rishi",
                line(3, "Masala Dosa", 70, 1),
                line(1, "Veg Burger", 80, 1),
                line(2, "Pizza", 180, 1)));

        List<CartItem> lines = repository.findAll().get(0).getCartItems();

        assertEquals("Masala Dosa", lines.get(0).getFoodItem().getName());
        assertEquals("Veg Burger", lines.get(1).getFoodItem().getName());
        assertEquals("Pizza", lines.get(2).getFoodItem().getName());
    }

    @Test
    @DisplayName("placedAt survives the round trip, so a reloaded order is not stamped 'now'")
    void placedAtIsPreserved() {
        Instant lastWeek = Instant.now().minus(7, ChronoUnit.DAYS);

        Order order = new Order(1, "Rishi", lastWeek);
        order.addCartItem(line(1, "Veg Burger", 80, 1));
        repository.save(order);

        Instant loaded = repository.findAll().get(0).getPlacedAt();

        // Millisecond precision: the column is a timestamp, not a nanosecond clock.
        assertEquals(lastWeek.toEpochMilli(), loaded.toEpochMilli());
        assertTrue(loaded.isBefore(Instant.now().minus(6, ChronoUnit.DAYS)));
    }

    @Test
    @DisplayName("the stored price is the price charged, not whatever the menu says later")
    void storesTheChargedPrice() {
        FoodItem burger = new FoodItem(1, "Veg Burger", 80, 20, "Snacks", "", "🍔");

        repository.save(orderOf(1, "Rishi", new CartItem(new FoodItem(burger), 2)));

        // An admin doubles the price the next day.
        burger.setPrice(160);
        burger.setName("Veg Burger Deluxe");

        Order loaded = repository.findAll().get(0);

        assertEquals(80.0, loaded.getCartItems().get(0).getFoodItem().getPrice(), 0.001);
        assertEquals("Veg Burger", loaded.getCartItems().get(0).getFoodItem().getName());
        assertEquals(160.0, loaded.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("findAll returns orders oldest first")
    void oldestFirst() {
        repository.save(orderOf(1, "Rishi", line(1, "Veg Burger", 80, 1)));
        repository.save(orderOf(2, "Asha", line(2, "Pizza", 180, 1)));
        repository.save(orderOf(3, "Dev", line(3, "Masala Dosa", 70, 1)));

        List<Order> loaded = repository.findAll();

        assertEquals(List.of("FB-0001", "FB-0002", "FB-0003"),
                loaded.stream().map(Order::getOrderCode).toList());
    }

    @Test
    @DisplayName("an empty database yields an empty list, not a failure")
    void emptyDatabase() {
        assertTrue(repository.findAll().isEmpty());
    }
}
