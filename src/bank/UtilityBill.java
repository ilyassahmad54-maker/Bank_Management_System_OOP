package bank;

public class UtilityBill {
    private int id;
    private int userId;
    private String billType;
    private double amount;
    private String dueDate;
    private boolean isPaid;

    public UtilityBill(int userId, String billType, double amount, String dueDate) {
        this.userId = userId;
        this.billType = billType;
        this.amount = amount;
        this.dueDate = dueDate;
        this.isPaid = false;
    }

    
    public int getUserId() { return userId; }
    public String getBillType() { return billType; }
    public double getAmount() { return amount; }
    public String getDueDate() { return dueDate; }
    public boolean isPaid() { return isPaid; }

    public void setPaid(boolean paid) { isPaid = paid; }
}

