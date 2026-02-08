package com.company.repositories;

import com.company.data.interfaces.IDB;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;

public class AdminRepository {

    private final IDB db;

    public AdminRepository(IDB db) {
        this.db = db;
    }

    // Covers Features: 01 (Dashboard), 02 (Financial), and 05 (Statistics)
    public void showAdminDashboard() {
        String revenueSql = "SELECT SUM(ps.\"Price\" * c.multiplier) as current_revenue " +
                "FROM parking_orders po " +
                "JOIN parking_spots ps ON po.spot_number = ps.spot_number " +
                "JOIN categories c ON ps.category_id = c.id " +
                "WHERE po.status = 'ACTIVE'";

        String detailSql = """
            SELECT ps.spot_number, c.name as category_name, u.username, po.end_date
            FROM parking_spots ps
            JOIN categories c ON ps.category_id = c.id
            LEFT JOIN parking_orders po ON ps.spot_number = po.spot_number AND po.status = 'ACTIVE'
            LEFT JOIN users u ON u."User_ID" = po."User_ID"
            ORDER BY ps.spot_number
        """;

        try (Connection conn = db.getConnection()) {
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(revenueSql)) {
                double revenue = rs.next() ? rs.getDouble("current_revenue") : 0;
                System.out.println("\n======================================================================");
                System.out.println("FINANCIAL OVERVIEW: Monthly Revenue: " + String.format("%.2f", revenue) + " USD");
                System.out.println("======================================================================");
            }

            System.out.printf("%-8s | %-12s | %-15s | %-12s\n", "SPOT ID", "CATEGORY", "USER", "EXPIRY");
            System.out.println("----------------------------------------------------------------------");

            int occupied = 0;
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(detailSql)) {
                while (rs.next()) {
                    String user = rs.getString("username");
                    if (user != null) occupied++;

                    Timestamp end = rs.getTimestamp("end_date");
                    String dateDisp = (end == null) ? "N/A" : end.toString().substring(0, 10);

                    System.out.printf("#%03d      | %-12s | %-15s | %-12s\n",
                            rs.getInt("spot_number"), rs.getString("category_name").toUpperCase(),
                            (user == null ? "FREE" : user), dateDisp);
                }
            }

            System.out.println("----------------------------------------------------------------------");
            System.out.println("SYSTEM STATS: Total: 100 | Occupied: " + occupied + " | Free: " + (100 - occupied));
            System.out.println("======================================================================\n");

        } catch (Exception e) {
            System.err.println("Dashboard Error: " + e.getMessage());
        }
    }

    public void manualTopUpByUsername(String username, double amount) {
        String sql = "UPDATE users SET balance = balance + ? WHERE username = ?";
        try (Connection conn = db.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {
            st.setDouble(1, amount);
            st.setString(2, username);
            int updated = st.executeUpdate();
            if (updated > 0) {
                System.out.println("SUCCESS: Balance updated for @" + username);
            } else {
                System.out.println("FAILED: User '" + username + "' not found.");
            }
        } catch (Exception e) {
            System.err.println("Top-up Error: " + e.getMessage());
        }
    }

    public void manualTopUp(int userId, double amount) {
        String sql = "UPDATE users SET balance = balance + ? WHERE \"User_ID\" = ?";
        try (Connection conn = db.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {
            st.setDouble(1, amount);
            st.setInt(2, userId);
            int updated = st.executeUpdate();
            if (updated > 0) {
                System.out.println("SUCCESS: " + amount + " USD added to User #" + userId);
            } else {
                System.out.println("FAILED: User ID not found.");
            }
        } catch (Exception e) {
            System.err.println("Top-up Error: " + e.getMessage());
        }
    }
}