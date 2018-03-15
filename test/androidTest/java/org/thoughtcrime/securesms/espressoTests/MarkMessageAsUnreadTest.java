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
import org.thoughtcrime.securesms.util.GenericUtil;

import static android.support.test.espresso.matcher.ViewMatchers.withId;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MarkMessageAsUnreadTest {

    @Rule
    public ActivityTestRule<ConversationListActivity> mActivityTestRule = new ActivityTestRule<>(ConversationListActivity.class);

    @Test
    public void checkMarkAsUnreadAvailableTest() {
        ConversationActions.createNewConversation("5144022093");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Sup?");
        ConversationActions.longPressMessageAt(0);
        Expectations.checkIsDisplayed(R.id.menu_context_mark_as_unread);
    }

    @Test
    public void checkMarkingAsUnreadTest() {
        ConversationActions.createNewConversation("5144022093");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Sup?");
        ConversationActions.longPressMessageAt(0);
        ConversationActions.markAsUnread();
        Expectations.checkIsShowingDescendantWithId(GenericUtil.nthChildOf(withId(android.R.id.list), 0), R.id.unread_indicator);
    }
}
