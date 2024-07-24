package com.mo.controllers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mo.entities.MyCoolPayOperator;
import com.mo.entities.MyProduct;
import com.mo.entities.Payment;
import com.mo.entities.PaymentType;
import com.mo.entities.StripeOperator;
import com.mo.providers.PaymentProvider;
import com.mo.stripe.LinkPay;
import com.mo.controllers.CommunMethodsController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class PaymentsController {
	
	@Autowired
	private CommunMethodsController communMethodsController;

	private PaymentProvider  paymentProvider;
	private PaymentType paymentType ;
	private StripeOperator stipeOperator;
	private MyCoolPayOperator myCoolPayOperator;
	
	
	
	
	
	public PaymentsController(PaymentProvider paymentProvider,  StripeOperator stipeOperator,
			MyCoolPayOperator myCoolPayOperator) {
		super();
		this.paymentProvider = paymentProvider;
		
		this.stipeOperator = stipeOperator;
		this.myCoolPayOperator = myCoolPayOperator;
	}
	
	

	public PaymentProvider getPaymentProvider() {
		return paymentProvider;
	}



	public void setPaymentProvider(PaymentProvider paymentProvider) {
		this.paymentProvider = paymentProvider;
	}



	public PaymentType getPaymentType() {
		return paymentType;
	}



	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}



	public StripeOperator getStipeOperator() {
		return stipeOperator;
	}



	public void setStipeOperator(StripeOperator stipeOperator) {
		this.stipeOperator = stipeOperator;
	}



	public MyCoolPayOperator getMyCoolPayOperator() {
		return myCoolPayOperator;
	}



	public void setMyCoolPayOperator(MyCoolPayOperator myCoolPayOperator) {
		this.myCoolPayOperator = myCoolPayOperator;
	}


	@CrossOrigin
	@PostMapping("/handle-payin")
	
	 public ResponseEntity<String> handleEvent(@RequestBody String payload, HttpServletRequest request) throws PulsarClientException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		return stipeOperator.handle_payment(payload) ; 
	 }
	@CrossOrigin
	@PostMapping("/handle-coolpay")
	public ResponseEntity<String> handleEventCoolpay(@RequestBody String payload, HttpServletRequest request) throws PulsarClientException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		return myCoolPayOperator.handle_payment(payload) ; 
	 }
	
	@CrossOrigin
	@GetMapping("/history")
	public ResponseEntity<List<Payment>>  GetHiatory() {
		List<Payment> paymentHistory = communMethodsController.getPaymentHistory(); 
		if (paymentHistory.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(paymentHistory);
	 }
	
	
	
	
	 @CrossOrigin
	 @PostMapping("/payin")
	 public String createPaymentLink(@RequestBody  MyProduct product) {
		 System.out.print(product.getPayment_type());
		 String url = paymentProvider.suscribe(product);
		 return url;
	 }
	 
	 
	
			

}




