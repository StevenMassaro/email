package email.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.mail.imap.IMAPMessage;
import email.service.MessageService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
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

@Getter
@Setter
@Log4j2
public class Message {

    private long id;
    private long uid;
    private Account account;
    private String subject;
    private String originalDateReceived;
    private long dateReceived;
    private Date dateCreated;
    private List<Address> recipient;
    private String fromAddress;
    private String fromPersonal;
    @JsonIgnore
    private List<BodyPart> bodyParts = new ArrayList<>();
    private boolean readInd;
    private List<Attachment> attachments = new ArrayList<>();

    public Message() {

    }

    public Message(javax.mail.Message message, long uid, boolean alreadyExists) throws Exception {
        // only care to parse out the parts of the email for emails that are not in the database already
        // parsing is an expensive IMAP operation
        if (!alreadyExists) {
            try {
                MimeMessageParser mimeMessageParser = new MimeMessageParser((MimeMessage) message);
                mimeMessageParser.parse();
                setAttachments(mimeMessageParser);
                setBodyParts(mimeMessageParser);
            } catch (Exception e) {
                log.warn("Failed to parse message using commons email parser, resorting to fallback method (which is less mature)", e);
                setBodyParts(message);
            }

            this.id = MessageService.messageIdSequence.incrementAndGet();
            this.subject = message.getSubject();
            this.dateReceived = getReceivedDate(message).getTime();
            this.originalDateReceived = getOriginalDateReceived(message);
            InternetAddress sender = (InternetAddress) ((IMAPMessage) message).getSender();
            this.fromAddress = sender.getAddress();
            this.fromPersonal = sender.getPersonal();
        }

        this.uid = uid;
        this.readInd = determineReadInd(message);
    }

    public static Date getReceivedDate(javax.mail.Message message) throws MessagingException {
        try {
            String[] dates = message.getHeader("Date");
            return DateUtils.parseDate(dates[0], ProviderEnum.getAllDateFormats());
        } catch (Exception e) {
            return message.getReceivedDate();
        }
    }

    public static String getOriginalDateReceived(javax.mail.Message message) throws MessagingException {
        try {
            return message.getHeader("Date")[0];
        } catch (Exception e) {
            return message.getReceivedDate().toString();
        }
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
        boolean matches = this.uid == message1.getUid();
        if (this.account != null && message1.account != null) {
            return matches && this.account.equals(message1.account);
        } else {
            return matches;
        }
    }

    public void setBodyParts(javax.mail.Message message) throws IOException, MessagingException {
        Object content = message.getContent();
        String contentType = message.getContentType();
        if (content instanceof Multipart) {
            Multipart mp = (Multipart) content;

            for (int i = 0; i < mp.getCount(); i++) {
                try {
                    bodyParts.add(new BodyPart(i, mp.getBodyPart(i)));
                } catch (Exception e) {
                    log.warn("Failed to get body part from message", e);
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
}
