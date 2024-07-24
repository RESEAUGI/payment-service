package com.mo.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Payment {
    private UUID id;
    private UUID product_id;
    private BigDecimal amount;
    private String currency;
    private Instant timestamp;
    private String status;
    
    
	public Payment(UUID id, UUID product_id, BigDecimal amount, String currency, Instant timestamp, String status) {
		super();
		this.id = id;
		this.product_id = product_id;
		this.amount = amount;
		this.currency = currency;
		this.timestamp = timestamp;
		this.status = status;
	}


	public UUID getId() {
		return id;
	}


	public void setId(UUID id) {
		this.id = id;
	}


	public UUID getProduct_id() {
		return product_id;
	}


	public void setProduct_id(UUID product_id) {
		this.product_id = product_id;
	}


	public BigDecimal getAmount() {
		return amount;
	}


	public void setAmount(BigDecimal amount) {
		this.amount = amount;
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
