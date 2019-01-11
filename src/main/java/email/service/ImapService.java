package email.service;

import email.model.Message;
import org.springframework.stereotype.Service;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class ImapService {

    public List<Message> getInboxMessages(String hostname, String username, String decryptedPassword) throws MessagingException, IOException {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect(hostname, username, decryptedPassword);

        Folder inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_ONLY);
        javax.mail.Message messages[] = inbox.getMessages();
        List<Message> returnMessages = new ArrayList<>();
        for(javax.mail.Message message : messages){
            returnMessages.add(new Message(message));
        }
//        FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.), false);
        store.close();
        return returnMessages;
    }
}
