package email.model;

import email.service.MessageService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Attachment {

    private long id;
    private long messageId;
    private long seqNum;
    private String name;
    private String contentType;
    private byte[] file;

    public Attachment(String name, String contentType, byte[] file) {
        this.id = MessageService.attachmentIdSequence.incrementAndGet();
        this.name = name;
        this.contentType = contentType;
        this.file = file;
    }
}
