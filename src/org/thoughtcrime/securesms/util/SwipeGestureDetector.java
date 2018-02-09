package org.thoughtcrime.securesms.util;

/**
 * Created by Claudia on 2018-02-09.
 */

import android.view.GestureDetector;
import android.view.MotionEvent;
import org.thoughtcrime.securesms.ConversationActivity;

public class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {

    //Min swiping distance & velocity so back swipe isn't triggered accidentally
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    ConversationActivity conversationActivity;

    public SwipeGestureDetector(ConversationActivity conversationActivity) {
        this.conversationActivity = conversationActivity;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            //Difference between the x coordinate of the first even (downpush)
            // and the second event (let go)
            float diff = e1.getX() - e2.getX();

            // Right swipe
            if (-diff > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                conversationActivity.onRightSwipe();
            }
        } catch (Exception e) {
           // Log.w(TAG, e);
        }
        return false;
    }
}
