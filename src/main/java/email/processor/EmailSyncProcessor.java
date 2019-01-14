package email.processor;

import email.model.Account;
import email.model.Message;
import email.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Flags;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
                List<Message> imapMessages = imapService.getInboxMessages(account.getDomain().getHostname(), account.getUsername(), decryptedPassword);
                List<Message> messages = messageService.list(account.getId());
                for (Message imapMessage : imapMessages) {
                    Message match = MessageService.findMatch(messages, imapMessage);

                    if (match == null) {
                        try {
                            insertNewMessage(imapMessage, account);
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
                            messageService.setReadIndicator(match.getId(), imapMessage.isReadInd());
                            System.out.println("Changed read indicator for email: " + match.getId());
                        }
                    }
                    messages.remove(match);
                }
                // the remaining messages in the messages list should be deleted, they no longer exist on the imap server
                if (!messages.isEmpty()) {
                    messageService.delete(messages);
                    bodyService.delete(messages);
                    System.out.println("Deleted " + messages.size() + " messages from local database.");
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

    @Transactional
    public void insertNewMessage(Message message, Account account) throws MessagingException, IOException {
        long bodyId = bodyService.insert(message);
        messageService.insertMessage(account.getId(), message.getSubject(), message.getDateReceived(), 1L, 1L, bodyId, message.isReadInd());
    }
}
