package email.endpoint;

import email.model.Message;
import email.service.ImapService;
import email.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/message")
public class MessageEndpoint {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ImapService imapService;

    @GetMapping("/listMessages")
    public List<Message> listMessages() {
        return messageService.list();
    }

    @DeleteMapping()
    public void deleteMessage(@RequestParam("uid") long uid) throws MessagingException {
        imapService.deleteMessage(uid);
        messageService.delete(uid);
    }
}
