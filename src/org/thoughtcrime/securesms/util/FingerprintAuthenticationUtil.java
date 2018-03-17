package org.thoughtcrime.securesms.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.hardware.fingerprint.FingerprintManager;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;

public class FingerprintAuthenticationUtil {
    public static boolean isFingerprintAuthenticationSupported(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);
            return fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints() && fingerprintManager.hasEnrolledFingerprints() && keyguardManager.isKeyguardSecure();
        }
        return false;
    }
}