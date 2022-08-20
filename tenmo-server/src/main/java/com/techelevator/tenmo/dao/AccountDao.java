package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Balance;

public interface AccountDao {

    Balance getBalance(String username);
    Account getAccountWithUserId(Long userId);
    Account getAccountWithAccountId(Long accountId);
}
