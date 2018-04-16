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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseFactory.class)
public class ConversationActivityUnitTest {
    private final String TAG = ConversationActivity.class.getSimpleName();

    private EmojiReactionDatabase mockEmojiDb;
    private MessageRecord mockMessageRecord;

    @Before
    public void setUp(){

        long messageId = 1112;

        ThreadDatabase mockThreadDb = PowerMockito.mock(ThreadDatabase.class);
        Context mockContext = PowerMockito.mock(Context.class);
        mockEmojiDb = PowerMockito.mock(EmojiReactionDatabase.class);
        PowerMockito.mockStatic(DatabaseFactory.class);

        when(DatabaseFactory.getThreadDatabase(mockContext)).thenReturn(mockThreadDb);
        when(DatabaseFactory.getEmojiReactionDatabase(mockContext)).thenReturn(mockEmojiDb);

        mockMessageRecord = PowerMockito.mock(MessageRecord.class);
        PowerMockito.when(mockMessageRecord.getId()).thenReturn(messageId);
    }

    @Test
    public void testOnRightSwipe() throws Exception{
        ConversationActivity conversationActivity = new ConversationActivity();
        
        conversationActivity.onRightSwipe();

        verifyPrivate(conversationActivity, times(1)).
                invoke("handleReturnToConversationList");
    }

    @Test
    public void verifySetEmojiCallsToDB() {
        //Class being tested
        ConversationActivity convoActivity = new ConversationActivity();

        //Method to test
        String handleEmoji = "handleEmojiReaction";

        try {

            //Invoke the emoji handler method
            Whitebox.invokeMethod(convoActivity, handleEmoji, mockMessageRecord);
            verify(mockEmojiDb).setMessageReaction(mockMessageRecord,anyString());


        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

}
