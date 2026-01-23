package com.company.controllers.interfaces;

import com.company.models.AuthUser;

public interface AuthorizationInterface {
    AuthUser login(String username, String password);
    AuthUser register(String username, String password);
}

