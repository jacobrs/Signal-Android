package org.thoughtcrime.securesms;

import android.content.Context;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.MmsDatabase;
import org.thoughtcrime.securesms.database.SmsDatabase;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.database.model.MessageRecord;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseFactory.class)
public class ConversationFragmentTest {
    private final String TAG = ConversationFragment.class.getSimpleName();

    long threadId = 1111;
    long messageId = 1112;

    private ThreadDatabase mockThreadDb;
    private Context mockContext;
    private SmsDatabase mockSmsDb;
    private MmsDatabase mockMmsDb;
    private MessageRecord mockMessageRecord;

    @Before
   public void setup(){
        mockThreadDb = PowerMockito.mock(ThreadDatabase.class);
        mockSmsDb = PowerMockito.mock(SmsDatabase.class);
        mockMmsDb = PowerMockito.mock(MmsDatabase.class);
        mockContext = PowerMockito.mock(Context.class);
        PowerMockito.mockStatic(DatabaseFactory.class);

        when(DatabaseFactory.getThreadDatabase(mockContext)).thenReturn(mockThreadDb);
        when(DatabaseFactory.getSmsDatabase(mockContext)).thenReturn(mockSmsDb);

        mockMessageRecord = PowerMockito.mock(MessageRecord.class);
        PowerMockito.when(mockMessageRecord.readReminderSet()).thenReturn(true);
        PowerMockito.when(mockMessageRecord.isMms()).thenReturn(false);
        PowerMockito.when(mockMessageRecord.getId()).thenReturn(messageId);
    }

    @Test
    public void setSmsMessageUnreadTestCallsToDb(){
        ConversationFragment conversationFragment = new ConversationFragment();

        Set<MessageRecord> mockMessages = new HashSet<MessageRecord>();
        mockMessages.add(mockMessageRecord);

        try {
            Whitebox.invokeMethod(conversationFragment, "handleMarkAsUnread", mockMessages);

            //make sure that the right db was called for the right message type
            //Can really only verify here, as the message itself isn't being changed
            verify(mockThreadDb).incrementUnread(threadId, 1);
            verify(mockSmsDb).setMessagesUnread(threadId,  messageId);
            verify(mockMmsDb, times(0)).setMessagesUnread(anyLong(), anyInt());

            when(mockMessageRecord.isMms()).thenReturn(true);

            Whitebox.invokeMethod(conversationFragment, "handleMarkAsUnread", mockMessages);

            //make sure that the mms db was called for the right message type
            verify(mockThreadDb).incrementUnread(threadId, 1);
            verify(mockMmsDb).setMessagesUnread(threadId,  messageId);
            verify(mockSmsDb, times(0)).setMessagesUnread(anyLong(), anyInt());
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
        }
    }

    @Test
    public void removeUnreadReminderCallsToDb(){
        ConversationFragment conversationFragment = new ConversationFragment();

        when(mockMessageRecord.readReminderSet()).thenReturn(true);

        try{

            Whitebox.invokeMethod(conversationFragment, "onItemClick", mockMessageRecord);

            verify(mockSmsDb).removeReadReminder(threadId, messageId);
            verify(mockMmsDb, times(0)).removeReadReminder(threadId, messageId);

            when(mockMessageRecord.isMms()).thenReturn(true);
            verify(mockMmsDb).removeReadReminder(threadId, messageId);
            verify(mockSmsDb, times(0)).removeReadReminder(threadId, messageId);

        } catch(Exception e){
            Log.d(TAG, e.getMessage());
        }

    }
}
