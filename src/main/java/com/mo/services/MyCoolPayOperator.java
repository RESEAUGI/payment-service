package com.mo.services;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import com.mo.dtos.MyCoolPayDTO;
import com.mo.entities.Payment;
import com.mo.entities.StartupRequestSender;
import com.mo.interfaces.CallbackInterface;
import com.mo.interfaces.PaymentInterface;
import com.mo.interfaces.ValidateDataInterface;
import com.mo.repositories.PaymentRepository;
import com.stripe.model.checkout.Session;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.google.gson.Gson;


import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Data @AllArgsConstructor @NoArgsConstructor
@Service
public class MyCoolPayOperator implements CallbackInterface, PaymentInterface, ValidateDataInterface {
	
	private Session resource ;
	
	private String sessionPayId;
		
	private CqlSession session;
	
	@Autowired
	PaymentRepository paymentRepository;
	
	private Gson gson = new Gson();
	
	private final String api_gateway = "http://localhost:8081/api_gateway";
	
    private String myCoolPayKey ="5a219fd9-b249-4a58-b362-1448584ffb42";

	@Override
	public boolean isValid(double amount, String currency) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public ResponseEntity<String> payin(String product) {
	    RestTemplate restTemplate = new RestTemplate();
	    String url = "https://my-coolpay.com/api/5a219fd9-b249-4a58-b362-1448584ffb42/payin";
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);

	    HttpEntity<String> request = new HttpEntity<>(product, headers);

	    try {
	        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
	        System.out.println(responseEntity.getBody());
	        return responseEntity;
	    } catch (HttpClientErrorException e) {
	        System.err.println("Client Error: " + e.getStatusCode() + " - " + e.getStatusText());
	        // Gérer les erreurs spécifiques aux codes d'état HTTP 4xx
	    } catch (ResourceAccessException e) {
	        System.err.println("Connection Error: " + e.getMessage());
	        // Gérer les erreurs de connexion (par exemple, timeout)
	    } catch (Exception e) {
	        System.err.println("Error: " + e.getMessage());
	        // Gérer d'autres exceptions imprévues
	    }

	    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
	}

	@Override
	public int operator() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ResponseEntity<String> handle_payment(@RequestBody String payload) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
        Payment payment = gson.fromJson(payload, Payment.class);
        
        if(payment.getTransaction_status().equalsIgnoreCase("success")) {
        	paymentRepository.save(payment);
        	 StartupRequestSender startupRequestSender = new StartupRequestSender(api_gateway,gson.toJson(payment));
			 startupRequestSender.onApplicationEvent(null);
        }
        return ResponseEntity.ok("Payment handled successfully.");

	}


	@Override
	public String payin(Payment payment, String api_key) {
		// TODO Auto-generated method stub
		return null;
	}
	
	


}
