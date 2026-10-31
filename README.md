# Banking Management System

A console-based Java banking application.

## Source files
- `BankingSystem.java` — command-line menu and user input
- `Bank.java` — account operations, transactions, and file persistence
- `Account.java` — abstract base account class
- `SavingsAccount.java` and `CurrentAccount.java` — account types
- `Transaction.java` — transaction record
- `Transactable.java` — transaction interface
- `InvalidAmountException.java`, `InsufficientFundsException.java`, and `AccountNotFoundException.java` — custom exceptions

## Run
Keep all `.java` files in the same folder. Open a terminal in that folder and run:

```bash
javac *.java
java BankingSystem
```

The program creates `accounts.txt` and `transactions.txt` in the working directory when it saves data.
