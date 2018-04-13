package org.thoughtcrime.securesms.util;

import android.util.Log;

import com.fasterxml.jackson.databind.ser.Serializers;

import org.thoughtcrime.securesms.database.model.MessageRecord;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageHashedId {

    public static String TAG = MessageHashedId.class.getSimpleName();

    /*  Generate a hashed ID with time the message was sent as parameters.
        These values are accessible to both sender and recipient, so both can generate the same hash
        ID for each message. */
    public static String generateHashedId(long sentTime) {
        String id = null;
        String clearString = (sentTime +"");
        id = getSha1Hex(clearString);
        return id;
    }

  public static String getSha1Hex(String clearString)
  {
    try
    {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
      messageDigest.update(clearString.getBytes("UTF-8"));
      byte[] bytes = messageDigest.digest();
      StringBuilder buffer = new StringBuilder();
      for (byte b : bytes)
      {
        buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
      }
      return buffer.toString();
    }
    catch (Exception ignored)
    {
      ignored.printStackTrace();
      return null;
    }
  }
}
