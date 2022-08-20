package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao{


    private JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void transfer(Transfer transfer) {
        boolean approved = transfer.getTransferStatusId() == 2;
        AccountDao accountDao = new JdbcAccountDao(jdbcTemplate);
        int fromAccountId = transfer.getAccountFrom();
        int toAccountId = transfer.getAccountTo();
        Account fromAccount = accountDao.getAccountWithAccountId((long) fromAccountId);
        Account toAccount = accountDao.getAccountWithAccountId((long) toAccountId);
        BigDecimal fromAccountBalance = fromAccount.getBalance().getBalance();
        BigDecimal toAccountBalance = toAccount.getBalance().getBalance();
        BigDecimal transferAmount = transfer.getAmount();
        if (approved) {
            fromAccountBalance = fromAccountBalance.subtract(transferAmount);
            toAccountBalance = toAccountBalance.add(transferAmount);
        }
        String sql = "START TRANSACTION; " +
                "UPDATE account " +
                "SET balance = ? " +
                "WHERE account_id = ?; " +
                "UPDATE account " +
                "SET balance = ? " +
                "WHERE account_id = ?; " +
                "INSERT INTO transfer (transfer_type_id, transfer_status_id,account_from, account_to, amount) " +
                " VALUES (?, ?, ?, ?, ?);" +
                "COMMIT;";

        jdbcTemplate.update(sql, fromAccountBalance, fromAccountId, toAccountBalance, toAccountId,
                transfer.getTransferTypeId(), transfer.getTransferStatusId(), fromAccountId, toAccountId, transferAmount);
   }




    private Transfer mapRowToTransfer(SqlRowSet results) {

        Transfer transfer = new Transfer();
        transfer.setTransferId(results.getInt("transfer_id"));
        transfer.setTransferTypeId(results.getInt("transfer_type_id"));
        transfer.setTransferStatusId(results.getInt("transfer_status_id"));
        transfer.setAccountFrom(results.getInt("account_from"));
        transfer.setAccountTo(results.getInt("account_to"));
        String stringAmount = results.getString("amount");
        transfer.setAmount(new BigDecimal(stringAmount));
        return transfer;
    }
}
