package email.service;

import email.model.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class MessageService {

    /**
     * Sequence generator for generating unique IDs for messages.
     */
    public static final AtomicLong messageIdSequence = new AtomicLong(0);
    /**
     * Sequence generator for generating unique IDs for attachments.
     */
    public static final AtomicLong attachmentIdSequence = new AtomicLong(0);

    private static final List<Message> messageList = new ArrayList<>();

    public Message get(long id) {
        return messageList.stream().filter(m -> m.getId() == id).findFirst().orElse(null);
    }

    public void insertMessage(Message message) {
        messageList.add(message);
    }

    public List<Message> list() {
        return Collections.unmodifiableList(messageList);
    }

    public List<Message> list(long accountId) {
        return Collections.unmodifiableList(messageList.stream().filter(m -> m.getAccount().getId() == accountId).collect(Collectors.toList()));
    }

    public void delete(long id) {
        messageList.removeIf(m -> m.getId() == id);
    }

    public void setReadIndicator(long id, boolean readInd) {
        messageList.stream().filter(m -> m.getId() == id).forEach(m -> m.setReadInd(readInd));
    }

    public static Message findMatch(List<Message> messages, Message key) {
        for (Message message : messages) {
            if (message.equals(key)) {
                return message;
            }
        }
        return null;
    }
}
