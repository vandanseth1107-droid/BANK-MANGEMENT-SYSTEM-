import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Bank {
    private HashMap<String, Account> accounts = new HashMap<String, Account>();
    private ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private int nextNumber = 1001;

    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final String TRANSACTIONS_FILE = "transactions.txt";

    private String clean(String text) {
        return text.replace(",", " ");
    }

    public String createAccount(int type, String name, double openingBalance)
            throws InvalidAmountException {
        if (openingBalance < 0) {
            throw new InvalidAmountException("Opening balance cannot be negative.");
        }

        if (type == 1 && openingBalance < 500) {
            throw new InvalidAmountException(
                    "Savings needs an opening balance of at least 500.");
        }

        String number = "ACC" + nextNumber;
        nextNumber++;
        name = clean(name);

        Account account;

        if (type == 1) {
            account = new SavingsAccount(number, name, openingBalance);
        } else {
            account = new CurrentAccount(number, name, openingBalance);
        }

        accounts.put(number, account);
        saveToFile();
        return number;
    }

    public Account findAccount(String number) throws AccountNotFoundException {
        Account account = accounts.get(number);

        if (account == null) {
            throw new AccountNotFoundException("Account " + number + " not found.");
        }

        return account;
    }

    public void deposit(String number, double amount)
            throws AccountNotFoundException, InvalidAmountException {
        Account account = findAccount(number);
        account.deposit(amount);

        transactions.add(new Transaction(
                "DEPOSIT", number, amount, "Cash deposit"));

        saveToFile();
    }

    public void withdraw(String number, double amount)
            throws AccountNotFoundException, InvalidAmountException,
            InsufficientFundsException {
        Account account = findAccount(number);
        account.withdraw(amount);

        transactions.add(new Transaction(
                "WITHDRAW", number, amount, "Cash withdrawal"));

        saveToFile();
    }

    public void transfer(String from, String to, double amount)
            throws AccountNotFoundException, InvalidAmountException,
            InsufficientFundsException {
        transfer(from, to, amount, "Transfer");
    }

    public void transfer(String from, String to, double amount, String note)
            throws AccountNotFoundException, InvalidAmountException,
            InsufficientFundsException {
        Account source = findAccount(from);
        Account target = findAccount(to);
        note = clean(note);

        source.withdraw(amount);
        target.deposit(amount);

        transactions.add(new Transaction(
                "TRANSFER_OUT", from, amount, note + " to " + to));

        transactions.add(new Transaction(
                "TRANSFER_IN", to, amount, note + " from " + from));

        saveToFile();
    }

    public void showAllAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts yet.");
            return;
        }

        for (Account account : accounts.values()) {
            System.out.println(account);
        }
    }

    public void showTransactions(String number)
            throws AccountNotFoundException {
        findAccount(number);
        boolean found = false;

        for (Transaction transaction : transactions) {
            if (transaction.getAccountNumber().equals(number)) {
                System.out.println(transaction);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No transactions yet.");
        }
    }

    public void applyInterestToAll() {
        for (Account account : accounts.values()) {
            try {
                double interest = account.calculateInterest();

                if (interest > 0) {
                    account.deposit(interest);
                    transactions.add(new Transaction(
                            "INTEREST", account.getAccountNumber(),
                            interest, "Interest added"));
                }

                if (account.getBalance() < 0) {
                    double fee = -account.getBalance() * 0.01;
                    account.withdraw(fee);
                    transactions.add(new Transaction(
                            "FEE", account.getAccountNumber(),
                            fee, "Overdraft charge 1%"));
                }
            } catch (InvalidAmountException e) {
                System.out.println(account.getAccountNumber()
                        + ": " + e.getMessage());
            } catch (InsufficientFundsException e) {
                System.out.println(account.getAccountNumber()
                        + ": " + e.getMessage());
            }
        }

        saveToFile();
        System.out.println("Interest applied.");
    }

    public void saveToFile() {
        try (FileWriter accountWriter = new FileWriter(ACCOUNTS_FILE)) {
            for (Account account : accounts.values()) {
                accountWriter.write(
                        account.getAccountType() + ","
                        + account.getAccountNumber() + ","
                        + account.getHolderName() + ","
                        + account.getBalance() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Could not save accounts: " + e.getMessage());
        }

        try (FileWriter transactionWriter = new FileWriter(TRANSACTIONS_FILE)) {
            for (Transaction transaction : transactions) {
                transactionWriter.write(
                        transaction.getType() + ","
                        + transaction.getAccountNumber() + ","
                        + transaction.getAmount() + ","
                        + transaction.getDate() + ","
                        + transaction.getNote() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Could not save transactions: " + e.getMessage());
        }
    }

    public void loadFromFile() {
        try (BufferedReader accountReader = new BufferedReader(
                new FileReader(ACCOUNTS_FILE))) {
            String line;

            while ((line = accountReader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length < 4) {
                    continue;
                }

                double balance = Double.parseDouble(parts[3]);
                Account account;

                if (parts[0].equals("Savings")) {
                    account = new SavingsAccount(parts[1], parts[2], balance);
                } else {
                    account = new CurrentAccount(parts[1], parts[2], balance);
                }

                accounts.put(parts[1], account);

                int number = Integer.parseInt(parts[1].substring(3));

                if (number >= nextNumber) {
                    nextNumber = number + 1;
                }
            }
        } catch (IOException e) {
            // The file may not exist when the program is run for the first time.
        } catch (NumberFormatException e) {
            System.out.println("accounts.txt has bad data, some accounts skipped.");
        }

        try (BufferedReader transactionReader = new BufferedReader(
                new FileReader(TRANSACTIONS_FILE))) {
            String line;

            while ((line = transactionReader.readLine()) != null) {
                String[] parts = line.split(",", 5);

                if (parts.length < 5) {
                    continue;
                }

                transactions.add(new Transaction(
                        parts[0],
                        parts[1],
                        Double.parseDouble(parts[2]),
                        parts[3],
                        parts[4]));
            }
        } catch (IOException e) {
            // The file may not exist when the program is run for the first time.
        } catch (NumberFormatException e) {
            System.out.println(
                    "transactions.txt has bad data, some lines skipped.");
        }
    }
}
