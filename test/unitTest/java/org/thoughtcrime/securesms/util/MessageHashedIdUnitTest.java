package org.thoughtcrime.securesms.util;

import android.util.Log;

import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;

public class MessageHashedIdUnitTest {

    private static String TAG = MessageHashedIdUnitTest.class.getSimpleName();

    // Test purity of the Base64 encode() method given same values and different values.
    @Test
    public void testBase64Method(){
        String sender = "benjenkins";
        String datetime1 = "mondayevening";
        String datetime2 = "mondayEvening";

        String hashId1 = null;
        String hashId2 = null;
        String hashId3 = null;
        String hashId4 = null;
        String hashId5 = null;

        try{
            hashId1 = Base64.encodeObject(sender);
            hashId2 = Base64.encodeObject(sender);
        }catch(IOException e){
            Log.e(TAG, "IOException caught.");
        }

        assertNotNull(hashId1);
        assertNotNull(hashId2);
        assertEquals(hashId1, hashId2);

        try{
            hashId3 = Base64.encodeObject(sender + datetime1);
            hashId4 = Base64.encodeObject(sender + datetime1);
            hashId5 = Base64.encodeObject(sender + datetime2);
        }catch(IOException e){
            Log.e(TAG, "IOException caught.");
        }

        assertNotNull(hashId3);
        assertNotNull(hashId4);
        assertNotNull(hashId5);
        assertEquals(hashId3, hashId4);
        assertNotSame(hashId4, hashId5);
        assertNotSame(hashId1, hashId4);
    }
}
