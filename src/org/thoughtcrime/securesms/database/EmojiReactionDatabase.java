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

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
            ID + " INTEGER PRIMARY KEY, " +
            REACTION + " TEXT NOT NULL, " +
            SMS_MESSAGE_ID + " TEXT DEFAULT NULL, " +
            MMS_MESSAGE_ID + " TEXT DEFAULT NULL, " +
            HASHED_ID + " TEXT DEFAULT NULL )"; // +
            //"FOREIGN KEY(" + SMS_MESSAGE_ID + ") REFERENCES " + SmsDatabase.TABLE_NAME + "(" + SmsDatabase.HASHED_ID + ")," +
            //"FOREIGN KEY(" + MMS_MESSAGE_ID + ") REFERENCES " + MmsDatabase.TABLE_NAME + "(" + MmsDatabase.HASHED_ID + "));";

    private static final String[] PROJECTION = new String[] {
            ID, REACTION, HASHED_ID
    };

    public Cursor getMessageReaction(MessageRecord messageRecord){
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + REACTION + " FROM " + TABLE_NAME + " WHERE " +
            getMessageType(messageRecord) + " = ?", new String[]{messageRecord.getHashedId()});
        return cursor;
    }

    public boolean checkIsDataAlreadyInDBorNot(String TableName, String dbfield, String fieldValue) {
        SQLiteDatabase sqldb = databaseHelper.getReadableDatabase();
        String Query = "Select * from " + TableName + " where " + dbfield + " = " + fieldValue;
        Cursor cursor = sqldb.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public String getReactionEmoji(MessageRecord messageRecord){
        String emoji = null;
        String hashedId = messageRecord.getHashedId();
        String where = HASHED_ID + "= '" + hashedId + "'";
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, PROJECTION, where, null, null, null, null);

        if(cursor.getCount() > 0) {
            cursor.moveToNext();
            int index = cursor.getColumnIndex(REACTION);

            emoji = cursor.getString(index); //
        }
        return emoji;
    }

    public void setMessageReaction(MessageRecord messageRecord, String reaction){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(REACTION, reaction);
            contentValues.put(HASHED_ID, messageRecord.getHashedId());

            db.insert(TABLE_NAME,null, contentValues);

            db.setTransactionSuccessful();
        }catch(Exception e){
            Log.d(TAG, e.getMessage());
        }finally{
            db.endTransaction();
        }
        long threadId = messageRecord.getThreadId();
        notifyConversationListeners(threadId);
    }

    public void setMessageReaction(String hashedId, String reaction){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(REACTION, reaction);
            contentValues.put(HASHED_ID, hashedId);

            db.insert(TABLE_NAME,null, contentValues);

            db.setTransactionSuccessful();
        }catch(Exception e){
            Log.d(TAG, e.getMessage());
        }finally{
            db.endTransaction();
        }
        //notifyConversationListeners(threadId);
    }



    public String getMessageType(MessageRecord messageRecord){
        return messageRecord.isMms() ? MMS_MESSAGE_ID : SMS_MESSAGE_ID;
    }
}
