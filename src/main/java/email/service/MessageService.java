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

    @Deprecated
    public Message getByUid(long uid) {
        return messageMapper.getByUid(uid);
    }

    public Message get(long id) {
        return messageMapper.get(id);
    }

    public void insertMessage(Message message) {
        long messageId = messageMapper.insertMessage(message.getUid(), message.getAccount().getId(), message.getSubject(), message.getDateReceived(),
                message.isReadInd(), 1L, message.getFromAddress(), message.getFromPersonal());
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

    public long delete(long id) {
        bodyPartService.delete(id);
        return messageMapper.deleteById(id);
    }

    public void setReadIndicator(long id, boolean readInd) throws MessagingException {
        imapService.setReadIndicator(id, readInd);
        messageMapper.setReadIndicator(id, readInd);
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
