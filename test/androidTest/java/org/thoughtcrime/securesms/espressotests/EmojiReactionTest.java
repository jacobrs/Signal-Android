package org.thoughtcrime.securesms.espressotests;

import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.thoughtcrime.securesms.ConversationListActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.ConversationActions;
import org.thoughtcrime.securesms.util.Expectations;

/*
IMPORTANT:
Given that emoji reactions can only be sent through sms and not text, it is important to run this test on a VERIFIED emulator
After running GetFakeVerifiedTest, you must go back to the emulator and verify with a real phone number before running this test
 */
public class EmojiReactionTest {

    @Rule
    public ActivityTestRule<ConversationListActivity> mActivityTestRule = new ActivityTestRule<>(ConversationListActivity.class);

    @Test
    public void emojiReactionTest(){
        // Open a new conversation.
        ConversationActions.createNewConversation("5147304866");
        ConversationActions.enableSignalForSMS();

        //Send a message and then long press it
        ConversationActions.sendMessage("Sup?");
        ConversationActions.longPressMessageAt(0);

        Expectations.checkIsDisplayed(R.id.menu_emoji_reaction);

        //Click on the emoji reaction button and verify the drawer pops up
        ConversationActions.clickOnViewWithId(R.id.menu_emoji_reaction);
        Expectations.checkIsDisplayed(R.id.emoji_drawer);

        ConversationActions.clickOnEmojiTab();
        ConversationActions.clickOnFirstEmoji();

        //check if the emoji drawer is closed & proper emoji is displayed
        Expectations.checkIsNotDisplayed(R.id.emoji_drawer);
        Expectations.checkIsDisplayed(R.id.emoji_reaction, "🏡");

    }
}
