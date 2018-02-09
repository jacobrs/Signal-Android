package org.thoughtcrime.securesms.util;

import android.view.MotionEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.ConversationActivity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by Claudia on 2018-02-09.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({MotionEvent.class, ConversationActivity.class, SwipeGestureDetector.class})
public class SwipeGesturedetectorUnitTest {

    private ConversationActivity mockConversationActivity;
    private SwipeGestureDetector testSwiper;
    private MotionEvent motionEvent1;
    private MotionEvent motionEvent2;

    @Before
    public void setUp() throws Exception {
        mockConversationActivity = PowerMockito.mock(ConversationActivity.class);
        testSwiper = new SwipeGestureDetector(mockConversationActivity);
        motionEvent1 = PowerMockito.mock(MotionEvent.class);
        motionEvent2 = PowerMockito.mock(MotionEvent.class);
    }

    @Test
    public void testOnFling() throws Exception {

        motionEvent1 = MotionEvent.obtain(200, 300, 0, 100.0f, 100.0f, 0);
        motionEvent2 = MotionEvent.obtain(400, 500, 1, 300.0f, 100.0f, 0);

        testSwiper.onFling(motionEvent1, motionEvent2, 200.0f, 0.0f);
        verify(mockConversationActivity).onRightSwipe();
    }
}
