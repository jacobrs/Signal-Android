package org.thoughtcrime.securesms.espressoTests;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.thoughtcrime.securesms.ConversationActivity;
import org.thoughtcrime.securesms.ConversationListActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.ConversationActions;
import org.thoughtcrime.securesms.util.Expectations;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DoubleTapCameraTest {

    @Rule
    public ActivityTestRule<ConversationListActivity> mActivityTestRule = new ActivityTestRule<>(ConversationListActivity.class);

    @Test
    public void doubleTapCameraTest(){
        // Open a new conversation.
        ConversationActions.createNewConversation("5149714708");
        ConversationActions.enableSignalForSMS();

        //Open the quick attachment camera
        ConversationActions.openQuickAttachmentDrawer();
        Expectations.checkIsDisplayed(R.id.back_facing_camera_icon); //assuming the camera starts as back facing
        Expectations.checkIsNotDisplayed(R.id.front_facing_camera_icon);

        //put the camera into full screen
        //this needs to be done or else the double click won't work (error: 90% of the view needs to be visible
        // to the user) and the test will fail
        Expectations.checkIsDisplayed(R.id.fullscreen_button);
        ConversationActions.clickOnViewWithId(R.id.fullscreen_button);

        //flip camera and check that it flipped
        ConversationActions.doubleClickViewWithId(R.id.quick_camera);
        Expectations.checkIsNotDisplayed(R.id.back_facing_camera_icon);
        Expectations.checkIsDisplayed(R.id.front_facing_camera_icon);

        //put the camera back to back facing so we can rerun the test
        ConversationActions.doubleClickViewWithId(R.id.quick_camera);
    }
}
