package com.mo.controllers;

import java.math.BigDecimal;

import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

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
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.google.gson.Gson;
import com.mo.entities.DataOut;
import com.mo.entities.MyProduct;
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
    
	
	public ResponseEntity<?> getPaiementReservationBySessionId(String sessionId) {
        // Création de la requête CQL
        String query = "SELECT * FROM payment.paiement_reservation WHERE sessionid = ? LIMIT 1";

        // Préparation de la requête
        PreparedStatement preparedStatement = getSession().prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(sessionId);

        // Exécution de la requête
        ResultSet resultSet = getSession().execute(boundStatement.setConsistencyLevel(DefaultConsistencyLevel.ONE));

        // Traitement du résultat
        Row row = resultSet.one();
        if (row != null) {
            UUID id = row.getUuid("product_id");
            Long amount = row.getBigDecimal("amount").longValue();
            
            return ResponseEntity.ok(id);
        }
        return null;
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
   

}
