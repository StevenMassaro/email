package email.model;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Message {

    private long id;
    private Account account;
    private String subject;
    private Date dateReceived;
    private Date datecreated;
    private List<Address> recipient;
    private List<Address> from;
    private Body body;

    public Message(javax.mail.Message message) throws MessagingException, IOException {
        this.subject = message.getSubject();
        this.dateReceived = message.getReceivedDate();
        this.body = new Body(message);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public Date getDatecreated() {
        return datecreated;
    }

    public void setDatecreated(Date datecreated) {
        this.datecreated = datecreated;
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
}
