package org.thoughtcrime.securesms.translation;

import org.thoughtcrime.securesms.database.model.MessageRecord;

public interface TranslationResultHandler {
    void processTranslationResult(TranslationResult result, final MessageRecord messageRecord);
}
