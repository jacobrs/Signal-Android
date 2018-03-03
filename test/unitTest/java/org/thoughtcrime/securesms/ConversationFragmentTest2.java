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
import org.thoughtcrime.securesms.database.model.MessageRecord;
import java.util.HashSet;
import java.util.Set;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseFactory.class)
public class ConversationFragmentTest2 {
		private final String TAG = ConversationFragment.class.getSimpleName();

		long threadId = 100;
		long messageId = 1;

		//Mocked Classes
    private Context mockContext;
    private SmsDatabase mockSmsDB;
    private MmsDatabase mockMmsDB;
    private MessageRecord mockMessageRecord;

		@Before
   	public void setup(){
			mockSmsDB = PowerMockito.mock(SmsDatabase.class);
			mockMmsDB = PowerMockito.mock(MmsDatabase.class);
			mockContext = PowerMockito.mock(Context.class);
			PowerMockito.mockStatic(DatabaseFactory.class);

			when(DatabaseFactory.getSmsDatabase(mockContext)).thenReturn(mockSmsDB);

			mockMessageRecord = PowerMockito.mock(MessageRecord.class);
			PowerMockito.when(mockMessageRecord.getId()).thenReturn(messageId);
		}

		@Test
		public void verifyPinningCallsToDB(){
   			//Class being tested
   			ConversationFragment convoFrag = new ConversationFragment();

   			//Method to test
				String handlePin = "handlePinMessage";
				String handleUnpin = "handleUnpinMessage";

				Set<MessageRecord> mockMsgs = new HashSet<>();
				mockMsgs.add(mockMessageRecord);

				try{
					//Sms Messages
					PowerMockito.when(mockMessageRecord.isMms()).thenReturn(false);

					//Invoke the pin method
					Whitebox.invokeMethod(convoFrag, handlePin, mockMsgs);
					verify(mockSmsDB).markMessagesAsPinned(threadId, messageId);
					verify(mockMmsDB, times(0)).markMessagesAsPinned(anyLong(), anyInt());

					//Invoke the unpin method
					Whitebox.invokeMethod(convoFrag, handleUnpin, mockMsgs);
					verify(mockSmsDB).markMessagesAsUnpinned(threadId, messageId);
					verify(mockMmsDB, times(0)).markMessagesAsUnpinned(anyLong(), anyInt());

					//Mms Messages
					PowerMockito.when(mockMessageRecord.isMms()).thenReturn(true);

					//Invoke the pin method
					Whitebox.invokeMethod(convoFrag, handlePin, mockMsgs);
					verify(mockMmsDB).markMessagesAsPinned(threadId, messageId);
					verify(mockSmsDB, times(0)).markMessagesAsPinned(anyLong(), anyInt());

					//Invoke the unpin method
					Whitebox.invokeMethod(convoFrag, handleUnpin, mockMsgs);
					verify(mockMmsDB).markMessagesAsUnpinned(threadId, messageId);
					verify(mockSmsDB, times(0)).markMessagesAsUnpinned(anyLong(), anyInt());

				}catch(Exception e) {
						Log.d(TAG, e.getMessage());
				}
		}
}
