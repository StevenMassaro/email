package email.processor;

import email.model.Account;
import email.model.Message;
import email.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.io.IOException;
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
    @Scheduled(fixedDelay = 10000) //this is every 10 seconds
    public void run() {
        long syncStart = System.nanoTime();
        System.out.println("Starting sync rule.");
        List<Account> accounts = accountService.list();

        for (Account account : accounts) {
            try {
                String decryptedPassword = encryptionService.decrypt(account.getPassword());
                List<Message> messages = imapService.getInboxMessages(account.getDomain().getHostname(), account.getUsername(), decryptedPassword);
                for (Message message : messages) {
//                    long start = System.nanoTime();
//                    Flags flags = message.getFlags();
//                    Flags.Flag[] systemflags = flags.getSystemFlags();
//                    String[] userflags = flags.getUserFlags();
//                    long end = System.nanoTime();
//                    System.out.println("Time to get flags: " + (end - start));
                    boolean readInd = false;

//                    try {
//                        readInd = systemflags[0].equals(Flags.Flag.SEEN);
//                    } catch (Exception e) {
                        // readInd would be false in this case, because there is no system flag saying that it is seen.
//                    }
                    long count = messageService.count(account.getId(), message.getSubject(), message.getDateReceived());
                    if(count == 0){
                        try{
                            insertNewMessage(message, account);
                        } catch (DuplicateKeyException exception){
                            System.out.println("Message violates primary key constraint.");
                        }
                    } else {
                        System.out.println("Message already in database.");
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

    @Transactional
    public void insertNewMessage(Message message, Account account) throws MessagingException, IOException {
        long bodyId = bodyService.insert(message);
        messageService.insertMessage(account.getId(), message.getSubject(), message.getDateReceived(), 1L, 1L, bodyId);
    }
}
