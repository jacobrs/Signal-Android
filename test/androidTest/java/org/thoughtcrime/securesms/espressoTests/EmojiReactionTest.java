package org.thoughtcrime.securesms.espressoTests;


import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.thoughtcrime.securesms.ConversationListActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.ConversationActions;
import org.thoughtcrime.securesms.util.Expectations;

public class EmojiReactionTest {

    @Rule
    public ActivityTestRule<ConversationListActivity> mActivityTestRule = new ActivityTestRule<>(ConversationListActivity.class);

    @Test
    public void emojiReactionTest(){
        // Open a new conversation.
        ConversationActions.createNewConversation("5149714708");
        ConversationActions.enableSignalForSMS();

        //Send a message and then long press it
        ConversationActions.sendMessage("Sup?");
        ConversationActions.longPressMessageAt(0);

        Expectations.checkIsDisplayed(R.id.emoji_reaction);
    }
}
