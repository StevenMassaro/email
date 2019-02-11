package email.service;

import email.mapper.BodyPartMapper;
import email.model.BodyPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BodyPartService {

    @Autowired
    private BodyPartMapper bodyPartMapper;

    public void insert(long messageId, long seqNum, String contentType, byte[] content) {
        bodyPartMapper.insert(messageId, seqNum, contentType, content);
    }

    public void insert(long messageId, List<BodyPart> bodyParts) {
        for (int seqNum = 0; seqNum < bodyParts.size(); seqNum++) {
            BodyPart bodyPart = bodyParts.get(seqNum);
            bodyPartMapper.insert(messageId, seqNum, bodyPart.getContentType(), bodyPart.getContent());
        }
    }
}
