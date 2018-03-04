package org.thoughtcrime.securesms.database.loaders;

import android.content.Context;
import android.database.Cursor;

import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.util.AbstractCursorLoader;

public class ConversationPinnedLoader extends AbstractCursorLoader {
    private final long    threadId;
    private       long    limit;

    public ConversationPinnedLoader(Context context, long threadId) {
        super(context);
        this.threadId = threadId;
        this.limit    = Long.MAX_VALUE;
    }

    @Override
    public Cursor getCursor() {
        return DatabaseFactory.getMmsSmsDatabase(context).getConversationPinned(threadId, limit);
    }
}