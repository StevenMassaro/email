package email.service;

import email.mapper.BodyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.IOException;

@Service
public class BodyService {

    @Autowired
    private BodyMapper bodyMapper;

    public long insert(Message message) throws IOException, MessagingException {
        String bodyString = null;
        try {
            Multipart mp = (Multipart) message.getContent();
            // todo this index might be specific to a single email or to AOL, not sure yet
            Object bodyPart = mp.getBodyPart(1).getContent();
            bodyString = bodyPart.toString();
//            System.out.println("GOOD: " + message.getContentType());
        } catch (ClassCastException e) {
//            System.out.println("BAD: " + message.getContentType());
            bodyString = (String) message.getContent();
        }

        if (bodyString != null) {
            return bodyMapper.insert(bodyString);
        } else {
            return 0;
        }
    }

    public String get(long bodyId) {
        return bodyMapper.get(bodyId);
    }
}
