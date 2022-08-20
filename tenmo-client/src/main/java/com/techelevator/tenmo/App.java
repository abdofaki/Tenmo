package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TenmoServices;
import com.techelevator.tenmo.services.UserServices;

import java.math.BigDecimal;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final UserServices userService = new UserServices(API_BASE_URL);
    private final TenmoServices tenmoServices = new TenmoServices();


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
        List<Transfer> transferList = userService.getAllTransfers(currentUser);
        if (transferList != null) {
            for (Transfer transfer : transferList) {
                System.out.println(transfer.toString());
            }
        } else{
            System.out.println("No Transfer History");
        }


        
	}

	private void viewPendingRequests() {
		// TODO Auto-generated method stub
		
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
        Account toAccount = tenmoServices.getAccountByUserId(currentUser,(currentUser.getUser().getId()));

        if (type == 2) {
            fromAccount = tenmoServices.getAccountByUserId(currentUser,(currentUser.getUser().getId()));
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
		
	}




}
