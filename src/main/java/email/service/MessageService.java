package email.service;

import email.model.Message;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    private static final Set<Message> messageList = new HashSet<>();
    private static final ConcurrentHashMap<UUID, Long> highWaterMarks = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> uidValidities = new ConcurrentHashMap<>();

    public Message get(long id) {
        return messageList.stream().filter(m -> m.getId() == id).findFirst().orElse(null);
    }

    public void insertMessage(Message message) {
        messageList.add(message);
    }

    public Set<Message> list() {
        return Collections.unmodifiableSet(messageList);
    }

    public List<Message> list(UUID accountBitwardenId) {
        return Collections.unmodifiableList(messageList.stream().filter(m -> m.getAccountBitwardenId().equals(accountBitwardenId)).collect(Collectors.toList()));
    }

    public void delete(long id) {
        messageList.removeIf(m -> m.getId() == id);
    }

    public void setReadIndicator(long id, boolean readInd) {
        messageList.stream().filter(m -> m.getId() == id).forEach(m -> m.setReadInd(readInd));
    }

    public long getHighWaterMark(UUID accountBitwardenId) {
        return highWaterMarks.getOrDefault(accountBitwardenId, 0L);
    }

    public void setHighWaterMark(UUID accountBitwardenId, long uid) {
        highWaterMarks.merge(accountBitwardenId, uid, Long::max);
    }

    public long getUidValidity(UUID accountBitwardenId) {
        return uidValidities.getOrDefault(accountBitwardenId, -1L);
    }

    public void setUidValidity(UUID accountBitwardenId, long uidValidity) {
        uidValidities.put(accountBitwardenId, uidValidity);
    }
}
