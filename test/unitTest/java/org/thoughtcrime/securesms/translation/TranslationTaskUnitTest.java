package org.thoughtcrime.securesms.translation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.BaseUnitTest;
import org.thoughtcrime.securesms.database.model.MessageRecord;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ OkHttpClient.class, Request.class, Response.class, Call.class })
public class TranslationTaskUnitTest extends BaseUnitTest {
    @Test
    public void testTranslation() {
        OkHttpClient client = mock(OkHttpClient.class);
        MessageRecord message = mock(MessageRecord.class);
        Request.Builder requestBuilder = mock(Request.Builder.class);
        Call clientCalled = mock(Call.class);
        Request builtRequest = mock(Request.class);
        Response fakeResponse = mock(Response.class);

        when(requestBuilder.url(Mockito.anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(builtRequest);
        when(client.newCall(builtRequest)).thenReturn(clientCalled);

        TranslationTask task = new TranslationTask(client, message);

        try {
            when(clientCalled.execute()).thenReturn(fakeResponse);
            Response test = task.translate("test", "en", requestBuilder);
            assertEquals(test, fakeResponse);
        } catch (IOException e) {
            fail("Failed to translate");
        }
    }

    @Test
    public void failBecauseOfIOException() {
        OkHttpClient client = mock(OkHttpClient.class);
        MessageRecord message = mock(MessageRecord.class);
        Request.Builder requestBuilder = mock(Request.Builder.class);
        Call clientCalled = mock(Call.class);
        Request builtRequest = mock(Request.class);

        when(requestBuilder.url(Mockito.anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(builtRequest);
        when(client.newCall(builtRequest)).thenReturn(clientCalled);

        TranslationTask task = new TranslationTask(client, message);

        try {
            when(clientCalled.execute()).thenThrow(new IOException("Testing"));
            task.translate("test", "en", requestBuilder);
            fail("Should have handled an IO Exception");
        } catch (IOException e) {
            // Passes
            return;
        }
    }
}
