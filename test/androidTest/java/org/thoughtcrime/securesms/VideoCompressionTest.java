package org.thoughtcrime.securesms;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.iceteck.silicompressorr.SiliCompressor;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import static org.mockito.Mockito.mock;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class VideoCompressionTest {
    @Test
    public void testCompressVideo() throws Exception{
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        String[] assests = testContext.getAssets().list("");
        InputStream testInput = testContext.getAssets().open("CompressionTestVideo.mp4") ;
        String outputfolder =  testContext.getCacheDir().toPath().toString();
        byte[] buffer = new byte[testInput.available()];
        testInput.read(buffer);

        File targetFile = new File(testContext.getFilesDir().getPath()+"/temp.mp4");//new File(outputfolder +"temp.mp4");
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);

        String output = SiliCompressor.with(testContext).compressVideo(targetFile.getPath(), outputfolder);
        assert( output.equals(""));
    }
    public static byte[] readFully(InputStream input) throws IOException
    {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }

}
