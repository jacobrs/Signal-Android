package org.thoughtcrime.securesms.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Message;
import android.util.Log;

import org.thoughtcrime.securesms.database.model.MessageRecord;

public class EmojiReactionDatabase extends Database {

    public static String TAG = EmojiReactionDatabase.class.getSimpleName();

    public static final String TABLE_NAME           = "emoji_reactions";
    public static final String ID                   = "id";
    public static final String REACTION             = "reaction";
    public static final String SMS_MESSAGE_ID       = "sms_message_id";
    public static final String MMS_MESSAGE_ID       = "mms_message_id";
    public static final String HASHED_ID            = "hashed_id";

    public EmojiReactionDatabase(Context context, SQLiteOpenHelper databaseHelper){
        super(context, databaseHelper);
    }

    public static final String CREATE_TABLE = "CREATE TABLE" + TABLE_NAME + "(" +
            ID + " INTEGER PRIMARY KEY, " +
            REACTION + " TEXT NOT NULL, " +
            SMS_MESSAGE_ID + " TEXT DEFAULT NULL, " +
            MMS_MESSAGE_ID + " TEXT DEFAULT NULL, " +
            "FOREIGN KEY(" + SMS_MESSAGE_ID + ") REFERENCES " + SmsDatabase.TABLE_NAME + "(" + SmsDatabase.HASHED_ID + ")," +
            "FOREIGN KEY(" + MMS_MESSAGE_ID + ") REFERENCES " + MmsDatabase.TABLE_NAME + "(" + MmsDatabase.HASHED_ID + "));";

    public Cursor getMessageReaction(MessageRecord messageRecord){
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " +
            getMessageType(messageRecord) + " = ?", new String[]{messageRecord.getHashedId()});
        return cursor;
    }

    public void setMessageReaction(MessageRecord messageRecord, String reaction){
        String messageType = getMessageType(messageRecord);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(REACTION, reaction);
            contentValues.put(messageType, messageRecord.getHashedId());

            db.update(TABLE_NAME,
                    contentValues,
                    messageType + " = ? AND " + REACTION + " = ?",
                    new String[]{
                        contentValues.getAsString(messageType) + "",
                        contentValues.getAsString(REACTION)});

            db.setTransactionSuccessful();
        }catch(Exception e){
            Log.d(TAG, e.getMessage());
        }finally{
            db.endTransaction();
        }
        long threadId = messageRecord.getThreadId();
        notifyConversationListeners(threadId);
    }

    public String getMessageType(MessageRecord messageRecord){
        return messageRecord.isMms() ? MMS_MESSAGE_ID : SMS_MESSAGE_ID;
    }
}
