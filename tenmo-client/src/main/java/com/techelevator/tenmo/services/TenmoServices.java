package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.*;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class TenmoServices {

    private final String baseUrl = "http://localhost:8080/tenmo";
    private final RestTemplate restTemplate = new RestTemplate();

    private String authToken = null;


    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }


    public Balance getBalance(AuthenticatedUser authenticatedUser) {
        Balance balance = null;
        try {
            ResponseEntity<Balance> response = restTemplate.exchange(baseUrl + "/balance", HttpMethod.GET, makeAuthEntity(authenticatedUser), Balance.class);
            balance = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return balance;
    }


    public void transfer(AuthenticatedUser authenticatedUser, Transfer transfer) {
        try {
            restTemplate.exchange(baseUrl + "/transfer", HttpMethod.POST, makeTransferAuthEntity(authenticatedUser, transfer), Transfer.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
    }


    public User[] getAllUsers(AuthenticatedUser authenticatedUser) {
        User[] users = null;
        try {
            ResponseEntity<User[]> response = restTemplate.exchange(baseUrl + "/users", HttpMethod.GET, makeAuthEntity(authenticatedUser), User[].class);
            users = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return users;
    }

    public Account getAccountByUserId(AuthenticatedUser authenticatedUser, int userId) {
        Account account = null;
        try {
            ResponseEntity<Account> response = restTemplate.exchange(baseUrl + "/account/users/" + userId, HttpMethod.GET, makeAuthEntity(authenticatedUser), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }
    public Transfer[] getTransferHistory(AuthenticatedUser authenticatedUser){
        Transfer[] transferList = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(baseUrl + "/review_transfers", HttpMethod.GET, makeAuthEntity(authenticatedUser), Transfer[].class);
            transferList = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transferList;
    }



    private HttpEntity makeAuthEntity(AuthenticatedUser authenticatedUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authenticatedUser.getToken());
        return new HttpEntity(headers);
    }


    private HttpEntity<Transfer> makeTransferAuthEntity(AuthenticatedUser authenticatedUser, Transfer transfer){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authenticatedUser.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Transfer> entity = new HttpEntity(transfer, headers);
        return entity;
    }



}
