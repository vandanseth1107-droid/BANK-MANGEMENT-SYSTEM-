import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;


class InvalidAmountException extends Exception {
    InvalidAmountException(String message) { super(message); }
}

class InsufficientFundsException extends Exception {
    InsufficientFundsException(String message) { super(message); }
}

class AccountNotFoundException extends Exception {
    AccountNotFoundException(String message) { super(message); }
}

interface Transactable {
    void deposit(double amount) throws InvalidAmountException;
    void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException;
    double getBalance();
}


abstract class Account implements Transactable {
    
    private String accountNumber;
    private String holderName;
    private double balance;

    
    Account(String accountNumber, String holderName, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = balance;
    }

    
    Account(String accountNumber, String holderName) {
        this(accountNumber, holderName, 0);
    }

    String getAccountNumber() { return accountNumber; }
    String getHolderName() { return holderName; }
    public double getBalance() { return balance; }


    protected void setBalance(double balance) { this.balance = balance; }

    protected void checkAmount(double amount) throws InvalidAmountException {
        if (amount <= 0) throw new InvalidAmountException("Amount must be greater than 0.");
    }


    public void deposit(double amount) throws InvalidAmountException {
        checkAmount(amount);
        balance += amount;
    }

    
    public void deposit(double amount, String note) throws InvalidAmountException {
        System.out.println("Note: " + note);
        deposit(amount);
    }

    public void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
        checkAmount(amount);
        if (amount > balance) throw new InsufficientFundsException("Not enough balance.");
        balance -= amount;
    }

    abstract double calculateInterest();
    abstract String getAccountType();

    public String toString() {
        return getAccountType() + " | " + accountNumber + " | " + holderName + " | Balance: " + balance;
    }
}


class SavingsAccount extends Account {
    SavingsAccount(String accountNumber, String holderName, double balance) {
        super(accountNumber, holderName, balance);
    }

    
    public void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
        checkAmount(amount);
        if (getBalance() - amount < 500) throw new InsufficientFundsException("Savings must keep a minimum balance of 500.");
        setBalance(getBalance() - amount);
    }

    double calculateInterest() { return getBalance() * 0.04; }
    String getAccountType() { return "Savings"; }
}

class CurrentAccount extends Account {
    CurrentAccount(String accountNumber, String holderName, double balance) {
        super(accountNumber, holderName, balance);
    }


    public void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
        checkAmount(amount);
        if (getBalance() - amount < -5000) throw new InsufficientFundsException("Overdraft limit of -5000 reached.");
        setBalance(getBalance() - amount);
    }

    double calculateInterest() { return 0; }
    String getAccountType() { return "Current"; }
}



class Transaction {
    private String type;
    private String accountNumber;
    private double amount;
    private String date;
    private String note;

    Transaction(String type, String accountNumber, double amount, String note) {
        this(type, accountNumber, amount, new Date().toString(), note);
    }

    Transaction(String type, String accountNumber, double amount, String date, String note) {
        this.type = type;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    String getType() { return type; }
    String getAccountNumber() { return accountNumber; }
    double getAmount() { return amount; }
    String getDate() { return date; }
    String getNote() { return note; }

    public String toString() {
        return date + " | " + type + " | " + accountNumber + " | " + amount + " | " + note;
    }
}



class Bank {
    
    private HashMap<String, Account> accounts = new HashMap<String, Account>();
    private ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private int nextNumber = 1001;

    
    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final String TRANSACTIONS_FILE = "transactions.txt";

    
    private String clean(String text) {
        return text.replace(",", " ");
    }

    
    String createAccount(int type, String name, double openingBalance) throws InvalidAmountException {
        if (openingBalance < 0) throw new InvalidAmountException("Opening balance cannot be negative.");
        if (type == 1 && openingBalance < 500) throw new InvalidAmountException("Savings needs an opening balance of at least 500.");

        String number = "ACC" + nextNumber;
        nextNumber++;
        name = clean(name);

       
        Account acc;
        if (type == 1) acc = new SavingsAccount(number, name, openingBalance);
        else acc = new CurrentAccount(number, name, openingBalance);

        accounts.put(number, acc);
        saveToFile();
        return number;
    }

    
    Account findAccount(String number) throws AccountNotFoundException {
        Account acc = accounts.get(number);
        if (acc == null) throw new AccountNotFoundException("Account " + number + " not found.");
        return acc;
    }

    
    void deposit(String number, double amount) throws AccountNotFoundException, InvalidAmountException {
        Account acc = findAccount(number);
        acc.deposit(amount);
        transactions.add(new Transaction("DEPOSIT", number, amount, "Cash deposit"));
        saveToFile();
    }

    
    void withdraw(String number, double amount)
            throws AccountNotFoundException, InvalidAmountException, InsufficientFundsException {
        Account acc = findAccount(number);
        acc.withdraw(amount);
        transactions.add(new Transaction("WITHDRAW", number, amount, "Cash withdrawal"));
        saveToFile();
    }

    
    void transfer(String from, String to, double amount)
            throws AccountNotFoundException, InvalidAmountException, InsufficientFundsException {
        transfer(from, to, amount, "Transfer");
    }

    
    void transfer(String from, String to, double amount, String note)
            throws AccountNotFoundException, InvalidAmountException, InsufficientFundsException {
        Account source = findAccount(from);
        Account target = findAccount(to);
        note = clean(note);

        source.withdraw(amount);   
        target.deposit(amount);

        transactions.add(new Transaction("TRANSFER_OUT", from, amount, note + " to " + to));
        transactions.add(new Transaction("TRANSFER_IN", to, amount, note + " from " + from));
        saveToFile();
    }

    
    void showAllAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts yet.");
            return;
        }
        for (Account acc : accounts.values()) {
            System.out.println(acc);
        }
    }

    
    void showTransactions(String number) throws AccountNotFoundException {
        findAccount(number);
        boolean found = false;
        for (Transaction t : transactions) {
            if (t.getAccountNumber().equals(number)) {
                System.out.println(t);
                found = true;
            }
        }
        if (!found) System.out.println("No transactions yet.");
    }

    
    void applyInterestToAll() {
        for (Account acc : accounts.values()) {
            try {
                double interest = acc.calculateInterest();
                if (interest > 0) {
                    acc.deposit(interest);
                    transactions.add(new Transaction("INTEREST", acc.getAccountNumber(), interest, "Interest added"));
                }
                
                if (acc.getBalance() < 0) {
                    double fee = -acc.getBalance() * 0.01;
                    acc.withdraw(fee);
                    transactions.add(new Transaction("FEE", acc.getAccountNumber(), fee, "Overdraft charge 1%"));
                }
            } catch (InvalidAmountException e) {
                System.out.println(acc.getAccountNumber() + ": " + e.getMessage());
            } catch (InsufficientFundsException e) {
                System.out.println(acc.getAccountNumber() + ": " + e.getMessage());
            }
        }
        saveToFile();
        System.out.println("Interest applied.");
    }

    
    void saveToFile() {
        try {
            FileWriter aw = new FileWriter(ACCOUNTS_FILE);
            for (Account acc : accounts.values()) {
                aw.write(acc.getAccountType() + "," + acc.getAccountNumber() + ","
                        + acc.getHolderName() + "," + acc.getBalance() + "\n");
            }
            aw.close();

            FileWriter tw = new FileWriter(TRANSACTIONS_FILE);
            for (Transaction t : transactions) {
                tw.write(t.getType() + "," + t.getAccountNumber() + "," + t.getAmount() + ","
                        + t.getDate() + "," + t.getNote() + "\n");
            }
            tw.close();
        } catch (IOException e) {
            System.out.println("Could not save: " + e.getMessage());
        }
    }

    
    void loadFromFile() {
        try {
            BufferedReader ar = new BufferedReader(new FileReader(ACCOUNTS_FILE));
            String line;
            while ((line = ar.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 4) continue;
                double balance = Double.parseDouble(p[3]);
                Account acc;
                if (p[0].equals("Savings")) acc = new SavingsAccount(p[1], p[2], balance);
                else acc = new CurrentAccount(p[1], p[2], balance);
                accounts.put(p[1], acc);

                
                int num = Integer.parseInt(p[1].substring(3));
                if (num >= nextNumber) nextNumber = num + 1;
            }
            ar.close();
        } catch (IOException e) {
           
        } catch (NumberFormatException e) {
            System.out.println("accounts.txt has bad data, some accounts skipped.");
        }

        try {
            BufferedReader tr = new BufferedReader(new FileReader(TRANSACTIONS_FILE));
            String line;
            while ((line = tr.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 5) continue;
                transactions.add(new Transaction(p[0], p[1], Double.parseDouble(p[2]), p[3], p[4]));
            }
            tr.close();
        } catch (IOException e) {
           
        } catch (NumberFormatException e) {
            System.out.println("transactions.txt has bad data, some lines skipped.");
        }
    }
}



public class BankingSystem {
    static Scanner sc = new Scanner(System.in);

    
    static String ask(String question) {
        System.out.print(question);
        return sc.nextLine().trim();
    }

    
    static double askNumber(String question) {
        return Double.parseDouble(ask(question));
    }

    public static void main(String[] args) {
        Bank bank = new Bank();
        bank.loadFromFile();
        int choice = 0;

        while (choice != 8) {
            System.out.println("\n1 Create Account  2 Deposit  3 Withdraw  4 Transfer");
            System.out.println("5 Show Accounts  6 Show Transactions  7 Apply Interest  8 Exit");

            try {
                choice = (int) askNumber("Choice: ");

                if (choice == 1) {
                    String name = ask("Name: ");
                    int type = (int) askNumber("Type (1 Savings, 2 Current): ");
                    double bal = askNumber("Opening balance: ");
                    System.out.println("Created: " + bank.createAccount(type, name, bal));

                } else if (choice == 2) {
                    String acc = ask("Account number: ");
                    bank.deposit(acc, askNumber("Amount: "));
                    System.out.println("Deposit done.");

                } else if (choice == 3) {
                    String acc = ask("Account number: ");
                    bank.withdraw(acc, askNumber("Amount: "));
                    System.out.println("Withdraw done.");

                } else if (choice == 4) {
                    String from = ask("From account: ");
                    String to = ask("To account: ");
                    double amt = askNumber("Amount: ");
                    String note = ask("Note (press Enter to skip): ");
                    if (note.equals("")) bank.transfer(from, to, amt);
                    else bank.transfer(from, to, amt, note);
                    System.out.println("Transfer done.");

                } else if (choice == 5) {
                    bank.showAllAccounts();

                } else if (choice == 6) {
                    bank.showTransactions(ask("Account number: "));

                } else if (choice == 7) {
                    bank.applyInterestToAll();

                } else if (choice != 8) {
                    System.out.println("Please choose 1 to 8.");
                }

            } catch (InvalidAmountException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (InsufficientFundsException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (AccountNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error: please type a valid number.");
            }
        }
        System.out.println("Goodbye!");
    }
}
