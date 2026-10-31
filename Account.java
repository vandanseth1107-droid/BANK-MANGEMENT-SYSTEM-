public abstract class Account implements Transactable {
    private String accountNumber;
    private String holderName;
    private double balance;

    public Account(String accountNumber, String holderName, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = balance;
    }

    public Account(String accountNumber, String holderName) {
        this(accountNumber, holderName, 0);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    @Override
    public double getBalance() {
        return balance;
    }

    protected void setBalance(double balance) {
        this.balance = balance;
    }

    protected void checkAmount(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than 0.");
        }
    }

    @Override
    public void deposit(double amount) throws InvalidAmountException {
        checkAmount(amount);
        balance += amount;
    }

    // Overloaded deposit method that accepts a note.
    public void deposit(double amount, String note) throws InvalidAmountException {
        System.out.println("Note: " + note);
        deposit(amount);
    }

    @Override
    public void withdraw(double amount)
            throws InvalidAmountException, InsufficientFundsException {
        checkAmount(amount);

        if (amount > balance) {
            throw new InsufficientFundsException("Not enough balance.");
        }

        balance -= amount;
    }

    public abstract double calculateInterest();

    public abstract String getAccountType();

    @Override
    public String toString() {
        return getAccountType() + " | " + accountNumber + " | "
                + holderName + " | Balance: " + balance;
    }
}
