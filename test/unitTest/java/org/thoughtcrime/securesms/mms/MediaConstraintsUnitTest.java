package org.thoughtcrime.securesms.mms;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import com.iceteck.silicompressorr.SiliCompressor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.util.Util;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class MediaConstraintsUnitTest {

    private static final String TAG = MediaConstraintsUnitTest.class.getSimpleName();
    @PrepareForTest({Util.class , PartAuthority.class})
    @Test
    public void testGetCompressedVideo() throws IOException{

        // Mocking classes
        MediaConstraints mockMediaConstraint = mock(MediaConstraints.class);
        MasterSecret mockMasterSecret = mock(MasterSecret.class);
        Attachment mockAttachment = mock(Attachment.class);
        Context mockContext = PowerMockito.mock(Context.class);
        PowerMockito.mockStatic(Parcelable.class);

        // Variables
        String cacheDir = "./";
        File file = new File(cacheDir);
        //need to find a way to actually load a test file
        Path path = Paths.get(this.getClass().getClassLoader().getResource("sampleTestVideo1080p22MB.mp4").getPath());
        byte[] data = Files.readAllBytes(path);
        // Stubbing
        PowerMockito.when(Util.readFully(null)).thenReturn(data);
        PowerMockito.when(PartAuthority.getAttachmentStream(mockContext,mockMasterSecret, null)).thenReturn(null);
        when(mockContext.getCacheDir()).thenReturn(file);
        when(mockAttachment.getDataUri()).thenReturn(null);
        when(mockAttachment.getContentType()).thenReturn("video");

        // Test
        try{
           assert(data.length>0);
           Log.d(TAG, "output is: " + data.length);
           mockMediaConstraint.getCompressedVideo(mockContext, mockMasterSecret, mockAttachment);
        }catch (IOException e){
           Log.e(TAG, "IOException error.");
        }
    }

    @Test
    public void testCompressVideo(){

        // Mocking classes
        Context mockContext = mock(Context.class);

        // Test
        try {
            //need to find a way to actually load the test file
            // output is actually cacheDir + "/VIDEO_yyyymmdd_hhmmss.mp4"
            String output = SiliCompressor.with(mockContext).compressVideo(this.getClass().getClassLoader().getResource("sampleTestVideo1080p22MB.mp4").getPath(), "./org/thoughtcrime/securesms/mms/");
            Log.d(TAG, "output is: " + output);

            assertThat(output, containsString("org/thoughtcrime/securesms/mms/"));

        }catch (URISyntaxException e){
            Log.e(TAG, "URISyntaxException error.", e);
        }
    }
}
