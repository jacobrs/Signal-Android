package org.thoughtcrime.securesms.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.database.model.MessageRecord;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseFactory.class)
public class EmojiReactionDatabaseUnitTest {

    // Fields
    private String          TABLE_NAME;
    private String          REACTION;
    private String          HASHED_ID;
    private String          where;
    private long            threadId;
    private ContentValues   testContents;

    // Mocked classes
    private SQLiteDatabase          mockSql;
    private MessageRecord           mockMessageRecord;

    // Non-mocked classes
    private EmojiReactionDatabase   emojiReactionDatabase;

    @Before
    public void setUp(){

        // Declaring and initializing local fields
        String ID                  = "id";
        String hashedId            = "hashed_id";
        String[] PROJECTION          = new String[] { ID, REACTION, HASHED_ID };

        // Initalizing global fields
        TABLE_NAME          = "emoji_reactions";
        REACTION            = "reaction";
        HASHED_ID           = "hashed_id";
        where               = "hashed_id= '" + hashedId + "'";
        threadId            = 123;
        testContents        = new ContentValues();

        // Declaring and initializing local mocked classes
        SQLiteOpenHelper mockSqlHelper      = PowerMockito.mock(SQLiteOpenHelper.class);
        SQLiteStatement mockSqlStatement    = PowerMockito.mock(SQLiteStatement.class);
        Context mockContext                 = PowerMockito.mock(Context.class);
        Cursor mockCursor                   = PowerMockito.mock(Cursor.class);
        Database mockDatabase               = PowerMockito.mock(Database.class);
        ContentResolver mockContentResolver = PowerMockito.mock(ContentResolver.class);
        ThreadDatabase mockThreadDatabase   = PowerMockito.mock(ThreadDatabase.class);

        // Initializing global mocked classes
        mockSql           = PowerMockito.mock(SQLiteDatabase.class);
        mockMessageRecord = PowerMockito.mock(MessageRecord.class);

        // Initializing global non-mocked class
        emojiReactionDatabase = new EmojiReactionDatabase(mockContext, mockSqlHelper);

        // Mocking static classes
        PowerMockito.mockStatic(DatabaseFactory.class);
        PowerMockito.mockStatic(Database.class);
        PowerMockito.mockStatic(Context.class);

        // Stubbing methods
        PowerMockito.when(mockMessageRecord.getHashedId()).thenReturn(hashedId);
        PowerMockito.when(mockMessageRecord.getThreadId()).thenReturn(threadId);

        PowerMockito.when(mockSqlHelper.getReadableDatabase()).thenReturn(mockSql);
        PowerMockito.when(mockSqlHelper.getWritableDatabase()).thenReturn(mockSql);

        PowerMockito.when(mockSql.query(TABLE_NAME, PROJECTION, where, null, null, null, null)).thenReturn(mockCursor);
        PowerMockito.when(mockSql.delete(TABLE_NAME, where, null)).thenReturn(1);

        PowerMockito.when(mockCursor.getCount()).thenReturn(1);
        PowerMockito.when(mockCursor.moveToNext()).thenReturn(true);
        PowerMockito.when(mockCursor.getColumnIndex(REACTION)).thenReturn(1);
        PowerMockito.when(mockCursor.getString(1)).thenReturn(REACTION);

        PowerMockito.when(mockSqlStatement.executeUpdateDelete()).thenReturn(1);

        PowerMockito.when(mockContext.getContentResolver()).thenReturn(mockContentResolver);

        PowerMockito.when(DatabaseFactory.getThreadDatabase(mockContext)).thenReturn(mockThreadDatabase);

        PowerMockito.doNothing().when(mockDatabase).notifyConversationListeners(threadId);
        PowerMockito.doNothing().when(mockContentResolver).notifyChange(Matchers.any(Uri.class), Matchers.any());
    }

    @Test
    public void testGetReactionEmoji() {
        // Test that the emoji reaction returned is the one expected for that message record.
        String emoji = emojiReactionDatabase.getReactionEmoji(mockMessageRecord);
        assertEquals(REACTION, emoji);
    }

    @Test
    public void testSetMessageReactionBySender() {
        // Stubbing update method; should return 0 meaning that the reaction does not currently exist.
        PowerMockito.when(mockSql.update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any())).thenReturn(0);

        // Verify that update and insert methods were called (reaction was set).
        emojiReactionDatabase.setMessageReaction(mockMessageRecord, REACTION);
        verify(mockSql).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any());
        verify(mockSql).insert(Matchers.anyString(), Matchers.any(), Matchers.any(ContentValues.class));

        // Assert that the update value returned 0 (a reaction did not exist previously).
        int update = mockSql.update(TABLE_NAME, testContents, where, null);
        assertEquals(0, update);

        // Assert that the reaction saved for the message is the one that was assigned.
        String emoji = emojiReactionDatabase.getReactionEmoji(mockMessageRecord);
        assertEquals(REACTION, emoji);
    }

    @Test
    public void testUpdateMessageReactionBySender() {
        // Stubbing update method; should return 1 meaning that the reaction currently exists.
        PowerMockito.when(mockSql.update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any())).thenReturn(1);

        // Verify that update method was called, but not the insert method (reaction is updated).
        emojiReactionDatabase.setMessageReaction(mockMessageRecord, REACTION);
        verify(mockSql).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any());
        verify(mockSql, never()).insert(Matchers.anyString(), Matchers.any(), Matchers.any(ContentValues.class));

        // Assert that the update value returned 1 (a reaction existed previously).
        int update = mockSql.update(TABLE_NAME, testContents, where, null);
        assertEquals(1, update);

        // Assert that the reaction saved for the message is the one that was assigned.
        String emoji = emojiReactionDatabase.getReactionEmoji(mockMessageRecord);
        assertEquals(REACTION, emoji);
    }

    @Test
    public void testSetMessageReactionByRecipient() {
        // Stubbing update method; should return 0 meaning that the reaction does not currently exist.
        PowerMockito.when(mockSql.update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any())).thenReturn(0);

        // Verify that update and insert methods were called (reaction was set).
        emojiReactionDatabase.setMessageReaction(HASHED_ID, REACTION, threadId);
        verify(mockSql).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any());
        verify(mockSql).insert(Matchers.anyString(), Matchers.any(), Matchers.any(ContentValues.class));

        // Assert that the update value returned 0 (a reaction did not exist previously).
        int update = mockSql.update(TABLE_NAME, testContents, where, null);
        assertEquals(0, update);

        // Assert that the reaction saved for the message is the one that was assigned.
        String emoji = emojiReactionDatabase.getReactionEmoji(mockMessageRecord);
        assertEquals(REACTION, emoji);
    }

    @Test
    public void testUpdateMessageReactionByRecipient() {
        // Stubbing update method; should return 1 meaning that the reaction currently exists.
        PowerMockito.when(mockSql.update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any())).thenReturn(1);

        // Verify that update method was called, but not the insert method (reaction is updated).
        emojiReactionDatabase.setMessageReaction(HASHED_ID, REACTION, threadId);
        verify(mockSql).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any());
        verify(mockSql, never()).insert(Matchers.anyString(), Matchers.any(), Matchers.any(ContentValues.class));

        // Assert that the update value returned 1 (a reaction existed previously).
        int update = mockSql.update(TABLE_NAME, testContents, where, null);
        assertEquals(1, update);

        // Assert that the reaction saved for the message is the one that was assigned.
        String emoji = emojiReactionDatabase.getReactionEmoji(mockMessageRecord);
        assertEquals(REACTION, emoji);
    }

    @Test
    public void testDeleteReaction() {
        // Stubbing delete method; should return 1, which is the number of rows in DB deleted.
        PowerMockito.when(mockSql.delete(Matchers.anyString(), Matchers.anyString(), Matchers.any())).thenReturn(1);

        // Verify that the delete method was called (reaction is deleted from DB).
        emojiReactionDatabase.deleteReaction(HASHED_ID, threadId);
        verify(mockSql).delete(Matchers.anyString(), Matchers.anyString(), Matchers.any());

        // Assert that the delete value returned 1 (number of rows deleted from the database).
        int deleted = mockSql.delete(Matchers.anyString(), Matchers.anyString(), Matchers.any());
        assertEquals(1, deleted);
    }
}
