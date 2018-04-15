package org.thoughtcrime.securesms.mms;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.iceteck.silicompressorr.SiliCompressor;

import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.mms.DecryptableStreamUriLoader.DecryptableUri;
import org.thoughtcrime.securesms.util.BitmapDecodingException;
import org.thoughtcrime.securesms.util.BitmapUtil;
import org.thoughtcrime.securesms.util.MediaUtil;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public abstract class MediaConstraints {
  private static final String TAG = MediaConstraints.class.getSimpleName();

  private static final int LOW_VIDEO_COMPRESSION_HEIGHT  = 1080;
  private static final int LOW_VIDEO_COMPRESSION_WIDTH  = 1920;
  private static final int LOW_VIDEO_COMPRESSION_BITRATE = 750000;
  private static final int MEDIUM_VIDEO_COMPRESSION_HEIGHT  = 720;
  private static final int MEDIUM_VIDEO_COMPRESSION_WIDTH  = 1280;
  private static final int MEDIUM_VIDEO_COMPRESSION_BITRATE = 600000;
  private static final int HIGH_VIDEO_COMPRESSION_HEIGHT  = 480;
  private static final int HIGH_VIDEO_COMPRESSION_WIDTH  = 640;
  private static final int HIGH_VIDEO_COMPRESSION_BITRATE =  450000;

  public static MediaConstraints getPushMediaConstraints() {
    return new PushMediaConstraints();
  }

  public static MediaConstraints getMmsMediaConstraints(int subscriptionId) {
    return new MmsMediaConstraints(subscriptionId);
  }

  public abstract int getImageMaxWidth(Context context);
  public abstract int getImageMaxHeight(Context context);
  public abstract int getImageMaxSize(Context context);

  public abstract int getGifMaxSize(Context context);
  public abstract int getVideoMaxSize(Context context);
  public abstract int getAudioMaxSize(Context context);
  public abstract int getDocumentMaxSize(Context context);

  public boolean isSatisfied(@NonNull Context context, @NonNull MasterSecret masterSecret, @NonNull Attachment attachment) {
    try {
      return (MediaUtil.isGif(attachment)    && attachment.getSize() <= getGifMaxSize(context)   && isWithinBounds(context, masterSecret, attachment.getDataUri())) ||
             (MediaUtil.isImage(attachment)  && attachment.getSize() <= getImageMaxSize(context) && isWithinBounds(context, masterSecret, attachment.getDataUri())) ||
             (MediaUtil.isAudio(attachment)  && attachment.getSize() <= getAudioMaxSize(context)) ||
             (MediaUtil.isVideo(attachment)  && attachment.getSize() <= getVideoMaxSize(context)) ||
             (MediaUtil.isFile(attachment) && attachment.getSize() <= getDocumentMaxSize(context));
    } catch (IOException ioe) {
      Log.w(TAG, "Failed to determine if media's constraints are satisfied.", ioe);
      return false;
    }
  }

  private boolean isWithinBounds(Context context, MasterSecret masterSecret, Uri uri) throws IOException {
    try {
      InputStream is = PartAuthority.getAttachmentStream(context, masterSecret, uri);
      Pair<Integer, Integer> dimensions = BitmapUtil.getDimensions(is);
      return dimensions.first  > 0 && dimensions.first  <= getImageMaxWidth(context) &&
             dimensions.second > 0 && dimensions.second <= getImageMaxHeight(context);
    } catch (BitmapDecodingException e) {
      throw new IOException(e);
    }
  }

  public boolean canResize(@Nullable Attachment attachment) {
    return attachment != null && MediaUtil.isImage(attachment) && !MediaUtil.isGif(attachment);
  }

  public MediaStream getResizedMedia(@NonNull Context context,
                                     @NonNull MasterSecret masterSecret,
                                     @NonNull Attachment attachment)
      throws IOException
  {
    if (!canResize(attachment)) {
      throw new UnsupportedOperationException("Cannot resize this content type");
    }

    try {
      // XXX - This is loading everything into memory! We want the send path to be stream-like.
      return new MediaStream(new ByteArrayInputStream(BitmapUtil.createScaledBytes(context, new DecryptableUri(masterSecret, attachment.getDataUri()), this)),
                             MediaUtil.IMAGE_JPEG);
    } catch (BitmapDecodingException e) {
      throw new IOException(e);
    }
  }

  public MediaStream getCompressedVideo(@NonNull Context context,
                                     @NonNull MasterSecret masterSecret,
                                     @NonNull Attachment attachment)
          throws IOException
  {
    if (!MediaUtil.isVideo(attachment)) {
      throw new UnsupportedOperationException("Cannot compress this content type");
    }
    try {
      byte [] data = Util.readFully(PartAuthority.getAttachmentStream(context, masterSecret, attachment.getDataUri()));
      String fileName= String.valueOf(Math.abs(Util.getSecureRandom().nextLong()));
      String path = context.getCacheDir().toString()+"/"+fileName+attachment.getContentType().replace('/','.');
      FileOutputStream out = new FileOutputStream(path);
      out.write(data);
      String output = null;
      if(TextSecurePreferences.getVideoCompressionLevel(context).equals("high")) {
        output = SiliCompressor.with(context).compressVideo(path, context.getCacheDir().toString() , HIGH_VIDEO_COMPRESSION_WIDTH , HIGH_VIDEO_COMPRESSION_HEIGHT , HIGH_VIDEO_COMPRESSION_BITRATE);
      }else if(TextSecurePreferences.getVideoCompressionLevel(context).equals("medium")){
        output = SiliCompressor.with(context).compressVideo(path, context.getCacheDir().toString(), MEDIUM_VIDEO_COMPRESSION_WIDTH, MEDIUM_VIDEO_COMPRESSION_HEIGHT , MEDIUM_VIDEO_COMPRESSION_BITRATE);
      }else{
        output = SiliCompressor.with(context).compressVideo(path, context.getCacheDir().toString(), LOW_VIDEO_COMPRESSION_WIDTH , LOW_VIDEO_COMPRESSION_HEIGHT , LOW_VIDEO_COMPRESSION_BITRATE);
      }
      out.close();
      File file = new File(path);
      file.delete();
      return new MediaStream(new FileInputStream(output),attachment.getContentType());
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }
}
