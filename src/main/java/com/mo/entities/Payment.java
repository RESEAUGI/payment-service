package com.mo.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Payment {
    private UUID id;
    private UUID reservationId;
    private BigDecimal amount;
    private String link;
    private String currency;
    private Instant timestamp;
    private String status;

    public Payment(UUID id, UUID reservationId, BigDecimal amount, String link, String currency, Instant timestamp, String status) {
        this.id = id;
        this.reservationId = reservationId;
        this.amount = amount;
        this.link = link;
        this.currency = currency;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
