package com.high.developer.fingerprint_poc;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


public class LoginActivity extends AppCompatActivity {


    private KeyStore keyStore;
    // Variable used for storing the key in the Android Keystore container
    private static final String KEY_NAME = "FingerPrintPoC";
    private Cipher cipher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // Iniciando el Android Keyguard Manager y el Fingerprint Manager
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        android.hardware.fingerprint.FingerprintManager fingerprintManager = (android.hardware.fingerprint.FingerprintManager)
                getSystemService(FINGERPRINT_SERVICE);

        // Verificando la existencia de un lector de huellas en el dispositivo
        if (!fingerprintManager.isHardwareDetected()) {

            Toast.makeText(this, "Su dispositivo no tiene un lector de huellas dactilares.", Toast.LENGTH_LONG).show();
        } else {
            // Verificando que se haya pedido el permiso en el AndroidManifest
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Es necesario activar el permiso de FingerPrint.", Toast.LENGTH_SHORT).show();
            } else {
                // Verificando que exista al menos una huella dactilar registrada en el dispositivo
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    Toast.makeText(this, "Se debe registrar al menos una huella en la configuración del dispositivo.", Toast.LENGTH_LONG).show();
                } else {
                    // Verificando que el lock screen security este activado
                    if (!keyguardManager.isKeyguardSecure()) {
                        Toast.makeText(this, "Lock screen security no está activado en la configuración del dispositivo.", Toast.LENGTH_LONG).show();
                    } else {
                        generateKey();
                        if (cipherInit()) {
                            FingerprintHelper helper = new FingerprintHelper(this);
                            android.hardware.fingerprint.FingerprintManager.CryptoObject cryptoObject = new android.hardware.fingerprint.FingerprintManager.CryptoObject(cipher);
                            helper.startAuth(fingerprintManager, cryptoObject);
                        }
                    }
                }
            }
        }
    }

    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }


        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }
        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }
}