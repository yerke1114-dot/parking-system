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
        ParkingUI.printLogo("NEXPO RESIDENT PARK");
        boolean running = true;
        while (running) {
            AuthUser user = authMenu();
            if (user == null) {
                running = false;
                continue;
            }

            if (user.getRole().equals("ADMIN")) {
                adminMenu();
            } else {
                userMenu(user);
            }
        }
        System.out.println("\n [!] System secured. Have a safe drive!");
    }


    private AuthUser authMenu() {
        while (true) {
            ParkingUI.printHeader("AUTHENTICATION GATEWAY");
            System.out.println(" [1] Login\n [2] Register\n [0] Shutdown");
            int choice = ParkingUI.readInt("Selection");

            if (choice == 0) return null;
            if (choice < 1 || choice > 2) continue;

            String username = ParkingUI.readString("Username");
            String password = ParkingUI.readString("Password");

            AuthUser authUser = (choice == 1)
                    ? auth.login(username, password)
                    : auth.register(username, password);

            if (authUser != null) {
                System.out.println("\n Access Granted. Welcome back!");
                return authUser;
            }
            System.out.println("Error: Invalid credentials or username taken.");
        }
    }

    private void userMenu(AuthUser user) {
        while (true) {
            ParkingUI.printHeader("RESIDENT DASHBOARD | ID: " + user.getUserId());
            System.out.println(" [1] View My Spots");
            System.out.println(" [2] Find Free Zones");
            System.out.println(" [3] Purchase Parking");
            System.out.println(" [4] Cancel Order");
            System.out.println(" [5] Extend Order");
            System.out.println(" [0] Logout");
            int option = ParkingUI.readInt("Action");
            if (option == 0) return;

            switch (option) {
                case 1 -> System.out.println(parkingRepo.getMyParking(user.getUserId()));
                case 2 -> System.out.println(parkingRepo.getFreeParking());
                case 3 -> buyParkingFlow(user.getUserId());
                case 4 -> cancelParkingFlow(user.getUserId());
                case 5 -> extendParkingFlow(user.getUserId());
                default -> System.out.println(" [!] Invalid choice.");
            }
        }
    }
    private void cancelParkingFlow(int userId) {
        ParkingUI.printHeader("CANCEL ORDER");
        int spot = ParkingUI.readInt("Enter Spot Number to release");

        System.out.println("Processing cancellation...");
        String result = parkingRepo.cancelOrder(userId, spot);
        System.out.println(result);
    }

    private void extendParkingFlow(int userId) {
        ParkingUI.printHeader("EXTEND LEASE");
        int spot = ParkingUI.readInt("Enter Spot Number to extend");
        int months = ParkingUI.readInt("How many extra months?");

        System.out.println("Updating lease term...");
        String result = parkingRepo.extendOrder(userId, spot, months);
        System.out.println(result);
    }

    private void buyParkingFlow(int userId) {
        ParkingUI.printHeader("SPOT ACQUISITION");
        System.out.println(parkingRepo.getFreeParking());

        int spot = ParkingUI.readInt("Enter Spot Number");
        String phone = ParkingUI.readString("Phone (11 digits)");
        String car = ParkingUI.readString("Car Number");

        int months = ParkingUI.readInt("Plan (0=Forever, 1, 3, 6 months)");
        int price = ParkingUI.calculatePrice(months);

        System.out.println("Final Price: " + price + "$");
        System.out.println("Finalizing transaction...");

        String result = parkingRepo.buyParking(userId, spot, phone, car, months);
        System.out.println(result);
    }

    private void adminMenu() {
        while (true) {
            ParkingUI.printHeader("ADMIN CONTROL PANEL");
            System.out.println(" [1] Full Parking Audit\n [0] Logout");
            int choice = ParkingUI.readInt("Execute");

            if (choice == 0) return;
            if (choice == 1) adminRepo.showAllParkingStatus();
        }
    }
    }


