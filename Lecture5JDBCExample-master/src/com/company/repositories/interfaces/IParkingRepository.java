package com.company.repositories.interfaces;

public interface IParkingRepository {
    String getMyParking(int userId);
    String getFreeParking();
    String buyParking(int userId, int spotNumber, String ownerPhone, String carNumber);
}
