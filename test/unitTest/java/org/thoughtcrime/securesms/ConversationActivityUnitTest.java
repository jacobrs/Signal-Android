package org.thoughtcrime.securesms;

import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;


@RunWith(PowerMockRunner.class)
public class ConversationActivityUnitTest {

    @Test
    public void testOnRightSwipe() throws Exception{
        ConversationActivity conversationActivity = new ConversationActivity();
        
        conversationActivity.onRightSwipe();

        verifyPrivate(conversationActivity, times(1)).
                invoke("handleReturnToConversationList");
    }
}
