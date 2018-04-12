package org.thoughtcrime.securesms.util;

import android.util.Log;

import java.io.IOException;

public class MessageHashedId {

    public static String TAG = MessageHashedId.class.getSimpleName();

    /*  Generate a hashed ID in Base64 using message body and time the message was sent as parameters.
        These values are accessible to both sender and recipient, so both can generate the same hash
        ID for each message. */
    public static String generateHashedId(String messageBody, long sentTime){
        String id = null;

        try{
          id = Base64.encodeObject(messageBody + sentTime);
          id.replaceAll("=","");
        } catch(IOException e){
            Log.e(TAG, "IOException caught.");
        }

        return id;
    }
}
