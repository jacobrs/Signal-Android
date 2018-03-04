/**
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.annimon.stream.Stream;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.PduHeaders;

import org.thoughtcrime.securesms.ApplicationContext;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.attachments.DatabaseAttachment;
import org.thoughtcrime.securesms.attachments.MmsNotificationAttachment;
import org.thoughtcrime.securesms.crypto.AsymmetricMasterCipher;
import org.thoughtcrime.securesms.crypto.MasterCipher;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.crypto.MasterSecretUnion;
import org.thoughtcrime.securesms.database.documents.IdentityKeyMismatch;
import org.thoughtcrime.securesms.database.documents.IdentityKeyMismatchList;
import org.thoughtcrime.securesms.database.documents.NetworkFailure;
import org.thoughtcrime.securesms.database.documents.NetworkFailureList;
import org.thoughtcrime.securesms.database.model.DisplayRecord;
import org.thoughtcrime.securesms.database.model.MediaMmsMessageRecord;
import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.database.model.NotificationMmsMessageRecord;
import org.thoughtcrime.securesms.jobs.TrimThreadJob;
import org.thoughtcrime.securesms.mms.IncomingMediaMessage;
import org.thoughtcrime.securesms.mms.MmsException;
import org.thoughtcrime.securesms.mms.OutgoingExpirationUpdateMessage;
import org.thoughtcrime.securesms.mms.OutgoingGroupMediaMessage;
import org.thoughtcrime.securesms.mms.OutgoingMediaMessage;
import org.thoughtcrime.securesms.mms.OutgoingSecureMediaMessage;
import org.thoughtcrime.securesms.mms.SlideDeck;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientFormattingException;
import org.thoughtcrime.securesms.util.JsonUtils;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.Util;
import org.whispersystems.jobqueue.JobManager;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.util.guava.Optional;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MmsDatabase extends MessagingDatabase {

  private static final String TAG = MmsDatabase.class.getSimpleName();

  public  static final String TABLE_NAME         = "mms";
          static final String DATE_SENT          = "date";
          static final String DATE_RECEIVED      = "date_received";
  public  static final String MESSAGE_BOX        = "msg_box";
          static final String CONTENT_LOCATION   = "ct_l";
          static final String EXPIRY             = "exp";
  public  static final String MESSAGE_TYPE       = "m_type";
          static final String MESSAGE_SIZE       = "m_size";
          static final String STATUS             = "st";
          static final String TRANSACTION_ID     = "tr_id";
          static final String PART_COUNT         = "part_count";
          static final String NETWORK_FAILURE    = "network_failures";

  public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY, "                          +
    THREAD_ID + " INTEGER, " + DATE_SENT + " INTEGER, " + DATE_RECEIVED + " INTEGER, " + MESSAGE_BOX + " INTEGER, " +
    READ + " INTEGER DEFAULT 0, " + "m_id" + " TEXT, " + "sub" + " TEXT, "                +
    "sub_cs" + " INTEGER, " + BODY + " TEXT, " + PART_COUNT + " INTEGER, "               +
    "ct_t" + " TEXT, " + CONTENT_LOCATION + " TEXT, " + ADDRESS + " TEXT, "               +
    ADDRESS_DEVICE_ID + " INTEGER, "                                                            +
    EXPIRY + " INTEGER, " + "m_cls" + " TEXT, " + MESSAGE_TYPE + " INTEGER, "             +
    "v" + " INTEGER, " + MESSAGE_SIZE + " INTEGER, " + "pri" + " INTEGER, "          +
    "rr" + " INTEGER, " + "rpt_a" + " INTEGER, " + "resp_st" + " INTEGER, " +
    STATUS + " INTEGER, " + TRANSACTION_ID + " TEXT, " + "retr_st" + " INTEGER, "         +
    "retr_txt" + " TEXT, " + "retr_txt_cs" + " INTEGER, " + "read_status" + " INTEGER, "    +
    "ct_cls" + " INTEGER, " + "resp_txt" + " TEXT, " + "d_tm" + " INTEGER, "     +
    DELIVERY_RECEIPT_COUNT + " INTEGER DEFAULT 0, " + MISMATCHED_IDENTITIES + " TEXT DEFAULT NULL, "     +
    NETWORK_FAILURE + " TEXT DEFAULT NULL," + "d_rpt" + " INTEGER, " +
    SUBSCRIPTION_ID + " INTEGER DEFAULT -1, " + EXPIRES_IN + " INTEGER DEFAULT 0, " +
    EXPIRE_STARTED + " INTEGER DEFAULT 0, " + NOTIFIED + " INTEGER DEFAULT 0, " +
    READ_RECEIPT_COUNT + " INTEGER DEFAULT 0, "+ READ_REMINDER +" INTEGER DEFAULT 0, "+ PINNED + " INTEGER DEFAULT 0);";

  public static final String[] CREATE_INDEXS = {
    "CREATE INDEX IF NOT EXISTS mms_thread_id_index ON " + TABLE_NAME + " (" + THREAD_ID + ");",
    "CREATE INDEX IF NOT EXISTS mms_read_index ON " + TABLE_NAME + " (" + READ + ");",
    "CREATE INDEX IF NOT EXISTS mms_read_and_notified_and_thread_id_index ON " + TABLE_NAME + "(" + READ + "," + NOTIFIED + "," + THREAD_ID + ");",
    "CREATE INDEX IF NOT EXISTS mms_message_box_index ON " + TABLE_NAME + " (" + MESSAGE_BOX + ");",
    "CREATE INDEX IF NOT EXISTS mms_date_sent_index ON " + TABLE_NAME + " (" + DATE_SENT + ");",
    "CREATE INDEX IF NOT EXISTS mms_thread_date_index ON " + TABLE_NAME + " (" + THREAD_ID + ", " + DATE_RECEIVED + ");"
  };

  private static final String[] MMS_PROJECTION = new String[] {
      MmsDatabase.TABLE_NAME + "." + ID + " AS " + ID,
      THREAD_ID, DATE_SENT + " AS " + NORMALIZED_DATE_SENT,
      DATE_RECEIVED + " AS " + NORMALIZED_DATE_RECEIVED,
      MESSAGE_BOX, READ,
      CONTENT_LOCATION, EXPIRY, MESSAGE_TYPE,
      MESSAGE_SIZE, STATUS, TRANSACTION_ID,
      BODY, PART_COUNT, ADDRESS, ADDRESS_DEVICE_ID,
      DELIVERY_RECEIPT_COUNT, READ_RECEIPT_COUNT, MISMATCHED_IDENTITIES, NETWORK_FAILURE, SUBSCRIPTION_ID,
      EXPIRES_IN, EXPIRE_STARTED, NOTIFIED,
      AttachmentDatabase.TABLE_NAME + "." + AttachmentDatabase.ROW_ID + " AS " + AttachmentDatabase.ATTACHMENT_ID_ALIAS,
      AttachmentDatabase.UNIQUE_ID,
      AttachmentDatabase.MMS_ID,
      AttachmentDatabase.SIZE,
      AttachmentDatabase.FILE_NAME,
      AttachmentDatabase.DATA,
      AttachmentDatabase.THUMBNAIL,
      AttachmentDatabase.CONTENT_TYPE,
      AttachmentDatabase.CONTENT_LOCATION,
      AttachmentDatabase.DIGEST,
      AttachmentDatabase.FAST_PREFLIGHT_ID,
      AttachmentDatabase.VOICE_NOTE,
      AttachmentDatabase.CONTENT_DISPOSITION,
      AttachmentDatabase.NAME,
      AttachmentDatabase.TRANSFER_STATE
  };

  private static final String RAW_ID_WHERE = TABLE_NAME + "._id = ?";

  private final EarlyReceiptCache earlyDeliveryReceiptCache = new EarlyReceiptCache();
  private final EarlyReceiptCache earlyReadReceiptCache     = new EarlyReceiptCache();

  private final JobManager jobManager;

  public MmsDatabase(Context context, SQLiteOpenHelper databaseHelper) {
    super(context, databaseHelper);
    this.jobManager = ApplicationContext.getInstance(context).getJobManager();
  }

  @Override
  protected String getTableName() {
    return TABLE_NAME;
  }

  public int getMessageCountForThread(long threadId) {
    SQLiteDatabase db = databaseHelper.getReadableDatabase();
    Cursor cursor     = null;

    try {
      cursor = db.query(TABLE_NAME, new String[] {"COUNT(*)"}, THREAD_ID + " = ?", new String[] {threadId+""}, null, null, null);

      if (cursor != null && cursor.moveToFirst())
        return cursor.getInt(0);
    } finally {
      if (cursor != null)
        cursor.close();
    }

    return 0;
  }

  public void addFailures(long messageId, List<NetworkFailure> failure) {
    try {
      addToDocument(messageId, NETWORK_FAILURE, failure, NetworkFailureList.class);
    } catch (IOException e) {
      Log.w(TAG, e);
    }
  }

  public void removeFailure(long messageId, NetworkFailure failure) {
    try {
      removeFromDocument(messageId, NETWORK_FAILURE, failure, NetworkFailureList.class);
    } catch (IOException e) {
      Log.w(TAG, e);
    }
  }

  public void incrementReceiptCount(SyncMessageId messageId, long timestamp, boolean deliveryReceipt, boolean readReceipt) {
    SQLiteDatabase database = databaseHelper.getWritableDatabase();
    Cursor         cursor   = null;
    boolean        found    = false;

    try {
      cursor = database.query(TABLE_NAME, new String[] {ID, THREAD_ID, MESSAGE_BOX, ADDRESS}, DATE_SENT + " = ?", new String[] {String.valueOf(messageId.getTimetamp())}, null, null, null, null);

      while (cursor.moveToNext()) {
        if (Types.isOutgoingMessageType(cursor.getLong(cursor.getColumnIndexOrThrow(MESSAGE_BOX)))) {
          Address theirAddress = Address.fromSerialized(cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS)));
          Address ourAddress   = messageId.getAddress();
          String  columnName   = deliveryReceipt ? DELIVERY_RECEIPT_COUNT : READ_RECEIPT_COUNT;

          if (ourAddress.equals(theirAddress) || theirAddress.isGroup()) {
            long id       = cursor.getLong(cursor.getColumnIndexOrThrow(ID));
            long threadId = cursor.getLong(cursor.getColumnIndexOrThrow(THREAD_ID));
            int  status   = deliveryReceipt ? GroupReceiptDatabase.STATUS_DELIVERED : GroupReceiptDatabase.STATUS_READ;

            found = true;

            database.execSQL("UPDATE " + TABLE_NAME + " SET " +
                             columnName + " = " + columnName + " + 1 WHERE " + ID + " = ?",
                             new String[] {String.valueOf(id)});

            DatabaseFactory.getGroupReceiptDatabase(context).update(ourAddress, id, status, timestamp);
            DatabaseFactory.getThreadDatabase(context).update(threadId, false);
            notifyConversationListeners(threadId);
          }
        }
      }

      if (!found) {
        if (deliveryReceipt) earlyDeliveryReceiptCache.increment(messageId.getTimetamp(), messageId.getAddress());
        if (readReceipt)     earlyReadReceiptCache.increment(messageId.getTimetamp(), messageId.getAddress());
      }
    } finally {
      if (cursor != null)
        cursor.close();
    }
  }

  public long getThreadIdForMessage(long id) {
    String sql        = "SELECT " + THREAD_ID + " FROM " + TABLE_NAME + " WHERE " + ID + " = ?";
    String[] sqlArgs  = new String[] {id+""};
    SQLiteDatabase db = databaseHelper.getReadableDatabase();

    Cursor cursor = null;

    try {
      cursor = db.rawQuery(sql, sqlArgs);
      if (cursor != null && cursor.moveToFirst())
        return cursor.getLong(0);
      else
        return -1;
    } finally {
      if (cursor != null)
        cursor.close();
    }
  }

  private long getThreadIdFor(IncomingMediaMessage retrieved) throws RecipientFormattingException, MmsException {
    if (retrieved.getGroupId() != null) {
      Recipient groupRecipients = Recipient.from(context, retrieved.getGroupId(), true);
      return DatabaseFactory.getThreadDatabase(context).getThreadIdFor(groupRecipients);
    } else {
      Recipient sender = Recipient.from(context, retrieved.getFrom(), true);
      return DatabaseFactory.getThreadDatabase(context).getThreadIdFor(sender);
    }
  }

  private long getThreadIdFor(@NonNull NotificationInd notification) {
    String fromString = notification.getFrom() != null && notification.getFrom().getTextString() != null
                      ? Util.toIsoString(notification.getFrom().getTextString())
                      : "";
    Recipient recipient = Recipient.from(context, Address.fromExternal(context, fromString), false);
    return DatabaseFactory.getThreadDatabase(context).getThreadIdFor(recipient);
  }

  private Cursor rawQuery(@NonNull String where, @Nullable String[] arguments) {
    SQLiteDatabase database = databaseHelper.getReadableDatabase();
    return database.rawQuery("SELECT " + Util.join(MMS_PROJECTION, ",") +
                             " FROM " + MmsDatabase.TABLE_NAME +  " LEFT OUTER JOIN " + AttachmentDatabase.TABLE_NAME +
                             " ON (" + MmsDatabase.TABLE_NAME + "." + MmsDatabase.ID + " = " + AttachmentDatabase.TABLE_NAME + "." + AttachmentDatabase.MMS_ID + ")" +
                             " WHERE " + where, arguments);
  }

  public Cursor getMessage(long messageId) {
    Cursor cursor = rawQuery(RAW_ID_WHERE, new String[] {messageId + ""});
    setNotifyConverationListeners(cursor, getThreadIdForMessage(messageId));
    return cursor;
  }

  public Reader getExpireStartedMessages(@Nullable MasterSecret masterSecret) {
    String where = EXPIRE_STARTED + " > 0";
    return readerFor(masterSecret, rawQuery(where, null));
  }

  public Reader getDecryptInProgressMessages(MasterSecret masterSecret) {
    String where = MESSAGE_BOX + " & " + (Types.ENCRYPTION_ASYMMETRIC_BIT) + " != 0";
    return readerFor(masterSecret, rawQuery(where, null));
  }

  private void updateMailboxBitmask(long id, long maskOff, long maskOn, Optional<Long> threadId) {
    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    db.execSQL("UPDATE " + TABLE_NAME +
                   " SET " + MESSAGE_BOX + " = (" + MESSAGE_BOX + " & " + (Types.TOTAL_MASK - maskOff) + " | " + maskOn + " )" +
                   " WHERE " + ID + " = ?", new String[] {id + ""});

    if (threadId.isPresent()) {
      DatabaseFactory.getThreadDatabase(context).update(threadId.get(), false);
    }
  }

  public void markAsOutbox(long messageId) {
    long threadId = getThreadIdForMessage(messageId);
    updateMailboxBitmask(messageId, Types.BASE_TYPE_MASK, Types.BASE_OUTBOX_TYPE, Optional.of(threadId));
  }

  public void markAsForcedSms(long messageId) {
    long threadId = getThreadIdForMessage(messageId);
    updateMailboxBitmask(messageId, Types.PUSH_MESSAGE_BIT, Types.MESSAGE_FORCE_SMS_BIT, Optional.of(threadId));
    notifyConversationListeners(threadId);
  }

  public void markAsPendingInsecureSmsFallback(long messageId) {
    long threadId = getThreadIdForMessage(messageId);
    updateMailboxBitmask(messageId, Types.BASE_TYPE_MASK, Types.BASE_PENDING_INSECURE_SMS_FALLBACK, Optional.of(threadId));
    notifyConversationListeners(threadId);
  }

//  public void markAsSending(long messageId) {
//    long threadId = getThreadIdForMessage(messageId);
//    updateMailboxBitmask(messageId, Types.BASE_TYPE_MASK, Types.BASE_SENDING_TYPE, Optional.of(threadId));
//    notifyConversationListeners(threadId);
//  }

  public void markAsSentFailed(long messageId) {
    long threadId = getThreadIdForMessage(messageId);
    updateMailboxBitmask(messageId, Types.BASE_TYPE_MASK, Types.BASE_SENT_FAILED_TYPE, Optional.of(threadId));
    notifyConversationListeners(threadId);
  }

  @Override
  public void markAsSent(long messageId, boolean secure) {
    long threadId = getThreadIdForMessage(messageId);
    updateMailboxBitmask(messageId, Types.BASE_TYPE_MASK, Types.BASE_SENT_TYPE | (secure ? Types.PUSH_MESSAGE_BIT | Types.SECURE_MESSAGE_BIT : 0), Optional.of(threadId));
    notifyConversationListeners(threadId);
  }

  public void markDownloadState(long messageId, long state) {
    SQLiteDatabase database     = databaseHelper.getWritableDatabase();
    ContentValues contentValues = new ContentValues();
    contentValues.put(STATUS, state);

    database.update(TABLE_NAME, contentValues, ID_WHERE, new String[] {messageId + ""});
    notifyConversationListeners(getThreadIdForMessage(messageId));
  }

  public void markAsNoSession(long messageId, long threadId) {
    updateMailboxBitmask(messageId, Types.ENCRYPTION_MASK, Types.ENCRYPTION_REMOTE_NO_SESSION_BIT, Optional.of(threadId));
    notifyConversationListeners(threadId);
  }

//  public void markAsSecure(long messageId) {
//    updateMailboxBitmask(messageId, 0, Types.SECURE_MESSAGE_BIT, Optional.<Long>absent());
//  }

  public void markAsInsecure(long messageId) {
    updateMailboxBitmask(messageId, Types.SECURE_MESSAGE_BIT, 0, Optional.<Long>absent());
  }

//  public void markAsPush(long messageId) {
//    updateMailboxBitmask(messageId, 0, Types.PUSH_MESSAGE_BIT, Optional.<Long>absent());
//  }

  public void markAsDecryptFailed(long messageId, long threadId) {
    updateMailboxBitmask(messageId, Types.ENCRYPTION_MASK, Types.ENCRYPTION_REMOTE_FAILED_BIT, Optional.of(threadId));
    notifyConversationListeners(threadId);
  }

  public void markAsDecryptDuplicate(long messageId, long threadId) {
    updateMailboxBitmask(messageId, Types.ENCRYPTION_MASK, Types.ENCRYPTION_REMOTE_DUPLICATE_BIT, Optional.of(threadId));
    notifyConversationListeners(threadId);
  }

  public void markAsLegacyVersion(long messageId, long threadId) {
    updateMailboxBitmask(messageId, Types.ENCRYPTION_MASK, Types.ENCRYPTION_REMOTE_LEGACY_BIT, Optional.of(threadId));
    notifyConversationListeners(threadId);
  }

  @Override
  public void markExpireStarted(long messageId) {
    markExpireStarted(messageId, System.currentTimeMillis());
  }

  @Override
  public void markExpireStarted(long messageId, long startedTimestamp) {
    ContentValues contentValues = new ContentValues();
    contentValues.put(EXPIRE_STARTED, startedTimestamp);

    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    db.update(TABLE_NAME, contentValues, ID_WHERE, new String[] {String.valueOf(messageId)});

    long threadId = getThreadIdForMessage(messageId);
    notifyConversationListeners(threadId);
  }

  public void markAsNotified(long id) {
    SQLiteDatabase database      = databaseHelper.getWritableDatabase();
    ContentValues  contentValues = new ContentValues();

    contentValues.put(NOTIFIED, 1);

    database.update(TABLE_NAME, contentValues, ID_WHERE, new String[] {String.valueOf(id)});
  }

  public void markMessagesAsPinned(long threadId, long messageId){
    markMessagesAsPinned(THREAD_ID + " = ? AND " + ID + " = ? AND "+ PINNED + " = 0", new String[] {String.valueOf(threadId), String.valueOf(messageId)});
  }

  private void markMessagesAsPinned(String where, String[] args) {
    SQLiteDatabase database      = databaseHelper.getWritableDatabase();
    database.beginTransaction();
    try{
      ContentValues contentValues = new ContentValues();
      contentValues.put(PINNED,1);

      database.update(TABLE_NAME,contentValues,where,args);
      database.setTransactionSuccessful();
    }finally{
      database.endTransaction();
    }
    long threadId = Long.parseLong(args[0]);
    notifyConversationListeners(threadId);
  }

  public void markMessagesAsUnpinned(long threadId, long messageId){
    markMessagesAsUnpinned(THREAD_ID + " = ? AND " + ID + " = ? AND "+ PINNED + " = 1", new String[] {String.valueOf(threadId), String.valueOf(messageId)});
  }

  private void markMessagesAsUnpinned(String where, String[] args) {
    SQLiteDatabase database      = databaseHelper.getWritableDatabase();
    database.beginTransaction();
    try{
      ContentValues contentValues = new ContentValues();
      contentValues.put(PINNED,0);

      database.update(TABLE_NAME,contentValues,where,args);
      database.setTransactionSuccessful();
    }finally{
      database.endTransaction();
    }
    long threadId = Long.parseLong(args[0]);
    notifyConversationListeners(threadId);
  }



  public void markAsUnpinned(long id) {
    SQLiteDatabase database      = databaseHelper.getWritableDatabase();
    ContentValues  contentValues = new ContentValues();

    contentValues.put(PINNED, 0);

    database.update(TABLE_NAME, contentValues, ID_WHERE, new String[] {String.valueOf(id)});
  }


  public List<MarkedMessageInfo> setMessagesRead(long threadId) {
    return setMessagesRead(THREAD_ID + " = ? AND " + READ + " = 0", new String[] {String.valueOf(threadId)});
  }

  public List<MarkedMessageInfo> setAllMessagesRead() {
    return setMessagesRead(READ + " = 0", null);
  }

  private List<MarkedMessageInfo> setMessagesRead(String where, String[] arguments) {
    SQLiteDatabase          database = databaseHelper.getWritableDatabase();
    List<MarkedMessageInfo> result   = new LinkedList<>();
    Cursor                  cursor   = null;

    database.beginTransaction();

    try {
      cursor = database.query(TABLE_NAME, new String[] {ID, ADDRESS, DATE_SENT, MESSAGE_BOX, EXPIRES_IN, EXPIRE_STARTED}, where, arguments, null, null, null);

      while(cursor != null && cursor.moveToNext()) {
        if (Types.isSecureType(cursor.getLong(3))) {
          SyncMessageId  syncMessageId  = new SyncMessageId(Address.fromSerialized(cursor.getString(1)), cursor.getLong(2));
          ExpirationInfo expirationInfo = new ExpirationInfo(cursor.getLong(0), cursor.getLong(4), cursor.getLong(5), true);

          result.add(new MarkedMessageInfo(syncMessageId, expirationInfo));
        }
      }

      ContentValues contentValues = new ContentValues();
      contentValues.put(READ, 1);

      database.update(TABLE_NAME, contentValues, where, arguments);
      database.setTransactionSuccessful();
    } finally {
      if (cursor != null) cursor.close();
      database.endTransaction();
    }

    return result;
  }

  public void setMessagesUnread(long threadId, long messageId) {
    setMessagesUnread(THREAD_ID + " = ? AND " + ID + " = ? AND "+ READ + " <> 0 AND " + READ_REMINDER + " = 0", new String[] {String.valueOf(threadId), String.valueOf(messageId)});
  }

  private void setMessagesUnread(String where, String[] arguments){
    SQLiteDatabase          database  = databaseHelper.getWritableDatabase();

    database.beginTransaction();
    try {
      ContentValues contentValues = new ContentValues();
      contentValues.put(READ, 0);
      contentValues.put(READ_REMINDER, 1);

      database.update(TABLE_NAME, contentValues, where, arguments);
      database.setTransactionSuccessful();
    } finally {
      database.endTransaction();
    }

    long threadId = Long.parseLong(arguments[0]);
    notifyConversationListeners(threadId);
  }

  public void removeReadReminder(long threadId, long messageId) {
    removeReadReminder(THREAD_ID + " = ? AND " + ID + " = ? AND " + READ_REMINDER + " <> 0", new String[] {String.valueOf(threadId), String.valueOf(messageId)});
  }

  private void removeReadReminder(String where, String[] arguments){
    SQLiteDatabase          database  = databaseHelper.getWritableDatabase();

    database.beginTransaction();
    try {
      ContentValues contentValues = new ContentValues();
      contentValues.put(READ, 1);
      contentValues.put(READ_REMINDER, 0);

      database.update(TABLE_NAME, contentValues, where, arguments);
      database.setTransactionSuccessful();
    } finally {
      database.endTransaction();
    }

    long threadId = Long.parseLong(arguments[0]);
    notifyConversationListeners(threadId);
  }

  public List<Pair<Long, Long>> setTimestampRead(SyncMessageId messageId, long expireStarted) {
    SQLiteDatabase         database        = databaseHelper.getWritableDatabase();
    List<Pair<Long, Long>> expiring        = new LinkedList<>();
    Cursor                 cursor          = null;

    try {
      cursor = database.query(TABLE_NAME, new String[] {ID, THREAD_ID, MESSAGE_BOX, EXPIRES_IN, ADDRESS}, DATE_SENT + " = ?", new String[] {String.valueOf(messageId.getTimetamp())}, null, null, null, null);

      while (cursor.moveToNext()) {
        Address theirAddress = Address.fromSerialized(cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS)));
        Address ourAddress   = messageId.getAddress();

        if (ourAddress.equals(theirAddress) || theirAddress.isGroup()) {
          long id        = cursor.getLong(cursor.getColumnIndexOrThrow(ID));
          long threadId  = cursor.getLong(cursor.getColumnIndexOrThrow(THREAD_ID));
          long expiresIn = cursor.getLong(cursor.getColumnIndexOrThrow(EXPIRES_IN));

          ContentValues values = new ContentValues();
          values.put(READ, 1);

          if (expiresIn > 0) {
            values.put(EXPIRE_STARTED, expireStarted);
            expiring.add(new Pair<>(id, expiresIn));
          }

          database.update(TABLE_NAME, values, ID_WHERE, new String[]{String.valueOf(id)});

          DatabaseFactory.getThreadDatabase(context).updateReadState(threadId);
          DatabaseFactory.getThreadDatabase(context).setLastSeen(threadId);
          notifyConversationListeners(threadId);
        }
      }
    } finally {
      if (cursor != null)
        cursor.close();
    }

    return expiring;
  }

  public void updateMessageBody(MasterSecretUnion masterSecret, long messageId, String body) {
    body = getEncryptedBody(masterSecret, body);

    long type;

    if (masterSecret.getMasterSecret().isPresent()) {
      type = Types.ENCRYPTION_SYMMETRIC_BIT;
    } else {
      type = Types.ENCRYPTION_ASYMMETRIC_BIT;
    }

    updateMessageBodyAndType(messageId, body, Types.ENCRYPTION_MASK, type);
  }

  private Pair<Long, Long> updateMessageBodyAndType(long messageId, String body, long maskOff, long maskOn) {
    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    db.execSQL("UPDATE " + TABLE_NAME + " SET " + BODY + " = ?, " +
               MESSAGE_BOX + " = (" + MESSAGE_BOX + " & " + (Types.TOTAL_MASK - maskOff) + " | " + maskOn + ") " +
               "WHERE " + ID + " = ?",
               new String[] {body, messageId + ""});

    long threadId = getThreadIdForMessage(messageId);

    DatabaseFactory.getThreadDatabase(context).update(threadId, true);
    notifyConversationListeners(threadId);
    notifyConversationListListeners();

    return new Pair<>(messageId, threadId);
  }

  public Optional<MmsNotificationInfo> getNotification(long messageId) {
    Cursor cursor = null;

    try {
      cursor = rawQuery(RAW_ID_WHERE, new String[] {String.valueOf(messageId)});

      if (cursor != null && cursor.moveToNext()) {
        return Optional.of(new MmsNotificationInfo(cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS)),
                                                   cursor.getString(cursor.getColumnIndexOrThrow(CONTENT_LOCATION)),
                                                   cursor.getString(cursor.getColumnIndexOrThrow(TRANSACTION_ID)),
                                                   cursor.getInt(cursor.getColumnIndexOrThrow(SUBSCRIPTION_ID))));
      } else {
        return Optional.absent();
      }
    } finally {
      if (cursor != null)
        cursor.close();
    }
  }

  public OutgoingMediaMessage getOutgoingMessage(MasterSecret masterSecret, long messageId)
      throws MmsException, NoSuchMessageException
  {
    AttachmentDatabase attachmentDatabase = DatabaseFactory.getAttachmentDatabase(context);
    Cursor             cursor             = null;

    try {
      cursor = rawQuery(RAW_ID_WHERE, new String[] {String.valueOf(messageId)});

      if (cursor != null && cursor.moveToNext()) {
        long             outboxType       = cursor.getLong(cursor.getColumnIndexOrThrow(MESSAGE_BOX));
        String           messageText      = cursor.getString(cursor.getColumnIndexOrThrow(BODY));
        long             timestamp        = cursor.getLong(cursor.getColumnIndexOrThrow(NORMALIZED_DATE_SENT));
        int              subscriptionId   = cursor.getInt(cursor.getColumnIndexOrThrow(SUBSCRIPTION_ID));
        long             expiresIn        = cursor.getLong(cursor.getColumnIndexOrThrow(EXPIRES_IN));
        List<Attachment> attachments      = new LinkedList<Attachment>(attachmentDatabase.getAttachmentsForMessage(masterSecret, messageId));
        String           address          = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS));
        String           body             = getDecryptedBody(masterSecret, messageText, outboxType);
        long             threadId         = cursor.getLong(cursor.getColumnIndexOrThrow(THREAD_ID));
        int              distributionType = DatabaseFactory.getThreadDatabase(context).getDistributionType(threadId);

        Recipient recipient = Recipient.from(context, Address.fromSerialized(address), false);

        if (body != null && (Types.isGroupQuit(outboxType) || Types.isGroupUpdate(outboxType))) {
          return new OutgoingGroupMediaMessage(recipient, body, attachments, timestamp, 0);
        } else if (Types.isExpirationTimerUpdate(outboxType)) {
          return new OutgoingExpirationUpdateMessage(recipient, timestamp, expiresIn);
        }

        OutgoingMediaMessage message = new OutgoingMediaMessage(recipient, body, attachments, timestamp, subscriptionId, expiresIn, distributionType);

        if (Types.isSecureType(outboxType)) {
          return new OutgoingSecureMediaMessage(message);
        }

        return message;
      }

      throw new NoSuchMessageException("No record found for id: " + messageId);
    } catch (IOException e) {
      throw new MmsException(e);
    } finally {
      if (cursor != null)
        cursor.close();
    }
  }

  public long copyMessageInbox(MasterSecret masterSecret, long messageId) throws MmsException {
    try {
      OutgoingMediaMessage request = getOutgoingMessage(masterSecret, messageId);
      ContentValues contentValues = new ContentValues();
      contentValues.put(ADDRESS, request.getRecipient().getAddress().serialize());
      contentValues.put(DATE_SENT, request.getSentTimeMillis());
      contentValues.put(MESSAGE_BOX, Types.BASE_INBOX_TYPE | Types.SECURE_MESSAGE_BIT | Types.ENCRYPTION_SYMMETRIC_BIT);
      contentValues.put(THREAD_ID, getThreadIdForMessage(messageId));
      contentValues.put(READ, 1);
      contentValues.put(DATE_RECEIVED, contentValues.getAsLong(DATE_SENT));
      contentValues.put(EXPIRES_IN, request.getExpiresIn());

      List<Attachment> attachments = new LinkedList<>();

      for (Attachment attachment : request.getAttachments()) {
        DatabaseAttachment databaseAttachment = (DatabaseAttachment)attachment;
        attachments.add(new DatabaseAttachment(databaseAttachment.getAttachmentId(),
                                               databaseAttachment.getMmsId(),
                                               databaseAttachment.hasData(),
                                               databaseAttachment.hasThumbnail(),
                                               databaseAttachment.getContentType(),
                                               AttachmentDatabase.TRANSFER_PROGRESS_DONE,
                                               databaseAttachment.getSize(),
                                               databaseAttachment.getFileName(),
                                               databaseAttachment.getLocation(),
                                               databaseAttachment.getKey(),
                                               databaseAttachment.getRelay(),
                                               databaseAttachment.getDigest(),
                                               databaseAttachment.getFastPreflightId(),
                                               databaseAttachment.isVoiceNote()));
      }

      return insertMediaMessage(new MasterSecretUnion(masterSecret),
                                request.getBody(),
                                attachments,
                                contentValues,
                                null);
    } catch (NoSuchMessageException e) {
      throw new MmsException(e);
    }
  }

  private Optional<InsertResult> insertMessageInbox(MasterSecretUnion masterSecret,
                                                    IncomingMediaMessage retrieved,
                                                    String contentLocation,
                                                    long threadId, long mailbox)
      throws MmsException
  {
    if (threadId == -1 || retrieved.isGroupMessage()) {
      try {
        threadId = getThreadIdFor(retrieved);
      } catch (RecipientFormattingException e) {
        Log.w("MmsDatabase", e);
        if (threadId == -1)
          throw new MmsException(e);
      }
    }

    ContentValues contentValues = new ContentValues();

    contentValues.put(DATE_SENT, retrieved.getSentTimeMillis());
    contentValues.put(ADDRESS, retrieved.getFrom().serialize());

    contentValues.put(MESSAGE_BOX, mailbox);
    contentValues.put(MESSAGE_TYPE, PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF);
    contentValues.put(THREAD_ID, threadId);
    contentValues.put(CONTENT_LOCATION, contentLocation);
    contentValues.put(STATUS, Status.DOWNLOAD_INITIALIZED);
    contentValues.put(DATE_RECEIVED, generatePduCompatTimestamp());
    contentValues.put(PART_COUNT, retrieved.getAttachments().size());
    contentValues.put(SUBSCRIPTION_ID, retrieved.getSubscriptionId());
    contentValues.put(EXPIRES_IN, retrieved.getExpiresIn());
    contentValues.put(READ, retrieved.isExpirationUpdate() ? 1 : 0);

    if (!contentValues.containsKey(DATE_SENT)) {
      contentValues.put(DATE_SENT, contentValues.getAsLong(DATE_RECEIVED));
    }

    if (retrieved.isPushMessage() && isDuplicate(retrieved, threadId)) {
      Log.w(TAG, "Ignoring duplicate media message (" + retrieved.getSentTimeMillis() + ")");
      return Optional.absent();
    }

    long messageId = insertMediaMessage(masterSecret, retrieved.getBody(), retrieved.getAttachments(), contentValues, null);

    if (!Types.isExpirationTimerUpdate(mailbox)) {
      DatabaseFactory.getThreadDatabase(context).incrementUnread(threadId, 1);
      DatabaseFactory.getThreadDatabase(context).update(threadId, true);
    }

    notifyConversationListeners(threadId);
    jobManager.add(new TrimThreadJob(context, threadId));

    return Optional.of(new InsertResult(messageId, threadId));
  }

  public Optional<InsertResult> insertMessageInbox(MasterSecretUnion masterSecret,
                                                   IncomingMediaMessage retrieved,
                                                   String contentLocation, long threadId)
      throws MmsException
  {
    long type = Types.BASE_INBOX_TYPE;

    if (masterSecret.getMasterSecret().isPresent()) {
      type |= Types.ENCRYPTION_SYMMETRIC_BIT;
    } else {
      type |= Types.ENCRYPTION_ASYMMETRIC_BIT;
    }

    if (retrieved.isPushMessage()) {
      type |= Types.PUSH_MESSAGE_BIT;
    }

    if (retrieved.isExpirationUpdate()) {
      type |= Types.EXPIRATION_TIMER_UPDATE_BIT;
    }

    return insertMessageInbox(masterSecret, retrieved, contentLocation, threadId, type);
  }

  public Optional<InsertResult> insertSecureDecryptedMessageInbox(MasterSecretUnion masterSecret,
                                                                  IncomingMediaMessage retrieved,
                                                                  long threadId)
      throws MmsException
  {
    long type = Types.BASE_INBOX_TYPE | Types.SECURE_MESSAGE_BIT;

    if (masterSecret.getMasterSecret().isPresent()) {
      type |= Types.ENCRYPTION_SYMMETRIC_BIT;
    } else {
      type |= Types.ENCRYPTION_ASYMMETRIC_BIT;
    }

    if (retrieved.isPushMessage()) {
      type |= Types.PUSH_MESSAGE_BIT;
    }

    if (retrieved.isExpirationUpdate()) {
      type |= Types.EXPIRATION_TIMER_UPDATE_BIT;
    }

    return insertMessageInbox(masterSecret, retrieved, "", threadId, type);
  }

  public Pair<Long, Long> insertMessageInbox(@NonNull NotificationInd notification, int subscriptionId) {
    SQLiteDatabase       db             = databaseHelper.getWritableDatabase();
    long                 threadId       = getThreadIdFor(notification);
    ContentValues        contentValues  = new ContentValues();
    ContentValuesBuilder contentBuilder = new ContentValuesBuilder(contentValues);

    Log.w(TAG, "Message received type: " + notification.getMessageType());


    contentBuilder.add(CONTENT_LOCATION, notification.getContentLocation());
    contentBuilder.add(DATE_SENT, System.currentTimeMillis());
    contentBuilder.add(EXPIRY, notification.getExpiry());
    contentBuilder.add(MESSAGE_SIZE, notification.getMessageSize());
    contentBuilder.add(TRANSACTION_ID, notification.getTransactionId());
    contentBuilder.add(MESSAGE_TYPE, notification.getMessageType());

    if (notification.getFrom() != null) {
      contentValues.put(ADDRESS, Address.fromExternal(context, Util.toIsoString(notification.getFrom().getTextString())).serialize());
    }

    contentValues.put(MESSAGE_BOX, Types.BASE_INBOX_TYPE);
    contentValues.put(THREAD_ID, threadId);
    contentValues.put(STATUS, Status.DOWNLOAD_INITIALIZED);
    contentValues.put(DATE_RECEIVED, generatePduCompatTimestamp());
    contentValues.put(READ, Util.isDefaultSmsProvider(context) ? 0 : 1);
    contentValues.put(SUBSCRIPTION_ID, subscriptionId);

    if (!contentValues.containsKey(DATE_SENT))
      contentValues.put(DATE_SENT, contentValues.getAsLong(DATE_RECEIVED));

    long messageId = db.insert(TABLE_NAME, null, contentValues);

    return new Pair<>(messageId, threadId);
  }

  public void markIncomingNotificationReceived(long threadId) {
    notifyConversationListeners(threadId);
    DatabaseFactory.getThreadDatabase(context).update(threadId, true);

    if (org.thoughtcrime.securesms.util.Util.isDefaultSmsProvider(context)) {
      DatabaseFactory.getThreadDatabase(context).incrementUnread(threadId, 1);
    }

    jobManager.add(new TrimThreadJob(context, threadId));
  }

  public long insertMessageOutbox(@NonNull MasterSecretUnion masterSecret,
                                  @NonNull OutgoingMediaMessage message,
                                  long threadId, boolean forceSms,
                                  @Nullable SmsDatabase.InsertListener insertListener)
      throws MmsException
  {
    long type = Types.BASE_SENDING_TYPE;

    if (masterSecret.getMasterSecret().isPresent()) type |= Types.ENCRYPTION_SYMMETRIC_BIT;
    else                                            type |= Types.ENCRYPTION_ASYMMETRIC_BIT;

    if (message.isSecure()) type |= (Types.SECURE_MESSAGE_BIT | Types.PUSH_MESSAGE_BIT);
    if (forceSms)           type |= Types.MESSAGE_FORCE_SMS_BIT;

    if (message.isGroup()) {
      if      (((OutgoingGroupMediaMessage)message).isGroupUpdate()) type |= Types.GROUP_UPDATE_BIT;
      else if (((OutgoingGroupMediaMessage)message).isGroupQuit())   type |= Types.GROUP_QUIT_BIT;
    }

    if (message.isExpirationUpdate()) {
      type |= Types.EXPIRATION_TIMER_UPDATE_BIT;
    }

    Map<Address, Long> earlyDeliveryReceipts = earlyDeliveryReceiptCache.remove(message.getSentTimeMillis());
    Map<Address, Long> earlyReadReceipts     = earlyReadReceiptCache.remove(message.getSentTimeMillis());

    ContentValues contentValues = new ContentValues();
    contentValues.put(DATE_SENT, message.getSentTimeMillis());
    contentValues.put(MESSAGE_TYPE, PduHeaders.MESSAGE_TYPE_SEND_REQ);

    contentValues.put(MESSAGE_BOX, type);
    contentValues.put(THREAD_ID, threadId);
    contentValues.put(READ, 1);
    contentValues.put(DATE_RECEIVED, System.currentTimeMillis());
    contentValues.put(SUBSCRIPTION_ID, message.getSubscriptionId());
    contentValues.put(EXPIRES_IN, message.getExpiresIn());
    contentValues.put(ADDRESS, message.getRecipient().getAddress().serialize());
    contentValues.put(DELIVERY_RECEIPT_COUNT, Stream.of(earlyDeliveryReceipts.values()).mapToLong(Long::longValue).sum());
    contentValues.put(READ_RECEIPT_COUNT, Stream.of(earlyReadReceipts.values()).mapToLong(Long::longValue).sum());

    long messageId = insertMediaMessage(masterSecret, message.getBody(), message.getAttachments(), contentValues, insertListener);

    if (message.getRecipient().getAddress().isGroup()) {
      List<Recipient>      members         = DatabaseFactory.getGroupDatabase(context).getGroupMembers(message.getRecipient().getAddress().toGroupString(), false);
      GroupReceiptDatabase receiptDatabase = DatabaseFactory.getGroupReceiptDatabase(context);

      receiptDatabase.insert(Stream.of(members).map(Recipient::getAddress).toList(),
                             messageId, GroupReceiptDatabase.STATUS_UNDELIVERED, message.getSentTimeMillis());

      for (Address address : earlyDeliveryReceipts.keySet()) receiptDatabase.update(address, messageId, GroupReceiptDatabase.STATUS_DELIVERED, -1);
      for (Address address : earlyReadReceipts.keySet())     receiptDatabase.update(address, messageId, GroupReceiptDatabase.STATUS_READ, -1);
    }

    DatabaseFactory.getThreadDatabase(context).setLastSeen(threadId);
    DatabaseFactory.getThreadDatabase(context).setHasSent(threadId, true);
    jobManager.add(new TrimThreadJob(context, threadId));

    return messageId;
  }

  private String getEncryptedBody(MasterSecretUnion masterSecret, String body) {
    if (masterSecret.getMasterSecret().isPresent()) {
      return new MasterCipher(masterSecret.getMasterSecret().get()).encryptBody(body);
    } else {
      return new AsymmetricMasterCipher(masterSecret.getAsymmetricMasterSecret().get()).encryptBody(body);
    }
  }

  private @Nullable String getDecryptedBody(@NonNull MasterSecret masterSecret,
                                            @Nullable String body, long outboxType)
  {
    try {
      if (!TextUtils.isEmpty(body) && Types.isSymmetricEncryption(outboxType)) {
        MasterCipher masterCipher = new MasterCipher(masterSecret);
        return masterCipher.decryptBody(body);
      } else {
        return body;
      }
    } catch (InvalidMessageException e) {
      Log.w(TAG, e);
    }

    return null;
  }

  private long insertMediaMessage(@NonNull MasterSecretUnion masterSecret,
                                  @Nullable String body,
                                  @NonNull List<Attachment> attachments,
                                  @NonNull ContentValues contentValues,
                                  @Nullable SmsDatabase.InsertListener insertListener)
      throws MmsException
  {
    SQLiteDatabase     db              = databaseHelper.getWritableDatabase();
    AttachmentDatabase partsDatabase   = DatabaseFactory.getAttachmentDatabase(context);

    if (Types.isSymmetricEncryption(contentValues.getAsLong(MESSAGE_BOX)) ||
        Types.isAsymmetricEncryption(contentValues.getAsLong(MESSAGE_BOX)))
    {
      if (!TextUtils.isEmpty(body)) {
        contentValues.put(BODY, getEncryptedBody(masterSecret, body));
      }
    }

    contentValues.put(PART_COUNT, attachments.size());

    db.beginTransaction();
    try {
      long messageId = db.insert(TABLE_NAME, null, contentValues);

      partsDatabase.insertAttachmentsForMessage(masterSecret, messageId, attachments);

      db.setTransactionSuccessful();
      return messageId;
    } finally {
      db.endTransaction();

      if (insertListener != null) {
        insertListener.onComplete();
      }

      notifyConversationListeners(contentValues.getAsLong(THREAD_ID));
      DatabaseFactory.getThreadDatabase(context).update(contentValues.getAsLong(THREAD_ID), true);
    }
  }

  public boolean delete(long messageId) {
    long               threadId           = getThreadIdForMessage(messageId);
    AttachmentDatabase attachmentDatabase = DatabaseFactory.getAttachmentDatabase(context);
    attachmentDatabase.deleteAttachmentsForMessage(messageId);

    GroupReceiptDatabase groupReceiptDatabase = DatabaseFactory.getGroupReceiptDatabase(context);
    groupReceiptDatabase.deleteRowsForMessage(messageId);

    SQLiteDatabase database = databaseHelper.getWritableDatabase();
    database.delete(TABLE_NAME, ID_WHERE, new String[] {messageId+""});
    boolean threadDeleted = DatabaseFactory.getThreadDatabase(context).update(threadId, false);
    notifyConversationListeners(threadId);
    return threadDeleted;
  }

  public void deleteThread(long threadId) {
    Set<Long> singleThreadSet = new HashSet<>();
    singleThreadSet.add(threadId);
    deleteThreads(singleThreadSet);
  }

  private boolean isDuplicate(IncomingMediaMessage message, long threadId) {
    SQLiteDatabase database = databaseHelper.getReadableDatabase();
    Cursor         cursor   = database.query(TABLE_NAME, null, DATE_SENT + " = ? AND " + ADDRESS + " = ? AND " + THREAD_ID + " = ?",
                                             new String[]{String.valueOf(message.getSentTimeMillis()), message.getFrom().serialize(), String.valueOf(threadId)},
                                             null, null, null, "1");

    try {
      return cursor != null && cursor.moveToFirst();
    } finally {
      if (cursor != null) cursor.close();
    }
  }


  /*package*/ void deleteThreads(Set<Long> threadIds) {
    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    String where      = "";
    Cursor cursor     = null;

    for (long threadId : threadIds) {
      where += THREAD_ID + " = '" + threadId + "' OR ";
    }

    where = where.substring(0, where.length() - 4);

    try {
      cursor = db.query(TABLE_NAME, new String[] {ID}, where, null, null, null, null);

      while (cursor != null && cursor.moveToNext()) {
        delete(cursor.getLong(0));
      }

    } finally {
      if (cursor != null)
        cursor.close();
    }
  }

  /*package*/void deleteMessagesInThreadBeforeDate(long threadId, long date) {
    Cursor cursor = null;

    try {
      SQLiteDatabase db = databaseHelper.getReadableDatabase();
      String where      = THREAD_ID + " = ? AND (CASE (" + MESSAGE_BOX + " & " + Types.BASE_TYPE_MASK + ") ";

      for (long outgoingType : Types.OUTGOING_MESSAGE_TYPES) {
        where += " WHEN " + outgoingType + " THEN " + DATE_SENT + " < " + date;
      }

      where += (" ELSE " + DATE_RECEIVED + " < " + date + " END)");

      Log.w("MmsDatabase", "Executing trim query: " + where);
      cursor = db.query(TABLE_NAME, new String[] {ID}, where, new String[] {threadId+""}, null, null, null);

      while (cursor != null && cursor.moveToNext()) {
        Log.w("MmsDatabase", "Trimming: " + cursor.getLong(0));
        delete(cursor.getLong(0));
      }

    } finally {
      if (cursor != null)
        cursor.close();
    }
  }


  public void deleteAllThreads() {
    DatabaseFactory.getAttachmentDatabase(context).deleteAllAttachments();
    DatabaseFactory.getGroupReceiptDatabase(context).deleteAllRows();

    SQLiteDatabase database = databaseHelper.getWritableDatabase();
    database.delete(TABLE_NAME, null, null);
  }

  public Cursor getCarrierMmsInformation(String apn) {
    Uri uri                = Uri.withAppendedPath(Uri.parse("content://telephony/carriers"), "current");
    String selection       = TextUtils.isEmpty(apn) ? null : "apn = ?";
    String[] selectionArgs = TextUtils.isEmpty(apn) ? null : new String[] {apn.trim()};

    try {
      return context.getContentResolver().query(uri, null, selection, selectionArgs, null);
    } catch (NullPointerException npe) {
      // NOTE - This is dumb, but on some devices there's an NPE in the Android framework
      // for the provider of this call, which gets rethrown back to here through a binder
      // call.
      throw new IllegalArgumentException(npe);
    }
  }

  public Reader readerFor(MasterSecret masterSecret, Cursor cursor) {
    return new Reader(masterSecret, cursor);
  }

  public OutgoingMessageReader readerFor(OutgoingMediaMessage message, long threadId) {
    return new OutgoingMessageReader(message, threadId);
  }

  public static class Status {
    public static final int DOWNLOAD_INITIALIZED     = 1;
    public static final int DOWNLOAD_NO_CONNECTIVITY = 2;
    public static final int DOWNLOAD_CONNECTING      = 3;
    public static final int DOWNLOAD_SOFT_FAILURE    = 4;
    public static final int DOWNLOAD_HARD_FAILURE    = 5;
    public static final int DOWNLOAD_APN_UNAVAILABLE = 6;
  }

  public static class MmsNotificationInfo {
    private final Address from;
    private final String  contentLocation;
    private final String  transactionId;
    private final int     subscriptionId;

    MmsNotificationInfo(@Nullable String from, String contentLocation, String transactionId, int subscriptionId) {
      this.from            = from == null ? null : Address.fromSerialized(from);
      this.contentLocation = contentLocation;
      this.transactionId   = transactionId;
      this.subscriptionId  = subscriptionId;
    }

    public String getContentLocation() {
      return contentLocation;
    }

    public String getTransactionId() {
      return transactionId;
    }

    public int getSubscriptionId() {
      return subscriptionId;
    }

    public @Nullable Address getFrom() {
      return from;
    }
  }

  public class OutgoingMessageReader {

    private final OutgoingMediaMessage message;
    private final long                 id;
    private final long                 threadId;

    public OutgoingMessageReader(OutgoingMediaMessage message, long threadId) {
      try {
        this.message = message;
        this.id = SecureRandom.getInstance("SHA1PRNG").nextLong();
        this.threadId = threadId;
      } catch (NoSuchAlgorithmException e) {
        throw new AssertionError(e);
      }
    }

    public MessageRecord getCurrent() {
      SlideDeck slideDeck = new SlideDeck(context, message.getAttachments());

      return new MediaMmsMessageRecord(context, id, message.getRecipient(), message.getRecipient(),
                                       1, System.currentTimeMillis(), System.currentTimeMillis(),
                                       0, threadId, new DisplayRecord.Body(message.getBody(), true),
                                       slideDeck, slideDeck.getSlides().size(),
                                       message.isSecure() ? MmsSmsColumns.Types.getOutgoingEncryptedMessageType() : MmsSmsColumns.Types.getOutgoingSmsMessageType(),
                                       new LinkedList<IdentityKeyMismatch>(),
                                       new LinkedList<NetworkFailure>(),
                                       message.getSubscriptionId(),
                                       message.getExpiresIn(),
                                       System.currentTimeMillis(), 0, 0, 0);
    }
  }

  public class Reader {

    private final Cursor       cursor;
    private final MasterSecret masterSecret;
    private final MasterCipher masterCipher;

    public Reader(MasterSecret masterSecret, Cursor cursor) {
      this.cursor       = cursor;
      this.masterSecret = masterSecret;

      if (masterSecret != null) masterCipher = new MasterCipher(masterSecret);
      else                      masterCipher = null;
    }

    public MessageRecord getNext() {
      if (cursor == null || !cursor.moveToNext())
        return null;

      return getCurrent();
    }

    public MessageRecord getCurrent() {
      long mmsType = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.MESSAGE_TYPE));

      if (mmsType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
        return getNotificationMmsMessageRecord(cursor);
      } else {
        return getMediaMmsMessageRecord(cursor);
      }
    }

    private NotificationMmsMessageRecord getNotificationMmsMessageRecord(Cursor cursor) {
      long      id                   = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.ID));
      long      dateSent             = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.NORMALIZED_DATE_SENT));
      long      dateReceived         = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.NORMALIZED_DATE_RECEIVED));
      long      threadId             = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.THREAD_ID));
      long      mailbox              = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.MESSAGE_BOX));
      String    address              = cursor.getString(cursor.getColumnIndexOrThrow(MmsDatabase.ADDRESS));
      int       addressDeviceId      = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.ADDRESS_DEVICE_ID));
      Recipient recipient            = getRecipientFor(address);

      String    contentLocation      = cursor.getString(cursor.getColumnIndexOrThrow(MmsDatabase.CONTENT_LOCATION));
      String    transactionId        = cursor.getString(cursor.getColumnIndexOrThrow(MmsDatabase.TRANSACTION_ID));
      long      messageSize          = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.MESSAGE_SIZE));
      long      expiry               = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.EXPIRY));
      int       status               = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.STATUS));
      int       deliveryReceiptCount = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.DELIVERY_RECEIPT_COUNT));
      int       readReceiptCount     = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.READ_RECEIPT_COUNT));
      int       subscriptionId       = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.SUBSCRIPTION_ID));
      int       readReminder         = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.READ_REMINDER));
      int       pinned               = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.PINNED));

      if (!TextSecurePreferences.isReadReceiptsEnabled(context)) {
        readReceiptCount = 0;
      }

      byte[]contentLocationBytes = null;
      byte[]transactionIdBytes   = null;

      if (!TextUtils.isEmpty(contentLocation))
        contentLocationBytes = org.thoughtcrime.securesms.util.Util.toIsoBytes(contentLocation);

      if (!TextUtils.isEmpty(transactionId))
        transactionIdBytes = org.thoughtcrime.securesms.util.Util.toIsoBytes(transactionId);

      SlideDeck slideDeck = new SlideDeck(context, new MmsNotificationAttachment(status, messageSize));


      return new NotificationMmsMessageRecord(context, id, recipient, recipient,
                                              addressDeviceId, dateSent, dateReceived, deliveryReceiptCount, threadId,
                                              contentLocationBytes, messageSize, expiry, status,
                                              transactionIdBytes, mailbox, subscriptionId, slideDeck,
                                              readReceiptCount, readReminder, pinned);
    }

    private MediaMmsMessageRecord getMediaMmsMessageRecord(Cursor cursor) {
      long               id                   = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.ID));
      long               dateSent             = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.NORMALIZED_DATE_SENT));
      long               dateReceived         = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.NORMALIZED_DATE_RECEIVED));
      long               box                  = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.MESSAGE_BOX));
      long               threadId             = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.THREAD_ID));
      String             address              = cursor.getString(cursor.getColumnIndexOrThrow(MmsDatabase.ADDRESS));
      int                addressDeviceId      = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.ADDRESS_DEVICE_ID));
      int                deliveryReceiptCount = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.DELIVERY_RECEIPT_COUNT));
      int                readReceiptCount     = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.READ_RECEIPT_COUNT));
      DisplayRecord.Body body                 = getBody(cursor);
      int                partCount            = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.PART_COUNT));
      String             mismatchDocument     = cursor.getString(cursor.getColumnIndexOrThrow(MmsDatabase.MISMATCHED_IDENTITIES));
      String             networkDocument      = cursor.getString(cursor.getColumnIndexOrThrow(MmsDatabase.NETWORK_FAILURE));
      int                subscriptionId       = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.SUBSCRIPTION_ID));
      long               expiresIn            = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.EXPIRES_IN));
      long               expireStarted        = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.EXPIRE_STARTED));
      int                markedUnread         = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.READ_REMINDER));    
      int                pinned               = cursor.getInt(cursor.getColumnIndexOrThrow(MmsDatabase.PINNED));
      
      if (!TextSecurePreferences.isReadReceiptsEnabled(context)) {
        readReceiptCount = 0;
      }

      Recipient                 recipient       = getRecipientFor(address);
      List<IdentityKeyMismatch> mismatches      = getMismatchedIdentities(mismatchDocument);
      List<NetworkFailure>      networkFailures = getFailures(networkDocument);
      SlideDeck                 slideDeck       = getSlideDeck(cursor);

      return new MediaMmsMessageRecord(context, id, recipient, recipient,
                                       addressDeviceId, dateSent, dateReceived, deliveryReceiptCount,
                                       threadId, body, slideDeck, partCount, box, mismatches,
                                       networkFailures, subscriptionId, expiresIn, expireStarted,
                                       readReceiptCount, markedUnread, pinned);
    }

    private Recipient getRecipientFor(String serialized) {
      Address address;

      if (TextUtils.isEmpty(serialized) || "insert-address-token".equals(serialized)) {
        address = Address.UNKNOWN;
      } else {
        address = Address.fromSerialized(serialized);

      }
      return Recipient.from(context, address, true);
    }

    private List<IdentityKeyMismatch> getMismatchedIdentities(String document) {
      if (!TextUtils.isEmpty(document)) {
        try {
          return JsonUtils.fromJson(document, IdentityKeyMismatchList.class).getList();
        } catch (IOException e) {
          Log.w(TAG, e);
        }
      }

      return new LinkedList<>();
    }

    private List<NetworkFailure> getFailures(String document) {
      if (!TextUtils.isEmpty(document)) {
        try {
          return JsonUtils.fromJson(document, NetworkFailureList.class).getList();
        } catch (IOException ioe) {
          Log.w(TAG, ioe);
        }
      }

      return new LinkedList<>();
    }

    private DisplayRecord.Body getBody(Cursor cursor) {
      try {
        String body = cursor.getString(cursor.getColumnIndexOrThrow(MmsDatabase.BODY));
        long box    = cursor.getLong(cursor.getColumnIndexOrThrow(MmsDatabase.MESSAGE_BOX));

        if (!TextUtils.isEmpty(body) && masterCipher != null && Types.isSymmetricEncryption(box)) {
          return new DisplayRecord.Body(masterCipher.decryptBody(body), true);
        } else if (!TextUtils.isEmpty(body) && masterCipher == null && Types.isSymmetricEncryption(box)) {
          return new DisplayRecord.Body(body, false);
        } else if (!TextUtils.isEmpty(body) && Types.isAsymmetricEncryption(box)) {
          return new DisplayRecord.Body(body, false);
        } else {
          return new DisplayRecord.Body(body == null ? "" : body, true);
        }
      } catch (InvalidMessageException e) {
        Log.w("MmsDatabase", e);
        return new DisplayRecord.Body(context.getString(R.string.MmsDatabase_error_decrypting_message), true);
      }
    }

    private SlideDeck getSlideDeck(@NonNull Cursor cursor) {
      Attachment attachment = DatabaseFactory.getAttachmentDatabase(context).getAttachment(masterSecret, cursor);
      return new SlideDeck(context, attachment);
    }

    public void close() {
      cursor.close();
    }
  }

  private long generatePduCompatTimestamp() {
    final long time = System.currentTimeMillis();
    return time - (time % 1000);
  }
}
