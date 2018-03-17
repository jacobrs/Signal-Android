package org.thoughtcrime.securesms;


import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.DataInteraction;
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
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

import static android.support.test.InstrumentationRegistry.getContext;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FingerprintIconTest {

  @Rule
  public ActivityTestRule<ConversationListActivity> mActivityTestRule = new ActivityTestRule<>(ConversationListActivity.class);

  @Test
  public void fingerprintIconTest() {

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //Go to settings
    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction appCompatTextView = onView(
        allOf(withId(R.id.title), withText("Settings"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                    0),
                0),
            isDisplayed()));
    appCompatTextView.perform(click());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //Select Privacy Settings
    ViewInteraction recyclerView = onView(
        allOf(withId(R.id.list),
            childAtPosition(
                withId(android.R.id.list_container),
                0)));
    recyclerView.perform(actionOnItemAtPosition(3, click()));

    //Select Enable Passphrase
    ViewInteraction recyclerView2 = onView(
        allOf(withId(R.id.list),
            childAtPosition(
                withId(android.R.id.list_container),
                0)));
    recyclerView2.perform(actionOnItemAtPosition(1, click()));

    //Set Passphrase to 123
    ViewInteraction appCompatEditText6 = onView(
        allOf(withId(R.id.new_passphrase),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.support.design.widget.TextInputLayout")),
                    0),
                0),
            isDisplayed()));
    appCompatEditText6.perform(replaceText("123"), closeSoftKeyboard());

    ViewInteraction appCompatEditText7 = onView(
        allOf(withId(R.id.repeat_passphrase),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.support.design.widget.TextInputLayout")),
                    0),
                0),
            isDisplayed()));
    appCompatEditText7.perform(replaceText("123"), closeSoftKeyboard());

    ViewInteraction appCompatButton3 = onView(
        allOf(withId(R.id.ok_button), withText("OK"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.widget.LinearLayout")),
                    3),
                1)));
    appCompatButton3.perform(scrollTo(), click());

    //Enable Fingerprint Authentication
    ViewInteraction recyclerView3 = onView(
        allOf(withId(R.id.list),
            childAtPosition(
                withId(android.R.id.list_container),
                0)));
    recyclerView3.perform(actionOnItemAtPosition(5, click()));

    //Head back to homepage
    ViewInteraction appCompatImageButton = onView(
        allOf(withContentDescription("Navigate up"),
            childAtPosition(
                allOf(withId(R.id.action_bar),
                    childAtPosition(
                        withId(R.id.action_bar_container),
                        0)),
                1),
            isDisplayed()));
    appCompatImageButton.perform(click());

    ViewInteraction appCompatImageButton2 = onView(
        allOf(withContentDescription("Navigate up"),
            childAtPosition(
                allOf(withId(R.id.action_bar),
                    childAtPosition(
                        withId(R.id.action_bar_container),
                        0)),
                1),
            isDisplayed()));
    appCompatImageButton2.perform(click());

    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

    //Lock Phone
    ViewInteraction appCompatTextView2 = onView(
        allOf(withId(R.id.title), withText("Lock"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                    0),
                0),
            isDisplayed()));
    appCompatTextView2.perform(click());

    //Assert that the fingerprint icon is displayed
    ViewInteraction imageView = onView(
        allOf(withId(R.id.fingerprint_icon),
            childAtPosition(
                allOf(withId(R.id.prompt_layout),
                    childAtPosition(
                        withId(R.id.scroll_parent),
                        0)),
                2),
            isDisplayed()));
    imageView.check(matches(isDisplayed()));

    ViewInteraction appCompatEditText8 = onView(
        allOf(withId(R.id.passphrase_edit),
            childAtPosition(
                childAtPosition(
                    withId(R.id.prompt_layout),
                    1),
                0)));
    appCompatEditText8.perform(scrollTo(), replaceText("123"), closeSoftKeyboard());

    ViewInteraction appCompatImageButton3 = onView(
        allOf(withId(R.id.ok_button), withContentDescription("Submit passphrase"),
            childAtPosition(
                childAtPosition(
                    withId(R.id.prompt_layout),
                    1),
                2)));
    appCompatImageButton3.perform(scrollTo(), click());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //Go back to settings
    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction appCompatTextView3 = onView(
        allOf(withId(R.id.title), withText("Settings"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                    0),
                0),
            isDisplayed()));
    appCompatTextView3.perform(click());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //Go to privacy options
    ViewInteraction recyclerView4 = onView(
        allOf(withId(R.id.list),
            childAtPosition(
                withId(android.R.id.list_container),
                0)));
    recyclerView4.perform(actionOnItemAtPosition(3, click()));

    //Disable fingerprint authentication
    ViewInteraction recyclerView5 = onView(
        allOf(withId(R.id.list),
            childAtPosition(
                withId(android.R.id.list_container),
                0)));
    recyclerView5.perform(actionOnItemAtPosition(5, click()));

    //Return to homescreen
    ViewInteraction appCompatImageButton4 = onView(
        allOf(withContentDescription("Navigate up"),
            childAtPosition(
                allOf(withId(R.id.action_bar),
                    childAtPosition(
                        withId(R.id.action_bar_container),
                        0)),
                1),
            isDisplayed()));
    appCompatImageButton4.perform(click());

    ViewInteraction appCompatImageButton5 = onView(
        allOf(withContentDescription("Navigate up"),
            childAtPosition(
                allOf(withId(R.id.action_bar),
                    childAtPosition(
                        withId(R.id.action_bar_container),
                        0)),
                1),
            isDisplayed()));
    appCompatImageButton5.perform(click());

    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

    //Lock phone
    ViewInteraction appCompatTextView4 = onView(
        allOf(withId(R.id.title), withText("Lock"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                    0),
                0),
            isDisplayed()));
    appCompatTextView4.perform(click());

    //Assert that fingerprint icon is no longer displayed indicating the feature is disabled.
    ViewInteraction imageView2 = onView(
        allOf(withId(R.id.fingerprint_icon),
            childAtPosition(
                allOf(withId(R.id.prompt_layout),
                    childAtPosition(
                        withId(R.id.scroll_parent),
                        0)),
                2),
            isDisplayed()));
    imageView2.check(doesNotExist());

    //The rest is for disabling passphrase for other tests

    ViewInteraction appCompatEditText9 = onView(
        allOf(withId(R.id.passphrase_edit),
            childAtPosition(
                childAtPosition(
                    withId(R.id.prompt_layout),
                    1),
                0)));
    appCompatEditText9.perform(scrollTo(), replaceText("123"), closeSoftKeyboard());

    ViewInteraction appCompatImageButton6 = onView(
        allOf(withId(R.id.ok_button), withContentDescription("Submit passphrase"),
            childAtPosition(
                childAtPosition(
                    withId(R.id.prompt_layout),
                    1),
                2)));
    appCompatImageButton6.perform(scrollTo(), click());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction appCompatTextView5 = onView(
        allOf(withId(R.id.title), withText("Settings"),
            childAtPosition(
                childAtPosition(
                    withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                    0),
                0),
            isDisplayed()));
    appCompatTextView5.perform(click());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //Privacy
    ViewInteraction recyclerView6 = onView(
        allOf(withId(R.id.list),
            childAtPosition(
                withId(android.R.id.list_container),
                0)));
    recyclerView6.perform(actionOnItemAtPosition(3, click()));


    ViewInteraction recyclerView7 = onView(
        allOf(withId(R.id.list),
            childAtPosition(
                withId(android.R.id.list_container),
                0)));
    recyclerView7.perform(actionOnItemAtPosition(1, click()));

    ViewInteraction appCompatButton4 = onView(
        allOf(withId(android.R.id.button1), withText("Disable"),
            childAtPosition(
                childAtPosition(
                    withId(R.id.buttonPanel),
                    0),
                3)));
    appCompatButton4.perform(scrollTo(), click());

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
