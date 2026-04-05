public abstract class SeatHierarchy {
    private String id;
    private double price;
    private String status;
    private String customerId;

    public SeatHierarchy(String id, double price, String status, String customerId) {
        this.id = id;
        this.price = price;
        this.status = status;
        this.customerId = customerId;
    }

    public String getId() { return id; }
    public String getStatus() { return status; }
    public String getCustomerId() { return customerId; }
    public double getPrice() { return price; }

    public abstract void displayInfo();
}

class VIPSeat extends SeatHierarchy {
    public VIPSeat(String id, double price, String status, String customerId) {
        super(id, price, status, customerId);
    }
    @Override
    public void displayInfo() { System.out.println("[VIP] Seat: " + getId()); }
}

class RegularSeat extends SeatHierarchy {
    public RegularSeat(String id, double price, String status, String customerId) {
        super(id, price, status, customerId);
    }
    @Override
    public void displayInfo() { System.out.println("[Regular] Seat: " + getId()); }
}