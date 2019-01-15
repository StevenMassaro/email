package email.service;

import email.mapper.BodyMapper;
import email.mapper.MessageMapper;
import email.model.Message;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
@Transactional
public class MessageService {

    @Autowired
    private ImapService imapService;

    @Autowired
    private MessageMapper messageMapper;

    public void insertMessage(long accountId, String subject, Date dateReceived, long recipientId, long fromId, long bodyId, boolean readInd) {
        messageMapper.insertMessage(accountId, subject, dateReceived, readInd, recipientId, fromId, bodyId, new Date());
    }

    public long count(long accountId, String subject, Date dateReceived){
        return messageMapper.count(accountId, subject, dateReceived);
    }

    public Message getByBodyId(long bodyId) {
        return messageMapper.getByBodyId(bodyId);
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

    public void setReadIndicator(long messageId, boolean readInd) {
        imapService.setReadIndicator();
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
