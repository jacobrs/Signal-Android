package org.thoughtcrime.securesms.database;

import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.ConversationFragment;
import org.thoughtcrime.securesms.database.model.MessageRecord;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseFactory.class)
public class SmsMmsDatabaseTest {


    @Test
    public void markMessageAsUnreadSmsTest(){
        long threadId = 1111;
        long messageId = 1112;

        String dbUpdateString = "thread_id = ? AND _id = ? AND read <> 0 AND marked_unread = 0";
        String[] dbUpdateArgs = {String.valueOf(threadId), String.valueOf(messageId)};

        //Mock databases
        SmsDatabase mockSmsDb = PowerMockito.mock(SmsDatabase.class);

        PowerMockito.mockStatic(DatabaseFactory.class);
        PowerMockito.when(DatabaseFactory.getSmsDatabase(null)).thenReturn(mockSmsDb);

        mockSmsDb.setMessagesUnread(threadId, messageId);

        try {
            PowerMockito.verifyPrivate(mockSmsDb).invoke("setMessagesAsUnread", dbUpdateString, dbUpdateArgs);
        } catch (Exception e){
        }
    }

    @Test
    public void markMessageAsUnreadMmsTest(){
        long threadId = 1111;
        long messageId = 1112;

        String dbUpdateString = "thread_id = ? AND _id = ? AND read <> 0 AND marked_unread = 0";
        String[] dbUpdateArgs = {String.valueOf(threadId), String.valueOf(messageId)};

        //Mock databases
        SmsDatabase mockMmsDb = PowerMockito.mock(SmsDatabase.class);

        PowerMockito.mockStatic(DatabaseFactory.class);
        PowerMockito.when(DatabaseFactory.getSmsDatabase(null)).thenReturn(mockMmsDb);

        mockMmsDb.setMessagesUnread(threadId, messageId);

        try {
            PowerMockito.verifyPrivate(mockMmsDb).invoke("setMessagesAsUnread", dbUpdateString, dbUpdateArgs);
        } catch (Exception e){
        }
    }
}
