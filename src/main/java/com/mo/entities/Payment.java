package com.mo.entities;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("payments")
@Data @AllArgsConstructor @NoArgsConstructor
public class Payment {
	
	@PrimaryKey
	String id;
	private String sessionId;
	private String product_name;
	private String product_description ;
	private double transaction_amount;
	private String transaction_currency;
	private String customer_name;
	private String customer_phone_number;
	private String customer_email;
	private String eventType;
	private Date paymentDateTime;
	private TransactionMethod paymentMethod;
	@Indexed
	private String consumerId;
	private String application;
    private String app_transaction_ref;
    private String operator_transaction_ref;
    private String transaction_ref;
    private String transaction_type;
    private int transaction_fees;
    private String transaction_operator;
    private String transaction_status;
    private String transaction_reason;
    private String transaction_message;
    private String signature;
	
	@CassandraType(type = CassandraType.Name.MAP, typeArguments = {CassandraType.Name.TEXT, CassandraType.Name.TEXT})
    private Map<String, Object> metadata;
	
	
	

}
