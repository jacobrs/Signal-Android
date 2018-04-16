package org.thoughtcrime.securesms.translation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslationResult {
    @JsonProperty
    private int code;

    @JsonProperty
    private String lang;

    @JsonProperty
    private String[] text;

    public int getCode() {
        return code;
    }

    public String getLanguage() {
        return lang;
    }

    public String[] getTranslation() { return text; }
}
