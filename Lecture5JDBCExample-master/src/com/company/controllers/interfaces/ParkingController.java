package com.company.controllers;

import com.company.repositories.interfaces.IParkingRepository;

public class ParkingController {

    private final IParkingRepository repo;

    public ParkingController(IParkingRepository repo) {
        this.repo = repo;
    }

    public double getBalance(int userId) {
        return repo.getBalance(userId);
    }

    public boolean updateBalance(int userId, double amount) {
        return repo.updateBalance(userId, amount);
    }

    public String getMyParking(int userId) {
        return repo.getMyParking(userId);
    }

    public String getFreeParking() {
        return repo.getFreeParking();
    }

    public String buyParking(int userId, int spot, String phone, String car, int months) {
        return repo.buyParking(userId, spot, phone, car, months);
    }

    public String cancelOrder(int userId, int spot) {
        return repo.cancelOrder(userId, spot);
    }

    public String extendOrder(int userId, int spot, int months) {
        return repo.extendOrder(userId, spot, months);
    }

    public void showAllParkingStatus() {
        repo.showAllParkingStatus();
    }
}
