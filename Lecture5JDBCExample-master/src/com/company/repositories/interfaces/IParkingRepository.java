package com.company.repositories.interfaces;

public interface IParkingRepository {
    String getMyParking(int userId);
    String getFreeParking();
    String buyParking(int userId, int spotNumber, String ownerPhone, String carNumber, int months);
    String cancelOrder(int userId, int spotNumber);
    String extendOrder(int userId, int spotNumber, int extraMonths);
}

