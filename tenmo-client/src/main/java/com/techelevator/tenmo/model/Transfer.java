package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Transfer {
    private Long id;
    private Long fromId;
    private Long toId;
    private BigDecimal transferAmount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFromId() {
        return fromId;
    }

    public void setFromId(Long fromId) {
        this.fromId = fromId;
    }

    public Long getToId() {
        return toId;
    }

    public void setToId(Long toId) {
        this.toId = toId;
    }

    public BigDecimal getTransferAmout() {
        return transferAmount;
    }

    public void setTransferAmout(BigDecimal transferAmout) {
        this.transferAmount = transferAmout;
    }
}
