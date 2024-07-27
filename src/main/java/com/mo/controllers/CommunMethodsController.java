package com.mo.controllers;

import java.math.BigDecimal;

import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.pulsar.core.DefaultSchemaResolver;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.pulsar.core.SchemaResolver.SchemaResolverCustomizer;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import com.google.gson.Gson;

import com.mo.entities.MyProduct;
import com.mo.entities.Payment;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionListLineItemsParams;

import jakarta.annotation.PostConstruct;

@Service
public class CommunMethodsController {
	
	@Autowired
	private SchemaResolverCustomizer<DefaultSchemaResolver> schemaResolverCustomizer;
	
	@Autowired
    private PulsarTemplate<String> stringTemplate;
	private CqlSession session;
	
	
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
    
	
	public ResponseEntity<?> getPaiementByPoductId(String productId) {
        // Création de la requête CQL
        String query = "SELECT * FROM payment.paiement WHERE product_id = ? LIMIT 1";

        // Préparation de la requête
        PreparedStatement preparedStatement = getSession().prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(productId);

        // Exécution de la requête
        ResultSet resultSet = getSession().execute(boundStatement.setConsistencyLevel(DefaultConsistencyLevel.ONE));

        // Traitement du résultat
        Row row = resultSet.one();
        if (row != null) {
            Long amount = row.getBigDecimal("amount").longValue();
        
            return ResponseEntity.ok(productId);
        }
        return null;
    }
	
	
	
	public boolean insertData(UUID paymentId, BigDecimal amount, String currency, String customer_id, String producer_id, String product_id, String status, Instant timestamp,  String tableName) {
        String keyspaceName = "payment";

        RegularInsert regularInsert = QueryBuilder.insertInto(keyspaceName, tableName)
                .value("id", QueryBuilder.literal(paymentId))
                .value("amount", QueryBuilder.literal(amount, TypeCodecs.DECIMAL))
                .value("currency", QueryBuilder.literal(currency))
                .value("product_id", QueryBuilder.literal(product_id))
                .value("customer_id", QueryBuilder.literal(customer_id))
                .value("producer_id", QueryBuilder.literal(producer_id))
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
	
	
	
	

	
	public boolean updatePaymentStatus(String product_id, String newStatus, String tableName) {
	    String keyspaceName = "payment";

	    ResultSet rs = session.execute("SELECT id FROM " + keyspaceName + "." + tableName + " WHERE product_id = '" + product_id + "'");
	    Row row = rs.one();
	    if (row != null) {
	        UUID paymentId = row.getUuid("id");

	        Update update = QueryBuilder.update(keyspaceName, tableName)
	                .setColumn("status", QueryBuilder.literal(newStatus))
	                .whereColumn("id").isEqualTo(QueryBuilder.literal(paymentId));

	        SimpleStatement updateStatement = update.build();

	        try {
	            session.execute(updateStatement);
	            return true; // Mise à jour réussie
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false; // Échec de la mise à jour
	        }
	    }

	    return false; // Échec de la mise à jour
	}
	public void sendMessageToPulsarTopic(String str, String topic) {
		stringTemplate.send(topic, str);
		
	}
	
	
	public  String encrypt(String message) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        // Clé publique sous forme de chaîne encodée en Base64
        String publicKeyString = genPublicKey();
        // Convertir la clé publique de la chaîne encodée en tableau de bytes
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
        // Générer l'objet PublicKey à partir du tableau de bytes
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        // Calculer le hachage SHA-256
        byte[] hashedBytes = calculateSHA256Hash(message.getBytes(StandardCharsets.UTF_8));
        // Chiffrer le hachage avec la clé publique
        byte[] encryptedBytes = encryptWithPublicKey(hashedBytes, publicKey);
        // Afficher le résultat
        String encryptedHash = Base64.getEncoder().encodeToString(encryptedBytes);
        return encryptedHash;
    }
	
	private static byte[] calculateSHA256Hash(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input);
    }

    private static byte[] encryptWithPublicKey(byte[] input, PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(input);
    }
    
    
    public String genPublicKey() throws NoSuchAlgorithmException {
    	KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Récupération de la clé publique
        PublicKey publicKey = keyPair.getPublic();

        // Encodage de la clé publique en Base64
        String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        // Affichage de la clé publique encodée
        return publicKeyString;
    }
    
    
    public List<Payment> getPaymentHistory() {
        String keyspaceName = "payment";
        String tableName = "paiement";

        Select select = QueryBuilder.selectFrom(keyspaceName, tableName)
                .all();
//                .orderBy("timestamp", ClusteringOrder.DESC); // Tri inversé sur la colonne "timestamp"

        SimpleStatement selectStatement = select.build();

        ResultSet resultSet = session.execute(selectStatement);

        List<Payment> paymentHistory = new ArrayList<>();

        for (Row row : resultSet) {
            UUID paymentId = row.getUuid("id");
            String product_id = row.getString("product_id");
            BigDecimal amount = row.getBigDecimal("amount");
            String currency = row.getString("currency");
            String customer_id = row.getString("customer_id");
            String producer_id = row.getString("producer_id");
            String status = row.getString("status");
            Instant timestamp = row.getInstant("timestamp");
            Payment payment = new Payment(paymentId, product_id, amount, currency,customer_id, producer_id, timestamp, status);
            paymentHistory.add(payment);
        }

        return paymentHistory;
    }

}
