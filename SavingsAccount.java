public class SavingsAccount extends Account {
    public SavingsAccount(String accountNumber, String holderName, double balance) {
        super(accountNumber, holderName, balance);
    }

    @Override
    public void withdraw(double amount)
            throws InvalidAmountException, InsufficientFundsException {
        checkAmount(amount);

        if (getBalance() - amount < 500) {
            throw new InsufficientFundsException(
                    "Savings must keep a minimum balance of 500.");
        }

        setBalance(getBalance() - amount);
    }

    @Override
    public double calculateInterest() {
        return getBalance() * 0.04;
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }
}
