package email.service;

import com.google.common.cache.*;
import com.sun.mail.imap.IMAPFolder;
import email.exception.SomeMessagesFailedToDownloadException;
import email.model.Account;
import email.model.Message;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

@Service
@Log4j2
public class ImapService {

    private final MessageService messageService;
    private final BitwardenService bitwardenService;
    Cache<String, Store> storeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .removalListener(new RemovalListener<String, Store>() {
                @Override
                public void onRemoval(RemovalNotification<String, Store> notification) {
                    try {
                        if (notification.getValue() != null) {
                            log.debug("Closing store for {}", notification.getKey());
                            notification.getValue().close();
                        }
                    } catch (MessagingException e) {
                        log.warn("Failed to close expired store for {}", notification.getKey(), e);
                    }
                }
            })
            .build();

    public ImapService(MessageService messageService, BitwardenService bitwardenService) {
        this.messageService = messageService;
        this.bitwardenService = bitwardenService;
    }

    public List<Message> getInboxMessages(String hostname, long port, String username, String decryptedPassword, List<Message> existingMessages) throws Exception {
        Store store = getStore(hostname, port, username, decryptedPassword);

        try (IMAPFolder inbox = openInbox(store, Folder.READ_ONLY)) {
            javax.mail.Message[] messages = inbox.getMessages();
            List<Message> returnMessages = new ArrayList<>();
            boolean allSuccess = true;
            for (int i = 0; i < messages.length; i++) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                int finalI = i;
                Future<Void> messageProcessingFuture = executor.submit(() -> {
                    javax.mail.Message message = messages[finalI];
                    long uid = inbox.getUID(message);

                    boolean messageAlreadyDownloaded = false;
                    for (Message existingMessage : existingMessages) {
                        if (existingMessage.getUid() == uid) {
                            messageAlreadyDownloaded = true;
                        }
                    }

                    log.debug("Processing email {} of {} for {}", finalI + 1, messages.length, username);
                    returnMessages.add(new Message(message, uid, messageAlreadyDownloaded));
                    return null; // this is useless, but is here to make this a callable, so that we can throw exceptions
                });

                try {
                    messageProcessingFuture.get(60, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    log.warn("Ran out of time while processing message {} of {} for {}", finalI + 1, messages.length, username, e);
                    messageProcessingFuture.cancel(true);
                    allSuccess = false;
                }
            }
            if (!allSuccess) {
                throw new SomeMessagesFailedToDownloadException(returnMessages);
            } else {
                return returnMessages;
            }
        }
    }

    @Async
    public void setReadIndicator(long id, boolean readInd) throws Exception {
        log.debug("Marking email {} as read ind {}", id, readInd);
        Message message = messageService.get(id);
        Store store = getStore(message);

        try (IMAPFolder imapFolder = openInbox(store, Folder.READ_WRITE)) {
            javax.mail.Message readMessage = imapFolder.getMessageByUID(message.getUid());
            imapFolder.setFlags(new javax.mail.Message[]{readMessage}, new Flags(Flags.Flag.SEEN), readInd);
        }
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

        try (IMAPFolder inbox = openInbox(store, Folder.READ_WRITE);
             IMAPFolder trash = openFolder(store, Folder.READ_WRITE, trashFolderName)) {
            javax.mail.Message readMessage = inbox.getMessageByUID(message.getUid());
            inbox.moveMessages(new javax.mail.Message[]{readMessage}, trash);
        }
    }

    private Store getStore(Message message) throws Exception {
        Account account = message.getAccount();
        String password = bitwardenService.getPasswordFromCache(account.getBitwardenItemId());
        return getStore(account.getHostname(), account.getPort(), account.getUsername(), password);
    }

    private Store getStore(String hostname, long port, String username, String decryptedPassword) throws MessagingException {
        log.debug("Getting store for {} {}", hostname, username);
        Store cached = storeCache.getIfPresent(username);
        if (cached != null && cached.isConnected()) {
            return cached;
        }
        int p = Integer.parseInt(Long.toString(port));

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        /*
        Partial fetch is disabled because I was sometimes getting the following exception when attempting to download
        large/corrupted email attachments from AOL:

        com.sun.mail.util.DecodingException: BASE64Decoder: Error in encoded stream: needed 4 valid base64 characters but only got 2 before EOF

        This stackoverflow page suggested I disable this: https://stackoverflow.com/questions/1755414/javamail-baseencode64-error

        It seems to have had no effect.
         */
        if (StringUtils.containsIgnoreCase(hostname, "aol")) {
            props.setProperty("mail.imap.partialfetch", "false");
        }
        props.setProperty("mail.imap.connectionpoolsize", "10");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect(hostname, p, username, decryptedPassword);
        storeCache.put(username, store);
        log.debug("Connected to store for {} {}", hostname, username);
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
