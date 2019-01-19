package email.processor;

import email.service.ImapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;

public class ImapReadIndicatorProcessor implements Runnable {
    private Logger logger = LoggerFactory.getLogger(EmailSyncProcessor.class);

    private ImapService imapService;

    private long uid;
    private boolean readInd;

    public ImapReadIndicatorProcessor(ImapService imapService, long uid, boolean readInd) {
        this.imapService = imapService;
        this.uid = uid;
        this.readInd = readInd;
    }

    @Override
    public void run() {
        try {
            imapService.setReadIndicator(uid, readInd);
        } catch (MessagingException e) {
            logger.error(String.format("Failed to set IMAP read indicator for message %s.", uid), e);
        }
    }
}
