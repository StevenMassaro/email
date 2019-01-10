package email.processor;

import email.model.Account;
import email.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.Flags;
import javax.mail.Message;
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
        List<Account> accounts = accountService.list();

        for (Account account : accounts) {
            try {
                String decryptedPassword = encryptionService.decrypt(account.getPassword());
                Message[] messages = imapService.getInboxMessages(account.getDomain().getHostname(), account.getUsername(), decryptedPassword);
                for (Message message : messages) {
                    long start = System.nanoTime();
                    Flags flags = message.getFlags();
                    Flags.Flag[] systemflags = flags.getSystemFlags();
                    String[] userflags = flags.getUserFlags();
                    long end = System.nanoTime();
                    System.out.println("Time to get flags: " + (end - start));
                    boolean readInd = false;

                    try {
                        readInd = systemflags[0].equals(Flags.Flag.SEEN);
                    } catch (Exception e) {
                        // readInd would be false in this case, because there is no system flag saying that it is seen.
                    }
                    long bodyId = bodyService.insert(message);
                    messageService.insertMessage(account.getId(), message.getSubject(), message.getReceivedDate(), 1L, 1L, bodyId);
                }
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
