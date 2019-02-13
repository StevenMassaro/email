package email.endpoint;

import email.model.Message;
import email.service.ImapService;
import email.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping(value = "/body")
public class BodyEndpoint {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ImapService imapService;

    @GetMapping()
    public String getBody(@RequestParam("id") long id) throws MessagingException {
        Message message = messageService.get(id);
        messageService.setReadIndicator(message.getId(), true);
        // todo, specify the type of body that is desired here, rather than just assuming a particular type is requested
        return message.getBodyParts().get(0).getContentAsString();
    }
}
