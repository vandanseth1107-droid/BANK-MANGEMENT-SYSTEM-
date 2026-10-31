public interface Transactable {
    void deposit(double amount) throws InvalidAmountException;

    void withdraw(double amount)
            throws InvalidAmountException, InsufficientFundsException;

    double getBalance();
}
