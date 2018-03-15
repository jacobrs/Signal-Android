package org.thoughtcrime.securesms.database.loaders;

import android.content.Context;
import android.database.Cursor;

import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.util.AbstractCursorLoader;

public class ConversationSearchResultLoader extends AbstractCursorLoader {

    private final long    threadId;
    private       String  searchTerm;

    public ConversationSearchResultLoader(Context context, long threadId, String searchTerm) {
        super(context);
        this.threadId   = threadId;
        this.searchTerm = searchTerm;
    }

    @Override
    public Cursor getCursor() {
        return DatabaseFactory.getMmsSmsDatabase(context).getConversationSearched(threadId, searchTerm);
    }

}
