package email.service;

import email.exception.SomeMessagesFailedToDownloadException;
import email.model.ExecStatusEnum;
import email.model.Message;
import email.model.SyncStatusResult;
import email.model.bitwarden.Item;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

@Service
@Log4j2
public class SyncService {

    private final ImapService imapService;
    private final MessageService messageService;
    private final BitwardenService bitwardenService;

    public SyncService(ImapService imapService, MessageService messageService, BitwardenService bitwardenService) {
        this.imapService = imapService;
        this.messageService = messageService;
        this.bitwardenService = bitwardenService;
    }

    @Async
    public Future<SyncStatusResult> sync(UUID account, String bitwardenMasterPassword) {
        String username = null;
        boolean messageFailure = false;
        boolean accountFailure = false;
        long totalDeletedCount = 0;
        long totalInsertedCount = 0;
        long totalChangedReadIndCount = 0;

        try {
            Item item = bitwardenService.getLogin(account, bitwardenMasterPassword);
            log.debug("{} - Sync started", item.getLogin().getUsername());
            username = item.getLogin().getUsername();
            List<Message> dbMessages = messageService.list(account);
            List<Message> imapMessages;
            try {
                imapMessages = imapService.getInboxMessages(item.getHostname(), item.getLogin().getPort(), item.getLogin().getUsername(), item.getLogin().getPassword(), dbMessages, account);
            } catch (SomeMessagesFailedToDownloadException e) {
                imapMessages = e.getReturnMessages();
                messageFailure = true;
            }

            // build lookup maps keyed by (uid, accountBitwardenId) for O(1) matching
            Map<String, Message> imapByUid = new HashMap<>();
            for (Message imapMessage : imapMessages) {
                imapByUid.put(imapMessage.getUid() + ":" + imapMessage.getAccountBitwardenId(), imapMessage);
            }
            Map<String, Message> dbByUid = new HashMap<>();
            for (Message dbMessage : dbMessages) {
                dbByUid.put(dbMessage.getUid() + ":" + dbMessage.getAccountBitwardenId(), dbMessage);
            }

            // first delete all messages from the local db that no longer exist on the imap server
            long deletedCount = 0;
            for (Message dbMessage : dbMessages) {
                if (!imapByUid.containsKey(dbMessage.getUid() + ":" + dbMessage.getAccountBitwardenId())) {
                    messageService.delete(dbMessage.getId());
                    deletedCount++;
                }
            }
            log.debug("{} - Deleted {} messages from local database.",
                    item.getLogin().getUsername(), deletedCount);

            // then add all messages that do exist on the imap server
            long insertedCount = 0;
            long changedReadIndCount = 0;
            long maxUid = 0;
            for (Message imapMessage : imapMessages) {
                if (imapMessage.getUid() > maxUid) {
                    maxUid = imapMessage.getUid();
                }
                Message match = dbByUid.get(imapMessage.getUid() + ":" + imapMessage.getAccountBitwardenId());

                if (match == null) {
                    try {
                        messageService.insertMessage(imapMessage);
                        insertedCount++;
                    } catch (Exception e) {
                        messageFailure = true;
                        log.error("{} - Failed to insert new message with UID {}.", item.getLogin().getUsername(), imapMessage.getUid(), e);
                    }
                } else {
                    // if the message has a different read indicator in the database than IMAP
                    if (imapMessage.isReadInd() != match.isReadInd()) {
                        messageService.setReadIndicator(match.getId(), imapMessage.isReadInd());
                        log.debug("{} - Changed read indicator to {}.",
                                match.getId(), imapMessage.isReadInd() ? "read" : "unread");
                        changedReadIndCount++;
                    }
                }
            }
            log.debug("{} - Inserted {} messages into local database.", item.getLogin().getUsername(), insertedCount);
            log.debug("{} - Changed read indicator for {} messages.", item.getLogin().getUsername(), changedReadIndCount);
            if (maxUid > 0) {
                messageService.setHighWaterMark(account, maxUid);
                log.debug("{} - Updated high water mark to {}", item.getLogin().getUsername(), maxUid);
            }
            totalChangedReadIndCount += changedReadIndCount;
            totalDeletedCount += deletedCount;
            totalInsertedCount += insertedCount;
        } catch (Exception e) {
            accountFailure = true;
            log.error("{} - Exception while processing account {}.", username, account, e);
        }

        ExecStatusEnum result;
        if (!accountFailure && !messageFailure) {
            result = ExecStatusEnum.RULE_END_SUCCESS;
        } else if (messageFailure && !accountFailure) {
            result = ExecStatusEnum.RULE_END_MESSAGE_FAILURE;
        } else {
            result = ExecStatusEnum.RULE_END_ACCOUNT_FAILURE;
        }
        log.debug("{} - Sync finished for {}", username, account);
        return new AsyncResult<>(new SyncStatusResult(totalInsertedCount, totalDeletedCount, totalChangedReadIndCount, result, username));
    }
}
