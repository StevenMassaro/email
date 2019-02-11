package email.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message {

    private long id;
    private long uid;
    private Account account;
    private String subject;
    @JsonFormat(pattern = "yyyy-MM-dd h:mm:ss a", timezone = "America/New_York")
    private Date dateReceived;
    private Date dateCreated;
    private List<Address> recipient;
    private List<Address> from;
    private List<BodyPart> bodyParts = new ArrayList<>();
    private boolean readInd;

    public Message() {

    }

    public Message(javax.mail.Message message, long uid) throws MessagingException, IOException {
        this.subject = message.getSubject();
        this.dateReceived = message.getReceivedDate();
        setBodyParts(message);
        this.readInd = determineReadInd(message);
        this.uid = uid;
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
        return this.subject.equals(message1.getSubject()) &&
                this.dateReceived.equals(message1.getDateReceived());
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

    public List<Address> getFrom() {
        return from;
    }

    public void setFrom(List<Address> from) {
        this.from = from;
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

    public boolean isReadInd() {
        return readInd;
    }

    public void setReadInd(boolean readInd) {
        this.readInd = readInd;
    }
}
