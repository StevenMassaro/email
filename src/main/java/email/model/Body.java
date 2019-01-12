package email.model;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.IOException;

public class Body {

    private long id;
    private String body;

    public Body() {

    }

    public Body(javax.mail.Message message) throws IOException, MessagingException {
        String bodyString = null;
        try {
            Multipart mp = (Multipart) message.getContent();
            // todo this index might be specific to a single email or to AOL, not sure yet
            Object bodyPart = null;
            try {
                bodyPart = mp.getBodyPart(1).getContent();
            } catch (ArrayIndexOutOfBoundsException e) {
                try {
                    bodyPart = mp.getBodyPart(0).getContent();
                } catch (ArrayIndexOutOfBoundsException e1) {
                    bodyPart = null;
                }
            }
            if (bodyPart != null) {
                bodyString = bodyPart.toString();
            }
//            System.out.println("GOOD: " + message.getContentType());
        } catch (ClassCastException e) {
//            System.out.println("BAD: " + message.getContentType());
            bodyString = (String) message.getContent();
        }
        this.body = bodyString;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
