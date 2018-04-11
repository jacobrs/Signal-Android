package org.thoughtcrime.securesms.sms;


import org.thoughtcrime.securesms.recipients.Recipient;

public class OutgoingEmojiReactionMessage extends OutgoingTextMessage {
    private final String        hashedId;
    private final String        emoji;

    public OutgoingEmojiReactionMessage(Recipient recipient, String message, int subscriptionId, String hashedId, String emoji) {
        super(recipient, message, subscriptionId);
        this.hashedId = hashedId;
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getHashedId() {
        return hashedId;
    }
}
