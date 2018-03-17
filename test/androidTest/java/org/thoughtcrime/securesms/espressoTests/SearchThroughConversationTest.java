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
public class SearchThroughConversationTest {

    @Rule
    public ActivityTestRule<ConversationListActivity> mActivityTestRule = new ActivityTestRule<>(ConversationListActivity.class);

    @Test
    public void searchThroughConversationTest() {

        // Open a new conversation.
        ConversationActions.createNewConversation("5149714708");
        ConversationActions.enableSignalForSMS();

        // Send a new message "hello". Check that the message appears in the conversation body.
        ConversationActions.sendMessage("hello");
        Expectations.checkIsDisplayed(R.id.conversation_item_body, "hello");

        // Send a few new messages. Check that the messages appears in the conversation body.
        ConversationActions.sendMessage("hello again");
        ConversationActions.sendMessage("helmet");
        ConversationActions.sendMessage("banana");
        ConversationActions.sendMessage("phone");
        Expectations.checkIsDisplayed(R.id.conversation_item_body, "hello again");
        Expectations.checkIsDisplayed(R.id.conversation_item_body, "helmet");
        Expectations.checkIsDisplayed(R.id.conversation_item_body, "banana");
        Expectations.checkIsDisplayed(R.id.conversation_item_body, "phone");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Search for the word "hello"
        ConversationActions.searchInConversation("hello");

        // Check that only message bubbles containing "hello" are visible;
        // All other messages are not displayed.
        Expectations.checkIsDisplayed(R.id.conversation_item_body, "hello");
        Expectations.checkIsDisplayed(R.id.conversation_item_body, "hello again");
        Expectations.checkIsNotDisplayed(R.id.conversation_item_body, "helmet");
        Expectations.checkIsNotDisplayed(R.id.conversation_item_body, "banana");
        Expectations.checkIsNotDisplayed(R.id.conversation_item_body, "phone");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
