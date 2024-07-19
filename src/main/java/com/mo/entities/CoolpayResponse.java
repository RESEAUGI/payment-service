package com.mo.entities;

public class CoolpayResponse {
	private String application;
    private String app_transaction_ref;
    private String operator_transaction_ref;
    private String transaction_ref;
    private String transaction_type;
    private int transaction_amount;
    private int transaction_fees;
    private String transaction_currency;
    private String transaction_operator;
    private String transaction_status;
    private String transaction_reason;
    private String transaction_message;
    private String customer_phone_number;
    private String signature;
	public CoolpayResponse(String application, String app_transaction_ref, String operator_transaction_ref,
			String transaction_ref, String transaction_type, int transaction_amount, int transaction_fees,
			String transaction_currency, String transaction_operator, String transaction_status,
			String transaction_reason, String transaction_message, String customer_phone_number, String signature) {
		super();
		this.application = application;
		this.app_transaction_ref = app_transaction_ref;
		this.operator_transaction_ref = operator_transaction_ref;
		this.transaction_ref = transaction_ref;
		this.transaction_type = transaction_type;
		this.transaction_amount = transaction_amount;
		this.transaction_fees = transaction_fees;
		this.transaction_currency = transaction_currency;
		this.transaction_operator = transaction_operator;
		this.transaction_status = transaction_status;
		this.transaction_reason = transaction_reason;
		this.transaction_message = transaction_message;
		this.customer_phone_number = customer_phone_number;
		this.signature = signature;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getApp_transaction_ref() {
		return app_transaction_ref;
	}
	public void setApp_transaction_ref(String app_transaction_ref) {
		this.app_transaction_ref = app_transaction_ref;
	}
	public String getOperator_transaction_ref() {
		return operator_transaction_ref;
	}
	public void setOperator_transaction_ref(String operator_transaction_ref) {
		this.operator_transaction_ref = operator_transaction_ref;
	}
	public String getTransaction_ref() {
		return transaction_ref;
	}
	public void setTransaction_ref(String transaction_ref) {
		this.transaction_ref = transaction_ref;
	}
	public String getTransaction_type() {
		return transaction_type;
	}
	public void setTransaction_type(String transaction_type) {
		this.transaction_type = transaction_type;
	}
	public int getTransaction_amount() {
		return transaction_amount;
	}
	public void setTransaction_amount(int transaction_amount) {
		this.transaction_amount = transaction_amount;
	}
	public int getTransaction_fees() {
		return transaction_fees;
	}
	public void setTransaction_fees(int transaction_fees) {
		this.transaction_fees = transaction_fees;
	}
	public String getTransaction_currency() {
		return transaction_currency;
	}
	public void setTransaction_currency(String transaction_currency) {
		this.transaction_currency = transaction_currency;
	}
	public String getTransaction_operator() {
		return transaction_operator;
	}
	public void setTransaction_operator(String transaction_operator) {
		this.transaction_operator = transaction_operator;
	}
	public String getTransaction_status() {
		return transaction_status;
	}
	public void setTransaction_status(String transaction_status) {
		this.transaction_status = transaction_status;
	}
	public String getTransaction_reason() {
		return transaction_reason;
	}
	public void setTransaction_reason(String transaction_reason) {
		this.transaction_reason = transaction_reason;
	}
	public String getTransaction_message() {
		return transaction_message;
	}
	public void setTransaction_message(String transaction_message) {
		this.transaction_message = transaction_message;
	}
	public String getCustomer_phone_number() {
		return customer_phone_number;
	}
	public void setCustomer_phone_number(String customer_phone_number) {
		this.customer_phone_number = customer_phone_number;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
    
	
}
