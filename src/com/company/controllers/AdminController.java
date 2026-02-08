package com.company.controllers;

import com.company.repositories.AdminRepository;

public class AdminController {
    private final AdminRepository repo;

    public AdminController(AdminRepository repo) {
        this.repo = repo;
    }

    public void showDashboard() {
        repo.showAdminDashboard();
    }

    public void topUpUser(String username, double amount) {
        repo.manualTopUpByUsername(username, amount);
    }
}