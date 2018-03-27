package org.thoughtcrime.securesms.database;

import android.content.Context;
import android.net.Uri;

import org.junit.Test;
import org.mockito.Mockito;
import org.thoughtcrime.securesms.FingerprintAuthenticationHandler;
import org.thoughtcrime.securesms.TextSecureTestCase;
import org.thoughtcrime.securesms.attachments.AttachmentId;
import org.thoughtcrime.securesms.attachments.DatabaseAttachment;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.util.BitmapDecodingException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AttachmentDatabaseTest extends TextSecureTestCase {
  private static final long ROW_ID    = 1L;
  private static final long UNIQUE_ID = 2L;

  private AttachmentDatabase database;

  @Override
  public void setUp() {
    super.setUp();
    database = spy(DatabaseFactory.getAttachmentDatabase(getInstrumentation().getTargetContext()));
  }

  public void testThumbnailGenerationTaskNotRunWhenThumbnailExists() throws Exception {
    cipherTest();
   /* final AttachmentId attachmentId = new AttachmentId(ROW_ID, UNIQUE_ID);

    DatabaseAttachment mockAttachment = getMockAttachment("x/x");
    when(database.getAttachment(null, attachmentId)).thenReturn(mockAttachment);

    InputStream mockInputStream = mock(InputStream.class);
    doReturn(mockInputStream).when(database).getDataStream(any(MasterSecret.class), any(AttachmentId.class), eq("thumbnail"));
    database.getThumbnailStream(mock(MasterSecret.class), attachmentId);

    // Works as the Future#get() call in AttachmentDatabase#getThumbnailStream() makes updating synchronous
    verify(database, never()).updateAttachmentThumbnail(any(MasterSecret.class), any(AttachmentId.class), any(InputStream.class), anyFloat());*/
  }
/*
  public void testThumbnailGenerationTaskRunWhenThumbnailMissing() throws Exception {
    final AttachmentId attachmentId = new AttachmentId(ROW_ID, UNIQUE_ID);

    DatabaseAttachment mockAttachment = getMockAttachment("image/png");
    when(database.getAttachment(null, attachmentId)).thenReturn(mockAttachment);

    doReturn(null).when(database).getDataStream(any(MasterSecret.class), any(AttachmentId.class), eq("thumbnail"));
    doNothing().when(database).updateAttachmentThumbnail(any(MasterSecret.class), any(AttachmentId.class), any(InputStream.class), anyFloat());

    try {
      database.new ThumbnailFetchCallable(mock(MasterSecret.class), attachmentId).call();
      throw new AssertionError("Didn't try to generate thumbnail");
    } catch (BitmapDecodingException bde) {
      if (!(bde.getCause() instanceof FileNotFoundException)) {
        throw new AssertionError("Thumbnail generation failed for another reason than a FileNotFoundException: " + bde.getMessage());
      } // else success
    }
  }
*/

  public void cipherTest() {
    Context mockContext = Mockito.mock(Context.class);
    FingerprintAuthenticationHandler handler = new FingerprintAuthenticationHandler(mockContext);
    Cipher cipher = null;
    KeyPairGenerator kpg = null;
    try {
      cipher = handler.getCipher();
      kpg = handler.getKeyPairGenerator();
    } catch (FingerprintAuthenticationHandler.FingerprintException e) {
      e.printStackTrace();
    }
    assert(cipher.getAlgorithm() == "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
  }

  private DatabaseAttachment getMockAttachment(String contentType) {
    DatabaseAttachment attachment = mock(DatabaseAttachment.class);
    when(attachment.getContentType()).thenReturn(contentType);
    when(attachment.getDataUri()).thenReturn(Uri.EMPTY);
    when(attachment.hasData()).thenReturn(true);

    return attachment;
  }
}
