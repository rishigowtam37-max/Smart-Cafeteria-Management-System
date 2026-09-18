-- FlowBite order storage.
--
-- Orders are the only state that survives a restart. The menu is reloaded from
-- data/menu.json every boot and carts live in the HTTP session, so neither has
-- a table here.
--
-- "if not exists" because this script runs on every start (see
-- spring.sql.init.mode in application.properties) against a database file that
-- is already there from last time.

create table if not exists orders (
    order_id      int primary key,             -- assigned in Java, not by the DB
    customer_name varchar(100) not null,
    placed_at     timestamp with time zone not null
);

-- One row per line of the bill. Every column is a snapshot of what was actually
-- charged: an admin editing or deleting the menu item afterwards must not change
-- a placed order, which is the rule CartItem's copy constructor enforces in
-- memory and this table enforces on disk.
create table if not exists order_item (
    order_id    int          not null references orders(order_id),
    line_no     int          not null,   -- keeps the bill in the order it was rung up
    food_id     int          not null,
    name        varchar(100) not null,
    price       double       not null,   -- the price charged, not today's price
    quantity    int          not null,
    category    varchar(50),
    description varchar(255),
    emoji       varchar(16),
    primary key (order_id, line_no)
);
