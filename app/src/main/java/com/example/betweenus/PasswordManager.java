//////////////////////////////////////////////////////
//              Password Manager Class              //
//                                                  //
// Author/Developer: Alex Longo                     //
//                                                  //
// Description: This class handles the encryption   //
// and decryption of user credentials provided to   //
// it by other classes. It utilizes Android         //
// Keystore to generate unique keys local to the    //
// user's device that enable encryption and secure  //
// storage of the user's password within the app's  //
// internal storage.                                //
//////////////////////////////////////////////////////
package com.example.betweenus;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class PasswordManager
{
    // Setup Android KeyStore and an instance of this class
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private KeyStore keyStore;
    private static PasswordManager instance;
    private Context context;
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    // Bind context to this instance of Password Manager Class
    private PasswordManager(Context context)
    {
        this.context = context;
    }

    // Public constructor for Password Manager instance that operates across the entire app
    public static synchronized PasswordManager getInstance(Context context)
    {
        if (instance == null) {
            instance = new PasswordManager(context.getApplicationContext());
            try
            {
                // Initialize Android KeyStore for this instance
                instance.initializeKeyStore();
            } catch (Exception e)
            {
                // Display initialization error
                e.printStackTrace();
            }
        }
        return instance;
    }

    // Method to initialize Android Keystore
    private void initializeKeyStore() throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException
    {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
    }

    // Method to encrypt and save a user password
    public boolean savePassword(String userId, String password) throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            NoSuchProviderException, UnrecoverableKeyException, IllegalBlockSizeException, BadPaddingException
    {
        // Get a key Alias for user's password to ensure it is stored under a separate key in the Android Keystore
        String keyAlias = getKeyAlias(userId);

        // Obtain the app's keystore instance
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        // Check if the credentials are already in use
        if (!keyStore.containsAlias(keyAlias))
        {
            // User credentials not in use; create new key
            createKey(keyAlias);
        }
        else
        {
            // User credentials IN USE ALREADY; return false
            return false;
        }

        // Get the application's cipher and prepare for encryption
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(keyAlias));


        // Generate Initialization Vector (IV)
        byte[] iv = cipher.getIV();

        // Encrypt the password
        byte[] encryptedPassword = cipher.doFinal(password.getBytes());
        String base64EncodedPassword = Base64.encodeToString(encryptedPassword, Base64.DEFAULT);

        // Save the encrypted password and IV to SharedPreferences
        SharedPreferences.Editor editor = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit();
        editor.putString(keyAlias + "_password", base64EncodedPassword);
        editor.putString(keyAlias + "_iv", Base64.encodeToString(iv, Base64.DEFAULT));
        editor.apply();

        return true;
    }

    // Method to obtain, decrypt, and return a password associated with a provided username
    public String getPassword(String userId) throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            UnrecoverableKeyException, IllegalBlockSizeException, BadPaddingException
    {

        // Get the key Alias for user's password
        String keyAlias = getKeyAlias(userId);

        // Obtain the app's keystore instance
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        // Check if username exists
        if (!keyStore.containsAlias(keyAlias))
        {
            // Username does not exist, return INVALID
            return "INVALID";
        }
        else
        {
            // Retrieve the encrypted password and IV from SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String base64EncodedPassword = prefs.getString(keyAlias + "_password", null);
            String base64EncodedIV = prefs.getString(keyAlias + "_iv", null);

            // Decode the base64 encoded password and IV
            byte[] encryptedPassword = Base64.decode(base64EncodedPassword, Base64.DEFAULT);
            byte[] iv = Base64.decode(base64EncodedIV, Base64.DEFAULT);

            // Get the application's cypher and prepare for decryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(keyAlias), new GCMParameterSpec(128, iv));

            // Decrypt and return the password
            byte[] decryptedPassword = cipher.doFinal(encryptedPassword);
            return new String(decryptedPassword);
        }
    }

    // Method to get a key Alias for user's password to ensure it is stored under a separate and specific key in the Android Keystore
    private String getKeyAlias(String userId)
    {
        return "BetweenUs_" + userId;
    }

    // Method to generate a key from the android keystore using proper username key alias
    private void createKey(String keyAlias) throws NoSuchProviderException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException
    {
        // Get Android KeyGenerator for the application
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);

        // Pass in specs for key
        keyGenerator.init(new KeyGenParameterSpec.Builder(keyAlias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());

        // Create key
        keyGenerator.generateKey();
    }

    // Method to obtain the key for a user's password from Android Keystore
    private SecretKey getSecretKey(String keyAlias) throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException, UnrecoverableKeyException
    {
        // Get Android KeyStore for the application
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        return (SecretKey) keyStore.getKey(keyAlias, null);
    }
}   // end PasswordManager class

