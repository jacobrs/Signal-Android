package org.thoughtcrime.securesms.espressoTests;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.thoughtcrime.securesms.ConversationListActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.ConversationActions;
import org.thoughtcrime.securesms.util.GenericActions;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ConversationListActivityTest {

    @Rule
    public ActivityTestRule<ConversationListActivity> mActivityTestRule = new ActivityTestRule<>(ConversationListActivity.class);

    @Test
    public void conversationListActivityTestSettingChatName() {
        ConversationActions.createNewConversation("5144022093");
        ConversationActions.openConversationSettings();
        ConversationActions.changeChatName("test");
        GenericActions.pressBack();

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.title), isDisplayed()));
        textView2.check(matches(withText("test")));

    }

    @Test
    public void conversationListActivityTestUnsettingChatName() {
        ConversationActions.createNewConversation("5144022093");
        ConversationActions.openConversationSettings();
        ConversationActions.changeChatName("test");
        ConversationActions.changeChatName("");
        GenericActions.pressBack();

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.title), isDisplayed()));
        textView2.check(matches(withText("+15144022093")));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
