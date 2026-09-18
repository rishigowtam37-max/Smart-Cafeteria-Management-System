package com.flowbite.service;

import com.flowbite.error.InsufficientStockException;
import com.flowbite.model.FoodItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The reason {@code FoodService}'s methods are synchronized.
 *
 * <p>In the console app one person typed one command at a time, so "check there
 * is enough stock, then subtract it" could never be interrupted halfway. On a
 * server it can: two requests land on two threads, both read "1 left", and both
 * subtract. These tests fail without the lock.
 */
class FoodServiceConcurrencyTest {

    @Test
    @DisplayName("fifty threads race for one unit of stock and exactly one wins")
    void onlyOneThreadGetsTheLastUnit() throws InterruptedException {

        List<FoodItem> seed = new ArrayList<>();
        seed.add(new FoodItem(1, "Pizza", 180, 1));
        FoodService foodService = new FoodService(seed);
        FoodItem pizza = foodService.requireFood(1);

        int threads = 50;
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger refusals = new AtomicInteger();

        // A latch so every thread is already waiting, then released together -
        // that is what makes them actually collide rather than run in sequence.
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    foodService.reserveStock(pizza, 1);
                    successes.incrementAndGet();
                } catch (InsufficientStockException e) {
                    refusals.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished.countDown();
                }
            });
        }

        startGun.countDown();
        assertTrue(finished.await(10, TimeUnit.SECONDS), "threads did not finish in time");
        pool.shutdownNow();

        assertEquals(1, successes.get(), "exactly one customer should get the last Pizza");
        assertEquals(threads - 1, refusals.get());
        assertEquals(0, pizza.getQuantity(), "stock must never go negative");
    }

    @Test
    @DisplayName("concurrent reserve and release leave stock exactly where it started")
    void reserveAndReleaseBalanceOut() throws InterruptedException {

        List<FoodItem> seed = new ArrayList<>();
        seed.add(new FoodItem(1, "Coke", 40, 500));
        FoodService foodService = new FoodService(seed);
        FoodItem coke = foodService.requireFood(1);

        int threads = 40;
        int roundsPerThread = 25;
        CountDownLatch finished = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    for (int round = 0; round < roundsPerThread; round++) {
                        foodService.reserveStock(coke, 1);
                        foodService.releaseStock(coke, 1);
                    }
                } finally {
                    finished.countDown();
                }
            });
        }

        assertTrue(finished.await(10, TimeUnit.SECONDS), "threads did not finish in time");
        pool.shutdownNow();

        assertEquals(500, coke.getQuantity(), "lost updates would show up as a number below 500");
    }

    @Test
    @DisplayName("concurrent createFood never hands out a duplicate id")
    void concurrentCreateGivesUniqueIds() throws InterruptedException {

        FoodService foodService = new FoodService(new ArrayList<>());

        int threads = 30;
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            final int n = i;
            pool.submit(() -> {
                try {
                    startGun.await();
                    foodService.createFood("Item " + n, 10, 1, "Snacks", "", "🍽️");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished.countDown();
                }
            });
        }

        startGun.countDown();
        assertTrue(finished.await(10, TimeUnit.SECONDS), "threads did not finish in time");
        pool.shutdownNow();

        long distinctIds = foodService.getFoodList().stream()
                .map(FoodItem::getId)
                .distinct()
                .count();

        assertEquals(threads, foodService.getFoodList().size());
        assertEquals(threads, distinctIds, "two items were given the same id");
    }
}
