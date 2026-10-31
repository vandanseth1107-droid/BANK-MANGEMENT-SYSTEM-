public class CurrentAccount extends Account {
    public CurrentAccount(String accountNumber, String holderName, double balance) {
        super(accountNumber, holderName, balance);
    }

    @Override
    public void withdraw(double amount)
            throws InvalidAmountException, InsufficientFundsException {
        checkAmount(amount);

        if (getBalance() - amount < -5000) {
            throw new InsufficientFundsException(
                    "Overdraft limit of -5000 reached.");
        }

        setBalance(getBalance() - amount);
    }

    @Override
    public double calculateInterest() {
        return 0;
    }

    @Override
    public String getAccountType() {
        return "Current";
    }
}
