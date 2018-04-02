package org.thoughtcrime.securesms;

import android.os.Handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.ThreadDatabase;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseFactory.class)
public class ConversationListFragmentUnitTest extends BaseUnitTest {

    private static boolean t1done = false;

    public static void terminateT1() {
        t1done = true;
    }

    @Test
    public void testOnDeleteSwipe() throws Exception {
        // Arrange
        long delay = 500;
        long threadId = 1111111;
        ConversationListFragment conversationListFragment = new ConversationListFragment();

        // Mock database
        ThreadDatabase threadDatabase = PowerMockito.mock(ThreadDatabase.class);

        Runnable mockDeleteCall = () -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            threadDatabase.deleteConversation(threadId);
        };

        Thread thread1 = new Thread(mockDeleteCall);
        Thread thread2 = new Thread(mockDeleteCall);

        Handler mockHandler = mock(Handler.class);
        doAnswer(invocation -> {
            if(t1done) thread2.start();
            else thread1.start();
            return null;
        }).when(mockHandler).postDelayed(any(Runnable.class), anyLong());

        doAnswer(invocation -> {
            terminateT1();
            thread1.stop();
            return null;
        }).when(mockHandler).removeCallbacks(any());

        conversationListFragment.deletionHandler = mockHandler;

        // Mocking database factory
        PowerMockito.mockStatic(DatabaseFactory.class);
        when(DatabaseFactory.getThreadDatabase(null)).thenReturn(threadDatabase);

        // Perform swipe
        conversationListFragment.delayedConversationThreadDeletion(threadId, delay);

        // Cancel
        conversationListFragment.cancelDelayedDeletion(threadId);

        // Perform swipe
        conversationListFragment.delayedConversationThreadDeletion(threadId, delay);

        // Don't cancel
        Thread.sleep(1000);

        // Assert Deletion
        verify(threadDatabase).deleteConversation(threadId);
    }
}
