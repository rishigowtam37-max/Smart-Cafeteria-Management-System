package com.flowbite.console;

import java.util.Scanner;

public class InputHelper {

    private static final Scanner scanner = new Scanner(System.in);

    // Read Integer
    public static int readInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input! Enter a number: ");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        return value;
    }

    // Read Double
    public static double readDouble() {
        while (!scanner.hasNextDouble()) {
            System.out.print("Invalid input! Enter a valid number: ");
            scanner.next();
        }
        double value = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        return value;
    }

    // Read String
    public static String readString() {
        return scanner.nextLine();
    }

    // Close Scanner
    public static void closeScanner() {
        scanner.close();
    }
}