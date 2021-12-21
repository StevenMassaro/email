package email.model;

import email.service.MessageService;

public class Attachment {

    private long id;
    private long messageId;
    private long seqNum;
    private String name;
    private String contentType;
    private byte[] file;

    public Attachment() {
    }

    public Attachment(String name, String contentType, byte[] file) {
        this.id = MessageService.attachmentIdSequence.incrementAndGet();
        this.name = name;
        this.contentType = contentType;
        this.file = file;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(long seqNum) {
        this.seqNum = seqNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }
}
