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
public class TranslationTest {

    @Rule
    public ActivityTestRule<ConversationListActivity> mActivityTestRule = new ActivityTestRule<>(ConversationListActivity.class);

    @Test
    public void testTranslatingText() {
        ConversationActions.createNewConversation("123457");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Bonjour");
        ConversationActions.translateMessageAt(0);
        Expectations.checkIsDisplayed(R.id.conversation_item_body, "Hello [Bonjour]");

        ConversationActions.sendMessage("Guten Tag");
        ConversationActions.translateMessageAt(0);
        Expectations.checkIsDisplayed(R.id.conversation_item_body, "Good Day [Guten Tag]");
    }
}
