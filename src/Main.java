import menu.Menu;
import model.Admin;
import model.Customer;
import model.FoodItem;
import model.Order;
import service.CartService;
import service.FoodService;
import service.LoginService;
import service.OrderService;
import util.InputHelper;

public class Main {

    public static void main(String[] args) {

        LoginService loginService = new LoginService();
        FoodService foodService = new FoodService();
        CartService cartService = new CartService();
        OrderService orderService = new OrderService();

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

                                System.out.print("Food ID: ");
                                int id = InputHelper.readInt();

                                System.out.print("Food Name: ");
                                String name = InputHelper.readString();

                                System.out.print("Price: ");
                                double price = InputHelper.readDouble();

                                System.out.print("Quantity: ");
                                int quantity = InputHelper.readInt();

                                foodService.addFood(
                                        new FoodItem(id, name, price, quantity));

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

                                cartService.addToCart(food, qty);

                                break;

                            case 3:

                                cartService.viewCart();
                                break;

                            case 4:

                                Order order = orderService.placeOrder(
                                        customer.getName(),
                                        cartService);

                                if (order != null)
                                    System.out.println(order);
                                else
                                    System.out.println("Cart is Empty.");

                                break;
                                                        case 5:

                                System.out.print("Enter Food ID to Remove: ");
                                int removeId = InputHelper.readInt();

                                cartService.removeFromCart(removeId);

                                break;

                            case 6:

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
