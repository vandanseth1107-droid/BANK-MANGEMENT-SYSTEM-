import java.util.Date;

public class Transaction {
    private String type;
    private String accountNumber;
    private double amount;
    private String date;
    private String note;

    public Transaction(String type, String accountNumber, double amount, String note) {
        this(type, accountNumber, amount, new Date().toString(), note);
    }

    public Transaction(String type, String accountNumber, double amount,
                       String date, String note) {
        this.type = type;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    public String getType() {
        return type;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    @Override
    public String toString() {
        return date + " | " + type + " | " + accountNumber
                + " | " + amount + " | " + note;
    }
}
