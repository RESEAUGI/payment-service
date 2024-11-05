package com.mo.providers;





import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.mo.entities.Payment;
import com.mo.entities.ProductApp;
import com.mo.services.MyCoolPayOperator;
import com.mo.services.StripeOperator;
import com.stripe.exception.StripeException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
@Data @AllArgsConstructor @NoArgsConstructor
public class TransactionProvider {
	
	@Autowired
	private StripeOperator stripe;
	@Autowired
	private MyCoolPayOperator mycoolpay;

	private Gson gson = new Gson();
	
	
	public String suscribe(Payment payment, String api_key) throws StripeException {
		String response = null;
	    switch (payment.getPaymentMethod()) {
	        case card:
	            if (stripe.isValid(payment.getTransaction_amount(), payment.getTransaction_currency())) {
	            	//linkPay = stripe.payIn(product, api_key);
	            	response = stripe.payin(payment, api_key);
	            }
	            break;
	    }
	    return response;
	}
	
	
}