package org.thoughtcrime.securesms.util;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.thoughtcrime.securesms.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

public class ConversationActions {
    public static void createNewConversation(String number) {
        ViewInteraction pulsingFloatingActionButton = onView(
                allOf(withId(R.id.fab), withContentDescription("New conversation"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_container),
                                        0),
                                3),
                        isDisplayed()));
        pulsingFloatingActionButton.perform(click());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.search_view),
                        childAtPosition(
                                allOf(withId(R.id.toggle_container),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("5144022093"), closeSoftKeyboard());

        onView(withId(R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }

    public static void enableSignalForSMS() {
        onView(withText("Enable Signal for SMS")).withFailureHandler((error, viewMatcher) -> System.out.println("Already enabled to default SMS.")).perform(click());
        onView(withText("Yes")).withFailureHandler((error, viewMatcher) -> System.out.println("Already enabled to default SMS.")).perform(click());
    }

    public static void sendMessage(String message) {
        ViewInteraction composeText2 = onView(
                allOf(withId(R.id.embedded_text_editor), withContentDescription("Message composition"),
                        isDisplayed()));
        composeText2.perform(replaceText(message));

        ViewInteraction composeText3 = onView(
                allOf(withId(R.id.embedded_text_editor), withText(message), withContentDescription("Message composition"),
                        childAtPosition(
                                allOf(withId(R.id.compose_bubble),
                                        childAtPosition(
                                                withClassName(is("android.widget.FrameLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        composeText3.perform(closeSoftKeyboard());

        onView(withId(R.id.send_button)).perform(click());
    }

    public static void openConversationSettings() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView2 = onView(
                allOf(withId(R.id.title), withText("Conversation settings"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView2.perform(click());
    }

    public static void changeChatName(String name) {
        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.list),
                        childAtPosition(
                                withId(android.R.id.list_container),
                                0)));
        recyclerView2.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(android.R.id.edit),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                1)));
        appCompatEditText4.perform(scrollTo(), replaceText(name), closeSoftKeyboard());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton2.perform(scrollTo(), click());
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
