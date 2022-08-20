package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class UserServices {
    public final String API_BASE_URL;
    private RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;
    public UserServices(String url){this.API_BASE_URL = url;}
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    public BigDecimal getBalance(AuthenticatedUser currentUser){
        BigDecimal balance = null;
        try {
            ResponseEntity<BigDecimal> response =
                    restTemplate.exchange(API_BASE_URL + "/user/balance/", HttpMethod.GET, makeAuthEntity(currentUser), BigDecimal.class);
            balance = response.getBody();

        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return balance;
    };
    public List<Transfer> getAllTransfers(AuthenticatedUser currentUser){
        List transferList = new ArrayList<>();
        try {
            ResponseEntity<List> response =
                    restTemplate.exchange(API_BASE_URL + "/user/transfers", HttpMethod.GET, makeAuthEntity(currentUser), List.class);
            transferList = response.getBody();
        }catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transferList;
    }
    private HttpEntity<User> makeUserEntity(User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(user, headers);
    }
    private HttpEntity<Void> makeAuthEntity(AuthenticatedUser currentUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(headers);
    }


}
