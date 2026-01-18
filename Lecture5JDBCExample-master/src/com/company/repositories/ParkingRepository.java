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
                "SELECT po.id, po.spot_number, po.owner_phone, po.car_number, po.status " +
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
        // свободные места = те, у которых нет ACTIVE заказа
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
                        .append(" | Price: ").append(rs.getInt("Price"))
                        .append("$\n");
            }
        } catch (Exception e) {
            return "sql error: " + e.getMessage();
        }

        return sb.length() == 0 ? "No free parking zones right now." : sb.toString();
    }

    @Override
    public String buyParking(int userId, int spotNumber, String ownerPhone, String carNumber) {
        if (!isPhoneValid(ownerPhone)) return "Owner phone must be exactly 11 digits!";
        if (!isCarNumberValid(carNumber)) return "Car number must be exactly 8 characters!";

        // 1) Проверка: место существует?
        String existsSpot = "SELECT 1 FROM parking_spots WHERE spot_number = ?";
        try (PreparedStatement st = db.getConnection().prepareStatement(existsSpot)) {
            st.setInt(1, spotNumber);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    return "This parking spot does not exist!";
                }
            }
        } catch (Exception e) {
            return "sql error: " + e.getMessage();
        }

        // 2) Проверка: место уже занято (есть ACTIVE заказ)?
        String checkBusy = "SELECT 1 FROM parking_orders WHERE spot_number = ? AND status = 'ACTIVE'";
        try (PreparedStatement st = db.getConnection().prepareStatement(checkBusy)) {
            st.setInt(1, spotNumber);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return "This parking spot is already occupied!";
                }
            }
        } catch (Exception e) {
            return "sql error: " + e.getMessage();
        }

        // 3) Вставка заказа
        String insert =
                "INSERT INTO parking_orders(\"User_ID\", spot_number, owner_phone, car_number, status) " +
                        "VALUES (?, ?, ?, ?, 'ACTIVE')";

        try (PreparedStatement st = db.getConnection().prepareStatement(insert)) {
            st.setInt(1, userId);
            st.setInt(2, spotNumber);
            st.setString(3, ownerPhone);
            st.setString(4, carNumber);

            int rows = st.executeUpdate();
            if (rows > 0) return "Parking bought successfully!";
            return "Buy parking failed!";
        } catch (Exception e) {
            return "sql error: " + e.getMessage();
        }
    }

    private boolean isPhoneValid(String phone) {
        if (phone == null || phone.length() != 11) return false;
        for (int i = 0; i < phone.length(); i++) {
            if (!Character.isDigit(phone.charAt(i))) return false;
        }
        return true;
    }

    private boolean isCarNumberValid(String carNumber) {
        return carNumber != null && carNumber.length() == 8;
    }
}
