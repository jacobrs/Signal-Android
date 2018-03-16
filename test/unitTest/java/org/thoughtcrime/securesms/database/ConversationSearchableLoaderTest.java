package org.thoughtcrime.securesms.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.database.loaders.ConversationSearchableLoader;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.method;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DatabaseFactory.class, MmsSmsDatabase.class})
public class ConversationSearchableLoaderTest {

    ConversationSearchableLoader conversationSearchableLoader;
    Context mockContext;
    long threadId;
    Cursor mockCursor;
    MmsSmsDatabase mmsSmsDatabase;
    SQLiteOpenHelper mockSQLhelper;

    @Before
    public void setup(){

        mockStatic(DatabaseFactory.class);
        mockContext = mock(Context.class);
        mockCursor = mock(Cursor.class);

        mockSQLhelper = mock(SQLiteOpenHelper.class);
        mmsSmsDatabase = new MmsSmsDatabase(mockContext, mockSQLhelper);
        MmsSmsDatabase spyDb = spy(mmsSmsDatabase);
        threadId = 1111;

        conversationSearchableLoader = new ConversationSearchableLoader(mockContext, threadId);

        when(DatabaseFactory.getMmsSmsDatabase(mockContext)).thenReturn(spyDb);
        when(spyDb.getConversation(threadId)).thenReturn(mockCursor);

    }

    @Test
    public void testGetCursor(){

        Cursor testResult = conversationSearchableLoader.getCursor();
    }
}
