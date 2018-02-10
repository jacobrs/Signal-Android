package org.thoughtcrime.securesms;

import android.util.Log;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.thoughtcrime.securesms.giph.util.InfiniteScrollListener.TAG;

/**
 * Created by Claudia on 2018-02-08.
 */

@RunWith(PowerMockRunner.class)
public class ConversationActivityUnitTest {

    @Test
    public void testOnRightSwipe() throws Exception{
        ConversationActivity conversationActivity = new ConversationActivity();
        
        conversationActivity.onRightSwipe();

        verifyPrivate(conversationActivity, times(1)).
                invoke("handleReturnToConversationList");
    }
}
