package email.service;

import email.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    public void insertMessage(long accountId, String subject, Date dateReceived, long recipientId, long fromId, long bodyId) {
        messageMapper.insertMessage(accountId, subject, dateReceived, recipientId, fromId, bodyId, new Date());
    }
}
