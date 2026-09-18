package com.flowbite.console;

import com.flowbite.error.CafeteriaException;
import com.flowbite.model.Admin;
import com.flowbite.model.Customer;
import com.flowbite.model.FoodItem;
import com.flowbite.model.Order;
import com.flowbite.service.CartService;
import com.flowbite.service.FoodService;
import com.flowbite.service.LoginService;
import com.flowbite.service.MenuLoader;
import com.flowbite.repository.InMemoryOrderRepository;
import com.flowbite.service.OrderService;
import tools.jackson.databind.ObjectMapper;

/**
 * The original console front end, still runnable:
 *
 * <pre>./mvnw compile exec:java -Dexec.mainClass=com.flowbite.console.ConsoleApp</pre>
 *
 * <p>It drives exactly the same service objects the web application does. The
 * services no longer print when they refuse something - they throw - so each
 * call site catches {@link CafeteriaException} and prints the message, which is
 * the same line this app has always shown.
 */
public class ConsoleApp {

    public static void main(String[] args) {

        LoginService loginService = new LoginService();
        FoodService foodService = new FoodService(new MenuLoader(new ObjectMapper()).loadSeedMenu());
        CartService cartService = new CartService(foodService);
        // In memory, not the web app's database: this is a single-user terminal
        // program that has never remembered orders between runs, and pointing it
        // at the same H2 file would mean two processes contending for it.
        OrderService orderService = new OrderService(new InMemoryOrderRepository());

        boolean running = true;

        while (running) {

            Menu.showMainMenu();
            int choice = InputHelper.readInt();

            switch (choice) {

                case 1:

                    System.out.print("Username: ");
                    String adminUser = InputHelper.readString();

                    System.out.print("Password: ");
                    String adminPass = InputHelper.readString();

                    Admin admin = loginService.adminLogin(adminUser, adminPass);

                    if (admin == null) {
                        System.out.println("Invalid Admin Credentials.");
                        break;
                    }

                    admin.displayRole();

                    boolean adminMenu = true;

                    while (adminMenu) {

                        Menu.showAdminMenu();
                        int adminChoice = InputHelper.readInt();

                        switch (adminChoice) {

                            case 1:
                                foodService.displayMenu();
                                break;

                            case 2:

                                System.out.print("Food Name: ");
                                String name = InputHelper.readString();

                                System.out.print("Price: ");
                                double price = InputHelper.readDouble();

                                System.out.print("Quantity: ");
                                int quantity = InputHelper.readInt();

                                try {
                                    // The id is assigned by the service now, so two
                                    // items can no longer be given the same one.
                                    FoodItem created = foodService.createFood(
                                            name, price, quantity, "Snacks", "", "🍽️");
                                    System.out.println(
                                            "Food item added successfully. (ID: " + created.getId() + ")");
                                } catch (CafeteriaException e) {
                                    System.out.println(e.getMessage());
                                }

                                break;

                            case 3:

                                System.out.print("Enter Food ID: ");
                                int updateId = InputHelper.readInt();

                                System.out.print("New Name: ");
                                String newName = InputHelper.readString();

                                System.out.print("New Price: ");
                                double newPrice = InputHelper.readDouble();

                                System.out.print("New Quantity: ");
                                int newQty = InputHelper.readInt();

                                if (foodService.updateFood(updateId, newName, newPrice, newQty))
                                    System.out.println("Food Updated Successfully.");
                                else
                                    System.out.println("Food Not Found.");

                                break;

                            case 4:

                                System.out.print("Enter Food ID to Delete: ");
                                int deleteId = InputHelper.readInt();

                                if (foodService.deleteFood(deleteId))
                                    System.out.println("Food Deleted Successfully.");
                                else
                                    System.out.println("Food Not Found.");

                                break;

                            case 5:

                                orderService.displayOrders();
                                break;

                            case 6:

                                adminMenu = false;
                                break;

                            default:

                                System.out.println("Invalid Choice.");
                        }
                    }

                    break;

                case 2:

                    System.out.print("Username: ");
                    String customerUser = InputHelper.readString();

                    System.out.print("Password: ");
                    String customerPass = InputHelper.readString();

                    Customer customer =
                            loginService.customerLogin(customerUser, customerPass);

                    if (customer == null) {
                        System.out.println("Invalid Customer Credentials.");
                        break;
                    }

                    customer.displayRole();

                    boolean customerMenu = true;

                    while (customerMenu) {

                        Menu.showCustomerMenu();

                        int customerChoice = InputHelper.readInt();

                        switch (customerChoice) {

                            case 1:

                                foodService.displayMenu();
                                break;

                            case 2:

                                System.out.print("Enter Food ID: ");
                                int foodId = InputHelper.readInt();

                                FoodItem food =
                                        foodService.searchFood(foodId);

                                if (food == null) {
                                    System.out.println("Food Not Found.");
                                    break;
                                }

                                System.out.print("Quantity: ");
                                int qty = InputHelper.readInt();

                                try {
                                    cartService.addToCart(food, qty);
                                    System.out.println("Item added to cart successfully.");
                                } catch (CafeteriaException e) {
                                    System.out.println(e.getMessage());
                                }

                                break;

                            case 3:

                                cartService.viewCart();
                                break;

                            case 4:

                                System.out.print("Enter Food ID to Remove: ");
                                int removeId = InputHelper.readInt();

                                if (cartService.removeFromCart(removeId))
                                    System.out.println("Item removed from cart.");
                                else
                                    System.out.println("Food item not found.");

                                break;

                            case 5:

                                try {
                                    Order order = orderService.placeOrder(
                                            customer.getName(),
                                            cartService);

                                    System.out.println("Order placed successfully!");
                                    System.out.println(order);
                                } catch (CafeteriaException e) {
                                    System.out.println(e.getMessage());
                                }

                                break;

                            case 6:

                                // Logging out abandons the cart, so the reserved
                                // stock has to go back on the counter.
                                cartService.clearCart();
                                customerMenu = false;
                                break;

                            default:

                                System.out.println("Invalid Choice.");
                        }
                    }

                    break;

                case 3:

                    running = false;
                    System.out.println("Thank you for using Smart Cafeteria Management System!");
                    break;

                default:

                    System.out.println("Invalid Choice.");
            }
        }

        InputHelper.closeScanner();
    }
}
