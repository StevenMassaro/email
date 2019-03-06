package email.service;

import email.model.Account;
import email.model.ExecStatusEnum;
import email.model.Message;
import email.model.SyncStatusResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncService {

    private Logger logger = LoggerFactory.getLogger(SyncService.class);

    @Autowired
    private ImapService imapService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private ExecutionLogService executionLogService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    /**
     * Attempts to execute a differential sync. If there is a running task and the queue is full, the sync will fail
     * to be scheduled.
     *
     * @throws TaskRejectedException when there is a running task and/or the queue is full.
     */
    public void attemptToScheduleDifferentialSync() throws TaskRejectedException {
        taskExecutor.execute(this::executeDifferentialSync);
    }

    public SyncStatusResult executeDifferentialSync() {
        logger.info(ExecStatusEnum.RULE_START.getMessage());
        executionLogService.insert(ExecStatusEnum.RULE_START);
        boolean messageFailure = false;
        boolean accountFailure = false;
        List<Account> accounts = accountService.list();
        long totalDeletedCount = 0;
        long totalInsertedCount = 0;
        long totalChangedReadIndCount = 0;

        for (Account account : accounts) {
            try {
                String decryptedPassword = encryptionService.decrypt(account.getPassword());
                List<Message> dbMessages = messageService.list(account.getId());
                List<Message> imapMessages = imapService.getInboxMessages(account.getHostname(), account.getPort(), account.getUsername(), decryptedPassword, dbMessages);

                // first delete all messages from the local db that no longer exist on the imap server
                long deletedCount = 0;
                for (Message dbMessage : dbMessages) {
                    Message match = MessageService.findMatch(imapMessages, dbMessage);
                    if (match == null) {
                        messageService.delete(dbMessage.getId());
                        deletedCount++;
                    }
                }
                logger.debug(String.format("Deleted %s messages from local database while processing account %s.",
                        deletedCount, account.getUsername()));

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
                            logger.error(String.format("Failed to insert new message with UID %s while processing account %s.", imapMessage.getUid(), account.getUsername()), e);
                        }
                    } else {
                        // if the message has a different read indicator in the database than IMAP
                        if (imapMessage.isReadInd() != match.isReadInd()) {
                            messageService.setReadIndicator(match.getId(), imapMessage.isReadInd());
                            logger.debug(String.format("Changed read indicator for email ID %s to %s.",
                                    match.getId(), imapMessage.isReadInd() ? "read" : "unread"));
                            changedReadIndCount++;
                        }
                    }
                }
                logger.debug(String.format("Inserted %s messages into local database while processing account %s.", insertedCount, account.getUsername()));
                logger.debug(String.format("Changed read indicator for %s messages while processing account %s.", changedReadIndCount, account.getUsername()));
                totalChangedReadIndCount += changedReadIndCount;
                totalDeletedCount += deletedCount;
                totalInsertedCount += insertedCount;
            } catch (Exception e) {
                accountFailure = true;
                logger.error(String.format("Exception while processing account %s.", account.getUsername()), e);
            }
        }

        ExecStatusEnum result;
        if (!accountFailure && !messageFailure) {
            result = ExecStatusEnum.RULE_END_SUCCESS;
        } else if (messageFailure && !accountFailure) {
            result = ExecStatusEnum.RULE_END_MESSAGE_FAILURE;
        } else {
            result = ExecStatusEnum.RULE_END_ACCOUNT_FAILURE;
        }
        executionLogService.insert(result);
        logger.info(result.getMessage());
        return new SyncStatusResult(totalInsertedCount, totalDeletedCount, totalChangedReadIndCount, result);
    }
}
