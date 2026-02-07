package com.company.controllers;

import com.company.repositories.AdminRepository;

public class AdminController {

    private final AdminRepository repo;

    public AdminController(AdminRepository repo) {
        this.repo = repo;
    }

    public void showAllParkingStatus() {
        repo.showAllParkingStatus();
    }
}
