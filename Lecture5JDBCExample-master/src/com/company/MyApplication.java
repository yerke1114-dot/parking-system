package com.company;

import com.company.controllers.interfaces.AuthorizationInterface;
import com.company.repositories.interfaces.IParkingRepository;

import java.util.InputMismatchException;
import java.util.Scanner;

public class MyApplication {

    private final Scanner scanner = new Scanner(System.in);

    private final AuthorizationInterface auth;
    private final IParkingRepository parkingRepo;

    public MyApplication(AuthorizationInterface auth, IParkingRepository parkingRepo) {
        this.auth = auth;
        this.parkingRepo = parkingRepo;
    }

    public void start() {
        Integer userId = authMenu();
        if (userId == null) return;

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
                case 1:
                    System.out.println(parkingRepo.getMyParking(userId));
                    break;

                case 2:
                    System.out.println(parkingRepo.getFreeParking());
                    break;

                case 3:
                    System.out.println(parkingRepo.getFreeParking());

                    System.out.print("Enter spot number: ");
                    int spotNumber = scanner.nextInt();

                    System.out.print("Owner phone (11 digits): ");
                    String phone = scanner.next();

                    System.out.print("Car number (8 chars): ");
                    String car = scanner.next();

                    System.out.println(parkingRepo.buyParking(userId, spotNumber, phone, car));
                    break;

                default:
                    System.out.println("Unknown option!");
            }
        }
    }

    private Integer authMenu() {
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

            Integer userId = (choice == 1)
                    ? auth.login(username, password)
                    : auth.register(username, password);

            if (userId != null) return userId;

            if (choice == 1) System.out.println("Wrong username or password!");
            else System.out.println("Registration failed (maybe username already exists).");
        }
    }
}