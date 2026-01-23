package com.company.repositories;

import com.company.data.interfaces.IDB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

public class AdminRepository {

    private final IDB db;

    public AdminRepository(IDB db) {
        this.db = db;
    }

    public void showAllParkingStatus() {
        String sql = """
            SELECT
              ps.spot_number,
              ps."Price",
              u.username,
              po.status,
              po.start_date,
              po.end_date
            FROM parking_spots ps
            LEFT JOIN parking_orders po
              ON po.spot_number = ps.spot_number AND po.status = 'ACTIVE'
            LEFT JOIN users u
              ON u."User_ID" = po."User_ID"
            ORDER BY ps.spot_number
        """;

        try (PreparedStatement st = db.getConnection().prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                int spot = rs.getInt("spot_number");
                int price = rs.getInt("Price");
                String username = rs.getString("username");
                String status = rs.getString("status");
                Timestamp end = rs.getTimestamp("end_date");

                if (username == null) {
                    System.out.println("Spot " + spot + " | Price: " + price + "$ | FREE");
                } else {
                    long daysLeft = daysLeft(end);
                    System.out.println(
                            "Spot " + spot +
                                    " | Price: " + price + "$" +
                                    " | User: " + username +
                                    " | Status: " + status +
                                    " | Ends: " + end +
                                    " | Days left: " + daysLeft
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long daysLeft(Timestamp end) {
        if (end == null) return -1;

        Instant now = Instant.now();
        Instant endI = end.toInstant();
        long days = Duration.between(now, endI).toDays();

        if (days > 36500) {
            return 9999;
        }

        return Math.max(days, 0);
    }
}
