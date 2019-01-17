package email.endpoint;

import email.service.ImapService;
import email.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

@RestController
@RequestMapping(value = "/body")
public class BodyEndpoint {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ImapService imapService;

    @GetMapping()
    public String getBody(@RequestParam("uid") long uid) throws MessagingException {
        messageService.setReadIndicator(uid, true);
        // todo, this needs to occur in a separate thread, it's too slow to be done live
        imapService.setReadIndicator(uid, true);
        return messageService.get(uid).getBody();
    }
}
