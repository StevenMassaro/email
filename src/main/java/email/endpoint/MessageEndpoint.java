package email.endpoint;

import email.model.DestinationEnum;
import email.model.Message;
import email.service.ImapService;
import email.service.MessageService;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping()
    public Set<Message> listMessages() {
        return messageService.list();
    }

    @DeleteMapping()
    public synchronized void deleteMessage(@RequestParam("id") long id) throws Exception {
        moveMessage(id, DestinationEnum.trash);
    }

    private void moveMessage(long id, DestinationEnum destination) throws Exception {
        imapService.moveMessage(id, destination);
        messageService.delete(id);
    }

    @PostMapping("/archive")
    public synchronized void archiveMessage(@RequestParam("id") long id) throws Exception {
        moveMessage(id, DestinationEnum.archive);
    }
}
