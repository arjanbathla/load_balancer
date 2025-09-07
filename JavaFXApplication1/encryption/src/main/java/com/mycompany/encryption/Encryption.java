/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.encryption;

/**
 *
 * @author ntu-user
 */

import java.io.File;
import javax.crypto.SecretKey;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.CipherOutputStream;


public class Encryption {

    public static void main(String[] args) {
        try {
         // Define the file name
         File file = new File("example.txt");

         // Create the file if it doesn't exist
         if (file.createNewFile()) {
             System.out.println("File created: " + file.getName());
         } else {
             System.out.println("File already exists.");
         }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        try {
            // Generate AES key and IV
            SecretKey key = getEncryption.generateKey();
            byte[] iv = getEncryption.generateIV();

            // Save key and IV as strings (store securely in practice)
            String keyStr = getEncryption.keyToString(key);
            String ivStr = getEncryption.ivToString(iv);
            System.out.println("Encryption Key: " + keyStr);
            System.out.println("IV: " + ivStr);

            // Encrypt and decrypt a file
            String inputFile = "example.txt";
            String encryptedFile = "example_encrypted.txt";
            String decryptedFile = "example_decrypted.txt";

            getEncryption.encryptFile(inputFile, encryptedFile, key, iv);
            System.out.println("File Encrypted Successfully!");

            getEncryption.decryptFile(encryptedFile, decryptedFile, key, iv);
            System.out.println("File Decrypted Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
