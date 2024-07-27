package com.mo.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Payment {
    private UUID id;
    private String product_id;
    private BigDecimal amount;
    private String currency;
    private String customer_id;
    private String producer_id;
    private Instant timestamp;
    private String status;
    
    
	public Payment(UUID id, String product_id, BigDecimal amount, String currency, String customer_id, String producer_id, Instant timestamp, String status) {
		super();
		this.id = id;
		this.product_id = product_id;
		this.amount = amount;
		this.currency = currency;
		this.customer_id = customer_id;
		this.producer_id = producer_id;
		this.timestamp = timestamp;
		this.status = status;
	}


	public UUID getId() {
		return id;
	}


	public void setId(UUID id) {
		this.id = id;
	}


	public String getProduct_id() {
		return product_id;
	}


	public void setProduct_id(String product_id) {
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

    
	
	public String getCustomer_id() {
		return customer_id;
	}


	public void setCustomer_id(String customer_id) {
		this.customer_id = customer_id;
	}


	public String getProducer_id() {
		return producer_id;
	}


	public void setProducer_id(String producer_id) {
		this.producer_id = producer_id;
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
