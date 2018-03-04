package org.thoughtcrime.securesms.jobs;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.AttachmentDatabase;
import org.thoughtcrime.securesms.mms.MediaConstraints;
import org.thoughtcrime.securesms.mms.MmsException;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class SendJobUnitTest {

    private static final String TAG = SendJobUnitTest.class.getSimpleName();

    @Test
    public void testScaleAttachment(){

        // Mocking classes
        AttachmentDatabase mockAttachmentDatabase = PowerMockito.mock(AttachmentDatabase.class);
        MediaConstraints mockMediaConstraints = mock(MediaConstraints.class);
        MasterSecret mockMasterSecret = mock(MasterSecret.class);
        Attachment mockAttachment = mock(Attachment.class);
        Context mockContext = mock(Context.class);
        Uri mockUri = mock(Uri.class);

        // Variables
        String cacheDir = "/data/user/0/org.thoughtcrime.securesms/cache";
        String testVideoUri = "content://org.thoughtcrime.securesms/part/1520130802982/13";

        // Stubbing
        when(mockContext.getCacheDir().toString()).thenReturn(cacheDir);
        when(mockAttachment.getDataUri()).thenReturn(mockUri.parse(testVideoUri));
        when(mockAttachment.getContentType()).thenReturn("video");

        try {
            // Need to get size of video originally.
            Attachment result = mockAttachmentDatabase.updateAttachmentData(mockMasterSecret, mockAttachment, mockMediaConstraints.getCompressedVideo(mockContext, mockMasterSecret, mockAttachment));
            // Then need to assert that the resulting compressed video size is smaller.
        }catch (IOException e){
            Log.e(TAG, "IOException error.");
        }catch (MmsException e){
            Log.e(TAG, "MmsException error.");
        }


    }

}
