package org.thoughtcrime.securesms;

import android.content.Context;
import android.util.Log;

import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.EmojiReactionDatabase;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.jobs.PushDecryptJob;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;
import org.whispersystems.signalservice.api.messages.SignalServiceEnvelope;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseFactory.class)
public class PushDecryptJobUnitTest {
  private final String TAG = PushDecryptJob.class.getSimpleName();

  private long messageId = 1112;

  private ThreadDatabase mockThreadDb;
  private Context mockContext;
  private EmojiReactionDatabase mockEmojiDb;
  private MessageRecord mockMessageRecord;
  private SignalServiceEnvelope mockEnvelope;
  private SignalServiceDataMessage mockDataMessage;
  private Recipient mockRecipient;

  @Before
  public void setUp(){

    long threadId = 1111;

    //Mocks
    mockThreadDb = PowerMockito.mock(ThreadDatabase.class);
    mockEmojiDb = PowerMockito.mock(EmojiReactionDatabase.class);
    mockContext = PowerMockito.mock(Context.class);
    PowerMockito.mockStatic(DatabaseFactory.class);
    mockEnvelope = PowerMockito.mock(SignalServiceEnvelope.class);
    mockDataMessage = PowerMockito.mock(SignalServiceDataMessage.class);
    mockRecipient = PowerMockito.mock(Recipient.class);

    Optional<String> messageString = PowerMockito.mock(Optional.class);

    //Stubs
    when(DatabaseFactory.getThreadDatabase(mockContext)).thenReturn(mockThreadDb);
    when(mockThreadDb.getThreadIdFor(mockRecipient)).thenReturn(threadId);
    when(DatabaseFactory.getEmojiReactionDatabase(mockContext)).thenReturn(mockEmojiDb);

    when(mockDataMessage.getBody()).thenReturn(messageString);
    when(messageString.get()).thenReturn("ReactionEmoji-:)-HashedId-123");

    //Mocks for the MessageRecord
    mockMessageRecord = PowerMockito.mock(MessageRecord.class);
    when(mockMessageRecord.getId()).thenReturn(messageId);
    when(mockMessageRecord.getHashedId()).thenReturn("123");
  }

  @Test
  public void verifySetEmojiCallsToDBOnReceivedMessage() {
    //Class being tested
    PushDecryptJob pushDecryptJob = new PushDecryptJob(mockContext,messageId);

    //Method to test
    String handleEmoji = "handleEmojiReactionMessage";

    try {
      Whitebox.invokeMethod(pushDecryptJob, handleEmoji, mockEnvelope, mockDataMessage);
      Optional<String> opString = mockDataMessage.getBody();
      String message = opString.get();
      String[] parsed = message.split("-");

      verify(mockEmojiDb).setMessageReaction(parsed[3],parsed[1],mockThreadDb.getThreadIdFor(mockRecipient));
      assert(mockEmojiDb.getReactionEmoji(mockMessageRecord).equalsIgnoreCase(":)"));


    } catch (Exception e) {
      Log.d(TAG, e.getMessage());
    }

  }

}
