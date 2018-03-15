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
import static android.support.test.espresso.matcher.ViewMatchers.withText;

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
        ConversationActions.goToConversation("+15144022093");
        Expectations.checkIsShowingDescendantWithId(GenericUtil.nthChildOf(withId(android.R.id.list), 0), R.id.read_reminder);
    }

    @Test
    public void checkMarkingMultipleAsUnreadTest() {
        ConversationActions.createNewConversation("5144022093");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Sup?");
        ConversationActions.sendMessage("Sup?");
        ConversationActions.longPressMessageAt(0);
        ConversationActions.pressMessageAt(1);
        ConversationActions.markAsUnread();
        ConversationActions.goToConversation("+15144022093");
        Expectations.checkIsShowingDescendantWithId(GenericUtil.nthChildOf(withId(android.R.id.list), 0), R.id.read_reminder);
        Expectations.checkIsShowingDescendantWithId(GenericUtil.nthChildOf(withId(android.R.id.list), 1), R.id.read_reminder);
    }

    @Test
    public void checkShouldNotHaveUnreadOptionTest() {
        ConversationActions.createNewConversation("5144022093");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Sup?");
        ConversationActions.longPressMessageAt(0);
        ConversationActions.markAsUnread();
        ConversationActions.goToConversation("+15144022093");
        ConversationActions.longPressMessageAt(0);
        Expectations.checkDoesNotExist(R.id.menu_context_mark_as_unread);
    }

    @Test
    public void checkShouldNotHaveUnreadOptionIfMixedTest() {
        ConversationActions.createNewConversation("5144022093");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Sup?");
        ConversationActions.sendMessage("Sup?");
        ConversationActions.longPressMessageAt(0);
        ConversationActions.markAsUnread();
        ConversationActions.goToConversation("+15144022093");
        ConversationActions.longPressMessageAt(0);
        ConversationActions.pressMessageAt(1);
        Expectations.checkDoesNotExist(R.id.menu_context_mark_as_unread);
    }

    // TODO: Check why unread_indicator is not working in the emulator
    //@Test
    public void checkConversationListItemHasMarkTest() {
        ConversationActions.createNewConversation("5144022093");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Sup?");
        ConversationActions.longPressMessageAt(0);
        ConversationActions.markAsUnread();
        GenericUtil.waitFor("org.thoughtcrime.securesms:id/unread_indicator", 3000);
        Expectations.checkIsShowingDescendantWithId(withText("+15144022093"), R.id.unread_indicator);
    }

    @Test
    public void tapReleasesMarkAsUnreadTest() {
        ConversationActions.createNewConversation("5144022093");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Sup?");
        ConversationActions.longPressMessageAt(0);
        ConversationActions.markAsUnread();
        ConversationActions.goToConversation("+15144022093");
        ConversationActions.pressMessageAt(0);
        Expectations.checkIsNotShowingDescendantWithId(GenericUtil.nthChildOf(withId(android.R.id.list), 0), R.id.read_reminder);
    }
}
