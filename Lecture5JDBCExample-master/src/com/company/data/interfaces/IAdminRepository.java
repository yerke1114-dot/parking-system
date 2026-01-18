package com.company.repositories.interfaces;

import com.company.models.User;
import java.util.List;

public interface IAdminRepository {
    List<User> getAllUsers();
    boolean deleteUser(int id);
}