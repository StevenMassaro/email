package email.service;

import email.exception.SomeMessagesFailedToDownloadException;
import email.model.Account;
import email.model.ExecStatusEnum;
import email.model.Message;
import email.model.SyncStatusResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public Future<SyncStatusResult> sync(Account account, String bitwardenMasterPassword) {
        log.debug("Sync started for {}", account.getUsername());
        boolean messageFailure = false;
        boolean accountFailure = false;
        long totalDeletedCount = 0;
        long totalInsertedCount = 0;
        long totalChangedReadIndCount = 0;

        try {
            String password = bitwardenService.getPassword(account.getBitwardenItemId(), bitwardenMasterPassword);
            List<Message> dbMessages = messageService.list(account.getId());
            List<Message> imapMessages;
            try {
                imapMessages = imapService.getInboxMessages(account.getHostname(), account.getPort(), account.getUsername(), password, dbMessages);
            } catch (SomeMessagesFailedToDownloadException e) {
                imapMessages = e.getReturnMessages();
                messageFailure = true;
            }

            // first delete all messages from the local db that no longer exist on the imap server
            long deletedCount = 0;
            for (Message dbMessage : dbMessages) {
                Message match = MessageService.findMatch(imapMessages, dbMessage);
                if (match == null) {
                    messageService.delete(dbMessage.getId());
                    deletedCount++;
                }
            }
            log.debug("Deleted {} messages from local database while processing account {}.",
                    deletedCount, account.getUsername());

            // then add all messages that do exist on the imap server
            long insertedCount = 0;
            long changedReadIndCount = 0;
            for (Message imapMessage : imapMessages) {
                Message match = MessageService.findMatch(dbMessages, imapMessage);

                if (match == null) {
                    try {
                        imapMessage.setAccount(account);
                        messageService.insertMessage(imapMessage);
                        insertedCount++;
                    } catch (Exception e) {
                        messageFailure = true;
                        log.error("Failed to insert new message with UID {} while processing account {}.", imapMessage.getUid(), account.getUsername(), e);
                    }
                } else {
                    // if the message has a different read indicator in the database than IMAP
                    if (imapMessage.isReadInd() != match.isReadInd()) {
                        messageService.setReadIndicator(match.getId(), imapMessage.isReadInd());
                        log.debug("Changed read indicator for email ID {} to {}.",
                                match.getId(), imapMessage.isReadInd() ? "read" : "unread");
                        changedReadIndCount++;
                    }
                }
            }
            log.debug("Inserted {} messages into local database while processing account {}.", insertedCount, account.getUsername());
            log.debug("Changed read indicator for {} messages while processing account {}.", changedReadIndCount, account.getUsername());
            totalChangedReadIndCount += changedReadIndCount;
            totalDeletedCount += deletedCount;
            totalInsertedCount += insertedCount;
        } catch (Exception e) {
            accountFailure = true;
            log.error("Exception while processing account {}.", account.getUsername(), e);
        }

        ExecStatusEnum result;
        if (!accountFailure && !messageFailure) {
            result = ExecStatusEnum.RULE_END_SUCCESS;
        } else if (messageFailure && !accountFailure) {
            result = ExecStatusEnum.RULE_END_MESSAGE_FAILURE;
        } else {
            result = ExecStatusEnum.RULE_END_ACCOUNT_FAILURE;
        }
        log.debug("Sync finished for {}", account.getUsername());
        return new AsyncResult<>(new SyncStatusResult(totalInsertedCount, totalDeletedCount, totalChangedReadIndCount, result, account.getUsername()));
    }
}
