package email.service;

import com.sun.mail.imap.IMAPFolder;
import email.model.Account;
import email.model.Message;
import email.processor.EmailSyncProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Service
public class ImapService {

    private Logger logger = LoggerFactory.getLogger(ImapService.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private AccountService accountService;

    public List<Message> getInboxMessages(String hostname, long port, String username, String decryptedPassword, List<Message> existingMessages) throws MessagingException, IOException {
        Store store = getStore(hostname, port, username, decryptedPassword);

        IMAPFolder inbox = openInbox(store, Folder.READ_ONLY);

        javax.mail.Message[] messages = inbox.getMessages();
        List<Message> returnMessages = new ArrayList<>();
        for (int i = 0; i < messages.length; i++) {
            javax.mail.Message message = messages[i];
            long uid = inbox.getUID(message);

            boolean messageAlreadyDownloaded = false;
            for (Message existingMessage : existingMessages) {
                if (existingMessage.getUid() == uid) {
                    messageAlreadyDownloaded = true;
                }
            }

            logger.debug(String.format("Processing email %s of %s for %s", i, messages.length, username));
            returnMessages.add(new Message(message, uid, messageAlreadyDownloaded));
        }
        store.close();
        return returnMessages;
    }

    public List<Message> getInboxMessages(String hostname, long port, String username, String decryptedPassword) throws MessagingException, IOException {
        return getInboxMessages(hostname, port, username, decryptedPassword, Collections.emptyList());
    }

    @Async
    public void setReadIndicator(long id, boolean readInd) throws MessagingException {
        Message message = messageService.get(id);
        Store store = getStore(message);

        IMAPFolder imapFolder = openInbox(store, Folder.READ_WRITE);

        javax.mail.Message readMessage = imapFolder.getMessageByUID(message.getUid());
        imapFolder.setFlags(new javax.mail.Message[]{readMessage}, new Flags(Flags.Flag.SEEN), readInd);

        store.close();
    }

    public void deleteMessage(long id) throws Exception {
        Message message = messageService.get(id);
        Store store = getStore(message);

        String hostname = message.getAccount().getHostname();

        String trashFolderName;
        if (hostname.contains("gmail")) {
            trashFolderName = "[Gmail]/Trash";
        } else if (hostname.contains("aol")) {
            trashFolderName = "Trash";
        } else {
            throw new Exception("Unknown domain.");
        }

        IMAPFolder inbox = openInbox(store, Folder.READ_WRITE);
        IMAPFolder trash = openFolder(store, Folder.READ_WRITE, trashFolderName);

        javax.mail.Message readMessage = inbox.getMessageByUID(message.getUid());
        inbox.moveMessages(new javax.mail.Message[]{readMessage}, trash);

        store.close();
    }

    private Store getStore(Message message) throws MessagingException {
        Account account = accountService.getDecrypted(message.getAccount().getId());
        return getStore(account.getHostname(), account.getPort(), account.getUsername(), account.getPassword());
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

    private IMAPFolder openFolder(Store store, int mode, String folder) throws MessagingException {
        IMAPFolder imapFolder = (IMAPFolder) store.getFolder(folder);
        imapFolder.open(mode);
        return imapFolder;
    }

    private IMAPFolder openInbox(Store store, int mode) throws MessagingException {
        return openFolder(store, mode, "Inbox");
    }
}
