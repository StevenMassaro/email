package email.service;

import com.sun.mail.imap.IMAPFolder;
import email.model.Account;
import email.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MessageService messageService;

    @Autowired
    private AccountService accountService;

    public List<Message> getInboxMessages(String hostname, long port, String username, String decryptedPassword) throws MessagingException, IOException {
        Store store = getStore(hostname, port, username, decryptedPassword);

        IMAPFolder inbox = openFolder(store, Folder.READ_ONLY);

        javax.mail.Message messages[] = inbox.getMessages();
        List<Message> returnMessages = new ArrayList<>();
        for(javax.mail.Message message : messages){
            long uid = inbox.getUID(message);
            returnMessages.add(new Message(message, uid));
        }
        store.close();
        return returnMessages;
    }

    public void setReadIndicator(long messageUid, boolean readInd) throws MessagingException {
        Message message = messageService.get(messageUid);
        Account account = accountService.getDecrypted(message.getAccount().getId());
        Store store = getStore(account.getDomain().getHostname(), account.getDomain().getPort(), account.getUsername(), account.getPassword());

        IMAPFolder imapFolder = openFolder(store, Folder.READ_WRITE);

        javax.mail.Message readMessage = imapFolder.getMessageByUID(messageUid);
        imapFolder.setFlags(new javax.mail.Message[]{readMessage}, new Flags(Flags.Flag.SEEN), readInd);

        store.close();
    }

    private Store getStore(String hostname, long port, String username, String decryptedPassword) throws MessagingException {
        int p = Integer.valueOf(Long.toString(port));

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect(hostname, p, username, decryptedPassword);
        return store;
    }

    private IMAPFolder openFolder(Store store, int mode) throws MessagingException {
        IMAPFolder imapFolder = (IMAPFolder) store.getFolder("Inbox");
        imapFolder.open(mode);
        return imapFolder;
    }
}
