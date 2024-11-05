package com.mo.controllers;

import java.security.InvalidKeyException;
import com.google.gson.Gson;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mo.repositories.PaymentRepository;


import com.mo.entities.Payment;
import com.mo.providers.TransactionProvider;
import com.mo.services.StripeOperator;
import com.mo.services.MyCoolPayOperator;
import com.stripe.exception.StripeException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@Data @AllArgsConstructor @NoArgsConstructor
@CrossOrigin("*")
@RequestMapping("/api")
public class TansactionController {
	

	@Autowired
	private StripeOperator stripeOperator;
	@Autowired
	private MyCoolPayOperator myCoolPayOperator;
	@Autowired
	private TransactionProvider transactionProvider;
	@Autowired
	PaymentRepository paymentRepository;
	
	
	Gson gson = new Gson();
	
	

	 @PostMapping("/create-checkout-session/{api_key}")
	 public String createCheckoutSession(@RequestBody Payment payment, @PathVariable String api_key) throws StripeException {
		 System.out.println(payment);
		 return transactionProvider.suscribe(payment, api_key);
	 }
	 

	@PostMapping("/handle-payin")
	 public ResponseEntity<String> handleEvent(@RequestBody String payload, HttpServletRequest request) throws  InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		return stripeOperator.handle_payment(payload) ; 
	 }

	@GetMapping("/history")
	public Iterable<Payment> getPaymentHistory(){
		Iterable<Payment> payments = paymentRepository.findAll();
		return payments;
	}
	
	@GetMapping("/history/{consumer_id}")
	public Iterable<Payment> getPaymentHistoryByConsumer(@PathVariable String consumer_id){
		Iterable<Payment> payments = paymentRepository.findByConsumerId(consumer_id).get();
		return payments;
	}
		

//	@CrossOrigin
//	@PostMapping("/api_gateway")
//	public String res(@RequestBody String payload) {
//		System.out.println("Voici le paiement recu par l'api gateway:"+ payload);
//		return "okay";
//	}

}




