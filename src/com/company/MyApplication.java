package com.company;

import com.company.controllers.AdminController;
import com.company.controllers.ParkingController;
import com.company.repositories.interfaces.AuthorizationInterface;
import com.company.models.AuthUser;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.company.ParkingUI.scanner;

public class MyApplication {

    private final AuthorizationInterface auth;
    private final ParkingController controller;
    private final AdminController adminController;

    public MyApplication(AuthorizationInterface auth,
                         ParkingController controller,
                         AdminController adminController) {
        this.auth = auth;
        this.controller = controller;
        this.adminController = adminController;
    }

    public void start() {
        ParkingUI.printLogo("NEXPO RESIDENT PARK");

        while (true) {
            AuthUser user = authMenu();
            if (user == null) {
                System.out.println("\n[!] System secured. Bye!");
                return;
            }

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                adminMenu(user);
            } else {
                userMenu(user);
            }
        }
    }

    private AuthUser authMenu() {
        while (true) {
            ParkingUI.printHeader("AUTHENTICATION GATEWAY");
            System.out.println(" [1] Login");
            System.out.println(" [2] Register");
            System.out.println(" [0] Exit");

            int choice = ParkingUI.readInt("Selection");
            if (choice == 0) return null;
            if (choice != 1 && choice != 2) {
                System.out.println(" [!] Choose 1 / 2 / 0");
                continue;
            }

            String username = ParkingUI.readString("Username");
            String password = ParkingUI.readString("Password");

            AuthUser user = (choice == 1)
                    ? auth.login(username, password)
                    : auth.register(username, password);

            if (user != null) {
                System.out.println("\n Access Granted. Welcome, " + user.getUsername() + "!");
                return user;
            }

            System.out.println("Invalid credentials or username taken.");
        }
    }

    private void userMenu(AuthUser user) {
        while (true) {
            ParkingUI.printHeader("RESIDENT DASHBOARD | ID: " + user.getUserId());

            Map<Integer, Runnable> actions = new LinkedHashMap<>();
            actions.put(1, () -> System.out.println(controller.getMyParking(user.getUserId())));
            actions.put(2, () -> System.out.println(controller.getFreeParking()));
            actions.put(3, () -> buyFlow(user.getUserId()));
            actions.put(4, () -> cancelFlow(user.getUserId()));
            actions.put(5, () -> extendFlow(user.getUserId()));
            actions.put(6, () -> walletFlow(user.getUserId()));
            actions.put(0, () -> { /* logout */ });

            System.out.println(" [1] My parking zones");
            System.out.println(" [2] Free parking zones");
            System.out.println(" [3] Buy / Rent parking");
            System.out.println(" [4] Cancel order");
            System.out.println(" [5] Extend order");
            System.out.println(" [6] Wallet");
            System.out.println(" [0] Logout");

            int option = ParkingUI.readInt("Action");
            if (option == 0) {
                System.out.println("Logged out.\n");
                return;
            }

            Runnable action = actions.get(option);
            if (action == null) {
                System.out.println(" [!] Invalid option.");
                continue;
            }
            action.run();
        }
    }

    private void buyFlow(int userId) {
        ParkingUI.printHeader("BUY / RENT PARKING");
        System.out.println(controller.getFreeParking());

        int spot = ParkingUI.readInt("Spot number");
        String phone = ParkingUI.readString("Owner phone (11 digits)");
        String car = ParkingUI.readString("Car number (8 chars)");

        System.out.println("\nChoose plan:");
        System.out.println(" [0] Forever (5000$)");
        System.out.println(" [1] 1 month (200$)");
        System.out.println(" [3] 3 months (550$)");
        System.out.println(" [6] 6 months (1000$)");
        int months = ParkingUI.readInt("Plan");

        int price = ParkingUI.calculatePrice(months);
        if (price == 0) {
            System.out.println("Plan must be 0, 1, 3 or 6.");
            return;
        }

        double bal = controller.getBalance(userId);
        System.out.println("Your balance: " + bal + "$");
        System.out.println("Price: " + price + "$");

        String confirm = ParkingUI.readString("Confirm payment? (y/n)");
        if (!"y".equalsIgnoreCase(confirm)) {
            System.out.println("Cancelled.");
            return;
        }

        if (bal < price) {
            System.out.println("Not enough balance.");
            return;
        }

        if (!controller.updateBalance(userId, -price)) {
            System.out.println("Payment error (balance not updated).");
            return;
        }

        String res = controller.buyParking(userId, spot, phone, car, months);
        System.out.println(res);
    }

    private void cancelFlow(int userId) {
        ParkingUI.printHeader("CANCEL ORDER");
        System.out.println(controller.getMyParking(userId));

        int spot = ParkingUI.readInt("Spot number to cancel");
        String confirm = ParkingUI.readString("Confirm cancel? (y/n)");
        if (!"y".equalsIgnoreCase(confirm)) {
            System.out.println("Cancelled.");
            return;
        }

        System.out.println(controller.cancelOrder(userId, spot));
    }

    private void extendFlow(int userId) {
        ParkingUI.printHeader("EXTEND ORDER");

        String myOrders = controller.getMyParking(userId);
        System.out.println(myOrders);

        if (myOrders.contains("No active parking.") || myOrders.equals("None")) {
            return;
        }

        int spot = ParkingUI.readInt("Spot number to extend");

        String[] lines = myOrders.split("\n");
        boolean isPermanent = false;
        boolean foundSpot = false;

        for (String line : lines) {
            if (line.contains("Spot: " + spot)) {
                foundSpot = true;
                if (line.contains("Forever")) {
                    isPermanent = true;
                }
                break;
            }
        }

        if (!foundSpot) {
            System.out.println("Error: Spot #" + spot + " is not your active parking!");
            return;
        }

        if (isPermanent) {
            System.out.println("\n[!] ACCESS DENIED: This spot is already owned PERMANENTLY.");
            System.out.println("[!] No extension is needed or allowed for this spot.");
            return;
        }

        System.out.println("\nExtend plan:");
        System.out.println(" [1] +1 month (200$)");
        System.out.println(" [3] +3 months (550$)");
        System.out.println(" [6] +6 months (1000$)");

        int extraMonths = ParkingUI.readInt("Months");
        int price = ParkingUI.calculatePrice(extraMonths);

        if (price == 0 || extraMonths == 0) {
            System.out.println("Invalid plan.");
            return;
        }

        double bal = controller.getBalance(userId);
        System.out.println("Your balance: " + bal + "$");
        System.out.println("Price: " + price + "$");

        if (bal < price) {
            System.out.println("Not enough balance!");
            return;
        }

        String confirm = ParkingUI.readString("Confirm payment? (y/n)");
        if (!"y".equalsIgnoreCase(confirm)) {
            System.out.println("Cancelled.");
            return;
        }

        if (controller.updateBalance(userId, -price)) {
            String res = controller.extendOrder(userId, spot, extraMonths);
            System.out.println(res);
        } else {
            System.out.println("Payment failed.");
        }
    }

    private void walletFlow(int userId) {
        ParkingUI.printHeader("WALLET");

        double bal = controller.getBalance(userId);
        System.out.println("Current balance: " + bal + "$");
        System.out.println(" [1] Top up");
        System.out.println(" [0] Back");

        int op = ParkingUI.readInt("Selection");
        if (op == 0) return;
        if (op != 1) {
            System.out.println(" [!] Invalid option.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(ParkingUI.readString("Amount"));
        } catch (NumberFormatException e) {
            System.out.println("Amount must be number.");
            return;
        }

        if (amount <= 0) {
            System.out.println("Amount must be > 0.");
            return;
        }

        boolean ok = controller.updateBalance(userId, amount);
        if (!ok) {
            System.out.println("Top up failed.");
            return;
        }

        System.out.println("New balance: " + controller.getBalance(userId) + "$");
    }

    private void adminMenu(AuthUser user) {
        while (true) {
            try {
                System.out.println("\n==========================================");
                System.out.println("       ADMIN CONTROL PANEL");
                System.out.println("==========================================");
                System.out.println("01. Show Strategic Dashboard");
                System.out.println("02. Manual Balance Top-up");
                System.out.println("00. Logout");
                System.out.println("------------------------------------------");
                System.out.print("Execute Action: ");

                String input = scanner.next();
                int action = Integer.parseInt(input);

                switch (action) {
                    case 1:
                        adminController.showDashboard();
                        break;
                    case 2:
                        System.out.print("Enter Username: ");
                        String targetUser = scanner.next();
                        System.out.print("Enter Amount to Add: ");
                        double money = scanner.nextDouble();
                        adminController.topUpUser(targetUser, money);
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Invalid selection.");
                }
            } catch (Exception e) {
                System.out.println(" ERROR: Invalid input! Please enter NUMBERS only.");
                scanner.nextLine();
            }
        }
    }
}