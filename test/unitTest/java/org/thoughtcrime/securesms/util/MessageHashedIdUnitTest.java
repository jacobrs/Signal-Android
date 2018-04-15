package org.thoughtcrime.securesms.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;

import static org.thoughtcrime.securesms.util.MessageHashedId.generateHashedId;
import static org.thoughtcrime.securesms.util.MessageHashedId.getSha1Hex;

public class MessageHashedIdUnitTest {

    // Test purity of the Sha1Hex hashing method given same and different values.
    @Test
    public void testGetSha1HexMethod(){
        String datetime1 = "mondayevening";
        String datetime2 = "wednesdayEvening";

        String hashId1 = getSha1Hex(datetime1);
        String hashId2 = getSha1Hex(datetime1);

        assertNotNull(hashId1);
        assertNotNull(hashId2);
        assertEquals(hashId1, hashId2);

        String hashId3 = getSha1Hex(datetime2);

        assertNotNull(hashId3);

        assertNotSame(hashId1, hashId3);
        assertNotSame(hashId2, hashId3);
    }

    // Test generateHashedId() method returns correct hashed id string value given long timestamp.
    @Test
    public void testGenerateHashedId(){
        String datetime1 = "123456789";
        long datetime2 = 123456789;

        String hashedDatetime1 = getSha1Hex(datetime1);
        String hashedDatetime2 = generateHashedId(datetime2);

        assertNotNull(hashedDatetime1);
        assertNotNull(hashedDatetime2);
        assertEquals(hashedDatetime1, hashedDatetime2);
    }
}
