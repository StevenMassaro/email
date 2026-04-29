package email.service;

import com.google.common.cache.*;
import com.sun.mail.imap.IMAPFolder;
import email.exception.SomeMessagesFailedToDownloadException;
import email.model.DestinationEnum;
import email.model.Message;
import email.model.bitwarden.Item;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.activation.DataSource;
import javax.mail.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@Log4j2
public class ImapService {

    private final MessageService messageService;
    private final BitwardenService bitwardenService;
    private final int messageProcessingTimeoutSeconds;
    private final Cache<String, Store> storeCache;
    private final boolean obfuscateAmazonOrderSubject;

    public ImapService(MessageService messageService,
                       BitwardenService bitwardenService,
                       @Value("${messageProcessingTimeoutSeconds:60}") int messageProcessingTimeoutSeconds,
                       @Value("${closeStoreWhenCacheExpires:true}") boolean shouldCloseStoreWhenCacheExpires,
                       @Value("${obfuscateAmazonOrderSubject:false}") boolean obfuscateAmazonOrderSubject) {
        this.messageService = messageService;
        this.bitwardenService = bitwardenService;
        this.messageProcessingTimeoutSeconds = messageProcessingTimeoutSeconds;
        this.obfuscateAmazonOrderSubject = obfuscateAmazonOrderSubject;
        storeCache = CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .removalListener((RemovalListener<String, Store>) notification -> {
                    if (shouldCloseStoreWhenCacheExpires) {
                        try {
                            if (notification.getValue() != null) {
                                log.debug("{} - Closing expired store", notification.getKey());
                                notification.getValue().close();
                            }
                        } catch (MessagingException e) {
                            log.warn("{} - Failed to close expired store", notification.getKey(), e);
                        }
                    } else {
                        log.trace("{} - Not closing expired store because closeStoreWhenCacheExpires is false", notification.getKey());
                    }

                })
                .build();
    }

    public List<Message> getInboxMessages(String hostname, int port, String username, String decryptedPassword, List<Message> existingMessages, UUID accountBitwardenId) throws Exception {
        Store store = getStore(hostname, port, username, decryptedPassword, false);
        long highWaterMark = messageService.getHighWaterMark(accountBitwardenId);

        try (IMAPFolder inbox = openInbox(store, Folder.READ_ONLY)) {
            // check UID validity — if it changed, the high water mark is stale
            long uidValidity = inbox.getUIDValidity();
            if (uidValidity != messageService.getUidValidity(accountBitwardenId)) {
                log.info("{} - UID validity changed (was {}, now {}), resetting high water mark",
                        username, messageService.getUidValidity(accountBitwardenId), uidValidity);
                messageService.setHighWaterMark(accountBitwardenId, 0);
                messageService.setUidValidity(accountBitwardenId, uidValidity);
                highWaterMark = 0;
            }

            javax.mail.Message[] messages = inbox.getMessages();
            List<Message> returnMessages = Collections.synchronizedList(new ArrayList<>());
            Set<Long> existingUids = new HashSet<>();
            for (Message existingMessage : existingMessages) {
                existingUids.add(existingMessage.getUid());
            }
            boolean allSuccess = true;
            ExecutorService executor = Executors.newFixedThreadPool(5);
            try {
                List<Future<Void>> futures = new ArrayList<>();
                for (int i = 0; i < messages.length; i++) {
                    int finalI = i;
                    long finalHighWaterMark = highWaterMark;
                    futures.add(executor.submit(() -> {
                        javax.mail.Message message = messages[finalI];
                        long uid = inbox.getUID(message);
                        // skip expensive MIME parsing for messages at or below the high water mark
                        // that we've already downloaded — just record the UID for deletion detection
                        boolean messageAlreadyDownloaded = existingUids.contains(uid) || uid <= finalHighWaterMark;

                        log.debug("{} - Processing email {} of {}", username, finalI + 1, messages.length);
                        returnMessages.add(new Message(message, uid, messageAlreadyDownloaded, username, accountBitwardenId, obfuscateAmazonOrderSubject));
                        return null;
                    }));
                }
                for (Future<Void> future : futures) {
                    try {
                        future.get(messageProcessingTimeoutSeconds, TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        log.warn("{} - Ran out of time while processing a message", username, e);
                        future.cancel(true);
                        allSuccess = false;
                    } catch (ExecutionException e) {
                        log.warn("{} - Failed to process a message", username, e.getCause());
                        allSuccess = false;
                    }
                }
            } finally {
                executor.shutdown();
            }
            if (!allSuccess) {
                throw new SomeMessagesFailedToDownloadException(returnMessages);
            } else {
                return returnMessages;
            }
        }
    }

    public byte[] downloadAttachment(long messageUid, UUID accountBitwardenId, String attachmentName) throws Exception {
        Item item = bitwardenService.getLoginFromCache(accountBitwardenId);
        Store store = getStore(item);

        try (IMAPFolder inbox = openInbox(store, Folder.READ_ONLY)) {
            javax.mail.Message message = inbox.getMessageByUID(messageUid);
            if (message == null) {
                throw new Exception("Message with UID " + messageUid + " not found on IMAP server");
            }
            MimeMessageParser parser = new MimeMessageParser((javax.mail.internet.MimeMessage) message).parse();
            for (DataSource ds : parser.getAttachmentList()) {
                if (StringUtils.equalsIgnoreCase(ds.getName(), attachmentName)) {
                    return IOUtils.toByteArray(ds.getInputStream());
                }
            }
            throw new Exception("Attachment '" + attachmentName + "' not found in message UID " + messageUid);
        }
    }

    @Async
    public void setReadIndicator(long id, boolean readInd) throws Exception {
        log.debug("{} - Marking email as read ind {}", id, readInd);
        Message message = messageService.get(id);
        Store store = getStore(message);

        try (IMAPFolder imapFolder = openInbox(store, Folder.READ_WRITE)) {
            javax.mail.Message readMessage = imapFolder.getMessageByUID(message.getUid());
            imapFolder.setFlags(new javax.mail.Message[]{readMessage}, new Flags(Flags.Flag.SEEN), readInd);
        }
    }

    public void moveMessage(long id, DestinationEnum destination) throws Exception {
        Message message = messageService.get(id);
        Item item = bitwardenService.getLoginFromCache(message.getAccountBitwardenId());
        Store store = getStore(item);

        String folderName = destination.getFolderName(item.getProvider());

        if (StringUtils.isEmpty(folderName)) {
            throw new Exception("Cannot determine the destination folder name for " + destination + " for this email provider " + item.getProvider());
        }

        try (IMAPFolder inbox = openInbox(store, Folder.READ_WRITE);
             IMAPFolder archive = openFolder(store, Folder.READ_WRITE, folderName)) {
            log.trace("{} - " + destination.getDisplayName() + " email", id);
            javax.mail.Message readMessage = inbox.getMessageByUID(message.getUid());
            inbox.moveMessages(new javax.mail.Message[]{readMessage}, archive);
            log.debug("{} - " + destination.getDisplayName() + " email successful", id);
        }
    }

    private Store getStore(Message message) throws Exception {
        Item item = bitwardenService.getLoginFromCache(message.getAccountBitwardenId());
        return getStore(item);
    }

    private Store getStore(Item item) throws Exception {
        return getStore(item.getHostname(), item.getLogin().getPort(), item.getLogin().getUsername(), item.getLogin().getPassword(), false);
    }

    private Store getStore(String hostname, int port, String username, String decryptedPassword, boolean isRetry) throws Exception {
        log.debug("{} - Getting store for {}", username, hostname);
        Store cached = storeCache.get(username, () -> {
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
            store.connect(hostname, port, username, decryptedPassword);
            log.debug("{} - Connected to store for {}", username, hostname);
            return store;
        });
        if (cached.isConnected()) {
            return cached;
        } else {
            storeCache.invalidate(username);
            if (!isRetry) {
                return getStore(hostname, port, username, decryptedPassword, true);
            } else {
                throw new Exception("The store obtained from the cache was not connected, so that entry was invalidated and an attempt was made to obtain another store. However, this second attempt created a store that was not connected. This is an extremely unlikely scenario.");
            }
        }
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
