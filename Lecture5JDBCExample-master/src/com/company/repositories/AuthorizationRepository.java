package com.company.repositories;

import com.company.controllers.interfaces.AuthorizationInterface;
import com.company.data.interfaces.IDB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthorizationRepository implements AuthorizationInterface {

    private final IDB db;

    public AuthorizationRepository(IDB db) {
        this.db = db;
    }

    @Override
    public Integer login(String username, String password) {
        String sql = "SELECT \"User_ID\" FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setString(1, username);
            st.setString(2, password);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("User_ID");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Integer register(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES (?, ?) RETURNING \"User_ID\"";
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setString(1, username);
            st.setString(2, password);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("User_ID");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}