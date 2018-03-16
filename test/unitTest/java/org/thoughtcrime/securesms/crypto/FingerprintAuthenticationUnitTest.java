package org.thoughtcrime.securesms.crypto;

import android.content.Context;

import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.thoughtcrime.securesms.BaseUnitTest;
import org.thoughtcrime.securesms.FingerprintAuthenticationHandler;
import org.thoughtcrime.securesms.util.FingerprintAuthenticationUtil;

import java.security.KeyPairGenerator;
import java.security.KeyStore;

import javax.crypto.Cipher;

import static junit.framework.Assert.assertFalse;


public class FingerprintAuthenticationUnitTest extends BaseUnitTest {
    @Test
    public void cipherTest() {
        FingerprintAuthenticationHandler handler = new FingerprintAuthenticationHandler(context);
        Cipher cipher = null;
        try {
            cipher = handler.getCipher();
        } catch (FingerprintAuthenticationHandler.FingerprintException e) {
            e.printStackTrace();
        }
        assert(cipher.getAlgorithm() == "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    }

    @Test
    public void unsupportedBuildVersionTest() {
        assertFalse(FingerprintAuthenticationUtil.isFingerprintAuthenticationSupported(context));
    }
}
