package com.company.controllers.interfaces;

public interface IParkingController {
    String myParking(int userId);
    String freeParking();
    String buyParking(int userId, int spotNumber, String phone, String car, int months);
    String cancel(int userId, int spotNumber);
    String extend(int userId, int spotNumber, int extraMonths);

    double balance(int userId);
    String topUp(int userId, double amount);
}
