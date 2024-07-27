package com.mo.entities;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mo.controllers.CommunMethodsController;
import com.mo.interfaces.CallbackInterface;
import com.mo.interfaces.PaymentInterface;
import com.mo.interfaces.ValidateDataInterface;
import com.mo.stripe.LinkPay;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.LineItemCollection;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionListLineItemsParams;

import jakarta.annotation.PostConstruct;


@Controller
public class StripeOperator implements CallbackInterface, ValidateDataInterface, PaymentInterface {
	
	private CommunMethodsController communMethodsController;

	private Session resource ;
	
	private String sessionPayId;
		
	private CqlSession session;
	
	private Gson gson = new Gson();
	
	private final PulsarClient client;
	
	
	  @Autowired
	 public StripeOperator(CommunMethodsController communMethodsController, PulsarClient client) {
		super();
		this.communMethodsController = communMethodsController;
		this.client = client;
		
	}
	 
	 

	public CommunMethodsController getCommunMethodsController() {
		return communMethodsController;
	}



	public void setCommunMethodsController(CommunMethodsController communMethodsController) {
		this.communMethodsController = communMethodsController;
	}



	public Session getResource() {
		return resource;
	}



	public void setResource(Session resource) {
		this.resource = resource;
	}



	public String getSessionPayId() {
		return sessionPayId;
	}



	public void setSessionPayId(String sessionPayId) {
		this.sessionPayId = sessionPayId;
	}



	public Gson getGson() {
		return gson;
	}



	public void setGson(Gson gson) {
		this.gson = gson;
	}



	public String getApiKey() {
		return apiKey;
	}



	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}



	public void setSession(CqlSession session) {
		this.session = session;
	}



	@PostConstruct
	    public void initialize() {
	        connect("127.0.0.1", 9042, "datacenter1");
	    }

    public void connect(String node, Integer port, String dataCenter) {
        CqlSessionBuilder builder = CqlSession.builder();
        builder.addContactPoint(new InetSocketAddress("127.0.0.1", 9042));
        builder.withLocalDatacenter(dataCenter);

        session = builder.build();
    }

    public CqlSession getSession() {
        return this.session;
    }

    public void close() {
        session.close();
    }
    
    
    private String apiKey = "sk_test_51PVJciERlR7Uy2xt1I68IsVZWLzrQJkYxJwVhf8ie3cjFZ3Q7Pi9E68luHyBY7zA4BwmCpZgoomnJ5TngoybPUC400GIRU8bD0";
    

	@Override
	
	public LinkPay payIn(MyProduct product) {
	    Stripe.apiKey = apiKey;
	    String YOUR_DOMAIN = "http://localhost:3000";
	    String tableName = "paiement";
	    SessionCreateParams params = SessionCreateParams.builder()
	            .setMode(SessionCreateParams.Mode.PAYMENT)
	            .setSuccessUrl(YOUR_DOMAIN + "/success")
	            .setCancelUrl(YOUR_DOMAIN + "/cancel")
	            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
	            .addLineItem(
	                    SessionCreateParams.LineItem.builder()
	                            .setQuantity(1L)
	                            .setPriceData(
	                                    SessionCreateParams.LineItem.PriceData.builder()
	                                            .setCurrency(product.getCurrency())
	                                            .setUnitAmount(product.getAmount())
	                                            .setProductData(
	                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
	                                                            .setName(product.getTransaction_reason())
	                                                            .setDescription(product.getDescription())
	                                                            .build())
	                                            .build())
	                            .build())
	            .putMetadata("service_name", product.getTransaction_reason().toString()) // Ajouter l'identifiant personnel aux métadonnées
	            .putMetadata("product_id", product.getProduct_id()) // Ajouter le product_id aux métadonnées
	            .build();
	    try {
	        Session session = Session.create(params);
	        String url = session.getUrl();
	        String sessionId = session.getId(); 

	        LinkPay linkPay = new LinkPay(url);
	        System.out.println(session.getUrl());
	        System.out.println("l'identifiant de la session est:  " + session.getId());
	        this.sessionPayId = session.getId();
	                   
	        System.out.print(communMethodsController.insertData(UUID.randomUUID(),new BigDecimal(product.getAmount()),product.getCurrency(),"c1","p1", product.getProduct_id(), "pending", Instant.now(), tableName));
	        return linkPay;
	    } catch (StripeException e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	

	@Override
	public int operator() {
		return 1;
	}

	@Override
	public boolean isValid(Long amount, String currency) {
		if(currency.equals("USD") || currency.equals("EUR") || currency.equals("XAF")){
			return true;
		}else { 
			return false;
			}
	}
	
	
	

	@Override
	public ResponseEntity<String> handle_payment(String payload) throws PulsarClientException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException  {
		 Event event = null;

	        try {
	            event = gson.fromJson(payload, Event.class);
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
						resource = Session.retrieve(this.sessionPayId);
						String product_id  = resource.getMetadata().get("product_id");
						System.out.println("voici l'identifiant de la session"+resource.getId() );
						communMethodsController.getPaiementByPoductId(product_id);
						SessionListLineItemsParams params = SessionListLineItemsParams.builder().build();
						if(resource != null) {
		                	String session_id = resource.getId();
		                	Long amount = resource.getAmountTotal();
		                    String service_name = resource.getMetadata().get("service_name");
		                    
		                    System.out.println(service_name);
		           
		                    ResponseEntity<?> response = communMethodsController.getPaiementByPoductId(product_id);
		              
		                    if (response.getBody() != null) {
		                    	communMethodsController.updatePaymentStatus(product_id, "success", "paiement"); 
	                        
	                            Instant instant = Instant.now();
	                            Timestamp timestamp = Timestamp.from(instant);
	                            String currency = resource.getCurrency();
	                            String encryptedHash = communMethodsController.encrypt(product_id+amount+currency);
		                        if (service_name.equals("reservation")) {
		                        	
		                            communMethodsController.sendMessageToPulsarTopic(product_id+ " " +amount+ " "+timestamp+" "+encryptedHash, service_name) ;//envoie de l'id via aphache pulsar
		                            System.out.println("L'identifiant du produit est : " + product_id);
		                        }
		                        
		                    	if (service_name.equals("souscription")) {
		          
		                            communMethodsController.sendMessageToPulsarTopic(product_id+ " " +amount+ " "+timestamp+" "+encryptedHash, service_name);//envoie de l'id via aphache pulsar
		                            System.out.println("L'identifiant du produit est : " + product_id);	                        
		                        }
		                    
		                    }
		                        
		                }
					} catch (StripeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                break;    
	            	 
	            // ... handle other event types
	            default:
	                System.out.println("Unhandled event type: " + event.getType());
	        }

	        return ResponseEntity.ok().build();

	}

}
