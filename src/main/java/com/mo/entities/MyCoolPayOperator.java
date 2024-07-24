package com.mo.entities;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.pulsar.client.api.PulsarClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.mo.controllers.CommunMethodsController;
import com.mo.interfaces.CallbackInterface;
import com.mo.interfaces.PaymentInterface;
import com.mo.interfaces.ValidateDataInterface;
import com.mo.stripe.LinkPay;
import com.stripe.model.checkout.Session;

import jakarta.annotation.PostConstruct;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.google.gson.Gson;



public class MyCoolPayOperator implements CallbackInterface, PaymentInterface, ValidateDataInterface {
	
	private CommunMethodsController communMethodsController;

	private Session resource ;
	
	private String sessionPayId;
		
	private CqlSession session;
	
	private Gson gson = new Gson();
	
	private final PulsarClient client;
	
	
	  @Autowired
	 public MyCoolPayOperator(CommunMethodsController communMethodsController, PulsarClient client) {
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
	public ResponseEntity<String> handle_payment(@RequestBody String payload) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
//		CoolpayResponse coolpayResponse = new CoolpayResponse()
//		System.out.print(payload);
		
		//String json = "{\"application\":\"{public_key}\",\"app_transaction_ref\":\"order_123\",\"operator_transaction_ref\":\"MP200618.1634.A34527\",\"transaction_ref\":\"18ac6335-2bdd-4b95-944e-ef029c49c5b5\",\"transaction_type\":\"PAYIN\",\"transaction_amount\":100,\"transaction_fees\":2,\"transaction_currency\":\"XAF\",\"transaction_operator\":\"CM_OM\",\"transaction_status\":\"SUCCESS\",\"transaction_reason\":\"Bic pen\",\"transaction_message\":\"Your transaction has been successfully completed\",\"customer_phone_number\":\"699009900\",\"signature\":\"d41d8cd98f00b204e9800998ecf8427e\"}";
		
		String json = payload; // Utilisez le payload reçu du formulaire

        Gson gson = new Gson();
        CoolpayResponse transaction = gson.fromJson(json, CoolpayResponse.class);

        // Affichez tous les attributs de l'objet transaction
        System.out.println("Application: " + transaction.getApplication());
        System.out.println("App Transaction Ref: " + transaction.getOperator_transaction_ref());
        System.out.println("Operator Transaction Ref: " + transaction.getApp_transaction_ref());
        System.out.println("Transaction Ref: " + transaction.getApp_transaction_ref());
        System.out.println("Transaction Type: " + transaction.getTransaction_type());
        System.out.println("Transaction Amount: " + transaction.getTransaction_amount());
        System.out.println("Transaction Fees: " + transaction.getTransaction_fees());
        System.out.println("Transaction Currency: " + transaction.getTransaction_currency());
        System.out.println("Transaction Operator: " + transaction.getTransaction_operator());
        System.out.println("Transaction Status: " + transaction.getTransaction_status());
        System.out.println("Transaction Reason: " + transaction.getTransaction_reason());
        System.out.println("Transaction Message: " + transaction.getTransaction_message());
        System.out.println("Customer Phone Number: " + transaction.getCustomer_phone_number());
        System.out.println("Signature: " + transaction.getSignature());
        
        
        String productId = transaction.getApp_transaction_ref();
        int amount = transaction.getTransaction_amount();
        String currency = transaction.getTransaction_currency();
        UUID product_id = UUID.fromString(productId);
        String sessionId = null;
        String status = "success";
        String service_name = transaction.getTransaction_reason();
        String tableName = "paiement_reservation";
        
        System.out.println(insertData(UUID.randomUUID(),new BigDecimal(amount),currency, product_id, "", "success", Instant.now(), tableName));
        String encryptedHash = communMethodsController.encrypt(productId+amount+currency);
        communMethodsController.sendMessageToPulsarTopic(productId+ " " +amount+ " "+Instant.now()+" "+encryptedHash, service_name) ;//envoie de l'id via aphache pulsar
        System.out.println("L'identifiant du produit est : " + productId);

        return ResponseEntity.ok("Payment handled successfully.");

	}
	
	
	public boolean insertData(UUID paymentId, BigDecimal amount, String currency, UUID product_id, String sessionPayId, String status, Instant timestamp,  String tableName) {
        String keyspaceName = "payment";

        RegularInsert regularInsert = QueryBuilder.insertInto(keyspaceName, tableName)
                .value("id", QueryBuilder.literal(paymentId))
                .value("amount", QueryBuilder.literal(amount, TypeCodecs.DECIMAL))
                .value("currency", QueryBuilder.literal(currency))
                .value("product_id", QueryBuilder.literal(product_id))
                .value("sessionid", QueryBuilder.literal(sessionPayId))
                .value("status", QueryBuilder.literal(status))
                .value("timestamp", QueryBuilder.literal(timestamp));

        SimpleStatement insertStatement = regularInsert.build();

        try {
            session.execute(insertStatement);
            return true; // Insertion réussie
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Échec de l'insertion
        }
    }
	
	
	
	

}
