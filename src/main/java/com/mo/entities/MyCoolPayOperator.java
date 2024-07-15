package com.mo.entities;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.mo.interfaces.CallbackInterface;
import com.mo.interfaces.PaymentInterface;
import com.mo.interfaces.ValidateDataInterface;
import com.mo.stripe.LinkPay;

public class MyCoolPayOperator implements CallbackInterface, PaymentInterface, ValidateDataInterface {

	@Override
	public boolean isValid(Long amount, String currency) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkPay payIn(MyProduct product) {
		// TODO Auto-generated method stub
	
		return null;

	}

	@Override
	public int operator() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ResponseEntity<String> handle_payment(@RequestBody String payload) {
		return null;
		// TODO Auto-generated method stub

	}

}
