package com.mo.interfaces;

import com.mo.entities.Payment;

public interface PaymentInterface {
	public String payin(Payment payment, String api_key);
	public int  operator();
	
}
