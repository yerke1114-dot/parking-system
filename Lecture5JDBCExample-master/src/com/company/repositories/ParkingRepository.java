package com.company.repositories;

import com.company.data.interfaces.IDB;
import com.company.repositories.interfaces.IParkingRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ParkingRepository implements IParkingRepository {

    private final IDB db;

    public ParkingRepository(IDB db) {
        this.db = db;
    }

    @Override
    public String getMyParking(int userId) {
        String sql =
                "SELECT po.id, po.spot_number, po.owner_phone, po.car_number, po.status, po.start_date, po.end_date " +
                        "FROM parking_orders po " +
                        "WHERE po.\"User_ID\" = ? AND po.status = 'ACTIVE' " +
                        "ORDER BY po.id";

        StringBuilder sb = new StringBuilder();

        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setInt(1, userId);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    sb.append("Order #").append(rs.getInt("id"))
                            .append(" | Spot: ").append(rs.getInt("spot_number"))
                            .append(" | Phone: ").append(rs.getString("owner_phone"))
                            .append(" | Car: ").append(rs.getString("car_number"))
                            .append(" | Status: ").append(rs.getString("status"))
                            .append(" | End: ").append(rs.getTimestamp("end_date"))
                            .append("\n");
                }
            }
        } catch (Exception e) {
            return "sql error: " + e.getMessage();
        }

        return sb.length() == 0 ? "You have no active parking zones." : sb.toString();
    }

    @Override
    public String getFreeParking() {
        String sql = "SELECT ps.spot_number, ps.\"Price\", c.name AS cat_name, c.price_multiplier " +
                "FROM parking_spots ps " +
                "JOIN categories c ON ps.category_id = c.id " +
                "WHERE NOT EXISTS (" +
                "  SELECT 1 FROM parking_orders po " +
                "  WHERE po.spot_number = ps.spot_number AND po.status = 'ACTIVE'" +
                ") " +
                "ORDER BY ps.spot_number";

        StringBuilder sb = new StringBuilder();
        try (PreparedStatement st = db.getConnection().prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                int basePrice = rs.getInt("Price");
                double multiplier = rs.getDouble("price_multiplier");
                double currentPrice = basePrice * multiplier;

                sb.append("Spot: ").append(rs.getInt("spot_number"))
                        .append(" | Type: ").append(rs.getString("cat_name"))
                        .append(" | Monthly Price: ").append(currentPrice)
                        .append("$\n");
            }
        } catch (Exception e) {
            return "sql error: " + e.getMessage();
        }
        return sb.length() == 0 ? "No free parking zones." : sb.toString();
    }
    @Override
    public String buyParking(int userId, int spotNumber, String ownerPhone, String carNumber, int months) {
        boolean isValidPlan = (months == 0 || months == 1 || months == 3 || months == 6);
        if (!isValidPlan) return "Invalid rental plan!";
        if (!isPhoneValid(ownerPhone)) return "Owner phone error!";
        if (!isCarNumberValid(carNumber)) return "Car number error!";

        String getInfoSql = "SELECT ps.\"Price\", c.price_multiplier, c.name " +
                "FROM parking_spots ps " +
                "JOIN categories c ON ps.category_id = c.id " +
                "WHERE ps.spot_number = ?";

        double finalPrice = 0;
        String categoryName = "";

        try (PreparedStatement st = db.getConnection().prepareStatement(getInfoSql)) {
            st.setInt(1, spotNumber);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) return "Spot does not exist!";

                int basePrice = rs.getInt("Price");
                double multiplier = rs.getDouble("price_multiplier");
                categoryName = rs.getString("name");

                if (months == 0) {
                    finalPrice = 5000 * multiplier; // Forever price * multiplier
                } else {
                    finalPrice = basePrice * months * multiplier;
                }
            }
        } catch (Exception e) {
            return "sql error: " + e.getMessage();
        }

        // Check if busy
        String checkBusy = "SELECT 1 FROM parking_orders WHERE spot_number = ? AND status = 'ACTIVE'";
        try (PreparedStatement st = db.getConnection().prepareStatement(checkBusy)) {
            st.setInt(1, spotNumber);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return "Spot already occupied!";
            }
        } catch (Exception e) {
            return "sql error: " + e.getMessage();
        }

        // Insert Order
        String insert = (months == 0) ?
                "INSERT INTO parking_orders(\"User_ID\", spot_number, owner_phone, car_number, status, start_date, end_date) VALUES (?, ?, ?, ?, 'ACTIVE', now(), NULL)" :
                "INSERT INTO parking_orders(\"User_ID\", spot_number, owner_phone, car_number, status, start_date, end_date) VALUES (?, ?, ?, ?, 'ACTIVE', now(), now() + (? || ' months')::interval)";

        try (PreparedStatement st = db.getConnection().prepareStatement(insert)) {
            st.setInt(1, userId);
            st.setInt(2, spotNumber);
            st.setString(3, ownerPhone);
            st.setString(4, carNumber);
            if (months != 0) st.setInt(5, months);

            int rows = st.executeUpdate();
            return rows > 0 ? "Success! Type: " + categoryName + " | Total: " + finalPrice + "$" : "Failed!";
        } catch (Exception e) {
            return "sql error: " + e.getMessage();
        }
    }
    private boolean isPhoneValid(String phone) {
        if (phone == null || phone.length() != 11) {
            return false;
        }
        for (char c : phone.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCarNumberValid(String car) {
        if (car == null) return false;
        return car.length() == 8;
    }
}