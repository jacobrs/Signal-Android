package org.thoughtcrime.securesms.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.ApplicationContext;
import org.whispersystems.jobqueue.JobManager;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseFactory.class)
public class SmsDatabaseTest {
    private static final String TAG = SmsDatabaseTest.class.getSimpleName();

    private long threadId;
    private long messageId;
    private int testMarkAsUnreadStatus;
    private boolean messageUnread;
    private int pinnedStatus;
    private boolean isPinned;

    private String tableName;

    private String dbMarkUnreadString;
    private String dbRemoveReadReminderString;
    private String dbPinnedString;
    private String dbUnpinnedString;

    private String[] dbUpdateArgs = new String[2];

    private ContentValues testContents = new ContentValues();

    //Mocked classes
    private SQLiteDatabase mockSql = PowerMockito.mock(SQLiteDatabase.class);
    private SQLiteOpenHelper mockSqlHelper = PowerMockito.mock(SQLiteOpenHelper.class);
    private Context mockContext = PowerMockito.mock(Context.class);
    private JobManager mockJobManger = PowerMockito.mock(JobManager.class);

    private ApplicationContext mockApplicationContext = PowerMockito.mock(ApplicationContext.class);

    @Before
    public void setup() {
        threadId = 1111;
        messageId = 1112;
        testMarkAsUnreadStatus = 0;
        pinnedStatus = 0;
        isPinned = false;

        tableName = "sms";

        dbMarkUnreadString = "thread_id = ? AND _id = ? AND read <> 0 AND read_reminder = 0";
        dbRemoveReadReminderString = "thread_id = ? AND _id = ? AND read_reminder <> 0";
        dbPinnedString = "thread_id = ? AND _id = ? AND pinned = 0";
        dbUnpinnedString = "thread_id = ? AND _id = ? AND pinned = 1";

        dbUpdateArgs[0] = String.valueOf(threadId);
        dbUpdateArgs[1] = String.valueOf(messageId);

        //Stubbed methods
        PowerMockito.when(mockSqlHelper.getWritableDatabase()).thenReturn(mockSql);
        PowerMockito.when(ApplicationContext.getInstance(mockContext)).thenReturn(mockApplicationContext);
        PowerMockito.when(mockApplicationContext.getJobManager()).thenReturn(mockJobManger);

    }

    @After
    public void resetValues(){
        messageUnread = false;
        testMarkAsUnreadStatus = 0;
        isPinned = false;
        pinnedStatus = 0;
    }

    @Test
    public void markMessageAsUnreadTest() {

        SmsDatabase smsDatabase = new SmsDatabase(mockContext, mockSqlHelper);
        testContents.put("read", 0);
        testContents.put("mark_unread", 1);

        PowerMockito.doAnswer((Answer) invocation -> {
            Object[] args = invocation.getArguments();

            //Verifying that the database update call was passed the right arguments
            assertEquals(tableName, args[0]);

            assertEquals(testContents.get("read"), ((ContentValues) args[1]).get("read"));
            assertEquals(testContents.get("marked_unread"), ((ContentValues) args[1]).get("marked_unread"));

            assertEquals(dbMarkUnreadString, args[2]);

            assert(args[3].getClass().isArray());
            assertEquals(dbUpdateArgs[0], ((String[])args[3])[0]);
            assertEquals(dbUpdateArgs[1], ((String[])args[3])[1]);

            markAsUnread();
            return null;
        }).when(mockSql).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any(String[].class));


        try {
            assertEquals(false, messageUnread);
            assertEquals(0, testMarkAsUnreadStatus);

            smsDatabase.setMessagesUnread(threadId, messageId);

            PowerMockito.verifyPrivate(smsDatabase).invoke("setMessagesAsUnread", dbMarkUnreadString, dbUpdateArgs);

            //verify that the database function was called with the proper arguments
            verify(mockSql.update(tableName, testContents, dbMarkUnreadString, dbUpdateArgs));

            assertEquals(1, testMarkAsUnreadStatus);
            assertEquals(true, messageUnread);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }


    @Test
    public void markMessageAsUnreadNothingHappens(){
        messageUnread = true;
        testMarkAsUnreadStatus = 1;

        testContents.put("read", 0);
        testContents.put("mark_unread", 1);

        SmsDatabase smsDatabase = new SmsDatabase(mockContext, mockSqlHelper);
        PowerMockito.doAnswer((Answer) invocation -> {
            Object[] args = invocation.getArguments();

            //Verifying that the database update call was passed the right arguments
            assertEquals(tableName, args[0]);

            assertEquals(testContents.get("read"), ((ContentValues) args[1]).get("read"));
            assertEquals(testContents.get("marked_unread"), ((ContentValues) args[1]).get("marked_unread"));

            assertEquals(dbMarkUnreadString, args[2]);

            assert(args[3].getClass().isArray());
            assertEquals(dbUpdateArgs[0], ((String[])args[3])[0]);
            assertEquals(dbUpdateArgs[1], ((String[])args[3])[1]);

            markAsUnread();
            return null;
        }).when(mockSql).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any(String[].class));


        try {
            assertEquals(true, messageUnread);
            assertEquals(1, testMarkAsUnreadStatus);

            smsDatabase.setMessagesUnread(threadId, messageId);

            PowerMockito.verifyPrivate(smsDatabase).invoke("setMessagesAsUnread", dbMarkUnreadString, dbUpdateArgs);
            verify(mockSql.update(tableName, testContents, dbMarkUnreadString, dbUpdateArgs));

            assertEquals(1, testMarkAsUnreadStatus);
            assertEquals(true, messageUnread);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Test
    public void unmarkMessageAsUnread(){
        SmsDatabase smsDatabase = new SmsDatabase(mockContext, mockSqlHelper);

        messageUnread = true;
        testMarkAsUnreadStatus = 1;

        testContents.put("read", 1);
        testContents.put("mark_unread", 0);

        PowerMockito.doAnswer((Answer) invocation -> {
            Object[] args = invocation.getArguments();

            //Verifying that the database update call was passed the right arguments
            assertEquals(tableName, args[0]);

            assertEquals(testContents.get("read"), ((ContentValues) args[1]).get("read"));
            assertEquals(testContents.get("marked_unread"), ((ContentValues) args[1]).get("marked_unread"));

            assertEquals(dbRemoveReadReminderString, args[2]);

            assert(args[3].getClass().isArray());
            assertEquals(dbUpdateArgs[0], ((String[])args[3])[0]);
            assertEquals(dbUpdateArgs[1], ((String[])args[3])[1]);

            removeMarkAsUnread();
            return null;
        }).when(mockSql).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any(String[].class));

        try {
            assertEquals(true, messageUnread);
            assertEquals(1, testMarkAsUnreadStatus);

            smsDatabase.removeReadReminder(threadId, messageId);

            PowerMockito.verifyPrivate(smsDatabase).invoke("removeReadReminder", dbRemoveReadReminderString, dbUpdateArgs);
            verify(mockSql.update(tableName, testContents, dbMarkUnreadString, dbUpdateArgs));

            assertEquals(0, testMarkAsUnreadStatus);
            assertEquals(false, messageUnread);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

    }

    @Test
    public void unmarkMessageAsUnreadNothingHappens(){
        SmsDatabase smsDatabase = new SmsDatabase(mockContext, mockSqlHelper);

        messageUnread = false;
        testMarkAsUnreadStatus = 0;

        testContents.put("read", 1);
        testContents.put("mark_unread", 0);

        PowerMockito.doAnswer((Answer) invocation -> {
            Object[] args = invocation.getArguments();

            //Verifying that the database update call was passed the right arguments
            assertEquals(tableName, args[0]);


            assertEquals(testContents.get("read"), ((ContentValues) args[1]).get("read"));
            assertEquals(testContents.get("marked_unread"), ((ContentValues) args[1]).get("marked_unread"));

            assertEquals(dbRemoveReadReminderString, args[2]);

            assert(args[3].getClass().isArray());
            assertEquals(dbUpdateArgs[0], ((String[])args[3])[0]);
            assertEquals(dbUpdateArgs[1], ((String[])args[3])[1]);


            removeMarkAsUnread();
            return null;
        }).when(mockSql).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any(String[].class));

        try {
            assertEquals(false, messageUnread);
            assertEquals(0, testMarkAsUnreadStatus);

            smsDatabase.removeReadReminder(threadId, messageId);

            PowerMockito.verifyPrivate(smsDatabase).invoke("removeReadReminder", dbRemoveReadReminderString, dbUpdateArgs);
            verify(mockSql.update(tableName, testContents, dbMarkUnreadString, dbUpdateArgs));

            assertEquals(0, testMarkAsUnreadStatus);
            assertEquals(false, messageUnread);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Test
    public void markMessageAsPinnedTest() {
        SmsDatabase smsDatabase = new SmsDatabase(mockContext, mockSqlHelper);
        testContents.put("pinned", 0);

        PowerMockito.doAnswer((Answer) invocation -> {
            Object[] args = invocation.getArguments();

            //Verifying that the database update call was passed the right arguments
            assertEquals(tableName, args[0]);

            assertEquals(testContents.get("pinned"), ((ContentValues) args[1]).get("pinned"));

            assertEquals(dbPinnedString, args[2]);

            assert (args[3].getClass().isArray());
            assertEquals(dbUpdateArgs[0], ((String[]) args[3])[0]);
            assertEquals(dbUpdateArgs[1], ((String[]) args[3])[1]);

            markAsPinned();
            return null;
        }).when(mockSql).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any(String[].class));


        try {
            assertEquals(false, isPinned);
            assertEquals(0, pinnedStatus);

            smsDatabase.markMessagesAsPinned(threadId, messageId);

            PowerMockito.verifyPrivate(smsDatabase).invoke("markMessageAsPinned", dbPinnedString, dbUpdateArgs);

            assertEquals(true, isPinned);
            assertEquals(1, pinnedStatus);

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Test
    public void markMessageAsUnpinnedTest() {
        SmsDatabase smsDatabase = new SmsDatabase(mockContext, mockSqlHelper);
        testContents.put("pinned", 1);

        //Set beginning states
        isPinned = true;
        pinnedStatus = 1;

        PowerMockito.doAnswer((Answer) invocation -> {
            Object[] args = invocation.getArguments();

            //Verifying that the database update call was passed the right arguments
            assertEquals(tableName, args[0]);

            assertEquals(testContents.get("pinned"), ((ContentValues) args[1]).get("pinned"));

            assertEquals(dbUnpinnedString, args[2]);

            assert (args[3].getClass().isArray());
            assertEquals(dbUpdateArgs[0], ((String[]) args[3])[0]);
            assertEquals(dbUpdateArgs[1], ((String[]) args[3])[1]);

            markAsUnpinned();
            return null;
        }).when(mockSql).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any(String[].class));


        try {
            assertEquals(true, isPinned);
            assertEquals(1, pinnedStatus);

            smsDatabase.markMessagesAsUnpinned(threadId, messageId);

            PowerMockito.verifyPrivate(smsDatabase).invoke("markMessageAsUnpinned", dbPinnedString, dbUpdateArgs);

            assertEquals(false, isPinned);
            assertEquals(0, pinnedStatus);

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void markAsUnread(){
        if(!messageUnread){
            messageUnread = true;
            testMarkAsUnreadStatus = 1;
        }
    }

    private void removeMarkAsUnread(){
        if(messageUnread){
            messageUnread = false;
            testMarkAsUnreadStatus = 0;
        }
    }

    private void markAsPinned() {
        if (!isPinned) {
            isPinned = true;
            pinnedStatus = 1;
        }
    }

    private void markAsUnpinned() {
        if (isPinned) {
            isPinned = false;
            pinnedStatus = 0;
        }
    }
}
