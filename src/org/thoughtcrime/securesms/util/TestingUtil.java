package org.thoughtcrime.securesms.util;

public class TestingUtil {
    public static boolean isEspressoTest() {
        boolean isRunningTest;

        try {
            Class.forName ("android.support.test.espresso.Espresso");
            isRunningTest = true;
        } catch (ClassNotFoundException e) {
            isRunningTest = false;
        }

        return isRunningTest;
    }
}
