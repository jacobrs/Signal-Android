package org.thoughtcrime.securesms;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Enumeration;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FingerprintAuthenticationTest {

  @Test
  public void keyPairGeneratorTest(){
    Context mockContext = Mockito.mock(Context.class);
    FingerprintAuthenticationHandler handler = new FingerprintAuthenticationHandler(mockContext);
    KeyPairGenerator kpGen = null;

    try {
      kpGen = handler.getKeyPairGenerator();
    } catch (FingerprintAuthenticationHandler.FingerprintException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    assertThat(kpGen.getAlgorithm(), is("RSA"));
    assertThat(kpGen.getProvider().toString(), is("AndroidKeyStore version 1.0"));
  }

  @Test
  public void keyStoreTest(){
    Context mockContext = Mockito.mock(Context.class);
    FingerprintAuthenticationHandler handler = new FingerprintAuthenticationHandler(mockContext);
    KeyStore keyStore = null;

    try {
      keyStore = handler.getKeyStore();
    } catch (FingerprintAuthenticationHandler.FingerprintException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    assertThat(keyStore.getProvider().toString(), is("AndroidKeyStore version 1.0"));
  }

  //needs read/right permission to run
  public void handlePassphraseChangeTest(){
    Context context = InstrumentationRegistry.getContext();
    FingerprintAuthenticationHandler handler = new FingerprintAuthenticationHandler(context);
    KeyStore keyStore;
    Enumeration aliases;
    Key keyBefore = null;
    Key keyAfter = null;

    handler.handlePassphraseChange("password");

    try {
      keyStore = handler.getKeyStore();
      aliases = keyStore.aliases();
      while(aliases.hasMoreElements()){
        String alias = (String)aliases.nextElement();
        keyStore.getKey(alias,null);
      }
    } catch (KeyStoreException e) {
      e.printStackTrace();
    } catch (FingerprintAuthenticationHandler.FingerprintException e) {
      e.printStackTrace();
    } catch (UnrecoverableKeyException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    handler.handlePassphraseChange("password");

    try {
      keyStore = handler.getKeyStore();
      aliases = handler.getKeyStore().aliases();
      while(aliases.hasMoreElements()){
        String alias = (String)aliases.nextElement();
        keyAfter = keyStore.getKey(alias,null);
      }
    } catch (KeyStoreException e) {
      e.printStackTrace();
    } catch (FingerprintAuthenticationHandler.FingerprintException e) {
      e.printStackTrace();
    } catch (UnrecoverableKeyException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    assertNotEquals(keyBefore, keyAfter);
  }

}
