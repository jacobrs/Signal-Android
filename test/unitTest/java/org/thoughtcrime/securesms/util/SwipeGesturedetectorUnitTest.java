package org.thoughtcrime.securesms.util;

import android.os.SystemClock;
import android.view.MotionEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.common.internal.PowerMockJUnitRunnerDelegate;
import org.thoughtcrime.securesms.ConversationActivity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by Claudia on 2018-02-09.
 */

@RunWith(PowerMockRunner.class)
public class SwipeGesturedetectorUnitTest {

    @Test
    public void testOnFling(){
        ConversationActivity mockConversationActivity = mock(ConversationActivity.class);

        //2 motion events, making sure the x coordinate difference is greater than the threshold for swiping
        MotionEvent motionEvent1 = MotionEvent.obtain(200, 300, MotionEvent.ACTION_DOWN, 100.0f, 100.0f, 0);
        MotionEvent motionEvent2 = MotionEvent.obtain(400, 500, MotionEvent.ACTION_UP, 300.0f, 100.0f, 0);

        SwipeGestureDetector testSwiper = new SwipeGestureDetector(mockConversationActivity);

        testSwiper.onFling(motionEvent1, motionEvent2, 200.0f, 0.0f);

        verify(mockConversationActivity).onRightSwipe();
    }
}
