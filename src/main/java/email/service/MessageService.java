package email.service;

import email.mapper.MessageMapper;
import email.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.util.Date;
import java.util.List;

@Component
@Transactional
public class MessageService {

    @Autowired
    private ImapService imapService;

    @Autowired
    private BodyPartService bodyPartService;

    @Autowired
    private MessageMapper messageMapper;

    public Message get(long uid) {
        return messageMapper.get(uid);
    }

    public void insertMessage(Message message) {
        long messageId = messageMapper.insertMessage(message.getUid(), message.getAccount().getId(), message.getSubject(), message.getDateReceived(),
                message.isReadInd(), 1L, 1L);
        bodyPartService.insert(messageId, message.getBodyParts());
    }

    public long count(long accountId, String subject, Date dateReceived){
        return messageMapper.count(accountId, subject, dateReceived);
    }

    public List<Message> list() {
        return messageMapper.listAll();
    }

    public List<Message> list(long accountId) {
        return messageMapper.list(accountId);
    }

    public long delete(List<Message> messages) {
        return messageMapper.delete(messages);
    }

    public long delete(long uid) {
        return messageMapper.deleteById(uid);
    }

    public void setReadIndicator(long messageId, boolean readInd) throws MessagingException {
        imapService.setReadIndicator(messageId, readInd);
        messageMapper.setReadIndicator(messageId, readInd);
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
