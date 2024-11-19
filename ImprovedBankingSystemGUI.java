package project3;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

class BankAccount {
    private double balance;
    private final Object lock = new Object();

    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        synchronized (lock) {
            balance += amount;
        }
    }

    public boolean withdraw(double amount) {
        synchronized (lock) {
            if (balance >= amount) {
                balance -= amount;
                return true;
            }
            return false;
        }
    }
}

class TransactionLogger {
    private static final String LOG_FILE = "transaction_log.txt";
    private static final Object logLock = new Object();

    public static void logTransaction(String transaction) {
        synchronized (logLock) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = dateFormat.format(new Date());
                writer.println(timestamp + " - " + transaction);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class BankSystem {
    private BankAccount[] accounts;

    public BankSystem(int numAccounts) {
        accounts = new BankAccount[numAccounts];
        for (int i = 0; i < numAccounts; i++) {
            accounts[i] = new BankAccount(1000); // Initial balance for each account
        }
    }

    public double getBalance(int accountNumber) {
        return accounts[accountNumber].getBalance();
    }

    public void deposit(int accountNumber, double amount) {
        accounts[accountNumber].deposit(amount);
        TransactionLogger.logTransaction("Deposit: Account " + accountNumber + ", Amount: $" + amount);
    }

    public boolean withdraw(int accountNumber, double amount) {
        if (accounts[accountNumber].withdraw(amount)) {
            TransactionLogger.logTransaction("Withdrawal: Account " + accountNumber + ", Amount: $" + amount);
            return true;
        }
        return false;
    }
}

public class ImprovedBankingSystemGUI extends JFrame {
    private BankSystem bankSystem;

    private JTextField accountNumberField;
    private JTextField amountField;
    private JTextArea logArea;

    public ImprovedBankingSystemGUI(int numAccounts) {
        bankSystem = new BankSystem(numAccounts);

        // Create GUI components
        accountNumberField = new JTextField(10);
        amountField = new JTextField(10);
        logArea = new JTextArea(10, 30);
        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        JButton balanceButton = new JButton("Check Balance");
        JButton exitButton = new JButton("Exit");

        // Set up event handlers
        depositButton.addActionListener(e -> performTransaction(true));
        withdrawButton.addActionListener(e -> performTransaction(false));
        balanceButton.addActionListener(e -> checkBalance());
        exitButton.addActionListener(e -> System.exit(0));

        // Set up layout using GridBagLayout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Row 1
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Account Number:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        add(accountNumberField, gbc);

        // Row 2
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Amount:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        add(amountField, gbc);

        // Row 3
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(depositButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        add(withdrawButton, gbc);

        // Row 4
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(balanceButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        add(exitButton, gbc);

        // Row 5
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(new JLabel("Transaction Log:"), gbc);

        // Row 6
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        add(new JScrollPane(logArea), gbc);

        // Set up frame
        setTitle("Improved Banking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null); // Center on screen
        setVisible(true);
    }

    private void performTransaction(boolean isDeposit) {
        try {
            int accountNumber = Integer.parseInt(accountNumberField.getText());
            double amount = Double.parseDouble(amountField.getText());

            // Create a separate thread for each transaction
            Thread transactionThread = new Thread(() -> {
                if (isDeposit) {
                    bankSystem.deposit(accountNumber, amount);
                } else {
                    bankSystem.withdraw(accountNumber, amount);
                }
            });

            transactionThread.start();
        } catch (NumberFormatException e) {
            // Handle invalid input
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkBalance() {
        try {
            int accountNumber = Integer.parseInt(accountNumberField.getText());

            // Create a separate thread for checking balance
            Thread balanceThread = new Thread(() -> {
                double balance = bankSystem.getBalance(accountNumber);
                SwingUtilities.invokeLater(() -> {
                    logArea.append("Account " + accountNumber + " Balance: $" + balance + "\n");
                });
            });

            balanceThread.start();
        } catch (NumberFormatException e) {
            // Handle invalid input
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid account number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImprovedBankingSystemGUI(5)); // 5 accounts for demonstration
    }
}

