package org.thoughtcrime.securesms;

import android.util.Log;

import org.junit.*;
import org.mockito.*;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.thoughtcrime.securesms.giph.util.InfiniteScrollListener.TAG;

/**
 * Created by Claudia on 2018-02-08.
 */

public class ConversationActivityUnitTest {

    @Test
    public void testOnRightSwipe(){
        ConversationActivity conversationActivity = mock(ConversationActivity.class);
        ArrayList<Method> methodsArray = new ArrayList<Method>(Arrays.asList(ConversationActivity.class.getMethods()));

        Method onrightswipe = null;
        Method returntoconversationlist = null;

        for(Method method : methodsArray){
            if (method.getName().equals("onRightSwipe")){
                onrightswipe = method;
                onrightswipe.setAccessible(true);
            }
            if(method.getName().equals("handleReturnToConversationList")){
                returntoconversationlist = method;
                returntoconversationlist.setAccessible(true);
            }
        }


        try {
            onrightswipe.invoke(conversationActivity,null);
        }catch(Exception e){
            Log.d(TAG, "failed");
        }

        //verify(conversationActivity).returntoconversationlist;
    }
}
