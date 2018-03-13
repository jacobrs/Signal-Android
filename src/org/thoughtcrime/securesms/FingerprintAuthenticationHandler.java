package org.thoughtcrime.securesms;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.widget.Toast;

import org.thoughtcrime.securesms.crypto.InvalidPassphraseException;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.crypto.MasterSecretUtil;
import org.thoughtcrime.securesms.util.FinguerprintAuthenticationUtil;
import org.whispersystems.jobqueue.util.Base64;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import static android.content.Context.FINGERPRINT_SERVICE;

/**
 * Created by bryce on 3/9/2018.
 */

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintAuthenticationHandler extends FingerprintManager.AuthenticationCallback{

    public String key;
    private Context context;
    private CancellationSignal cancellationSignal;
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyPairGenerator keyPairGenerator;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;

    public FingerprintAuthenticationHandler(Context context) {
        this.context = context;
        initializeFingerprintResources();
    }

    public void beginAuthentication(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        if (!FinguerprintAuthenticationUtil.isFinguerprintAuthenticaionSupported(context)) {
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {

        Toast.makeText(context, "Authentication error" + System.lineSeparator() + errString, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        Toast.makeText(context, "Authentication help"  + System.lineSeparator() + helpString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        //encrypt("1");
        Cipher cipher = result.getCryptoObject().getCipher();
        String password = decrypt(cipher);
        MasterSecret masterSecret = null;
        try {
            masterSecret = MasterSecretUtil.getMasterSecret(this.context, password);
        } catch (InvalidPassphraseException e) {
            e.printStackTrace();
        }

        ((PassphrasePromptActivity )this.context ).setMasterSecret(masterSecret);
        Toast.makeText(context, "Authentication successful: " + password, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(context, "Authentication failed", Toast.LENGTH_LONG).show();
    }

    private void initializeFingerprintResources() {
        try{
            keyPairGenerator = getKeyPairGenerator();
            cipher = getCipher();
            keyStore = getKeyStore();
            createKeyPair();

            encrypt("1");
            fingerprintManager = (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);
            if (initCipher(Cipher.DECRYPT_MODE)) {
                cryptoObject = new FingerprintManager.CryptoObject(cipher);
                beginAuthentication(fingerprintManager, cryptoObject);
            }
        } catch (FingerprintAuthenticationHandler.FingerprintException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void createKeyPair()  throws FingerprintAuthenticationHandler.FingerprintException {
        try {
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_DECRYPT)
                            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                            .setUserAuthenticationRequired(true)
                            .build());
            keyPairGenerator.generateKeyPair();
        } catch(InvalidAlgorithmParameterException exception) {
            throw new FingerprintAuthenticationHandler.FingerprintException( exception);
        }
    }
    public boolean initCipher(int opmode) throws FingerprintException {
        try {
            keyStore.load(null);
            if(opmode == Cipher.ENCRYPT_MODE) {
                PublicKey key = keyStore.getCertificate(KEY_NAME).getPublicKey();
                PublicKey unrestricted = KeyFactory.getInstance(key.getAlgorithm()).generatePublic(new X509EncodedKeySpec(key.getEncoded()));
                OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
                cipher.init(opmode, unrestricted, spec);
            } else {
                PrivateKey key = (PrivateKey) keyStore.getKey(KEY_NAME, null);
                cipher.init(opmode, key);
            }
            return true;
        } catch (NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableKeyException | InvalidKeyException | InvalidKeySpecException | InvalidAlgorithmParameterException  | KeyStoreException e) {
            throw new FingerprintException("Failure to initialize cipher", e);
        }
    }
    public KeyStore getKeyStore() throws FingerprintException {
        try {
            return KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new FingerprintException( "Failure to retrieve a KeyStore", e);
        }
    }

    public KeyPairGenerator getKeyPairGenerator() throws FingerprintException {
        try {
            return KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        } catch(NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new FingerprintException( "Failure to retrieve a KeyPairGenerator" , e );
        }
    }

    public Cipher getCipher() throws FingerprintException {
        try {
            return Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new FingerprintException("Failure to retrieve a Cipher", e);
        }
    }

    private void encrypt(String password) {
        try {
            initCipher(Cipher.ENCRYPT_MODE);
            byte[] encryptedBytes = cipher.doFinal(password.getBytes());
            String encrypted = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
            context.getSharedPreferences( "org.thoughtcrime.securesms", Context.MODE_PRIVATE).edit().putString("passwordEncrypted", encrypted).commit();
        } catch(IllegalBlockSizeException | BadPaddingException | FingerprintException exception) {
            throw new RuntimeException("Failure to encrypt password", exception);
        }
    }

    private String decrypt(Cipher cipher) {
        try {
            String encrypted =  context.getSharedPreferences( "org.thoughtcrime.securesms", Context.MODE_PRIVATE).getString("passwordEncrypted" , "");
            byte[] encryptedBytes = Base64.decode(encrypted, Base64.NO_WRAP);
            byte[] outputBytes = cipher.doFinal(encryptedBytes);
            return new String(outputBytes);
        } catch (IllegalBlockSizeException | BadPaddingException exception) {
            throw new RuntimeException("Failure to decrypt password", exception);
        }
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
        public FingerprintException(String message, Exception e) {
            super(message, e);
        }
    }

}
