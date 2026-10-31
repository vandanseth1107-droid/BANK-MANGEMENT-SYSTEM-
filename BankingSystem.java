import java.util.Scanner;

public class BankingSystem {
    private static Scanner sc = new Scanner(System.in);

    private static String ask(String question) {
        System.out.print(question);
        return sc.nextLine().trim();
    }

    private static double askNumber(String question) {
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
                    double balance = askNumber("Opening balance: ");

                    System.out.println(
                            "Created: " + bank.createAccount(type, name, balance));

                } else if (choice == 2) {
                    String account = ask("Account number: ");
                    bank.deposit(account, askNumber("Amount: "));
                    System.out.println("Deposit done.");

                } else if (choice == 3) {
                    String account = ask("Account number: ");
                    bank.withdraw(account, askNumber("Amount: "));
                    System.out.println("Withdraw done.");

                } else if (choice == 4) {
                    String from = ask("From account: ");
                    String to = ask("To account: ");
                    double amount = askNumber("Amount: ");
                    String note = ask("Note (press Enter to skip): ");

                    if (note.equals("")) {
                        bank.transfer(from, to, amount);
                    } else {
                        bank.transfer(from, to, amount, note);
                    }

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
