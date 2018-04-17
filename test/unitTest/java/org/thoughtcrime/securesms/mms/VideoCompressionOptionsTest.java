package org.thoughtcrime.securesms.mms;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.BaseUnitTest;
import org.thoughtcrime.securesms.util.MediaUtil;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest(TextSecurePreferences.class)
public class VideoCompressionOptionsTest extends BaseUnitTest {

    @Test
    public void testDisableCompression(){
        mockStatic(TextSecurePreferences.class);
        when(TextSecurePreferences.getVideoCompressionStatus(context)).thenReturn("no");
        assertEquals (MediaUtil.isCompressionEnabled(context),false);
        verifyStatic(times(1));
    }

    @Test
    public void testEnableCompression(){
        mockStatic(TextSecurePreferences.class);
        when(TextSecurePreferences.getVideoCompressionStatus(context)).thenReturn("yes");
        assertEquals (MediaUtil.isCompressionEnabled(context),true);
        verifyStatic(times(1));
    }

    @Test
    public void testOffWifiCompression(){
        //mocking
        mockStatic(TextSecurePreferences.class);
        NetworkInfo networkInfo = PowerMockito.mock(NetworkInfo.class);
        ConnectivityManager connectivityManager = PowerMockito.mock(ConnectivityManager.class);

        //stubbing
        when(networkInfo.isConnected()).thenReturn(true);
        when(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).thenReturn(networkInfo);
        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);
        when(TextSecurePreferences.getVideoCompressionStatus(context)).thenReturn("only off wifi");

        //act/assert
        assertEquals (MediaUtil.isCompressionEnabled(context),false);
        verifyStatic(times(1));
    }

    @Test
    public void testOnWifiCompression(){
        //mocking
        mockStatic(TextSecurePreferences.class);
        NetworkInfo networkInfo = PowerMockito.mock(NetworkInfo.class);
        ConnectivityManager connectivityManager = PowerMockito.mock(ConnectivityManager.class);

        //stubbing
        when(networkInfo.isConnected()).thenReturn(false);
        when(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).thenReturn(networkInfo);
        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);
        when(TextSecurePreferences.getVideoCompressionStatus(context)).thenReturn("only off wifi");

        //act/assert
        assertEquals (MediaUtil.isCompressionEnabled(context),true);
        verifyStatic(times(1));
    }
}
