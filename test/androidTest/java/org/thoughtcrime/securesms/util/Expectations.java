package org.thoughtcrime.securesms.util;

import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withResourceName;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class Expectations {
    public static void checkIsDisplayed(int id) {
        onView(withId(id)).check(matches(isDisplayed()));
    }

    public static void checkIsDisplayedWithRearCameraDrawable(int id){
        onView(allOf(withId(id), withResourceName("R.drawable.quick_camera_rear"))).check(matches(isDisplayed()));
    }

    public static void checkIsDisplayed(int id, String message) {
        onView(allOf(withId(id), withText(message))).check(matches(isDisplayed()));
    }

    public static void checkIsDisplayed(Matcher<View> customMatcher) {
        onView(customMatcher).check(matches(isDisplayed()));
    }

    public static void checkIsNotDisplayed(int id, String message) {
        onView(allOf(withId(id), withText(message))).check(matches(not(isDisplayed())));
    }

    public static void checkIsNotDisplayed(int id) {
        onView(withId(id)).check(matches(not(isDisplayed())));
    }

    public static void checkDoesNotExist(int id) {
        onView(withId(id)).check(doesNotExist());
    }

    public static void checkIsShowingDescendantWithId(Matcher<View> customMatcher, int descendantId) {
        onView(allOf(withId(descendantId), isDescendantOfA(customMatcher))).check(matches(isDisplayed()));
    }

    public static void checkIsNotShowingDescendantWithId(Matcher<View> customMatcher, int descendantId) {
        onView(allOf(withId(descendantId), isDescendantOfA(customMatcher))).check(matches(not(isDisplayed())));
    }
}
