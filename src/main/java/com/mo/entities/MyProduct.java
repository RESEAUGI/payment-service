package com.mo.entities;

import java.math.BigDecimal;
import java.util.UUID;

public class MyProduct {
	
	 private String product_id; 
	 private Long amount;
	 private String transaction_reason ;
	 private String phone_number ;
	 private String customer_name;
	 private String customer_email; 
	 private String langague ;
	 private String description ;
	 private String currency;
	 private PaymentType payment_type ;
	 
	 
	 
	public MyProduct(String product_id, Long amount, String transaction_reason, String phone_number, String customer_name,
			String customer_email, String langague, String description, String currency, PaymentType  payment_type) {
		super();
		this.product_id = product_id;
		this.amount = amount;
		this.transaction_reason = transaction_reason;
		this.phone_number = phone_number;
		this.customer_name = customer_name;
		this.customer_email = customer_email;
		this.langague = langague;
		this.description = description;
		this.currency = currency;
		this.payment_type = payment_type;
	}



	public String getProduct_id() {
		return product_id;
	}



	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}



	public Long getAmount() {
		return amount;
	}



	public void setAmount(Long amount) {
		this.amount = amount;
	}



	public String getTransaction_reason() {
		return transaction_reason;
	}



	public void setTransaction_reason(String transaction_reason) {
		this.transaction_reason = transaction_reason;
	}



	public String getPhone_number() {
		return phone_number;
	}



	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}



	public String getCustomer_name() {
		return customer_name;
	}



	public void setCustomer_name(String tRcustomer_name) {
		customer_name = tRcustomer_name;
	}



	public String getCustomer_email() {
		return customer_email;
	}



	public void setCustomer_email(String customer_email) {
		this.customer_email = customer_email;
	}



	public String getLangague() {
		return langague;
	}



	public void setLangague(String langague) {
		this.langague = langague;
	}



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public String getCurrency() {
		return currency;
	}



	public void setCurrency(String currency) {
		this.currency = currency;
	}



	public PaymentType  getPayment_type() {
		return payment_type;
	}



	public void setPayment_type(PaymentType  payment_type) {
		this.payment_type = payment_type;
	}
}
