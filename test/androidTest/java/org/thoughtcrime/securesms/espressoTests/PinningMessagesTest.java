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
import org.thoughtcrime.securesms.util.EspressoUtil;

import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PinningMessagesTest {

    @Rule
    public ActivityTestRule<ConversationListActivity> mActivityTestRule = new ActivityTestRule<>(ConversationListActivity.class);

    @Test
    public void checkPinningAvailableTest() {
        ConversationActions.createNewConversation("123456");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Sup?");
        ConversationActions.longPressMessageAt(0);
        Expectations.checkIsDisplayed(R.id.menu_context_pin_message);
    }

    @Test
    public void pinningTest() {
        ConversationActions.createNewConversation("123456");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Sup?");
        ConversationActions.pinLastMessage();
        Expectations.checkIsShowingDescendantWithId(EspressoUtil.nthChildOf(withId(android.R.id.list), 0), R.id.pinned_indicator);
    }

    @Test
    public void checkPinningUnavailableIfPinnedTest() {
        ConversationActions.createNewConversation("123456");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Sup?");
        ConversationActions.pinLastMessage();
        ConversationActions.longPressMessageAt(0);
        Expectations.checkDoesNotExist(R.id.menu_context_pin_message);
    }

    @Test
    public void checkUnpinningAvailableTest() {
        ConversationActions.createNewConversation("123456");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Sup?");
        ConversationActions.pinLastMessage();
        ConversationActions.longPressMessageAt(0);
        Expectations.checkIsDisplayed(R.id.menu_context_unpin_message);
    }

    @Test
    public void checkUnpinningTakesAwayTest() {
        ConversationActions.createNewConversation("123456");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Sup?");
        ConversationActions.pinLastMessage();
        ConversationActions.unpinLastMessage();
        Expectations.checkIsNotShowingDescendantWithId(EspressoUtil.nthChildOf(withId(android.R.id.list), 0), R.id.pinned_indicator);
    }

    @Test
    public void checkMenuItemsDontShowOnMultiSelectTest() {
        ConversationActions.createNewConversation("123456");
        ConversationActions.enableSignalForSMS();
        ConversationActions.sendMessage("Sup?");
        ConversationActions.sendMessage("Sup?");
        ConversationActions.pinLastMessage();
        ConversationActions.longPressMessageAt(0);
        ConversationActions.pressMessageAt(1);
        Expectations.checkDoesNotExist(R.id.menu_context_pin_message);
        Expectations.checkDoesNotExist(R.id.menu_context_unpin_message);
    }

    @Test
    public void hasOptionToCheckPinnedMessagesTest() {
        ConversationActions.createNewConversation("123456");
        ConversationActions.openSettingsDropDown();
        Expectations.checkIsDisplayed(allOf(withId(R.id.title), withText("View pinned messages")));
    }

    @Test
    public void canOpenPinnedMessagesViewTest() {
        ConversationActions.createNewConversation("123456");
        ConversationActions.openViewPinnedMessages();
    }
}
