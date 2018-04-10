package org.thoughtcrime.securesms.util;

import android.util.Log;

import org.thoughtcrime.securesms.database.Address;
import org.thoughtcrime.securesms.database.model.MessageRecord;

import java.io.IOException;

public class MessageHashedId {

    public static String TAG = MessageHashedId.class.getSimpleName();

    /*  Generate a hashed ID in Base64 using smessage body and time the message was sent as parameters.
        These values are accessible to both sender and recipient, so both can generate the same hash
        ID for each message. */
    public static String generateHashedId(MessageRecord messageRecord, String sentTime){
        String id = null;
        String record = messageRecord.getDisplayBody().toString();

        try{
          id = Base64.encodeObject(record + sentTime);
        } catch(IOException e){
            Log.e(TAG, "IOException caught.");
        }

        return id;
    }
}
