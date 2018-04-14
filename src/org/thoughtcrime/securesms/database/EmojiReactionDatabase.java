package org.thoughtcrime.securesms.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.thoughtcrime.securesms.database.model.MessageRecord;

public class EmojiReactionDatabase extends Database {

    public static String TAG = EmojiReactionDatabase.class.getSimpleName();

    public static final String TABLE_NAME           = "emoji_reactions";
    public static final String ID                   = "id";
    public static final String REACTION             = "reaction";
    public static final String HASHED_ID            = "hashed_id";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
            ID + " INTEGER PRIMARY KEY, " +
            REACTION + " TEXT NOT NULL, " +
            HASHED_ID + " TEXT DEFAULT NULL )"; // +

    private static final String[] PROJECTION = new String[] {
            ID, REACTION, HASHED_ID
    };

    public EmojiReactionDatabase(Context context, SQLiteOpenHelper databaseHelper){
        super(context, databaseHelper);
    }

    public String getReactionEmoji(MessageRecord messageRecord){
        String emoji = null;
        String hashedId = messageRecord.getHashedId();
        String where = HASHED_ID + "= '" + hashedId + "'";
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Return a cursor containing the row with the specified hashed id.
        Cursor cursor = db.query(TABLE_NAME, PROJECTION, where, null, null, null, null);

        // If the cursor contains a row, the reaction exists.
        if(cursor.getCount() > 0) {
            cursor.moveToNext();
            int index = cursor.getColumnIndex(REACTION);
            emoji = cursor.getString(index);
        }
        return emoji;
    }

    // Add emoji reaction to the database (method used by SENDER)
    public void setMessageReaction(MessageRecord messageRecord, String reaction){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            String hashedId = messageRecord.getHashedId();

            ContentValues contentValues = new ContentValues();
            contentValues.put(REACTION, reaction);
            contentValues.put(HASHED_ID, hashedId);

            String where = HASHED_ID + "= '" + hashedId + "'";

            // Update the reaction in the DB for the message with the specified hashed id.
            int update = db.update(TABLE_NAME, contentValues, where, null);

            // If update == 0, reaction does not exists for the hashed id, and is inserted.
            if(update == 0){
                db.insert(TABLE_NAME,null, contentValues);
            }

            db.setTransactionSuccessful();
        }catch(Exception e){
            Log.d(TAG, e.getMessage());
        }finally{
            db.endTransaction();
        }

        // Notify the conversation listener of a change, to refresh the conversation.
        long threadId = messageRecord.getThreadId();
        notifyConversationListeners(threadId);
    }

    //Add emoji reaction to the database (method used by RECEIVER)
    public void setMessageReaction(String hashedId, String reaction, long threadId){
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(REACTION, reaction);
            contentValues.put(HASHED_ID, hashedId);

            String where = HASHED_ID + "= '" + hashedId + "'";
            int update = db.update(TABLE_NAME, contentValues, where, null);

            if(update == 0){
                db.insert(TABLE_NAME,null, contentValues);
            }

            db.setTransactionSuccessful();
        }catch(Exception e){
            Log.d(TAG, e.getMessage());
        }finally{
            db.endTransaction();
        }
        notifyConversationListeners(threadId);
    }

    public boolean deleteReaction(String hashedId, long threadId) {
        Log.i("EmojiReactionDatabase", "Deleting: " + hashedId);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String where = HASHED_ID + "='" + hashedId + "'";
        int success = db.delete(TABLE_NAME, where, null);
        Log.i("EmojiReactionDatabase", "Deleted: " + success + " rows where " + where);

        boolean threadDeleted = DatabaseFactory.getThreadDatabase(context).update(threadId, false);
        notifyConversationListeners(threadId);
        return threadDeleted;
    }
}
