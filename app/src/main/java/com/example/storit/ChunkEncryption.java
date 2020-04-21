package com.example.storit;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.KeyPairGenerator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.NoSuchPaddingException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;

public class ChunkEncryption {



    public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String KEY_SPEC_ALGORITHM = "AES";
    public static final String PROVIDER = "BC";
    public static final String SECRET_KEY = "SECRET_KEY";
    public static final int OUTPUT_KEY_LENGTH = 256;
    public static SharedPreferences myPrefs = null;



    public ChunkEncryption(Context context){
        myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //Function to encrypt file
    public byte[] encode(byte[] fileData) throws Exception {

        byte[] data = getSecretKey().getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(data, 0, data.length, KEY_SPEC_ALGORITHM);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM); // creating java cipher instance

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()])); //Initializing the Cipher

        return cipher.doFinal(fileData);
    }

    public byte[] decode ( byte[] fileData) {

        //call the file in the bite array
        byte[] decrypted = null;
        try {
            //call cipher function accroding the alogrithm
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, PROVIDER);
            //IV parameter
            IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[cipher.getBlockSize()]);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), ivParameterSpec);
            decrypted = cipher.doFinal(fileData);
        } catch(Exception e) {
            Log.d("webrtc", e.getMessage());
        }
        return decrypted;
    }
    private static SecretKey getSecretKey () throws NoSuchAlgorithmException {

        String encodedKey = getKey();
        // If no key found, Generate a new one
        if (null == encodedKey || encodedKey.isEmpty()) {
            SecureRandom secureRandom = new SecureRandom();
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_SPEC_ALGORITHM);
            keyGenerator.init(OUTPUT_KEY_LENGTH, secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();
            saveKey(Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP));

            return secretKey;
        }

        byte[] decodedKey = Base64.decode(encodedKey, Base64.NO_WRAP);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, KEY_SPEC_ALGORITHM);
        return originalKey;
    }

    public static void saveKey (String value){
        myPrefs.edit().putString(SECRET_KEY, value).commit();
    }

    public static String getKey () {
        return myPrefs.getString(SECRET_KEY, null);
    }

    public static void shutDown () {
        myPrefs = null;
    }
}
