package org.thoughtcrime.securesms;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.MmsDatabase;
import org.whispersystems.jobqueue.JobManager;

import static junit.framework.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseFactory.class)
public class MmsDatabaseTest2 {
	private static final String TAG = MmsDatabaseTest2.class.getSimpleName();

	long threadId = 1000;
	long messageId = 1;
	int pinnedStatus = 0;
	boolean isPinned = false;

	private String tableName;
	private String dbPinnedString;
	private String dbUnpinnedString;

	private String[] dbUpdateArgs = new String[2];

	private ContentValues testContents = new ContentValues();

	//Mocked Classes
	private SQLiteDatabase mockSqlDB = PowerMockito.mock(SQLiteDatabase.class);
	private SQLiteOpenHelper mockSqlHelper = PowerMockito.mock(SQLiteOpenHelper.class);
	private Context mockContext =  PowerMockito.mock(Context.class);
	private JobManager mockJobManager = PowerMockito.mock(JobManager.class);
	private ApplicationContext mockAC = PowerMockito.mock(ApplicationContext.class);


	@Before
	public void setup(){
			tableName = "mms";
			dbPinnedString = "thread_id = ? AND _id = ? AND pinned = 0";
			dbUnpinnedString = "thread_id = ? AND _id = ? AND pinned = 1";

			dbUpdateArgs[0] = String.valueOf(threadId);
			dbUpdateArgs[1] = String.valueOf(messageId);

			PowerMockito.when(mockSqlHelper.getWritableDatabase()).thenReturn(mockSqlDB);
			PowerMockito.when(ApplicationContext.getInstance(mockContext)).thenReturn(mockAC);
			PowerMockito.when(mockAC.getJobManager()).thenReturn(mockJobManager);

	}

	@After
	public void resetValues(){
			isPinned = false;
			pinnedStatus = 0;
	}

	@Test
	public void markMessageAsPinnedTest(){
			MmsDatabase mmsDatabase = new MmsDatabase(mockContext, mockSqlHelper);
			testContents.put("pinned", 0);

			PowerMockito.doAnswer((Answer) invocation -> {
					Object[] args = invocation.getArguments();

					//Verifying that the database update call was passed the right arguments
					assertEquals(tableName, args[0]);

					assert(args[1].getClass().isInstance(ContentValues.class));
					assertEquals(testContents.get("pinned"), ((ContentValues) args[1]).get("pinned"));

					assertEquals(dbPinnedString, args[2]);

					assert(args[3].getClass().isArray());
					assertEquals(dbUpdateArgs[0], ((String[])args[3])[0]);
					assertEquals(dbUpdateArgs[1], ((String[])args[3])[1]);

					markAsPinned();
					return null;
			}).when(mockSqlDB).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any(String[].class));


			try{
					assertEquals(false, isPinned);
					assertEquals(0,pinnedStatus);

					mmsDatabase.markMessagesAsPinned(threadId, messageId);

					PowerMockito.verifyPrivate(mmsDatabase).invoke("markMessageAsPinned", dbPinnedString, dbUpdateArgs);

					assertEquals(true, isPinned);
					assertEquals(1, pinnedStatus);

			}catch(Exception e){
					Log.d(TAG, e.getMessage());
			}
	}

	@Test
	public void markMessageAsUnpinnedTest(){
			MmsDatabase mmsDatabase = new MmsDatabase(mockContext, mockSqlHelper);
			testContents.put("pinned", 1);

			//Set beginning states
			isPinned = true;
			pinnedStatus = 1;

			PowerMockito.doAnswer((Answer) invocation -> {
					Object[] args = invocation.getArguments();

					//Verifying that the database update call was passed the right arguments
					assertEquals(tableName, args[0]);

					assert(args[1].getClass().isInstance(ContentValues.class));
					assertEquals(testContents.get("pinned"), ((ContentValues) args[1]).get("pinned"));

					assertEquals(dbUnpinnedString, args[2]);

					assert(args[3].getClass().isArray());
					assertEquals(dbUpdateArgs[0], ((String[])args[3])[0]);
					assertEquals(dbUpdateArgs[1], ((String[])args[3])[1]);

					markAsUnpinned();
					return null;
			}).when(mockSqlDB).update(Matchers.anyString(), Matchers.any(ContentValues.class), Matchers.anyString(), Matchers.any(String[].class));


			try{
					assertEquals(true, isPinned);
					assertEquals(1, pinnedStatus);

					mmsDatabase.markMessagesAsUnpinned(threadId, messageId);

					PowerMockito.verifyPrivate(mmsDatabase).invoke("markMessageAsUnpinned", dbPinnedString, dbUpdateArgs);

					assertEquals(false, isPinned);
					assertEquals(0, pinnedStatus);

			}catch(Exception e){
					Log.d(TAG, e.getMessage());
			}
	}

	private void markAsPinned(){
			if(!isPinned){
					isPinned = true;
					pinnedStatus = 1;
			}
	}

		private void markAsUnpinned(){
				if(isPinned){
						isPinned = false;
						pinnedStatus = 0;
				}
		}
}
