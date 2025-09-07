/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.encryption;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.CipherOutputStream;


/**
 *
 * @author ntu-user
 */
public class getEncryption {
private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    
    // Generate a random AES key
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256); // AES-256
        return keyGenerator.generateKey();
    }

    // Generate a random IV (Initialization Vector)
    public static byte[] generateIV() {
        byte[] iv = new byte[16]; // AES block size
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    // Encrypt a file
    public static void encryptFile(String inputFile, String outputFile, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        processFile(cipher, inputFile, outputFile);
    }

    // Decrypt a file
    public static void decryptFile(String inputFile, String outputFile, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        processFile(cipher, inputFile, outputFile);
    }

    // Read, encrypt/decrypt, and write files
    private static void processFile(Cipher cipher, String inputFile, String outputFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
    }

    // Convert SecretKey to Base64 string (for saving keys)
    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // Convert Base64 string back to SecretKey
    public static SecretKey stringToKey(String keyStr) {
        byte[] decodedKey = Base64.getDecoder().decode(keyStr);
        return new SecretKeySpec(decodedKey, "AES");
    }

    // Convert IV to Base64
    public static String ivToString(byte[] iv) {
        return Base64.getEncoder().encodeToString(iv);
    }

    // Convert Base64 back to IV
    public static byte[] stringToIv(String ivStr) {
        return Base64.getDecoder().decode(ivStr);
    }
}
