package email.service;

import email.mapper.BodyMapper;
import email.model.Body;
import email.model.Message;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class BodyService {

    @Autowired
    private BodyMapper bodyMapper;

    public long insert(Message message) throws IOException, MessagingException {
        String body = message.getBody().getBody();

        if (!StringUtils.isEmpty(body)) {
            return bodyMapper.insert(body);
        } else {
            return 0;
        }
    }

    public long delete(List<Message> messages) {
        return bodyMapper.delete(messages);
    }

    public String get(long bodyId) {
        return bodyMapper.get(bodyId);
    }
}
