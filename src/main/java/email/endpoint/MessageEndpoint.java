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
