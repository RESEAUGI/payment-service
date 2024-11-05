package com.mo.services;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import com.mo.entities.StartupRequestSender;
import com.mo.entities.Payment;
import com.mo.entities.ProductApp;
import com.mo.interfaces.CallbackInterface;
import com.mo.interfaces.PaymentInterface;
import com.mo.interfaces.ValidateDataInterface;
import com.mo.repositories.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.LineItemCollection;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionListLineItemsParams;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
public class StripeOperator implements CallbackInterface, ValidateDataInterface, PaymentInterface {
	
	
    //private String apiKey = "sk_test_51PVJciERlR7Uy2xt1I68IsVZWLzrQJkYxJwVhf8ie3cjFZ3Q7Pi9E68luHyBY7zA4BwmCpZgoomnJ5TngoybPUC400GIRU8bD0";
	
	private Session resource ;
	
	private String sessionPayId;
		
	private CqlSession session;
	
	private Gson gson = new Gson();
	
	
	@Autowired
	private PaymentRepository paymentRepository;
	
	@Autowired
	private PaymentService paymentService;
	
	private final String api_gateway = "http://localhost:8081/api_gateway";
	
	
	
	
	
	
 	@Override
 	public String payin(Payment payment, String api_key) {				
 	    SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
 	            .setMode(SessionCreateParams.Mode.PAYMENT)
 	            .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
 	            .setReturnUrl("http://localhost:3000/success?session_id={CHECKOUT_SESSION_ID}")
 	           .setExpiresAt(Instant.now().getEpochSecond() + Duration.ofMinutes(60).getSeconds())
 	            .addLineItem(
 	                    SessionCreateParams.LineItem.builder()
 	                            .setQuantity(1L)
 	                            .setPriceData(
 	                                    SessionCreateParams.LineItem.PriceData.builder()
 	                                            .setCurrency(payment.getTransaction_currency())
 	                                            .setUnitAmountDecimal(new BigDecimal(payment.getTransaction_amount()))
 	                                            .setProductData(
 	                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
 	                                                            .setName(payment.getProduct_name())
 	                                                            .setDescription(payment.getProduct_description())
 	                                                            .build())
 	                                            .build())
 	                            .build());

 	    // Vérifier si metadata n'est pas vide avant de le parcourir
 	    payment.setId(UUID.randomUUID().toString());
 	    paramsBuilder.putMetadata("payment_id", payment.getId());
 	    if (payment.getMetadata() != null && !payment.getMetadata().isEmpty()) {
 	        for (Map.Entry<String, Object> entry : payment.getMetadata().entrySet()) {
 	            paramsBuilder.putMetadata(entry.getKey(), entry.getValue().toString());
 	        }
 	    }
 	    
 	   paramsBuilder.setCustomerEmail("mosanisangou@gmail.com");

 	    SessionCreateParams params = paramsBuilder.build();

 	    Session session = null;
		try {
			session = Session.create(params);
		} catch (StripeException e) {
			System.out.print(e.getMessage());
		}
		
		if(session != null) 
		{
			this.sessionPayId = session.getId();

	 	    Map<String, String> map = new HashMap<>();
	 	    map.put("clientSecret", session.getRawJsonObject().getAsJsonPrimitive("client_secret").getAsString());
	 	    payment.setTransaction_status("pending");
	 	    paymentRepository.save(payment);
	 	    paymentService.schedulePaymentUpdateTask(payment.getId());
	 	    return gson.toJson(map);
		}
		
		return null;
 	}
	

	

	@Override
	public ResponseEntity<String> handle_payment(String payload) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException  {
		 Event event = null;
		 //System.out.println(payload);
	        try {
	            event = this.gson.fromJson(payload, Event.class);
	        } catch (JsonSyntaxException e) {
	            // Invalid payload
	            return ResponseEntity.badRequest().build();
	        }

	        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
	        StripeObject stripeObject = null;
	        if (dataObjectDeserializer.getObject().isPresent()) {
	            stripeObject = dataObjectDeserializer.getObject().get();
	        } else {
	            // Deserialization failed, probably due to an API version mismatch.
	            // Handle this case accordingly or return an error response.
	        }
	        switch (event.getType()) {
	            case "checkout.session.completed":
					try {
						Session resource = Session.retrieve(this.sessionPayId);
					
						SessionListLineItemsParams params = SessionListLineItemsParams.builder().build();
						if(resource != null) {
							String product_metadata  = resource.getMetadata().get("metadata");
							System.out.println("voici les metadata du produit***************"+resource.getMetadata());
							Gson gson = new Gson();
						    JsonObject metadataJson = gson.fromJson(product_metadata, JsonObject.class);
						    Payment payment = paymentRepository.findById(resource.getMetadata().get("payment_id")).get();
						    payment.setTransaction_status("success");
						    payment.setTransaction_fees(0);
						    payment.setPaymentDateTime(new Date());
						    paymentRepository.save(payment);
						    StartupRequestSender startupRequestSender = new StartupRequestSender(api_gateway,gson.toJson(payment));
						    startupRequestSender.onApplicationEvent(null);
      
		                }
					} catch (StripeException e) {
						e.printStackTrace();
					}
	                break;    
	            	 
	            // ... handle other event types
	            default:
	                System.out.println("Unhandled event type: " + event.getType());
	        }

	        return ResponseEntity.ok().build();

	}





	@Override
	public int operator() {
		// TODO Auto-generated method stub
		return 0;
	}





	@Override
	public boolean isValid(double amount, String currency) {
	    if (amount > 0 && (currency.equals("XAF") || ( currency.equals("USD") || currency.equals("EUR") && amount>=1))) {
	        return true;
	    } else {
	        return false;
	    }
	}


	
}
