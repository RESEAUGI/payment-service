package com.mo.entities;

import java.sql.Timestamp;
import java.util.UUID;


public class DataOut {
	private UUID product_id; 
	private Timestamp timestamp;
	
	public DataOut(UUID product_id, Timestamp timestamp) {
		super();
		this.product_id = product_id;
		this.timestamp = timestamp;
	}

	public UUID getProduct_id() {
		return product_id;
	}

	public void setProduct_id(UUID product_id) {
		this.product_id = product_id;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	
	
	

}
