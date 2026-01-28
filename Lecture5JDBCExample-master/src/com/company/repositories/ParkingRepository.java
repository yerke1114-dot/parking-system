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
        String sql = "SELECT id, spot_number, owner_phone, car_number, status, end_date " +
                "FROM parking_orders WHERE \"User_ID\" = ? AND status = 'ACTIVE' ORDER BY id";
        StringBuilder sb = new StringBuilder();
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    sb.append("Order #").append(rs.getInt("id"))
                            .append(" | Spot: ").append(rs.getInt("spot_number"))
                            .append(" | Car: ").append(rs.getString("car_number"))
                            .append(" | End: ").append(rs.getTimestamp("end_date")).append("\n");
                }
            }
        } catch (Exception e) { return "SQL Error: " + e.getMessage(); }
        return sb.length() == 0 ? "No active parking." : sb.toString();
    }

    @Override
    public String getFreeParking() {
        String sql = "SELECT ps.spot_number, ps.\"Price\", c.name FROM parking_spots ps " +
                "JOIN categories c ON ps.category_id = c.id " +
                "WHERE ps.spot_number NOT IN (SELECT spot_number FROM parking_orders WHERE status = 'ACTIVE')";
        StringBuilder sb = new StringBuilder();
        try (PreparedStatement st = db.getConnection().prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                sb.append("Spot: ").append(rs.getInt("spot_number"))
                        .append(" | Type: ").append(rs.getString("name"))
                        .append(" | Price: ").append(rs.getInt("Price")).append("$\n");
            }
        } catch (Exception e) { return "SQL Error: " + e.getMessage(); }
        return sb.toString();
    }

    @Override
    public String buyParking(int userId, int spotNumber, String phone, String car, int months) {
        if (!isPhoneValid(phone) || !isCarNumberValid(car)) return "Input validation failed!";
        String sql = "INSERT INTO parking_orders(\"User_ID\", spot_number, owner_phone, car_number, status, start_date, end_date) " +
                "VALUES (?, ?, ?, ?, 'ACTIVE', now(), now() + (? || ' months')::interval)";
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setInt(1, userId); st.setInt(2, spotNumber);
            st.setString(3, phone); st.setString(4, car); st.setInt(5, months);
            return st.executeUpdate() > 0 ? "Purchase successful!" : "Purchase failed!";
        } catch (Exception e) { return "SQL Error: " + e.getMessage(); }
    }

    private boolean isPhoneValid(String phone) {
        return phone != null && phone.length() == 11;
    }

    private boolean isCarNumberValid(String car) {
        return car != null && car.length() == 8;
    }

    @Override
    public String cancelOrder(int userId, int spotNumber) {
        // 使用 DELETE 确保彻底释放车位，绕过 status 约束检查
        String sql = "DELETE FROM parking_orders WHERE \"User_ID\" = ? AND spot_number = ? AND status = 'ACTIVE'";
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setInt(1, userId);
            st.setInt(2, spotNumber);
            int rows = st.executeUpdate();
            return rows > 0 ? "Order released successfully!" : "No active order found.";
        } catch (Exception e) { return "DB error: " + e.getMessage(); }
    }

    @Override
    public String extendOrder(int userId, int spotNumber, int extraMonths) {
        if (extraMonths <= 0) return "Months must be positive!";
        String sql = "UPDATE parking_orders SET end_date = end_date + (? || ' months')::interval " +
                "WHERE \"User_ID\" = ? AND spot_number = ? AND status = 'ACTIVE'";
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setInt(1, extraMonths); st.setInt(2, userId); st.setInt(3, spotNumber);
            return st.executeUpdate() > 0 ? "Extension successful!" : "Failed to extend.";
        } catch (Exception e) { return "DB error: " + e.getMessage(); }
    }
}