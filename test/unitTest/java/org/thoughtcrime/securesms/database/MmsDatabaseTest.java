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
public class MmsDatabaseTest {
    private static final String TAG = MmsDatabaseTest.class.getSimpleName();

    private long threadId;
    private long messageId;
    private int testMarkAsUnreadStatus;
    private boolean messageUnread;

    private String tableName;

    private String dbMarkUnreadString;
    private String dbRemoveReadReminder;

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

        tableName = "mms";

        dbMarkUnreadString = "thread_id = ? AND _id = ? AND read <> 0 AND read_reminder = 0";
        dbRemoveReadReminder = "thread_id = ? AND _id = ? AND read_reminder <> 0";

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
    }

    @Test
    public void markMessageAsUnreadTest() {

        MmsDatabase mmsDatabase = new MmsDatabase(mockContext, mockSqlHelper);
        testContents.put("read", 0);
        testContents.put("mark_unread", 1);

        PowerMockito.doAnswer((Answer) invocation -> {
            Object[] args = invocation.getArguments();

            //Verifying that the database update call was passed the right arguments
            assertEquals(tableName, args[0]);

            assert(args[1].getClass().isInstance(ContentValues.class));
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

            mmsDatabase.setMessagesUnread(threadId, messageId);

            PowerMockito.verifyPrivate(mmsDatabase).invoke("setMessagesAsUnread", dbMarkUnreadString, dbUpdateArgs);

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

        MmsDatabase mmsDatabase = new MmsDatabase(mockContext, mockSqlHelper);
        PowerMockito.doAnswer((Answer) invocation -> {
            Object[] args = invocation.getArguments();

            //Verifying that the database update call was passed the right arguments
            assertEquals(tableName, args[0]);

            assert(args[1].getClass().isInstance(ContentValues.class));
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

            mmsDatabase.setMessagesUnread(threadId, messageId);

            PowerMockito.verifyPrivate(mmsDatabase).invoke("setMessagesAsUnread", dbMarkUnreadString, dbUpdateArgs);
            verify(mockSql.update(tableName, testContents, dbMarkUnreadString, dbUpdateArgs));

            assertEquals(1, testMarkAsUnreadStatus);
            assertEquals(true, messageUnread);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Test
    public void unmarkMessageAsUnread(){
        MmsDatabase mmsDatabase = new MmsDatabase(mockContext, mockSqlHelper);

        messageUnread = true;
        testMarkAsUnreadStatus = 1;

        testContents.put("read", 1);
        testContents.put("mark_unread", 0);

        PowerMockito.doAnswer((Answer) invocation -> {
            Object[] args = invocation.getArguments();

            //Verifying that the database update call was passed the right arguments
            assertEquals(tableName, args[0]);

            assert(args[1].getClass().isInstance(ContentValues.class));
            assertEquals(testContents.get("read"), ((ContentValues) args[1]).get("read"));
            assertEquals(testContents.get("marked_unread"), ((ContentValues) args[1]).get("marked_unread"));

            assertEquals(dbRemoveReadReminder, args[2]);

            assert(args[3].getClass().isArray());
            assertEquals(dbUpdateArgs[0], ((String[])args[3])[0]);
            assertEquals(dbUpdateArgs[1], ((String[])args[3])[1]);

            removeMarkAsUnread();
            return null;
        }).when(mockSql).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any(String[].class));

        try {
            assertEquals(true, messageUnread);
            assertEquals(1, testMarkAsUnreadStatus);

            mmsDatabase.removeReadReminder(threadId, messageId);

            PowerMockito.verifyPrivate(mmsDatabase).invoke("removeReadReminder", dbRemoveReadReminder, dbUpdateArgs);
            verify(mockSql.update(tableName, testContents, dbMarkUnreadString, dbUpdateArgs));

            assertEquals(0, testMarkAsUnreadStatus);
            assertEquals(false, messageUnread);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

    }

    @Test
    public void unmarkMessageAsUnreadNothingHappens(){
        MmsDatabase mmsDatabase = new MmsDatabase(mockContext, mockSqlHelper);

        messageUnread = false;
        testMarkAsUnreadStatus = 0;

        testContents.put("read", 1);
        testContents.put("mark_unread", 0);

        PowerMockito.doAnswer((Answer) invocation -> {
            Object[] args = invocation.getArguments();

            //Verifying that the database update call was passed the right arguments
            assertEquals(tableName, args[0]);

            assert(args[1].getClass().isInstance(ContentValues.class));
            assertEquals(testContents.get("read"), ((ContentValues) args[1]).get("read"));
            assertEquals(testContents.get("marked_unread"), ((ContentValues) args[1]).get("marked_unread"));

            assertEquals(dbRemoveReadReminder, args[2]);

            assert(args[3].getClass().isArray());
            assertEquals(dbUpdateArgs[0], ((String[])args[3])[0]);
            assertEquals(dbUpdateArgs[1], ((String[])args[3])[1]);


            removeMarkAsUnread();
            return null;
        }).when(mockSql).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any(String[].class));

        try {
            assertEquals(false, messageUnread);
            assertEquals(0, testMarkAsUnreadStatus);

            mmsDatabase.removeReadReminder(threadId, messageId);

            PowerMockito.verifyPrivate(mmsDatabase).invoke("removeReadReminder", dbRemoveReadReminder, dbUpdateArgs);
            verify(mockSql.update(tableName, testContents, dbMarkUnreadString, dbUpdateArgs));

            assertEquals(0, testMarkAsUnreadStatus);
            assertEquals(false, messageUnread);
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
}
