package com.company.repositories;

import com.company.data.interfaces.IDB;
import com.company.models.User;
import com.company.repositories.interfaces.IAdminRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminRep implements IAdminRepository { // Название как на скрине
    private final IDB db;

    public AdminRep(IDB db) {
        this.db = db;
    }

    @Override
    public List<User> getAllUsers() {
        try {
            Connection con = db.getConnection();
            String sql = "SELECT id, name, surname, gender, role FROM users";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getBoolean("gender"),
                        rs.getString("role")
                ));
            }
            return users;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean deleteUser(int id) {
        try {
            Connection con = db.getConnection();
            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement st = con.prepareStatement(sql);
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
}