package org.thoughtcrime.securesms.mms;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import com.iceteck.silicompressorr.SiliCompressor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.SecureRandom;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class MediaConstraintsUnitTest {

    private static final String TAG = MediaConstraintsUnitTest.class.getSimpleName();

    @Test
    public void testGetCompressedVideo(){

        // Mocking classes
        MediaConstraints mockMediaConstraint = mock(MediaConstraints.class);
        MasterSecret mockMasterSecret = mock(MasterSecret.class);
        Attachment mockAttachment = mock(Attachment.class);
        Context mockContext = PowerMockito.mock(Context.class);
        PowerMockito.mockStatic(Uri.class);
        PowerMockito.mockStatic(Parcelable.class);

        // Variables
        String cacheDir = "/data/user/0/org.thoughtcrime.securesms/cache";
        File file = new File(cacheDir);
        String testVideoUri = "content://org.thoughtcrime.securesms/part/1520130802982/13";

        // Stubbing
        when(mockContext.getCacheDir()).thenReturn(file);
        when(mockAttachment.getDataUri()).thenReturn(Uri.parse(testVideoUri));
        when(mockAttachment.getContentType()).thenReturn("video");

        // Test
        try {
           mockMediaConstraint.getCompressedVideo(mockContext, mockMasterSecret, mockAttachment);
        }catch (IOException e){
           Log.e(TAG, "IOException error.");
        }
    }

    @Test
    public void testCompressVideo(){

        // Mocking classes
        MasterSecret mockMasterSecret = mock(MasterSecret.class);
        PowerMockito.mockStatic(PartAuthority.class);
        Attachment mockAttachment = mock(Attachment.class);
        Context mockContext = mock(Context.class);
        PowerMockito.mockStatic(Util.class);
        PowerMockito.mockStatic(Uri.class);
        PowerMockito.mockStatic(Parcelable.class);
        SecureRandom mockSecure = PowerMockito.mock(SecureRandom.class);

        // Variables
        String cacheDir = "/data/user/0/org.thoughtcrime.securesms/cache";
        File file = new File(cacheDir);
        String testVideoUri = "content://org.thoughtcrime.securesms/part/1520130802982/13";

        // Stubbing
        when(mockContext.getCacheDir()).thenReturn(file);
        when(mockAttachment.getDataUri()).thenReturn(Uri.parse(testVideoUri));
        when(mockAttachment.getContentType()).thenReturn("video");
        when(mockSecure.nextLong()).thenReturn(123456789L);

        // Test
        try {
            byte [] data = Util.readFully(PartAuthority.getAttachmentStream(mockContext, mockMasterSecret, mockAttachment.getDataUri()));
            String fileName= String.valueOf(Math.abs(Util.getSecureRandom().nextLong()));
            String path = mockContext.getCacheDir().toString()+"/"+fileName+mockAttachment.getContentType().replace('/','.');
            FileOutputStream fileOut = new FileOutputStream(path);
            fileOut.write(data);

            // output is actually cacheDir + "/VIDEO_yyyymmdd_hhmmss.mp4"
            String output = SiliCompressor.with(mockContext).compressVideo(path, mockContext.getCacheDir().toString());
            Log.d(TAG, "output is: " + output);

            assertThat(output, containsString(cacheDir));

        }catch (IOException e){
            Log.e(TAG, "IOException error.", e);
        }catch (URISyntaxException e){
            Log.e(TAG, "URISyntaxException error.", e);
        }
    }
}
