package com.company.repositories;

import com.company.data.interfaces.IDB;
import com.company.repositories.interfaces.IParkingRepository;
import java.sql.*;

public class ParkingRepository implements IParkingRepository {
    private final IDB db;

    public ParkingRepository(IDB db) {
        this.db = db;
    }

    @Override
    public String getMyParking(int userId) {
        String sql = "SELECT spot_number, car_number FROM parking_orders WHERE \"User_ID\" = ? AND status = 'ACTIVE'";
        StringBuilder sb = new StringBuilder();
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                sb.append("Spot: ").append(rs.getInt("spot_number")).append(" | Car: ").append(rs.getString("car_number")).append("\n");
            }
        } catch (Exception e) { return e.getMessage(); }
        return sb.length() == 0 ? "None" : sb.toString();
    }

    @Override
    public String getFreeParking() {

        String statsSql = "SELECT c.name, COUNT(ps.spot_number) as available_count FROM parking_spots ps " +
                "JOIN categories c ON ps.category_id = c.id WHERE ps.spot_number NOT IN " +
                "(SELECT spot_number FROM parking_orders WHERE status = 'ACTIVE') GROUP BY c.name";

        String detailSql = "SELECT ps.spot_number, ps.\"Price\", c.name AS cat_name, c.multiplier FROM parking_spots ps " +
                "JOIN categories c ON ps.category_id = c.id WHERE ps.spot_number NOT IN " +
                "(SELECT spot_number FROM parking_orders WHERE status = 'ACTIVE') ORDER BY c.name, ps.spot_number";

        StringBuilder sb = new StringBuilder();
        try (Connection conn = db.getConnection()) {
            sb.append("\n===============================================================\n");
            sb.append("                 🅿️  PARKING AVAILABILITY SYSTEM\n");
            sb.append("===============================================================\n");

            try (PreparedStatement st = conn.prepareStatement(statsSql);
                 ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    sb.append(String.format("  %-12s : %2d spots available\n",
                            rs.getString("name").toUpperCase(), rs.getInt("available_count")));
                }
            }
            sb.append("---------------------------------------------------------------\n");

            try (PreparedStatement st = conn.prepareStatement(detailSql);
                 ResultSet rs = st.executeQuery()) {
                String currentCategory = "";
                int countInRow = 0;

                while (rs.next()) {
                    String catName = rs.getString("cat_name");
                    double finalPrice = rs.getDouble("Price") * rs.getDouble("multiplier");
                    int spot = rs.getInt("spot_number");

                    if (!catName.equals(currentCategory)) {
                        if (!currentCategory.isEmpty()) {

                            while (countInRow < 5) { sb.append("│           "); countInRow++; }
                            sb.append("│\n└─────────────────────────────────────────────────────────────┘\n");
                        }
                        sb.append(String.format("\n>>> %-15s [ PRICE: %.0f$ ]\n", catName.toUpperCase(), finalPrice));
                        sb.append("┌─────────────────────────────────────────────────────────────┐\n");
                        currentCategory = catName;
                        countInRow = 0;
                    }

                    sb.append(String.format("│ #%03d:%4.0f$ ", spot, finalPrice));
                    countInRow++;

                    if (countInRow == 5) {
                        sb.append("│\n");
                        countInRow = 0;
                    }
                }

                if (!currentCategory.isEmpty()) {
                    while (countInRow > 0 && countInRow < 5) { sb.append("│           "); countInRow++; }
                    if (countInRow != 0) sb.append("│\n");
                    sb.append("└─────────────────────────────────────────────────────────────┘\n");
                }
            }
        } catch (Exception e) {
            return "Dashboard error: " + e.getMessage();
        }
        return sb.toString();
    }

    @Override
    public String buyParking(int userId, int spot, String phone, String car, int months) {
        int m = (months == 0) ? 1200 : months;
        String sql = "INSERT INTO parking_orders(\"User_ID\", spot_number, owner_phone, car_number, status, start_date, end_date) " +
                "VALUES (?, ?, ?, ?, 'ACTIVE', now(), now() + (? || ' months')::interval)";
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setInt(1, userId); st.setInt(2, spot); st.setString(3, phone);
            st.setString(4, car); st.setInt(5, m);
            return st.executeUpdate() > 0 ? "Success" : "Fail";
        } catch (Exception e) { return e.getMessage(); }
    }

    @Override
    public String cancelOrder(int userId, int spot) {
        String sql = "DELETE FROM parking_orders WHERE \"User_ID\" = ? AND spot_number = ?";
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setInt(1, userId); st.setInt(2, spot);
            return st.executeUpdate() > 0 ? "Done" : "Not found";
        } catch (Exception e) { return e.getMessage(); }
    }

    @Override
    public String extendOrder(int userId, int spot, int months) {
        String sql = "UPDATE parking_orders SET end_date = end_date + (? || ' months')::interval WHERE \"User_ID\" = ? AND spot_number = ?";
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setInt(1, months); st.setInt(2, userId); st.setInt(3, spot);
            return st.executeUpdate() > 0 ? "Extended" : "Fail";
        } catch (Exception e) { return e.getMessage(); }
    }

    @Override
    public double getBalance(int userId) {
        try (PreparedStatement st = db.getConnection().prepareStatement("SELECT balance FROM users WHERE \"User_ID\" = ?")) {
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return rs.getDouble("balance");
        } catch (Exception e) {}
        return 0;
    }

    @Override
    public boolean updateBalance(int userId, double amount) {
        try (PreparedStatement st = db.getConnection().prepareStatement("UPDATE users SET balance = balance + ? WHERE \"User_ID\" = ?")) {
            st.setDouble(1, amount); st.setInt(2, userId);
            return st.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    @Override
    public void showAllParkingStatus() {
        try (Statement st = db.getConnection().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT spot_number FROM parking_spots");
            while (rs.next()) { System.out.println("Spot: " + rs.getInt(1)); }
        } catch (Exception e) {}
    }
}