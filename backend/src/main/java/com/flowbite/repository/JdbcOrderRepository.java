package com.flowbite.repository;

import com.flowbite.model.CartItem;
import com.flowbite.model.FoodItem;
import com.flowbite.model.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orders on disk, in H2, over plain JDBC.
 *
 * <p>No JPA: there are two tables and two statements, and an ORM would add a
 * mapping layer, a session lifecycle and lazy-loading semantics to a model that
 * is already a plain object graph. The cost is the hand-written row mapping
 * below, which is the entire class.
 *
 * <p>The database never assigns an order id. {@code OrderService} does, because
 * {@code FB-0001} is derived from it and the console app - which has no database
 * at all - must produce the same codes.
 */
@Repository
public class JdbcOrderRepository implements OrderRepository {

    private static final String INSERT_ORDER = """
            insert into orders (order_id, customer_name, placed_at)
            values (?, ?, ?)
            """;

    private static final String INSERT_ITEM = """
            insert into order_item
                (order_id, line_no, food_id, name, price, quantity, category, description, emoji)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    // A left join so an order with no lines would still come back rather than
    // vanishing. Checkout refuses an empty cart, so that should never happen -
    // but a row silently disappearing is the wrong way to find out it did.
    private static final String SELECT_ALL = """
            select o.order_id, o.customer_name, o.placed_at,
                   i.food_id, i.name, i.price, i.quantity,
                   i.category, i.description, i.emoji
            from orders o
            left join order_item i on i.order_id = o.order_id
            order by o.order_id, i.line_no
            """;

    private final JdbcTemplate jdbc;

    public JdbcOrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Writes the order and all of its lines in one transaction.
     *
     * <p>Without the transaction a failure partway through the lines would leave
     * a bill on disk that charges less than the customer was charged.
     */
    @Override
    @Transactional
    public void save(Order order) {

        jdbc.update(INSERT_ORDER,
                order.getOrderId(),
                order.getCustomerName(),
                OffsetDateTime.ofInstant(order.getPlacedAt(), ZoneOffset.UTC));

        List<CartItem> lines = order.getCartItems();

        for (int lineNo = 0; lineNo < lines.size(); lineNo++) {

            FoodItem item = lines.get(lineNo).getFoodItem();

            jdbc.update(INSERT_ITEM,
                    order.getOrderId(),
                    lineNo,
                    item.getId(),
                    item.getName(),
                    item.getPrice(),
                    lines.get(lineNo).getQuantity(),
                    item.getCategory(),
                    item.getDescription(),
                    item.getEmoji());
        }
    }

    @Override
    public List<Order> findAll() {

        // LinkedHashMap, not HashMap: the query is ordered by order_id and the
        // caller is promised oldest first.
        Map<Integer, Order> byId = jdbc.query(SELECT_ALL, rs -> {

            Map<Integer, Order> orders = new LinkedHashMap<>();

            while (rs.next()) {

                int orderId = rs.getInt("order_id");
                Order order = orders.get(orderId);

                if (order == null) {
                    order = new Order(orderId,
                            rs.getString("customer_name"),
                            readInstant(rs.getObject("placed_at", OffsetDateTime.class)));
                    orders.put(orderId, order);
                }

                // Null on the left-join miss described above: an order with no lines.
                if (rs.getObject("food_id") == null) {
                    continue;
                }

                order.addCartItem(new CartItem(readFoodItem(rs), rs.getInt("quantity")));
            }

            return orders;
        });

        return new ArrayList<>(byId.values());
    }

    private static Instant readInstant(OffsetDateTime stored) {
        return stored == null ? Instant.EPOCH : stored.toInstant();
    }

    /**
     * Rebuilds the snapshot of the menu item as it was when the order was placed.
     *
     * <p>Quantity is passed as {@code 0} and is the one field that does not round
     * trip. On a snapshot it means "units that were still available at the time",
     * which is a fact about a moment in the past that nothing reads: the bill
     * uses the <i>line</i> quantity, which is stored in its own column.
     */
    private static FoodItem readFoodItem(ResultSet rs) throws SQLException {
        return new FoodItem(
                rs.getInt("food_id"),
                rs.getString("name"),
                rs.getDouble("price"),
                0,
                rs.getString("category"),
                rs.getString("description"),
                rs.getString("emoji"));
    }
}
