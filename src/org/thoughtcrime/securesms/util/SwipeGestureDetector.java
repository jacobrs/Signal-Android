package org.thoughtcrime.securesms.util;

import android.view.GestureDetector;
import android.view.MotionEvent;
import org.thoughtcrime.securesms.ConversationActivity;

public class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {

    //Min swiping distance & velocity so back swipe isn't triggered accidentally
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;

    ConversationActivity conversationActivity;

    public SwipeGestureDetector(ConversationActivity conversationActivity) {
        this.conversationActivity = conversationActivity;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            // Difference between the x coordinate of the first even (downpush)
            // and the second event (let go)
            float diff = e1.getX() - e2.getX();
            float absoluteVelocity = Math.abs(velocityX);
            // Right swipe
            if (-diff > SWIPE_MIN_DISTANCE && absoluteVelocity > SWIPE_THRESHOLD_VELOCITY) {
                conversationActivity.onRightSwipe();
            }
        } catch (Exception e) {
        }
        return false;
    }
}
