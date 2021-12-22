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
import java.util.Set;

@RestController
@RequestMapping(value = "/message")
public class MessageEndpoint {

    private final MessageService messageService;
    private final ImapService imapService;

    public MessageEndpoint(MessageService messageService, ImapService imapService) {
        this.messageService = messageService;
        this.imapService = imapService;
    }

    @GetMapping("/listMessages")
    public Set<Message> listMessages() {
        return messageService.list();
    }

    @DeleteMapping()
    public void deleteMessage(@RequestParam("id") long id) throws Exception {
        imapService.deleteMessage(id);
        messageService.delete(id);
    }

    @PatchMapping("{id}/read")
    public long setReadIndicator(@PathVariable("id") long id) throws MessagingException {
        imapService.setReadIndicator(id, true);
        messageService.setReadIndicator(id, true);
        return id;
    }
}
