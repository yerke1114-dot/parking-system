package com.company.controllers.interfaces;

public interface AuthorizationInterface {
    Integer login(String username, String password);
    Integer register(String username, String password);
}
