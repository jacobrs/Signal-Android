package org.thoughtcrime.securesms.util;

import android.support.test.espresso.ViewInteraction;

import org.thoughtcrime.securesms.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

public class GenericActions {
    public static void navigateBack() {
        navigateBack("preferences");
    }

    public static void navigateBack(String screen) {
        ViewInteraction appCompatImageButton;

        switch (screen) {
            case "conversation":
                appCompatImageButton = onView(
                        allOf(withId(R.id.up_button),
                                isDisplayed()));
                break;
            default:
            case "preferences":
                appCompatImageButton = onView(
                        allOf(withContentDescription("Navigate up"),
                                isDisplayed()));
                break;
        }
        appCompatImageButton.perform(click());
    }
}
