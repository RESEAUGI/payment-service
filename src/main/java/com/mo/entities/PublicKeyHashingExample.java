package com.mo.entities;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class PublicKeyHashingExample {

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        // Clé publique sous forme de chaîne encodée en Base64
        String publicKeyString = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxX3cL...";

        // Convertir la clé publique de la chaîne encodée en tableau de bytes
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);

        // Générer l'objet PublicKey à partir du tableau de bytes
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        // Chaîne à hacher
        String message = "Hello, World!";

        // Calculer le hachage SHA-256
        byte[] hashedBytes = calculateSHA256Hash(message.getBytes(StandardCharsets.UTF_8));

        // Chiffrer le hachage avec la clé publique
        byte[] encryptedBytes = encryptWithPublicKey(hashedBytes, publicKey);

        // Afficher le résultat
        String encryptedHash = Base64.getEncoder().encodeToString(encryptedBytes);
        System.out.println("Hachage SHA-256 chiffré avec la clé publique : " + encryptedHash);
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
}
