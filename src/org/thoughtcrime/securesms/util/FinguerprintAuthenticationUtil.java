package org.thoughtcrime.securesms.util;

import android.content.Context;
import android.os.Build;
import android.hardware.fingerprint.FingerprintManager;
/**
 * Created by bryce on 3/9/2018.
 */

public class FinguerprintAuthenticationUtil {

    public static boolean isFinguerprintAuthenticaionSupported(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            return !fingerprintManager.isHardwareDetected() && !fingerprintManager.hasEnrolledFingerprints();
        }
        return false;
    }


}
