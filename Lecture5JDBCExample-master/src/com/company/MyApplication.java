package com.company;

import com.company.controllers.interfaces.AuthorizationInterface;
import com.company.models.AuthUser;
import com.company.repositories.AdminRepository;
import com.company.repositories.interfaces.IParkingRepository;

import java.util.InputMismatchException;
import java.util.Scanner;

public class MyApplication {

    private final Scanner scanner = new Scanner(System.in);

    private final AuthorizationInterface auth;
    private final IParkingRepository parkingRepo;
    private final AdminRepository adminRepo;

    public MyApplication(AuthorizationInterface auth, IParkingRepository parkingRepo, AdminRepository adminRepo) {
        this.auth = auth;
        this.parkingRepo = parkingRepo;
        this.adminRepo = adminRepo;
    }

    public void start() {
        while (true) {
            AuthUser user = authMenu();
            if (user == null) return;

            if (user.getRole().equals("ADMIN")) {
                adminMenu();
            } else {
                userMenu(user.getUserId());
            }
        }
    }


    private AuthUser authMenu() {
        while (true) {
            System.out.println("\n1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            int choice;
            try {
                choice = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Input must be integer!");
                scanner.nextLine();
                continue;
            }

            if (choice == 0) return null;
            if (choice != 1 && choice != 2) {
                System.out.println("Choose 1, 2 or 0");
                continue;
            }

            System.out.print("Username: ");
            String username = scanner.next();

            System.out.print("Password: ");
            String password = scanner.next();

            AuthUser user = (choice == 1)
                    ? auth.login(username, password)
                    : auth.register(username, password);


        }
    }


    private void userMenu(int userId) {
        while (true) {
            System.out.println("\nWelcome to My Application!");
            System.out.println("Select option:");
            System.out.println("1. My parking zone");
            System.out.println("2. Free parking zones");
            System.out.println("3. Buy parking");
            System.out.println("0. Exit");
            System.out.print("Enter option: ");

            int option;
            try {
                option = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Input must be integer!");
                scanner.nextLine();
                continue;
            }

            if (option == 0) return;

            switch (option) {
                case 1 -> System.out.println(parkingRepo.getMyParking(userId));
                case 2 -> System.out.println(parkingRepo.getFreeParking());
                case 3 -> buyParkingFlow(userId);
                default -> System.out.println("Unknown option!");
            }
        }
    }

    private void buyParkingFlow(int userId) {
        System.out.println("\n--- FREE PARKING ZONES ---");
        System.out.println(parkingRepo.getFreeParking());

        System.out.print("Enter spot number: ");
        int spotNumber = scanner.nextInt();

        System.out.print("Owner phone (11 digits): ");
        String phone = scanner.next();

        System.out.print("Car number (8 chars): ");
        String car = scanner.next();

        int months = askMonths();

        int price = calcPrice(months);
        System.out.println("Selected plan: " + (months == 0 ? "FOREVER" : months + " month(s)") + " | Price: " + price + "$");

        System.out.println(parkingRepo.buyParking(userId, spotNumber, phone, car, months));
    }

    private int askMonths() {
        while (true) {
            System.out.print("Choose plan (0=Forever, 1 / 3 / 6 months): ");
            try {
                int months = scanner.nextInt();
                if (months == 0 || months == 1 || months == 3 || months == 6) return months;
                System.out.println("Only 0, 1, 3 or 6 allowed!");
            } catch (InputMismatchException e) {
                System.out.println("Plan must be integer!");
                scanner.nextLine();
            }
        }
    }


    private int calcPrice(int months) {
        if (months == 0) return 5000;
        if (months == 1) return 200;
        if (months == 3) return 550;
        return 1000;
    }

    private void adminMenu() {
        while (true) {
            System.out.println("\n=== ADMIN MENU ===");
            System.out.println("1. Show all parking zones");
            System.out.println("0. Logout");
            System.out.print("Choose: ");

            int choice;
            try {
                choice = scanner.nextInt();
            } catch (InputMismatchException e) {
                scanner.nextLine();
                continue;
            }

            if (choice == 0) {
                System.out.println("Admin logged out.");
                return;
            }

            if (choice == 1) {
                adminRepo.showAllParkingStatus();
            } else {
                System.out.println("Unknown option!");
            }
        }
    }
}

