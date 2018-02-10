package org.thoughtcrime.securesms.util;

import android.view.MotionEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.ConversationActivity;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MotionEvent.class, ConversationActivity.class, SwipeGestureDetector.class})
public class SwipeGesturedetectorUnitTest {

    private ConversationActivity mockConversationActivity;
    private SwipeGestureDetector testSwiper;
    private MotionEvent motionEvent1;
    private MotionEvent motionEvent2;

    @Before
    public void setUp() {

        mockConversationActivity = PowerMockito.mock(ConversationActivity.class);
        testSwiper = new SwipeGestureDetector(mockConversationActivity);
        motionEvent1 = PowerMockito.mock(MotionEvent.class);
        motionEvent2 = PowerMockito.mock(MotionEvent.class);
    }

    @Test
    public void testOnFling() {

        PowerMockito.mockStatic(MotionEvent.class);
        PowerMockito.mockStatic(Math.class);
        when(MotionEvent.obtain(200, 300, 0, 100.0f, 100.0f, 0)).
                thenReturn(motionEvent1);
        when(MotionEvent.obtain(400, 500, 1, 300.0f, 100.0f, 0)).
                thenReturn(motionEvent2);

        when(motionEvent1.getX()).thenReturn(100.0f);
        when(motionEvent2.getX()).thenReturn(300.0f);
        when(Math.abs(200.0f)).thenReturn(200.0f);


        motionEvent1 = MotionEvent.obtain(200, 300, 0, 100.0f, 100.0f, 0);
        motionEvent2 = MotionEvent.obtain(400, 500, 1, 300.0f, 100.0f, 0);

        testSwiper.onFling(motionEvent1, motionEvent2, 200.0f, 0.0f);
        verify(mockConversationActivity).onRightSwipe();
    }
}
