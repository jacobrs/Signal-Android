package org.thoughtcrime.securesms.espressoTests;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.thoughtcrime.securesms.ConversationListActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.ConversationActions;
import org.thoughtcrime.securesms.util.Expectations;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ConversationDeleteTest {

    @Rule
    public ActivityTestRule<ConversationListActivity> mActivityTestRule = new ActivityTestRule<>(ConversationListActivity.class);

    @Test
    public void deleteConversationSimpleTest() throws InterruptedException {

        String convoRecipient = "+1119991111";

        ConversationActions.createNewConversationWithImmediateExit(convoRecipient, "hello");
        Expectations.checkIsDisplayed(R.id.from, convoRecipient);

        ConversationActions.deleteConversation(convoRecipient);
        Thread.sleep(6000);
        Expectations.checkDoesNotExist(R.id.from, convoRecipient);

    }

    @Test
    public void deleteConversationAndUndoTest() throws InterruptedException {

        String convoRecipient = "+1119991112";

        ConversationActions.createNewConversationWithImmediateExit(convoRecipient, "hello");
        Expectations.checkIsDisplayed(R.id.from, convoRecipient);

        ConversationActions.deleteConversation(convoRecipient);
        Expectations.checkDoesNotExist(R.id.from, convoRecipient);
        Thread.sleep(500);
        ConversationActions.undoDeletion();
        Expectations.checkIsDisplayed(R.id.from, convoRecipient);

    }
}
