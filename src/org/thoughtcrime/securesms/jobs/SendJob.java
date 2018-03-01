package org.thoughtcrime.securesms.jobs;

import android.content.Context;
import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.BuildConfig;
import org.thoughtcrime.securesms.TextSecureExpiredException;
import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.AttachmentDatabase;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.mms.MediaConstraints;
import org.thoughtcrime.securesms.mms.MediaStream;
import org.thoughtcrime.securesms.mms.MmsException;
import org.thoughtcrime.securesms.transport.UndeliverableMessageException;
import org.thoughtcrime.securesms.util.MediaUtil;
import org.thoughtcrime.securesms.util.Util;
import org.whispersystems.jobqueue.JobParameters;
import com.iceteck.silicompressorr.SiliCompressor;
import org.whispersystems.libsignal.logging.Log;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

public abstract class SendJob extends MasterSecretJob {

  private final static String TAG = SendJob.class.getSimpleName();

  public SendJob(Context context, JobParameters parameters) {
    super(context, parameters);
  }

  @Override
  public final void onRun(MasterSecret masterSecret) throws Exception {
    if (Util.getDaysTillBuildExpiry() <= 0) {
      throw new TextSecureExpiredException(String.format("TextSecure expired (build %d, now %d)",
                                                         BuildConfig.BUILD_TIMESTAMP,
                                                         System.currentTimeMillis()));
    }

    onSend(masterSecret);
  }

  protected abstract void onSend(MasterSecret masterSecret) throws Exception;

  protected void markAttachmentsUploaded(long messageId, @NonNull List<Attachment> attachments) {
    AttachmentDatabase database = DatabaseFactory.getAttachmentDatabase(context);

    for (Attachment attachment : attachments) {
      database.markAttachmentUploaded(messageId, attachment);
    }
  }

  protected List<Attachment> scaleAttachments(@NonNull MasterSecret masterSecret,
                                              @NonNull MediaConstraints constraints,
                                              @NonNull List<Attachment> attachments)
      throws UndeliverableMessageException
  {
    AttachmentDatabase attachmentDatabase = DatabaseFactory.getAttachmentDatabase(context);
    List<Attachment>   results            = new LinkedList<>();

    for (Attachment attachment : attachments) {
      try {
        if(MediaUtil.isVideo(attachment)) {
          Log.d(TAG, "Is a video of size " + attachment.getSize());
          // The attachment location is null, which will cause an error when passed to compressor method.
          // If null, given a spaced value for now.
          String location = attachment.getLocation() == null ? " " : attachment.getLocation();
          String filePath = SiliCompressor.with(context).compressVideo(attachment.getDataUri(), location);
          Attachment resizedAttachment = attachmentDatabase.updateAttachmentLocation(attachment, filePath);
          Log.d(TAG, "Is a video of NEW size " + resizedAttachment.getSize());
          results.add(resizedAttachment);
        }
        if (constraints.isSatisfied(context, masterSecret, attachment)) {
          results.add(attachment);
        } else if (constraints.canResize(attachment)) {
          MediaStream resized = constraints.getResizedMedia(context, masterSecret, attachment);
          results.add(attachmentDatabase.updateAttachmentData(masterSecret, attachment, resized));
        } else {
          throw new UndeliverableMessageException("Size constraints could not be met!");
        }
      } catch (IOException | MmsException | URISyntaxException e ) {
        throw new UndeliverableMessageException(e);
      }
    }

    return results;
  }
}
