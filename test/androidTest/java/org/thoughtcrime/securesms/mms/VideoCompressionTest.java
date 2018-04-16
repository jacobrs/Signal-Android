package org.thoughtcrime.securesms.mms;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.iceteck.silicompressorr.SiliCompressor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.util.MediaUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class VideoCompressionTest {
    @Test(expected= IOException.class)
    public void compressVideoTest() throws Exception{
        //arrange
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        InputStream testInput = testContext.getAssets().open("CompressionTestVideo.mp4") ;
        String outputfolder =  testContext.getCacheDir().toPath().toString();
        byte[] buffer = new byte[testInput.available()];
        testInput.read(buffer);

        File targetFile = new File(outputfolder+"/temp.mp4");
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);

        //act
        String output = SiliCompressor.with(testContext).compressVideo(targetFile.getPath(), outputfolder);

        //assert
        assert( new File(output).length() < targetFile.length());
    }

    @Test(expected =  UnsupportedOperationException.class)
    public void allowedAttachmentTest() throws Exception{
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        MediaConstraints mc = new MmsMediaConstraints(0);
        Attachment attachment = mock(Attachment.class);
        when(attachment.getContentType()).thenReturn(MediaUtil.IMAGE_JPEG);
        mc.getCompressedVideo(testContext, null,attachment );
    }

}
