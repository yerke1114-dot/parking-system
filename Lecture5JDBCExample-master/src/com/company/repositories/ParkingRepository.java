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
        String sql =
                "SELECT ps.spot_number, ps.\"Price\" " +
                        "FROM parking_spots ps " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM parking_orders po " +
                        "  WHERE po.spot_number = ps.spot_number AND po.status = 'ACTIVE'" +
                        ") " +
                        "ORDER BY ps.spot_number";

        StringBuilder sb = new StringBuilder();

        try (PreparedStatement st = db.getConnection().prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                sb.append("Spot: ").append(rs.getInt("spot_number"))
                        .append(" | Base Price: ").append(rs.getInt("Price"))
                        .append("$\n");
            }
        } catch (Exception e) {
            return "sql error: " + e.getMessage();
        }

        return sb.length() == 0 ? "No free parking zones right now." : sb.toString();
    }

    @Override
    public String buyParking(int userId, int spotNumber, String ownerPhone, String carNumber, int months) {
        boolean isValidPlan = (months == 0 || months == 1 || months == 3 || months == 6);

        if (!isValidPlan) {
            return "Invalid rental plan! Please choose: 0 (Forever), 1, 3, or 6 months.";
        }

        if (!isPhoneValid(ownerPhone)) return "Owner phone must be exactly 11 digits!";
        if (!isCarNumberValid(carNumber)) return "Car number must be exactly 8 characters!";

        String existsSpot = "SELECT 1 FROM parking_spots WHERE spot_number = ?";
        try (PreparedStatement st = db.getConnection().prepareStatement(existsSpot)) {
            st.setInt(1, spotNumber);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) return "This parking spot does not exist!";
            }
        } catch (Exception e) {
            return "sql error: " + e.getMessage();
        }


        String checkBusy = "SELECT 1 FROM parking_orders WHERE spot_number = ? AND status = 'ACTIVE'";
        try (PreparedStatement st = db.getConnection().prepareStatement(checkBusy)) {
            st.setInt(1, spotNumber);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return "This parking spot is already occupied!";
            }
        } catch (Exception e) {
            return "sql error: " + e.getMessage();
        }


        String insert;

        if (months == 0) {
            insert =
                    "INSERT INTO parking_orders(\"User_ID\", spot_number, owner_phone, car_number, status, start_date, end_date) " +
                            "VALUES (?, ?, ?, ?, 'ACTIVE', now(), NULL)";
        } else {
            insert =
                    "INSERT INTO parking_orders(\"User_ID\", spot_number, owner_phone, car_number, status, start_date, end_date) " +
                            "VALUES (?, ?, ?, ?, 'ACTIVE', now(), now() + (? || ' months')::interval)";
        }

        try (PreparedStatement st = db.getConnection().prepareStatement(insert)) {
            st.setInt(1, userId);
            st.setInt(2, spotNumber);
            st.setString(3, ownerPhone);
            st.setString(4, carNumber);

            if (months != 0) {
                st.setInt(5, months);
            }

            int rows = st.executeUpdate();
            return rows > 0
                    ? (months == 0 ? "Parking bought forever for 5000$!" : "Parking rented for " + months + " month(s)!")
                    : "Operation failed!";
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