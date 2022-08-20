package com.techelevator.tenmo.controller;
import com.techelevator.tenmo.dao.*;
import com.techelevator.tenmo.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "/tenmo")
public class TenmoController {

    private UserDao userDao;
    private AccountDao accountDao;
    private TransferDao transferDao;


    public TenmoController(UserDao userDao, AccountDao accountDao, TransferDao transferDao) {
        this.userDao = userDao;
        this.accountDao = accountDao;
        this.transferDao = transferDao;

    }

    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public Balance getBalance(Principal principal) {
        return accountDao.getBalance(principal.getName());
    }

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> getAllUsers() {
        return userDao.findAll();
    }


    @RequestMapping(path = "/account/users/{id}", method = RequestMethod.GET)
    public Account getAccountByUserId(@PathVariable long id) {
        return accountDao.getAccountWithUserId(id);
    }

    //@ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/transfer", method = RequestMethod.POST)
    public void transfer(@RequestBody Transfer transfer) {
        transferDao.transfer(transfer);
    }

}
