package org.thoughtcrime.securesms.util;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.thoughtcrime.securesms.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.doubleClick;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.hasChildCount;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withResourceName;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

public class ConversationActions {
    public static void createNewConversationWithImmediateExit(String number, String placeholder) {
        createNewConversation(number);
        sendMessage(placeholder);
        pressBack();
    }

    public static void createNewConversation(String number) {
        ViewInteraction pulsingFloatingActionButton = onView(
                allOf(withId(R.id.fab), withContentDescription("New conversation"),
                        isDisplayed()));
        pulsingFloatingActionButton.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.search_view),
                        childAtPosition(
                                allOf(withId(R.id.toggle_container),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText.perform(replaceText(number), closeSoftKeyboard());
        onView(withId(R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }

    public static void goToConversation(String nameOrNumber) {
        onView(allOf(allOf(withText(nameOrNumber), isDescendantOfA(withResourceName("list")))))
                .perform(click());
    }

    public static void deleteConversation(String nameOrNumber) {
        onView(allOf(withId(R.id.from), withText(nameOrNumber))).perform(swipeLeft());
    }

    public static void undoDeletion(){
        onView(withText("UNDO")).perform(click());
    }

    public static void enableSignalForSMS() {
        onView(withText("Enable Signal for SMS")).withFailureHandler((error, viewMatcher) -> System.out.println("Already enabled to default SMS.")).perform(click());
        // Initialize UiDevice instance
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Search for correct button in the dialog and click it.
        try {
            UiObject button = uiDevice.findObject(new UiSelector().text("YES"));
            if (button.exists() && button.isEnabled()) {
                button.click();
            }
        } catch (Exception e) {
            System.out.println("Already enabled");
        }
    }

    public static void sendMessage(String message) {
        ViewInteraction composeText = onView(
                allOf(withId(R.id.embedded_text_editor), withContentDescription("Message composition"),
                        isDisplayed()));
        composeText.perform(replaceText(message));

        ViewInteraction composeTextMessage = onView(
                allOf(withId(R.id.embedded_text_editor), withText(message), withContentDescription("Message composition"),
                        childAtPosition(
                                allOf(withId(R.id.compose_bubble),
                                        childAtPosition(
                                                withClassName(is("android.widget.FrameLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        composeTextMessage.perform(closeSoftKeyboard());

        // Initialize UiDevice instance
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        try {
            UiObject button = uiDevice.findObject(new UiSelector().resourceId("org.thoughtcrime.securesms:id/send_button"));
            button.waitForExists(3000);
            if (button.isEnabled()) {
                button.click();
            }
        } catch (Exception e) {
            System.out.println("Already enabled");
        }
    }


    public static void openQuickAttachmentDrawer(){
        // Initialize UiDevice instance
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        try {
            UiObject button = uiDevice.findObject(new UiSelector().resourceId("org.thoughtcrime.securesms:id/quick_camera_toggle"));
            button.waitForExists(3000);
            if (button.isEnabled()) {
                button.click();
            }
        } catch (Exception e) {
            System.out.println("Already enabled");
        }
    }

    public static void openSettingsDropDown() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
    }

    public static void openConversationSettings() {
        openSettingsDropDown();

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.title), withText("Conversation settings"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());
    }

    public static void openViewPinnedMessages() {
        openSettingsDropDown();

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.title), withText("View pinned messages"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());
    }

    public static void changeChatName(String name) {
        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list),
                        childAtPosition(
                                withId(android.R.id.list_container),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction appCompatEditText = onView(
                allOf(withId(android.R.id.edit),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                1)));
        appCompatEditText.perform(scrollTo(), replaceText(name), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton.perform(scrollTo(), click());
    }

    public static void pinLastMessage() {
        longPressMessageAt(0);

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.menu_context_pin_message), withContentDescription("Pin message"),
                        isDisplayed()));
        actionMenuItemView.perform(click());
    }

    public static void unpinLastMessage() {
        longPressMessageAt(0);

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.menu_context_unpin_message), withContentDescription("Unpin message"),
                        isDisplayed()));
        actionMenuItemView.perform(click());
    }

    public static void markAsUnread() {
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.menu_context_mark_as_unread),
                        isDisplayed()));
        actionMenuItemView.perform(click());
    }

    public static void longPressMessageAt(int messageIndex) {
        ViewInteraction recyclerView = onView(
                allOf(withId(android.R.id.list)));
        recyclerView.perform(actionOnItemAtPosition(messageIndex, longClick()));
    }

    public static void doubleClickViewWithId(int id){
        ViewInteraction view = onView(withId(id));
        view.perform(doubleClick());
    }

    public static void clickOnViewWithId(int id){
        ViewInteraction view = onView(withId(id));
        view.perform(click());

    }

    public static void pressMessageAt(int messageIndex) {
        ViewInteraction recyclerView = onView(
                allOf(withId(android.R.id.list)));
        recyclerView.perform(actionOnItemAtPosition(messageIndex, click()));
    }

    public static void clickOnFirstEmoji(){
        ViewInteraction emojiView = onView(allOf(withId(R.id.emoji), hasChildCount(45),
                childAtPosition(withId(-1),0),
                isDisplayed()));

        emojiView.perform(click());
    }

    public static void clickOnEmojiTab(){
        ViewInteraction emojiPeopleTabButton = onView(allOf(withId(R.id.tabs), isDisplayed()));

        emojiPeopleTabButton.perform(click());
    }

    public static void searchInConversation(String searchTerm){
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.menu_conversation_search), withContentDescription("Search Conversation"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.action_bar),
                                        1),
                                0),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction searchAutoComplete = onView(
                allOf(withId(R.id.search_src_text),
                        childAtPosition(
                                allOf(withId(R.id.search_plate),
                                        childAtPosition(
                                                withId(R.id.search_edit_frame),
                                                1)),
                                0),
                        isDisplayed()));
        searchAutoComplete.perform(replaceText(searchTerm), closeSoftKeyboard());
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

    public static void switchToMessagingSMS() {
        onView(withId(R.id.button_toggle)).perform(longClick());
        onView(withText("Insecure SMS")).perform(click());
    }

    public static void switchToMessagingSignal() {
        onView(withId(R.id.button_toggle)).perform(longClick());
        onView(withText("Signal")).perform(click());
    }
}
