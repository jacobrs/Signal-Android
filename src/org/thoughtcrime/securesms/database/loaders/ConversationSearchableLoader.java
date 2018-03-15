package org.thoughtcrime.securesms.database.loaders;

import android.content.Context;
import android.database.Cursor;

import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.util.AbstractCursorLoader;

public class ConversationSearchableLoader extends AbstractCursorLoader {

    private final long    threadId;

    public ConversationSearchableLoader(Context context, long threadId) {
        super(context);
        this.threadId   = threadId;
    }

    @Override
    public Cursor getCursor() {
        return DatabaseFactory.getMmsSmsDatabase(context).getConversation(threadId);
    }

}
