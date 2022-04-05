package email.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

@Getter
@Setter
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

        boolean stringContentType = false;
        for (ContentTypeEnum contentTypeEnum : ContentTypeEnum.getNonMultipartContentTypeEnums()) {
            if (StringUtils.containsIgnoreCase(contentType, contentTypeEnum.getImapContentType())) {
                stringContentType = true;
            }
        }

        if (stringContentType) {
            String x = bodyPart.getContent().toString();
            this.content = x.getBytes();
        } else {
            // note this does not properly handle the case where there is an attachment in AOL
            if (bodyPart.getContent() instanceof MimeMultipart) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ((MimeMultipart)bodyPart.getContent()).writeTo(baos);
                this.content = baos.toString().getBytes();
            } else {
                this.content = "Unable to process body part".getBytes();
            }
        }
    }

    @JsonIgnore
    public String getContentAsString() {
        return new String(content);
    }
}
