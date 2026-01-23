package com.company.repositories;

import com.company.controllers.interfaces.AuthorizationInterface;
import com.company.data.interfaces.IDB;
import com.company.models.AuthUser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthorizationRepository implements AuthorizationInterface {

    private final IDB db;

    public AuthorizationRepository(IDB db) {
        this.db = db;
    }

    @Override
    public AuthUser login(String username, String password) {
        String sql = "SELECT \"User_ID\", username, role FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setString(1, username);
            st.setString(2, password);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new AuthUser(
                            rs.getInt("User_ID"),
                            rs.getString("username"),
                            rs.getString("role")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AuthUser register(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES (?, ?) RETURNING \"User_ID\", username, role";
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setString(1, username);
            st.setString(2, password);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new AuthUser(
                            rs.getInt("User_ID"),
                            rs.getString("username"),
                            rs.getString("role")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
