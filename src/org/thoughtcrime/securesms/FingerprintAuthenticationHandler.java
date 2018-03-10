package org.thoughtcrime.securesms;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.widget.Toast;

import org.thoughtcrime.securesms.util.FinguerprintAuthenticaionUtil;

/**
 * Created by bryce on 3/9/2018.
 */

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintAuthenticationHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;
    private CancellationSignal cancellationSignal;

    public FingerprintAuthenticationHandler(Context context) {
        this.context = context;
    }

    public void beginAuthentication(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        if (!FinguerprintAuthenticaionUtil.isFinguerprintAuthenticaionSupported(context)) {
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
        Toast.makeText(context, "Authentication successful", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(context, "Authentication failed", Toast.LENGTH_LONG).show();
    }
}
