package menu;

public class Menu {

    // Main Menu
    public static void showMainMenu() {

        System.out.println("\n======================================");
        System.out.println(" SMART CAFETERIA MANAGEMENT SYSTEM");
        System.out.println("======================================");
        System.out.println("1. Admin Login");
        System.out.println("2. Customer Login");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
    }

    // Admin Menu
    public static void showAdminMenu() {

        System.out.println("\n========== ADMIN MENU ==========");
        System.out.println("1. View Food Menu");
        System.out.println("2. Add Food Item");
        System.out.println("3. Update Food Item");
        System.out.println("4. Delete Food Item");
        System.out.println("5. View Orders");
        System.out.println("6. Logout");
        System.out.print("Enter your choice: ");
    }

    // Customer Menu
    public static void showCustomerMenu() {

        System.out.println("\n========= CUSTOMER MENU =========");
        System.out.println("1. View Food Menu");
        System.out.println("2. Add Food to Cart");
        System.out.println("3. View Cart");
        System.out.println("4. Remove Item from Cart");
        System.out.println("5. Place Order");
        System.out.println("6. Logout");
        System.out.print("Enter your choice: ");
    }
}