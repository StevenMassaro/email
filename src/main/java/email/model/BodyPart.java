package email.model;

import org.apache.commons.io.IOUtils;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Arrays;

public class BodyPart {

    private long seqNum;
    private String contentType;
    private byte[] content;

    public BodyPart() {
    }

    public BodyPart(long seqNum, String contentType, byte[] content) {
        this.seqNum = seqNum;
        this.contentType = contentType;
        this.content = content;
    }

    public BodyPart(long seqNum, javax.mail.BodyPart bodyPart) throws MessagingException, IOException {
        this.seqNum = seqNum;
        this.contentType = bodyPart.getContentType();
        if (contentType.contains("text/html") || contentType.contains("text/plain") || contentType.contains("alternative")) {
            String x = bodyPart.getContent().toString();
            this.content = x.getBytes();
        } else {
            // todo, uncomment this when I figure out how to handle attachments
//            this.content = IOUtils.toByteArray(bodyPart.getInputStream());
        }
    }

    public long getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(long seqNum) {
        this.seqNum = seqNum;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public String getContentAsString() {
        return new String(content);
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
