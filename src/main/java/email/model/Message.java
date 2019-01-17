package email.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.mail.Flags;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Message {

    private long uid;
    private Account account;
    private String subject;
    @JsonFormat(pattern = "yyyy-MM-dd h:mm:ss a", timezone = "America/New_York")
    private Date dateReceived;
    private Date dateCreated;
    private List<Address> recipient;
    private List<Address> from;
    private Body body;
    private boolean readInd;

    public Message() {

    }

    public Message(javax.mail.Message message, long uid) throws MessagingException, IOException {
        this.subject = message.getSubject();
        this.dateReceived = message.getReceivedDate();
        this.body = new Body(message);
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

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public boolean isReadInd() {
        return readInd;
    }

    public void setReadInd(boolean readInd) {
        this.readInd = readInd;
    }
}
