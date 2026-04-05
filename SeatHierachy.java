import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RealTimeRepository {
    private String url = "jdbc:sqlserver://LAPTOP-I6GCH1M5:1433;databaseName=EsportsTickets;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";


    public List<SeatHierarchy> getLiveInventory() {
        List<SeatHierarchy> seats = new ArrayList<SeatHierarchy>();
        String releaseSql = "UPDATE Seats SET Status = 'Available', CustomerID = NULL, LockTimestamp = NULL " +
                            "WHERE Status = 'Reserved' AND DATEDIFF(minute, LockTimestamp, GETDATE()) >= 15";

        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) { stmt.executeUpdate(releaseSql); }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM Seats ORDER BY SeatID ASC")) {
                while (rs.next()) {
                    String id = rs.getString("SeatID");
                    double price = rs.getDouble("Price");
                    String status = rs.getString("Status");
                    String tier = rs.getString("Tier");
                    String cust = rs.getString("CustomerID");

                    if (tier != null && tier.equalsIgnoreCase("VIP")) {
                        seats.add(new VIPSeat(id, price, status, cust));
                    } else {
                        seats.add(new RegularSeat(id, price, status, cust));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Sync Error: " + e.getMessage());
        }
        return seats;
    }

    public boolean bookSeat(String seatId, String custId) {
        String sql = "UPDATE Seats SET Status = 'Reserved', CustomerID = ?, LockTimestamp = GETDATE() " +
                     "WHERE TRIM(SeatID) = ? AND Status = 'Available'";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, custId);
            pstmt.setString(2, seatId.trim());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean processPayment(String seatId, String custId) {
        double price = 0;
        List<SeatHierarchy> current = getLiveInventory();
        for(SeatHierarchy s : current) {
            if(s.getId().equalsIgnoreCase(seatId)) price = s.getPrice();
        }

        String sql = "UPDATE Seats SET Status = 'Sold', LockTimestamp = NULL " +
                     "WHERE TRIM(SeatID) = ? AND TRIM(CustomerID) = ? AND Status = 'Reserved'";
        
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, seatId.trim());
            pstmt.setString(2, custId.trim());
            
            boolean success = pstmt.executeUpdate() > 0;
            
            if (success) {
               
                printReceiptToConsole(seatId, price, custId);
            }
            return success;
        } catch (SQLException e) { return false; }
    }

    
    private void printReceiptToConsole(String seat, double price, String user) {
        System.out.println("\n\u001B[32m************************************");
        System.out.println("* ESPORTS TICKET RECEIPT      *");
        System.out.println("**********************************");
        System.out.println("  Customer: " + user);
        System.out.println("  Seat ID:  " + seat.trim());
        System.out.println("  Price:    P" + price);
        System.out.println("  Status:   PAID & CONFIRMED");
        System.out.println("  Date:     " + new java.util.Date());
        System.out.println("************************************\u001B[0m\n");
    }
}