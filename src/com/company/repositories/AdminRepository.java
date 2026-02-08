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

    public void showAdminDashboard() {
        String revenueSql = "SELECT SUM(ps.\"Price\" * c.multiplier) as current_revenue " +
                "FROM parking_orders po " +
                "JOIN parking_spots ps ON po.spot_number = ps.spot_number " +
                "JOIN categories c ON ps.category_id = c.id " +
                "WHERE po.status = 'ACTIVE'";

        String detailSql = """
            SELECT
              ps.spot_number,
              c.name as category_name,
              ps."Price",
              u.username,
              po.status,
              po.end_date
            FROM parking_spots ps
            JOIN categories c ON ps.category_id = c.id
            LEFT JOIN parking_orders po
              ON po.spot_number = ps.spot_number AND po.status = 'ACTIVE'
            LEFT JOIN users u
              ON u."User_ID" = po."User_ID"
            ORDER BY ps.spot_number
        """;

        try (Connection conn = db.getConnection()) {

            // 1. Financial Revenue Header
            try (PreparedStatement st = conn.prepareStatement(revenueSql);
                 ResultSet rs = st.executeQuery()) {
                double totalRevenue = 0;
                if (rs.next()) {
                    totalRevenue = rs.getDouble("current_revenue");
                }
                System.out.println("\n==================================================================================");
                System.out.println("ADMIN STRATEGIC DASHBOARD");
                System.out.println("TOTAL MONTHLY REVENUE FROM ACTIVE ORDERS: " + String.format("%.2f", totalRevenue) + " USD");
                System.out.println("==================================================================================");
            }

            // 2. Table Headers
            System.out.printf("%-8s | %-12s | %-12s | %-15s | %-10s | %-10s\n",
                    "SPOT ID", "CATEGORY", "STATUS", "USER", "DAYS LEFT", "EXPIRY");
            System.out.println("----------------------------------------------------------------------------------");

            int occupiedCount = 0;
            try (PreparedStatement st = conn.prepareStatement(detailSql);
                 ResultSet rs = st.executeQuery()) {

                while (rs.next()) {
                    int spot = rs.getInt("spot_number");
                    String category = rs.getString("category_name");
                    String username = rs.getString("username");
                    Timestamp end = rs.getTimestamp("end_date");

                    String statusLabel;
                    String daysLabel = "N/A";
                    String expiryLabel = "N/A";

                    if (username == null) {
                        statusLabel = "FREE";
                    } else {
                        occupiedCount++;
                        statusLabel = "OCCUPIED";
                        long days = daysLeft(end);

                        if (days == 9999) {
                            daysLabel = "FOREVER";
                            expiryLabel = "PERMANENT";
                        } else {
                            daysLabel = String.valueOf(days);
                            expiryLabel = end.toString().substring(0, 10);
                        }
                    }

                    System.out.printf("#%03d      | %-12s | %-12s | %-15s | %-10s | %-10s\n",
                            spot, category.toUpperCase(), statusLabel,
                            (username == null ? "-" : username), daysLabel, expiryLabel);
                }
            }

            // 3. Footer Statistics
            System.out.println("----------------------------------------------------------------------------------");
            System.out.println("OPERATIONAL SUMMARY: Occupancy Rate: " + occupiedCount + "% | Total Spots: 100");
            System.out.println("==================================================================================\n");

        } catch (Exception e) {
            System.err.println("Database Error: " + e.getMessage());
        }
    }

    private long daysLeft(Timestamp end) {
        if (end == null) return -1;
        Instant now = Instant.now();
        Instant endI = end.toInstant();
        long days = Duration.between(now, endI).toDays();
        return (days > 36500) ? 9999 : Math.max(days, 0);
    }
}