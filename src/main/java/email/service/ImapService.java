package email.service;

import org.springframework.stereotype.Service;

import javax.mail.*;
import java.util.Properties;

@Service
public class ImapService {

    public Message[] getInboxMessages(String hostname, String username, String decryptedPassword) throws MessagingException {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect(hostname, username, decryptedPassword);

        Folder inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_ONLY);
        //Message messages[] = inbox.getMessages();
//        FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.), false);
        return inbox.getMessages();
    }
}
