package org.thoughtcrime.securesms.espressoTests;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.thoughtcrime.securesms.ConversationListActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.ConversationActions;
import org.thoughtcrime.securesms.util.GenericActions;

import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PersistentMessageStateTest {

    // These should be replaced with your own number
    // or to another user who is a valid signal user
    private String number = "5555123457";
    private String numberPlusOne = "+15555123457";

    @Rule
    public ActivityTestRule<ConversationListActivity> mActivityTestRule = new ActivityTestRule<>(ConversationListActivity.class);

    // These tests should only run manually as you need to actually be verified
    //@Test
    @Ignore
    public void shouldRememberSMSStateTest() {
        ConversationActions.createNewConversation(number);
        ConversationActions.enableSignalForSMS();
        ConversationActions.switchToMessagingSMS();
        GenericActions.navigateBack("conversation");
        ConversationActions.goToConversation(numberPlusOne);
        allOf(withId(R.id.send_button)).matches(withContentDescription("Insecure SMS"));
    }

    //@Test
    @Ignore
    public void shouldRememberSignalStateTest() {
        ConversationActions.createNewConversation(number);
        ConversationActions.enableSignalForSMS();
        ConversationActions.switchToMessagingSMS();
        GenericActions.navigateBack("conversation");
        ConversationActions.goToConversation(numberPlusOne);
        allOf(withId(R.id.send_button)).matches(withContentDescription("Insecure SMS"));
        ConversationActions.switchToMessagingSignal();
        GenericActions.navigateBack("conversation");
        ConversationActions.goToConversation(numberPlusOne);
        allOf(withId(R.id.send_button)).matches(withContentDescription("Signal"));
    }
}
