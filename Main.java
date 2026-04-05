import java.util.*;

public class Main {
    private static RealTimeRepository repo = new RealTimeRepository();
    private static Scanner sc = new Scanner(System.in);
    
    private static String currentSessionUser = "DREDD-MARCO";
    
    public static final String GREEN = "\u001B[32m";  
    public static final String ORANGE = "\u001B[33m"; 
    public static final String GRAY = "\u001B[37m";   
    public static final String RED = "\u001B[31m";   
    public static final String RESET = "\u001B[0m";

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n===== ESPORTS TICKETING SYSTEM =====");
            System.out.println("[1] View Arena Map (Filter by Row)");
            System.out.println("[2] Reserve Seat (1 Seat Limit)");
            System.out.println("[3] Confirm Payment & Get Receipt");
            System.out.println("[4] My Tickets Dashboard");
            System.out.println("[5] Exit");
            System.out.print("Action: ");

            try {
                int choice = sc.nextInt();
                sc.nextLine(); 

                switch (choice) {
                    case 1: renderMap(); break;
                    case 2: handleBooking(); break;
                    case 3: handlePayment(); break;
                    case 4: showDashboard(); break;
                    case 5: 
                        System.out.println("Shutting down system...");
                        return;
                    default: System.out.println("Invalid Option.");
                }
            } catch (Exception e) {
                System.out.println("Input Error: Use numbers 1-5.");
                sc.nextLine(); 
            }
        }
    }

    private static void renderMap() {
        System.out.print("\nView [A] VIP, [B] Lower, [C] Upper, or [ALL]: ");
        String filter = sc.nextLine().toUpperCase().trim();

        System.out.println("\n      ************************************");
        System.out.println("      * [      ESPORTS ARENA STAGE     ] *");
        System.out.println("      ************************************\n");
        
        List<SeatHierarchy> map = repo.getLiveInventory(); 
        
        int count = 0;
        for (SeatHierarchy s : map) {
            if (filter.equals("ALL") || s.getId().trim().startsWith(filter)) {
                String status = s.getStatus().trim();
                String color;

                if (status.equalsIgnoreCase("Available")) {
                    color = GREEN;
                } else if (status.equalsIgnoreCase("Reserved")) {
                    color = ORANGE;
                } else {
                    color = GRAY;
                }

                System.out.print(color + "[" + s.getId().trim() + "]" + RESET + "   ");
                
                count++;
                if (filter.equals("ALL") && count % 6 == 0) {
                    System.out.println("\n"); 
                }
            }
        }
        
        if (!filter.equals("ALL")) System.out.println("\n");

        System.out.println("Legend: " + GREEN + "Available" + RESET + 
                           " | " + ORANGE + "Locked (15m)" + RESET + 
                           " | " + GRAY + "Sold" + RESET);
    }

    private static void handleBooking() {
        List<SeatHierarchy> inventory = repo.getLiveInventory();
        for (SeatHierarchy s : inventory) {

            if (s.getCustomerId() != null && s.getCustomerId().trim().equalsIgnoreCase(currentSessionUser) 
                && s.getStatus().trim().equalsIgnoreCase("Reserved")) {
                System.out.println(RED + "Limit Reached: You already have a pending reservation for " + s.getId() + "." + RESET);
                return;
            }
        }
        System.out.print("Enter Seat ID (e.g., A1): ");
        String id = sc.nextLine().toUpperCase().trim();
        if (repo.bookSeat(id, currentSessionUser)) {
            System.out.println(GREEN + "Success: Seat " + id + " reserved for 15 mins." + RESET);
        } else {
            System.out.println(ORANGE + "Error: Seat is taken or invalid." + RESET);
        }
    }

    private static void handlePayment() {
        System.out.print("Enter Reserved Seat ID to Pay: ");
        String id = sc.nextLine().toUpperCase().trim();
        
        if (repo.processPayment(id, currentSessionUser)) {
            System.out.println(GREEN + "Success: Payment confirmed. Local receipt generated!" + RESET);
        } else {
            System.out.println(ORANGE + "Error: Payment failed. Check reservation status." + RESET);
        }
    }

    private static void showDashboard() {
        System.out.println("\n--- MY TICKETS DASHBOARD ---");
        System.out.println("User: " + currentSessionUser);
        
        List<SeatHierarchy> allSeats = repo.getLiveInventory();
        boolean found = false;
        double totalPaid = 0;

        for (SeatHierarchy s : allSeats) {
            
            if (s.getCustomerId() != null && s.getCustomerId().trim().equalsIgnoreCase(currentSessionUser)) {
                String status = s.getStatus().trim();
                String state = status.equalsIgnoreCase("Reserved") ? ORANGE + "PENDING" : GREEN + "CONFIRMED";
                String type = (s instanceof VIPSeat) ? "VIP" : "Regular";
                
                System.out.println("- Seat " + s.getId().trim() + " [" + type + "] Status: " + state + RESET);
                
                if (status.equalsIgnoreCase("Sold")) {
                    totalPaid += s.getPrice();
                }
                found = true;
            }
        }

        if (!found) {
            System.out.println("No tickets associated with this account.");
        } else {
            System.out.println("Total Balance Paid: P" + totalPaid);
        }
        System.out.println("----------------------------");
    }
}