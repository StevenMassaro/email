package email.processor;

import email.model.*;
import email.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class EmailSyncProcessor implements IProcessor {

    private Logger logger = LoggerFactory.getLogger(EmailSyncProcessor.class);

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

    @Override
    @Scheduled(fixedDelay = 15 * 60 * 1000)
    public void run() {
        logger.info(ExecStatusEnum.RULE_START.getMessage());
        executionLogService.insert(ExecStatusEnum.RULE_START);
        boolean messageFailure = false;
        boolean accountFailure = false;
        List<Account> accounts = accountService.list();

        for (Account account : accounts) {
            try {
                String decryptedPassword = encryptionService.decrypt(account.getPassword());
                List<Message> imapMessages = imapService.getInboxMessages(account.getHostname(), account.getPort(), account.getUsername(), decryptedPassword);
                List<Message> dbMessages = messageService.list(account.getId());

                // first delete all messages from the local db that no longer exist on the imap server
                long deletedCount = 0;
                for (Message dbMessage : dbMessages) {
                    Message match = MessageService.findMatch(imapMessages, dbMessage);
                    if (match == null) {
                        messageService.delete(Collections.singletonList(dbMessage));
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
                            logger.error(String.format("Failed to insert new message %s while processing account %s.", imapMessage.getUid(), account.getUsername()), e);
                        }
                    } else {
                        // if the message has a different read indicator in the database than IMAP
                        if (imapMessage.isReadInd() != match.isReadInd()) {
                            messageService.setReadIndicator(match.getUid(), imapMessage.isReadInd());
                            logger.debug(String.format("Changed read indicator for email %s to %s.",
                                    match.getUid(), imapMessage.isReadInd() ? "read" : "unread"));
                            changedReadIndCount++;
                        }
                    }
                }
                logger.debug(String.format("Inserted %s messages into local database while processing account %s.", insertedCount, account.getUsername()));
                logger.debug(String.format("Changed read indicator for %s messages while processing account %s.", changedReadIndCount, account.getUsername()));
            } catch (MessagingException | IOException e) {
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
    }
}
