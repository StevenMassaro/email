package email.exception;

import email.model.Message;

import java.util.List;

/**
 * Thrown to indicate that some messages failed to download.
 */
public class SomeMessagesFailedToDownloadException extends Exception {
    private final List<Message> returnMessages;
    public SomeMessagesFailedToDownloadException(List<Message> returnMessages) {
        this.returnMessages = returnMessages;
    }

    public List<Message> getReturnMessages() {
        return returnMessages;
    }
}
