package com.company.repositories;

import com.company.data.interfaces.IDB; import com.company.models.Category; import com.company.repositories.interfaces.IParkingRepository;

import java.sql.Connection; import java.sql.PreparedStatement; import java.sql.ResultSet;

public class ParkingRepository implements IParkingRepository { private final IDB db;

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
        } catch (Exception e) {
            return "SQL Error: " + e.getMessage();
        }
        return sb.length() == 0 ? "No active parking." : sb.toString();
    }

    @Override
    public String getFreeParking() {
        String statsSql =
                "SELECT c.name, COUNT(ps.spot_number) as available_count " +
                        "FROM parking_spots ps " +
                        "JOIN categories c ON ps.category_id = c.id " +
                        "WHERE ps.spot_number NOT IN (SELECT spot_number FROM parking_orders WHERE status = 'ACTIVE') " +
                        "GROUP BY c.name";

        String detailSql =
                "SELECT ps.spot_number, ps.\"Price\", c.name AS cat_name, c.multiplier " +
                        "FROM parking_spots ps " +
                        "JOIN categories c ON ps.category_id = c.id " +
                        "WHERE ps.spot_number NOT IN (SELECT spot_number FROM parking_orders WHERE status = 'ACTIVE') " +
                        "ORDER BY c.name, ps.spot_number";

        StringBuilder sb = new StringBuilder();

        try (Connection conn = db.getConnection()) {
            sb.append("\n=========================================\n");
            sb.append("         🅿️  PARKING DASHBOARD\n");
            sb.append("=========================================\n");

            try (PreparedStatement st = conn.prepareStatement(statsSql);
                 ResultSet rs = st.executeQuery()) {
                int totalFree = 0;
                while (rs.next()) {
                    String cat = rs.getString("name");
                    int count = rs.getInt("available_count");
                    sb.append(String.format("  %-10s : %2d spots left\n", cat, count));
                    totalFree += count;
                }
                sb.append("-----------------------------------------\n");
                sb.append("  TOTAL FREE: ").append(totalFree).append(" / 100\n");
                sb.append("=========================================\n\n");
            }

            try (PreparedStatement st = conn.prepareStatement(detailSql);
                 ResultSet rs = st.executeQuery()) {
                String currentCategory = "";
                int countInRow = 0;

                while (rs.next()) {
                    String catName = rs.getString("cat_name");
                    double basePrice = rs.getDouble("Price");
                    double mult = 1.0;
                    try {
                        mult = rs.getDouble("multiplier");
                    } catch (Exception e) {}

                    int spot = rs.getInt("spot_number");
                    double finalPrice = basePrice * mult;

                    if (!catName.equals(currentCategory)) {
                        if (!currentCategory.isEmpty()) {
                            if (countInRow > 0) {
                                while (countInRow < 7) {
                                    sb.append("│      ");
                                    countInRow++;
                                }
                                sb.append("│\n");
                            }
                            sb.append("└─────────────────────────────────────────────────────────┘\n");
                        }
                        sb.append("\n┌─────────────────────────────────────────────────────────┐\n");
                        sb.append(String.format("│ %-15s | Base: %-7.1f$ | Rate: x%-5.1f │\n",
                                catName.toUpperCase(), basePrice, mult));
                        sb.append("├─────────────────────────────────────────────────────────┤\n");
                        currentCategory = catName;
                        countInRow = 0;
                    }

                    sb.append(String.format("│ #%03d ", spot));
                    countInRow++;

                    if (countInRow == 7) {
                        sb.append("│\n");
                        countInRow = 0;
                    }
                }

                if (countInRow > 0) {
                    while (countInRow < 7) {
                        sb.append("│      ");
                        countInRow++;
                    }
                    sb.append("│\n");
                }
                if (!currentCategory.isEmpty()) {
                    sb.append("└─────────────────────────────────────────────────────────┘\n");
                }
            }
        } catch (Exception e) {
            return "Dashboard error: " + e.getMessage();
        }

        return sb.toString();
    }

    @Override
    public String buyParking(int userId, int spotNumber, String phone, String car, int months) {
        if (!isPhoneValid(phone) || !isCarNumberValid(car)) return "Input validation failed!";

        // 修复逻辑：如果是 0 个月（Forever），我们按 1200 个月（100年）来计算截止日期
        int intervalMonths = (months == 0) ? 1200 : months;

        String sql = "INSERT INTO parking_orders(\"User_ID\", spot_number, owner_phone, car_number, status, start_date, end_date) " +
                "VALUES (?, ?, ?, ?, 'ACTIVE', now(), now() + (? || ' months')::interval)";

        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setInt(1, userId);
            st.setInt(2, spotNumber);
            st.setString(3, phone);
            st.setString(4, car);
            st.setInt(5, intervalMonths); // 使用处理过的月份

            return st.executeUpdate() > 0 ? "Purchase successful!" : "Purchase failed!";
        } catch (Exception e) {
            return "SQL Error: " + e.getMessage();
        }
    }

    @Override
    public String cancelOrder(int userId, int spotNumber) {
        String sql = "DELETE FROM parking_orders WHERE \"User_ID\" = ? AND spot_number = ? AND status = 'ACTIVE'";
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setInt(1, userId);
            st.setInt(2, spotNumber);
            int rows = st.executeUpdate();
            return rows > 0 ? "Order released successfully!" : "No active order found.";
        } catch (Exception e) {
            return "DB error: " + e.getMessage();
        }
    }

    @Override
    public String extendOrder(int userId, int spotNumber, int extraMonths) {
        if (extraMonths <= 0) return "Months must be positive!";
        String sql = "UPDATE parking_orders SET end_date = end_date + (? || ' months')::interval " +
                "WHERE \"User_ID\" = ? AND spot_number = ? AND status = 'ACTIVE'";
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setInt(1, extraMonths);
            st.setInt(2, userId);
            st.setInt(3, spotNumber);
            return st.executeUpdate() > 0 ? "Extension successful!" : "Failed to extend.";
        } catch (Exception e) {
            return "DB error: " + e.getMessage();
        }
    }

    @Override
    public double getBalance(int userId) {
        double balance = 0;
        String sql = "SELECT balance FROM users WHERE \"User_ID\" = ?";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    balance = rs.getDouble("balance");
                }
            }
        } catch (Exception e) {
            System.err.println("Balance Error: " + e.getMessage());
        }
        return balance;
    }

    @Override
    public boolean updateBalance(int userId, double amount) {
        String sql = "UPDATE users SET balance = balance + ? WHERE \"User_ID\" = ?";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Update Balance Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void showAllParkingStatus() {
    }

    private boolean isPhoneValid(String phone) {
        return phone != null && phone.length() >= 10;
    }

    private boolean isCarNumberValid(String car) {
        return car != null && car.length() == 8;
    }
}