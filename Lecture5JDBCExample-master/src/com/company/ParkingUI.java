package com.company;

import java.util.Scanner;

public class ParkingUI {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String LINE = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";

    public static void printLogo(String name) {
        System.out.println("\n" + LINE);
        System.out.println("      Welcome to " + name);
        System.out.println(LINE);
    }

    public static void printHeader(String title) {
        System.out.println("\n╔" + "═".repeat(40) + "╗");
        System.out.println("║ " + String.format("%-38s", title) + " ║");
        System.out.println("╚" + "═".repeat(40) + "╝");
    }

    public static int readInt(String label) {
        System.out.print(" ❯ " + label + ": ");
        try {
            return Integer.parseInt(scanner.next());
        } catch (NumberFormatException e) {
            System.out.println(" [!] Error: Please enter a valid integer.");
            return -1;
        }
    }

    public static String readString(String label) {
        System.out.print(" ❯ " + label + ": ");
        return scanner.next();
    }

    public static int calculatePrice(int months) {
        return switch (months) {
            case 0 -> 5000;
            case 1 -> 200;
            case 3 -> 550;
            case 6 -> 1000;
            default -> 0;
        };
    }
}