package email.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sun.mail.imap.IMAPMessage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;

import javax.activation.DataSource;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static email.model.ProviderEnum.AOL;

public class Message {

    private long id;
    private long uid;
    private Account account;
    private String subject;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/New_York")
    private Date dateReceived;
    private Date dateCreated;
    private List<Address> recipient;
    private String fromAddress;
    private String fromPersonal;
    private List<BodyPart> bodyParts = new ArrayList<>();
    private boolean readInd;
    private List<Attachment> attachments = new ArrayList<>();

    public Message() {

    }

    public Message(javax.mail.Message message, long uid, boolean alreadyExists, String username) throws Exception {
        // only care to parse out the parts of the email for emails that are not in the database already
        // parsing is an expensive IMAP operation
        if (!alreadyExists) {
            // there is some bug in the MimeMessageParser where it cannot handle an AOL email with an attachment, not
            // sure what's up, but these need to be handled differently
            if (!StringUtils.containsIgnoreCase(username, AOL.toString())) {
                MimeMessageParser mimeMessageParser = new MimeMessageParser((MimeMessage) message);
                mimeMessageParser.parse();
                setAttachments(mimeMessageParser);
                setBodyParts(mimeMessageParser);
            } else {
                setBodyParts(message);
            }

            this.subject = message.getSubject();
            this.dateReceived = message.getReceivedDate();
            InternetAddress sender = (InternetAddress) ((IMAPMessage) message).getSender();
            this.fromAddress = sender.getAddress();
            this.fromPersonal = sender.getPersonal();
        }

        this.uid = uid;
        this.readInd = determineReadInd(message);
    }

    private boolean determineReadInd(javax.mail.Message message) throws MessagingException {
//        long start = System.nanoTime();
        Flags flags = message.getFlags();
        Flags.Flag[] systemflags = flags.getSystemFlags();
//        String[] userflags = flags.getUserFlags();
//        long end = System.nanoTime();
//        System.out.println("Time to get flags: " + (end - start));
        boolean readInd = false;

        try {
            // todo, this is not very robust, if there is more than one flag
            readInd = systemflags[0].equals(Flags.Flag.SEEN);
        } catch (Exception e) {
//            readInd would be false in this case,because there is no system flag saying that it is seen.
        }
        return readInd;
    }

    @Override
    public boolean equals(Object message) {
        if (getClass() != message.getClass()) {
            return false;
        }

        Message message1 = (Message) message;
        return this.uid == message1.getUid();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(Date dateReceived) {
        this.dateReceived = dateReceived;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public List<Address> getRecipient() {
        return recipient;
    }

    public void setRecipient(List<Address> recipient) {
        this.recipient = recipient;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getFromPersonal() {
        return fromPersonal;
    }

    public void setFromPersonal(String fromPersonal) {
        this.fromPersonal = fromPersonal;
    }

    public List<BodyPart> getBodyParts() {
        return bodyParts;
    }

    public void setBodyParts(List<BodyPart> bodyParts) {
        this.bodyParts = bodyParts;
    }

    public void setBodyParts(javax.mail.Message message) throws IOException, MessagingException {
        Object content = message.getContent();
        String contentType = message.getContentType();
        if (content instanceof Multipart) {
            Multipart mp = (Multipart) content;

            boolean moreBodyParts = true;
            int index = 0;
            while (moreBodyParts) {
                try {
                    bodyParts.add(new BodyPart(index, mp.getBodyPart(index)));
                    index++;
                } catch (IndexOutOfBoundsException e) {
                    moreBodyParts = false;
                }
            }
        } else if (content instanceof String) {
            bodyParts.add(new BodyPart(0, contentType, ((String) content).getBytes()));
        }
    }

    public void setBodyParts(MimeMessageParser mimeMessageParser) {
        String plain = mimeMessageParser.getPlainContent();
        if (StringUtils.isNotEmpty(plain)) {
            bodyParts.add(new BodyPart(0, ContentTypeEnum.TEXT_PLAIN.getImapContentType(), plain.getBytes(StandardCharsets.UTF_8)));
        }

        String html = mimeMessageParser.getHtmlContent();
        if (StringUtils.isNotEmpty(html)) {
            bodyParts.add(new BodyPart(1, ContentTypeEnum.TEXT_HTML.getImapContentType(), html.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public void setAttachments(MimeMessageParser mimeMessageParser) throws IOException {
        List<DataSource> attachments = mimeMessageParser.getAttachmentList();
        for (DataSource attachment : attachments) {
            String name = attachment.getName();
            String contentType = attachment.getContentType();
            byte[] file = IOUtils.toByteArray(attachment.getInputStream());
            if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(contentType) && file != null) {
                this.attachments.add(new Attachment(attachment.getName(), attachment.getContentType(), IOUtils.toByteArray(attachment.getInputStream())));
            }
        }
    }

    public boolean isReadInd() {
        return readInd;
    }

    public void setReadInd(boolean readInd) {
        this.readInd = readInd;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
