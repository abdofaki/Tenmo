package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao{
    private JdbcTemplate jdbcTemplate;
    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Transfer> listAllTransfers(String userName){
        List<Transfer> transferList = new ArrayList<>();
        String sql = "SELECT * FROM transfer WHERE account_from = (SELECT account_id FROM account " +
                "WHERE user_id = (SELECT account_id FROM account WHERE user_id = (SELECT user_id FROM user WHERE user_name = ?) " +
                "OR account_to = (SELECT account_id FROM account WHERE user_id = (SELECT user_id FROM user WHERE user_name = ?))";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, userName, userName);
        while (rs.next()){
            transferList.add(mapRowToTransfer(rs));
        }
        return transferList;
    }

    private Transfer mapRowToTransfer(SqlRowSet rs){
        Transfer transfer = new Transfer();
        transfer.setId(rs.getLong("transfer_id"));
        transfer.setFromId(rs.getLong("account_from"));
        transfer.setToId(rs.getLong("account_to"));
        transfer.setTransferAmount(rs.getBigDecimal("amount"));
        return transfer;
    }
}
