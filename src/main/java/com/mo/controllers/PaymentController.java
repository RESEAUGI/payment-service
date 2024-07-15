package com.mo.controllers;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.model.StripeObject;
import com.stripe.net.ApiResource;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.LineItemCollection;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;

import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import com.datastax.oss.driver.api.querybuilder.update.UpdateStart;
import com.mo.entities.MyProduct;
import com.mo.stripe.LinkPay;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionListLineItemsParams;

import jakarta.annotation.PostConstruct;

@Controller
public class PaymentController {
		
	@Autowired
    private PulsarTemplate<String> stringTemplate;
	
	private Session resource ;
	
	private String sessionPayId;
	
	private SessionListLineItemsParams params;
	
	private CqlSession session;
	
	private Gson gson = new Gson();
	
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
    
// STRIPES
 // Configurez votre clé secrète Stripe ici
    private static final String STRIPE_SECRET_KEY = "sk_test_51PVJciERlR7Uy2xt1I68IsVZWLzrQJkYxJwVhf8ie3cjFZ3Q7Pi9E68luHyBY7zA4BwmCpZgoomnJ5TngoybPUC400GIRU8bD0";

    static {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }
    

    //Producteur pulsar
    public void sendStringMessageToPulsarTopic(String str, String topic) throws PulsarClientException {
        stringTemplate.send(topic, str);
    }
    
   
    
    @PostMapping("/handle-event")
    public ResponseEntity<String> handleEvent(@RequestBody String payload) throws PulsarClientException {
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
        System.out.println("event type: " + event.getType());
        switch (event.getType()) {
            case "checkout.session.completed":
				try {
					resource = Session.retrieve(this.sessionPayId);
					System.out.println("voici l'identifiant de la session  "+resource.getId() );
					getPaiementReservationBySessionId(resource.getId());
					SessionListLineItemsParams params = SessionListLineItemsParams.builder().build();
					LineItemCollection lineItems = resource.listLineItems(params);
					if(resource != null) {
	                	String session_id = resource.getId();
	                    String service_name = resource.getMetadata().get("service_name");
	                    System.out.println(service_name);
	                    ResponseEntity<?> response = getPaiementReservationBySessionId(resource.getId());
	                    
	                    if (service_name.equals("reservation")) {
	                    	
	                    	if (response.getBody() != null) {
	                            String productId = response.getBody().toString();
	                            sendStringMessageToPulsarTopic(productId, "reservation");//envoie de l'id via aphache pulsar
	                            System.out.println("L'identifiant du produit est : " + productId);
	                        }
	                    }
	                    if (service_name.equals("souscription")) {
	                    	
	                    	if (response.getBody() != null) {
	                            String productId = response.getBody().toString();
	                            sendStringMessageToPulsarTopic(productId, "souscription");//envoie de l'id via aphache pulsar
	                            System.out.println("L'identifiant du produit est : " + productId);
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
    
    
    public ResponseEntity<?> getPaiementReservationBySessionId(String sessionId) {
        // Création de la requête CQL
        String query = "SELECT * FROM payment.paiement_souscription WHERE sessionid = ? LIMIT 1";

        // Préparation de la requête
        PreparedStatement preparedStatement = getSession().prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(sessionId);

        // Exécution de la requête
        ResultSet resultSet = getSession().execute(boundStatement.setConsistencyLevel(DefaultConsistencyLevel.ONE));

        // Traitement du résultat
        Row row = resultSet.one();
        if (row != null) {
          
            UUID product_id = row.getUuid("product_id");
           
            return ResponseEntity.ok(product_id);
        }
        return null;
    }
    
 // Fonction qui effectue un remboursement
    
    public void processRefund(String sessionPayId, int refundAmount) throws StripeException {
        Stripe.apiKey = apiKey;

        RefundCreateParams refundParams = RefundCreateParams.builder()
                .setCharge(sessionPayId)
                .setAmount((long) refundAmount)
                .build();

        Refund refund = Refund.create(refundParams);

        // Traitez la réponse de remboursement ici
        String refundId = refund.getId();
        boolean refundSuccessful = refund.getStatus().equals("succeeded");

        if (refundSuccessful) {
            // Le remboursement a réussi
        } else {
            // Le remboursement a échoué
        }
    }
    
  
    
   // Met à jour le status d'un produit apres avoir reçu la confirmation de paiement 
  public boolean updatePaymentStatusByServiceId(String session_id, String newStatus, String tableName) {
  String keyspaceName = "payment";
  
  CqlIdentifier keyspaceIdentifier = CqlIdentifier.fromCql(keyspaceName);
  CqlIdentifier tableIdentifier = CqlIdentifier.fromCql(tableName);

  UpdateStart updateStart = QueryBuilder.update(keyspaceIdentifier, tableIdentifier);
  Update update = updateStart.setColumn("status", QueryBuilder.literal(newStatus))
          .whereColumn("sessionid").isEqualTo(QueryBuilder.literal(session_id));

  try {
      getSession().execute(update.build());
      return true; // Mise à jour réussie
  } catch (Exception e) {
      e.printStackTrace();
      return false; // Échec de la mise à jour
  }
}

    

    
    @MutationMapping
    public LinkPay createPaymentLink(@Argument MyProduct product) {
    	Stripe.apiKey = "sk_test_51PVJciERlR7Uy2xt1I68IsVZWLzrQJkYxJwVhf8ie3cjFZ3Q7Pi9E68luHyBY7zA4BwmCpZgoomnJ5TngoybPUC400GIRU8bD0";
    	String YOUR_DOMAIN = "http://localhost:3000";
    	String tableName = null;
    	if (product.getTransaction_reason().equals("souscription")) {
    	    tableName = "paiement_souscription";
    	}
    	if (product.getTransaction_reason().equals("reservation")) {
    	    tableName = "paiement_reservation";
    	}
    	if(product.getTransaction_reason().equals("reservation")) tableName = "paiement_reservation";
    	if(product.getTransaction_reason().equals("souscription")) tableName = "paiement_souscription";
    	SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(YOUR_DOMAIN + "/success.html")
                .setCancelUrl(YOUR_DOMAIN + "/cancel.html")
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
                .build();
		        try {
		            Session session = Session.create(params);
		            String url = session.getUrl();
		            String sessionId = session.getId(); 
		
		            LinkPay linkPay = new LinkPay(url);
		            System.out.println(session.getUrl());
		            System.out.println("l'identifiant de la session est:  " + session.getId());
		            this.sessionPayId = session.getId();
		            
//		            System.out.println(session);
		           
		            System.out.print(insertData(UUID.randomUUID(),new BigDecimal(product.getAmount()),product.getCurrency(), product.getProduct_id(), session.getId(), "pending", Instant.now(), tableName));
		            return linkPay;
		        } catch (StripeException e) {
		            e.printStackTrace();
		            return null;
		        }
    
    }   
    

   
   
  // fin stripes      
	    
    
// @QueryMapping("/mypaiements")
//    public List<Payment> getPayments() {
//    	String keyspaceName = "payment";
//        String tableName = "paiement";
//        String selectQuery = String.format("SELECT * FROM %s.%s", keyspaceName, tableName);
//
//        ResultSet resultSet = session.execute(selectQuery);
//        List<Payment> payments = new ArrayList<Payment>();
//
//        for (Row row : resultSet) {
//            UUID paymentId = row.getUuid("id");
//            UUID reservationId = row.getUuid("reservation_id");
//            BigDecimal amount = row.getBigDecimal("amount");
//            String link = row.getString("link");
//            String currency = row.getString("currency");
//            Instant timestamp = row.getInstant("timestamp");
//            String status = row.getString("status");
//
//            Payment payment = new Payment(paymentId, reservationId, amount, link, currency, timestamp, status);
//            payments.add(payment);
//        }
//        
//        return payments;
//    }
   
     
//    public void deletePayment(@RequestParam UUID paymentId) {
//        String keyspaceName = "payment";
//        String tableName = "paiement";
//
//        SimpleStatement deleteStatement = QueryBuilder.deleteFrom(keyspaceName, tableName)
//                .whereColumn("id").isEqualTo(QueryBuilder.literal(paymentId))
//                .build();
//
//        session.execute(deleteStatement);
//    }
    
    

   
    
    

    
    
    
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
    

    
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    @GetMapping("/createdataS")
    public void createTableSouscriptionAndInsertData(String sessionPayId) {
        String keyspaceName = "payment";
        String tableName = "paiement_souscription";

        // Insertion d'une entrée de paiement
        UUID paymentId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.0");
        String currency = "USD";
        Instant timestamp = Instant.now();
        String status = "completed";
         
        
        RegularInsert regularInsert = QueryBuilder.insertInto(keyspaceName, tableName)
                .value("id", QueryBuilder.literal(paymentId))
                .value("amount", QueryBuilder.literal(amount, TypeCodecs.DECIMAL))
                .value("currency", QueryBuilder.literal(currency))
                .value("product_id", QueryBuilder.literal(reservationId))
                .value("sessionid", QueryBuilder.literal(sessionPayId))
                .value("status", QueryBuilder.literal(status))
                .value("timestamp", QueryBuilder.literal(timestamp));

        SimpleStatement insertStatement = regularInsert.build();

        session.execute(insertStatement);
    }
    
    
    @GetMapping("/createdataR")
    public void createTableReservationAndInsertData(String sessionPayId) {
        String keyspaceName = "payment";
        String tableName = "paiement_reservation";
        

     // Insertion d'une entrée de paiement
        UUID paymentId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.0");
        String currency = "USD";
        Instant timestamp = Instant.now();
        String status = "completed";
         
        
        RegularInsert regularInsert = QueryBuilder.insertInto(keyspaceName, tableName)
                .value("id", QueryBuilder.literal(paymentId))
                .value("amount", QueryBuilder.literal(amount, TypeCodecs.DECIMAL))
                .value("currency", QueryBuilder.literal(currency))
                .value("product_id", QueryBuilder.literal(reservationId))
                .value("sessionid", QueryBuilder.literal(sessionPayId))
                .value("status", QueryBuilder.literal(status))
                .value("timestamp", QueryBuilder.literal(timestamp));
//        SimpleStatement insertStatement = regularInsert.build();

//        session.execute(insertStatement);
      
    }
    
    
   
}

