package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TenmoServices;
import com.techelevator.tenmo.services.UserServices;

import java.math.BigDecimal;
import java.util.List;

public class App {

    public enum TransferType{
        Request,
        Send
    }

    public enum TransferStatus{
        Pending,
        Approved,
        Rejected
    }


    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final UserServices userService = new UserServices(API_BASE_URL);
    private final TenmoServices tenmoServices = new TenmoServices();
    //private Transfer transfer;


    private AuthenticatedUser currentUser;


    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        } else {
            userService.setAuthToken(currentUser.getToken());
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {
        // TODO Auto-generated method stub
//        BigDecimal balance = userService.getBalance(currentUser);
//        System.out.println(balance);

        Balance balance = tenmoServices.getBalance(currentUser);
        System.out.println("Your current account balance is: $" + balance.getBalance());

    }

    private void viewTransferHistory() {
        // TODO Auto-generated method stub




        Account userAccount = tenmoServices.getAccountByUserId(currentUser, currentUser.getUser().getId());
        int userAccountId = userAccount.getAccountId();
        Transfer[] transfers = tenmoServices.getTransferHistory(currentUser, userAccountId);

        System.out.println("-------------------------------------------");
        System.out.println("Transfers");
        System.out.printf("%-22s%-22s%-22s\n","ID"," From/To  ","Amount");
        System.out.println("--------------------------------------------------");

        if (transfers != null) {
            for (Transfer transfer : transfers) {
                if (transfer.getAccountFrom() == userAccountId) {
                    String username = tenmoServices.getUserByUserId(currentUser,
                            tenmoServices.getAccountByAccountId(currentUser, transfer.getAccountTo()).getUserId()).getUsername();
                    String to = "To:";
                    System.out.printf("%-22d%-22s%-22s\n",transfer.getTransferId(),to+username,transfer.getAmount());


                }
                if (transfer.getAccountTo() == userAccountId) {
                    String username = tenmoServices.getUserByUserId(currentUser,
                            tenmoServices.getAccountByAccountId(currentUser, transfer.getAccountFrom()).getUserId()).getUsername();
                    String from = "From:";
                    System.out.printf("%-22d%-22s%-22s\n",transfer.getTransferId(),from+username,transfer.getAmount());


                }
            }

            int userInput = consoleService.promptForInt("Enter the ID of the transfer you'd like to view: (enter 0 to cancel): ");
            while (userInput != 0) {

                Transfer transfer = tenmoServices.getTransferById(currentUser, userInput);
                if (transfer != null) {
                    printTransferDetails(transfer);
                    break;
                } else {
                    userInput = consoleService.promptForInt("Invalid ID\n " +
                            "Please enter the VALID ID of the transfer you'd like to view: (enter 0 to cancel): ");
                }
            }
        } else {
            System.out.println("No Transfer History");
        }


    }

    private void viewPendingRequests() {
        // TODO Auto-generated method stub
        Account userAccount = tenmoServices.getAccountByUserId(currentUser, currentUser.getUser().getId());
        int userAccountId = userAccount.getAccountId();
        Transfer [] pendingTransfers = tenmoServices.getPendingTransfers(currentUser,userAccountId);

        if(pendingTransfers != null){
            System.out.println("-------------------------------------------\n" +
                    "Pending Transfers\n" +
                    "ID          To                     Amount\n" +
                    "-------------------------------------------");
            for (Transfer pending : pendingTransfers) {

                String userName = tenmoServices.getUserByUserId(currentUser,
                        tenmoServices.getAccountByAccountId(currentUser, pending.getAccountFrom()).getUserId()).getUsername();
                //System.out.println( pending.getTransferId() + "\t\t" + userName + "\t\t" + pending.getAmount());

                System.out.printf("%-22d%-22s%-22s\n",pending.getTransferId(),userName,pending.getAmount());
            }

            System.out.println("---------\n" +
                    "Please enter transfer ID to approve/reject (0 to cancel): \"");
        }else{
            System.out.println("No Pending Transfer History");
        }
    }

    private void sendBucks() {
        // TODO Auto-generated method stub

        User[] users = tenmoServices.getAllUsers(currentUser);
        printUsers(users);

        int userInput = consoleService.promptForInt("Enter the ID of the user you want to send money to (enter 0 to cancel): ");

        if (userInput == 0 || userInput == currentUser.getUser().getId()) {
            System.out.println("Transaction cancelled, please enter a valid ID :)");
        } else {
            int toId = 0;
            for (User user : users) {
                if (user.getId() == userInput) {
                    toId = userInput;
                }
            }

            BigDecimal transferAmount = consoleService.promptForBigDecimal("Enter amount to send: ");
            while (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
                transferAmount = consoleService.promptForBigDecimal("Transfer amount must be more than 0. Enter amount to transfer: ");
            }
            while (transferAmount.compareTo(tenmoServices.getBalance(currentUser).getBalance()) == 1) {
                transferAmount = consoleService.promptForBigDecimal("Transfer amount cannot be more than user balance. Enter amount to transfer: ");
            }
            Transfer transfer = createTransfer(2, 2, toId, transferAmount);
            tenmoServices.transfer(currentUser, transfer);

            Balance newBalance = tenmoServices.getBalance(currentUser);
            checkBalance(newBalance);
            System.out.println("Successfully sent: $" + transferAmount);
            System.out.println("Your new balance is: $" + checkBalance(newBalance));

        }

    }


    private Transfer createTransfer(int status, int type, int UserId, BigDecimal transferAmount) {
        Transfer transfer = new Transfer();
        transfer.setTransferStatusId(status);
        transfer.setTransferTypeId(type);

        Account fromAccount = tenmoServices.getAccountByUserId(currentUser, UserId);
        Account toAccount = tenmoServices.getAccountByUserId(currentUser, (currentUser.getUser().getId()));

        if (type == 1) {
            fromAccount = tenmoServices.getAccountByUserId(currentUser, UserId);
            toAccount = tenmoServices.getAccountByUserId(currentUser, (currentUser.getUser().getId()));
        }

        if (type == 2) {
            fromAccount = tenmoServices.getAccountByUserId(currentUser, (currentUser.getUser().getId()));
            toAccount = tenmoServices.getAccountByUserId(currentUser, UserId);
        }
        transfer.setAccountFrom(fromAccount.getAccountId());
        transfer.setAccountTo(toAccount.getAccountId());
        transfer.setAmount(transferAmount);
        return transfer;
    }

    private void printUsers(User[] users) {
        System.out.println("Choose from the following users: ");
        for (User user : users) {
            if (user.getUsername().equals(currentUser.getUser().getUsername())) {
                continue;
            }
            System.out.println(user.getId() + ": " + user.getUsername());
        }
    }

    private BigDecimal checkBalance(Balance newBalance) {
        BigDecimal balance = BigDecimal.valueOf(0.00);
        if (!newBalance.getBalance().equals(BigDecimal.ZERO)) {
            balance = newBalance.getBalance();
        }
        return balance;
    }

    private void requestBucks() {
        // TODO Auto-generated method stub
        User[] users = tenmoServices.getAllUsers(currentUser);
        printUsers1(users);

        //prompting user to enter an user id to request from
        int userInput = consoleService.promptForInt("Enter ID of user you are requesting from (0 to cancel): ");

        if (userInput == 0) {
            System.out.println("Transaction cancelled.");
        } else if (userInput == currentUser.getUser().getId()) {
            System.out.println("Cannot request from yourself.");
        } else {
            int fromId = 0;
            for (User user : users) {
                if (userInput == user.getId()) {
                    fromId = userInput;
                    break;
                }
            }

            BigDecimal transferAmount = consoleService.promptForBigDecimal("Enter request amount: ");
            while (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
                transferAmount = consoleService.promptForBigDecimal("Request amount must be greater than zero. Enter request amount: ");
            }

            Transfer transfer = createTransfer(1, 1, fromId, transferAmount);
            tenmoServices.request(currentUser, transfer);

            System.out.println("Request sent successfully");
        }
    }

    private void printUsers1(User[] users) {
        System.out.println("-------------------------------------------\n" +
                "Users\n" +
                "ID          Name\n" +
                "-------------------------------------------");

        for (User user : users) {
            //ignoring the logged in user from displaying
            if (user.getUsername().equals(currentUser.getUser().getUsername())) continue;

            //printing the remaining users
            System.out.println(user.getId() + "\t\t" + user.getUsername());
        }
        System.out.println("-------------------------------------------");
    }

    private void printTransferDetails(Transfer transfer){
        System.out.println("--------------------------------------------\n" +
                "Transfer Details\n" +
                "--------------------------------------------");

        String accountToUsername = tenmoServices.getUserByUserId(currentUser,
                tenmoServices.getAccountByAccountId(currentUser, transfer.getAccountTo()).getUserId()).getUsername();

        String accountFromUsername = tenmoServices.getUserByUserId(currentUser,
                tenmoServices.getAccountByAccountId(currentUser, transfer.getAccountFrom()).getUserId()).getUsername();

        System.out.println("Id: " + transfer.getTransferId());
        System.out.println("From: " + accountFromUsername);
        System.out.println("To: " + accountToUsername);
        System.out.println("Type: " + getTransferType(transfer.getTransferTypeId()));
        System.out.println("Status: " + getTransferStatus(transfer.getTransferStatusId()));
        System.out.println("Amount: " + transfer.getAmount());
    }


    public TransferType getTransferType(int type){
        return (type == 1)? TransferType.Request : TransferType.Send;
    }

    public TransferStatus getTransferStatus(int status){
        if(status == 1) return TransferStatus.Pending;
        else if (status ==2) return TransferStatus.Approved;
        else return TransferStatus.Rejected;
    }

}
