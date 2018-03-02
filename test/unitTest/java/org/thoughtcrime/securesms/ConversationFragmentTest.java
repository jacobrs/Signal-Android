package org.thoughtcrime.securesms.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.ConversationFragment;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.MmsDatabase;
import org.thoughtcrime.securesms.database.SmsDatabase;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.database.model.MessageRecord;

import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseFactory.class)
public class ConversationFragmentTest {


    @Test
    public void removeMessageAsUnreadTest(){
        long threadId = 1111;
        long messageId = 1112;

        ConversationFragment conversationFragment = new ConversationFragment();

        MessageRecord mockMessageRecord = PowerMockito.mock(MessageRecord.class);
        PowerMockito.when(mockMessageRecord.isMarkedAsUnread()).thenReturn(true);

        



    }
}
