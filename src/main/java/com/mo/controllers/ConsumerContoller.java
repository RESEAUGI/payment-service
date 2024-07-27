package com.mo.controllers;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Producer;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.PulsarClientException.AlreadyClosedException;
import org.apache.pulsar.client.api.PulsarClientException.InvalidMessageException;
import org.apache.pulsar.client.api.PulsarClientException.ProducerBusyException;
import org.apache.pulsar.client.api.PulsarClientException.ProducerFencedException;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



@Service
@Component
@RestController
public class ConsumerContoller {
	private final PulsarClient client;
	private CommunMethodsController communMethodsController;
	
	@Autowired	
public ConsumerContoller(PulsarClient client, CommunMethodsController communMethodsController) {
		super();
		this.client = client;
		this.communMethodsController = communMethodsController;
	}



	 @PulsarListener(
		      subscriptionName = "string-topic-subscription",
		      topics = "souscription",
		      subscriptionType = SubscriptionType.Shared
		    )
   @GetMapping("/consumer")
   public void stringTopicListener(String str) {
		System.out.println("Received String message:" + str);
   }

	 

	public void decrypt(String message) throws Exception {
		  
		    String publicKeyString = communMethodsController.genPublicKey(); // Génère la clé publique encodée en Base64
	        
	        String encryptedHash = communMethodsController.encrypt(message);
	        
	        byte[] encryptedHashBytes = Base64.getDecoder().decode(encryptedHash);
	        
	        // Convertir la clé publique de la chaîne encodée en tableau de bytes
	        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
	        
	        // Générer l'objet PublicKey à partir du tableau de bytes
	        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
	        
	        // Déchiffrer le hash avec la clé publique
	        byte[] decryptedHashBytes = decryptWithPublicKey(encryptedHashBytes, publicKey);
	        
	        // Calculer le hash du message original
	        byte[] originalHashBytes = calculateSHA256Hash(message.getBytes());
	        
	        // Comparer les deux hashes
	        boolean hashMatch = MessageDigest.isEqual(originalHashBytes, decryptedHashBytes);
	        
	        // Afficher le résultat
	        if (hashMatch) {
	            System.out.println("Le hash correspond. Le message est authentique.");
	        } else {
	            System.out.println("Le hash ne correspond pas. Le message a été modifié ou est invalide.");
	        }
			
	    }
	   
	    
	    public static byte[] decryptWithPublicKey(byte[] input, PublicKey publicKey) throws Exception {
	        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	        cipher.init(Cipher.DECRYPT_MODE, publicKey);
	        return cipher.doFinal(input);
	    }
	    
	    public static byte[] calculateSHA256Hash(byte[] input) throws NoSuchAlgorithmException {
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        return digest.digest(input);
	    }
	
	 @GetMapping("/")
	   public void n() {
			System.out.println("Welcome to my app !");
	   } 	 

}

