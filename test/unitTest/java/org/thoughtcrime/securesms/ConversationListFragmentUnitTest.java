package org.thoughtcrime.securesms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.ThreadDatabase;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

/**
 * Created by Bryce on 2/11/2018.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseFactory.class)
public class ConversationListFragmentUnitTest extends BaseUnitTest {
    @Test
    public void testOnDeleteSwipe() throws Exception{
        //Arrange
        long threadId = 1111111;
        ConversationListFragment conversationListFragment = new ConversationListFragment();

        //Mock database
        ThreadDatabase threadDatabase = PowerMockito.mock(ThreadDatabase.class);

        //Mocking database factory
        PowerMockito.mockStatic(DatabaseFactory.class);
        PowerMockito.when(DatabaseFactory.getThreadDatabase(null)).thenReturn(threadDatabase);

        //Act
        conversationListFragment.deleteConversationThread(threadId);


        //Assert
        verify(threadDatabase).deleteConversation(threadId);
    }
}
