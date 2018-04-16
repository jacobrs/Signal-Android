package org.thoughtcrime.securesms;


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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class VideoCompressionOptionMenuTest {

    @Rule
    public ActivityTestRule<ConversationListActivity> mActivityTestRule = new ActivityTestRule<>(ConversationListActivity.class);

    @Test
    public void videoCompressionOptionMenuTest() {
        //open action bar menu
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        //open settings tab
        openSettingsMenu();
        //open chat and media settings tab
        openChatAndMediaMenu();
        //asserts that the default value of if compression if enabled is no
        assertDefaultVideoCompressionStatus();
        //asserts that the default value of compression level is Medium
        assertDefaultVideoCompressionLevel();

    }
    private static void openSettingsMenu() {
        onView(allOf(withId(R.id.title),
                withText("Settings"),
                childAtPosition(
                        childAtPosition(
                                withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                1),
                        0),
                isDisplayed())
        ).perform(click());
    }
    private static void openChatAndMediaMenu() {
        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list),
                        childAtPosition(
                                withId(android.R.id.list_container),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(5, click()));
    }

    private static void assertDefaultVideoCompressionStatus(){
        ViewInteraction textView2 = onView(
                allOf(withId(R.id.right_summary), withText("No"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.widget_frame),
                                        0),
                                0),
                        isDisplayed()));
        textView2.check(matches(withText("No")));
    }

    private static void assertDefaultVideoCompressionLevel(){
        ViewInteraction textView4 = onView(
                allOf(withId(R.id.right_summary), withText("Medium"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.widget_frame),
                                        0),
                                0),
                        isDisplayed()));
        textView4.check(matches(withText("Medium")));
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
