package org.thoughtcrime.securesms;

import android.content.Context;
import android.os.AsyncTask;
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
import org.thoughtcrime.securesms.translation.TranslationResult;
import org.thoughtcrime.securesms.translation.TranslationTask;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
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

  @Test
  public void verifyPinningCallsToDB() {
    //Class being tested
    ConversationFragment convoFrag = new ConversationFragment();

    //Method to test
    String handlePin = "handlePinMessage";
    String handleUnpin = "handleUnpinMessage";

    Set<MessageRecord> mockMsgs = new HashSet<>();
    mockMsgs.add(mockMessageRecord);

    try {
      //Sms Messages
      PowerMockito.when(mockMessageRecord.isMms()).thenReturn(false);

      //Invoke the pin method
      Whitebox.invokeMethod(convoFrag, handlePin, mockMsgs);
      long threadId = 100;
      verify(mockSmsDb).markMessagesAsPinned(threadId, messageId);
      verify(mockMmsDb, times(0)).markMessagesAsPinned(anyLong(), anyInt());

      //Invoke the unpin method
      Whitebox.invokeMethod(convoFrag, handleUnpin, mockMsgs);
      verify(mockSmsDb).markMessagesAsUnpinned(threadId, messageId);
      verify(mockMmsDb, times(0)).markMessagesAsUnpinned(anyLong(), anyInt());

      //Mms Messages
      PowerMockito.when(mockMessageRecord.isMms()).thenReturn(true);

      //Invoke the pin method
      Whitebox.invokeMethod(convoFrag, handlePin, mockMsgs);
      verify(mockMmsDb).markMessagesAsPinned(threadId, messageId);
      verify(mockSmsDb, times(0)).markMessagesAsPinned(anyLong(), anyInt());

      //Invoke the unpin method
      Whitebox.invokeMethod(convoFrag, handleUnpin, mockMsgs);
      verify(mockMmsDb).markMessagesAsUnpinned(threadId, messageId);
      verify(mockSmsDb, times(0)).markMessagesAsUnpinned(anyLong(), anyInt());

    } catch (Exception e) {
      Log.d(TAG, e.getMessage());
    }
  }

  @Test
  public void verifyTranslationTaskIsCalled() {
      //Class being tested
      ConversationFragment convoFrag = new ConversationFragment();

      //Method to test
      String handleTranslate = "handleTranslateMessage";

      Set<MessageRecord> mockedMessage = new HashSet<>();
      mockedMessage.add(mockMessageRecord);
      TranslationTask mockedTask = mock(TranslationTask.class);
      AsyncTask<String, Void, TranslationResult> mockedAsync = mock(AsyncTask.class);

      try {
          when(mockedTask.execute(anyString(), anyString())).thenReturn(mockedAsync);

          //Invoke the translate method
          Whitebox.invokeMethod(convoFrag, handleTranslate, mockedMessage, mockedTask);

          verify(mockedTask).execute(anyString(), anyString());
      } catch (Exception e) {
          Log.e(TAG, e.getMessage());
      }
  }
}
