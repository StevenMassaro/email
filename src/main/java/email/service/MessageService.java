package email.service;

import email.mapper.MessageMapper;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Component
@Transactional
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    public void insertMessage(long accountId, String subject, Date dateReceived, long recipientId, long fromId, long bodyId) {
        messageMapper.insertMessage(accountId, subject, dateReceived, recipientId, fromId, bodyId, new Date());
    }

    public long count(long accountId, String subject, Date dateReceived){
        return messageMapper.count(accountId, subject, dateReceived);
    }
}
