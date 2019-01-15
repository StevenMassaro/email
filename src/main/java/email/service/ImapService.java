package email.service;

import com.sun.mail.imap.IMAPFolder;
import email.model.Message;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class ImapService {

    public List<Message> getInboxMessages(String hostname, long port, String username, String decryptedPassword) throws MessagingException, IOException {
        Store store = getStore(hostname, port, username, decryptedPassword);

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

    public void setReadIndicator(String hostname, long port, String username, String decryptedPassword) throws NoSuchProviderException {
        Store store = getStore(hostname, port, username, decryptedPassword);

        Folder inbox = store.getFolder("Inbox");
        IMAPFolder imapFolder = new IMAPFolder();
        imapFolder.getUID()
        inbox.open(Folder.READ_WRITE);
        inbox.setFlags();
    }

    private Store getStore(String hostname, long port, String username, String decryptedPassword) throws NoSuchProviderException {
        int p = Integer.valueOf(Long.toString(port));

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect(hostname, p, username, decryptedPassword);
    }
}
