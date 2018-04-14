package org.thoughtcrime.securesms.translation;

import android.os.AsyncTask;
import android.util.Log;

import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.util.JsonUtils;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TranslationTask extends AsyncTask<String, Void, TranslationResult> {
    private String API_KEY = "trnsl.1.1.20180414T123741Z.cb33892c63f8cdf4.7ad0218e05c0224d9231c1e35b7d121988a91aac";
    private OkHttpClient httpClient;
    private MessageRecord messageRecord;
    public TranslationResultHandler resultHandler;

    public TranslationTask(MessageRecord messageRecord) {
        this.httpClient = new OkHttpClient();
        this.messageRecord = messageRecord;
    }

    @Override
    protected TranslationResult doInBackground(String... strings) {
        TranslationResult result = null;
        try {
            result = getTranslationResult(strings[0], strings[1]);
        } catch (IOException e) {
            Log.e("Translation", "IOException");
        }
        return result;
    }

    @Override
    protected void onPostExecute(TranslationResult result) {
        resultHandler.processTranslationResult(result, messageRecord);
    }

    public TranslationTask(OkHttpClient client, MessageRecord messageRecord) {
        this.httpClient = client;
        this.messageRecord = messageRecord;
    }

    public TranslationResult getTranslationResult(String text, String language) throws IOException {
        Response response = this.translate(text, language);
        return JsonUtils.fromJson(response.body().string(), TranslationResult.class);
    }

    public Response translate(String text, String language) throws IOException {
        return this.translate(text, language, new Request.Builder());
    }

    public Response translate(String text, String language, Request.Builder requestBuilder) throws IOException {
        String url = String.format("https://translate.yandex.net/api/v1.5/tr.json/translate?key=%s&text=%s&lang=%s",
                API_KEY, URLEncoder.encode(text, "UTF-8"), language);
        Request request = requestBuilder.url(url).build();
        return httpClient.newCall(request).execute();
    }
}
