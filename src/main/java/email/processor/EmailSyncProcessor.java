package email.processor;

import email.model.Account;
import email.model.Body;
import email.model.Message;
import email.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class EmailSyncProcessor implements IProcessor {

    @Autowired
    private ImapService imapService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private BodyService bodyService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EncryptionService encryptionService;

    @Override
    @Scheduled(fixedDelay = 500 * 1000) //this is every 10 seconds
    public void run() {
        long syncStart = System.nanoTime();
        System.out.println("Starting sync rule.");
        List<Account> accounts = accountService.list();

        for (Account account : accounts) {
            try {
                String decryptedPassword = encryptionService.decrypt(account.getPassword());
                List<Message> imapMessages = imapService.getInboxMessages(account.getDomain().getHostname(), account.getDomain().getPort(), account.getUsername(), decryptedPassword);
                List<Message> dbMessages = messageService.list(account.getId());

                // first delete all messages from the local db that no longer exist on the imap server
                long deletedCount = 0;
                for (Message dbMessage : dbMessages) {
                    Message match = MessageService.findMatch(imapMessages, dbMessage);
                    if (match == null) {
                        messageService.delete(Collections.singletonList(dbMessage));
                        bodyService.delete(Collections.singletonList(dbMessage));
                        deletedCount++;
                    }
                }

                System.out.println("Deleted " + deletedCount + " messages from local database.");

                // then add all messages that do exist on the imap server
                for (Message imapMessage : imapMessages) {
                    Message match = MessageService.findMatch(dbMessages, imapMessage);

                    if (match == null) {
                        try {
                            imapMessage.setAccount(account);
                            insertNewMessage(imapMessage);
                            System.out.println("Inserted new message.");
                        } catch (DuplicateKeyException exception) {
                            System.out.println("Message violates primary key constraint.");
                        } catch (Exception e) {
                            System.out.println("Failed to insert new message. " + e.getMessage());
                        }
                    } else {
                        System.out.println("Message already in database.");

                        // if the message has a different read indicator in the database than IMAP
                        if (imapMessage.isReadInd() != match.isReadInd()) {
                            messageService.setReadIndicator(match.getUid(), imapMessage.isReadInd());
                            System.out.println("Changed read indicator for email: " + match.getUid());
                        }
                    }
                }
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
        }
        long syncEnd = System.nanoTime();
        long syncTime = syncEnd - syncStart;
        double seconds = (double)syncTime / 1_000_000_000.0;
        System.out.println("Time to run sync rule (seconds): " + seconds);
    }

    //todo this transaction does not rollback from the body table correctly
    @Transactional
    public void insertNewMessage(Message message) throws MessagingException, IOException {
        long bodyId = bodyService.insert(message);
        message.setBody(new Body(bodyId));
        messageService.insertMessage(message);
    }
}
