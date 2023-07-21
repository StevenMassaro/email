package email.endpoint;

import email.model.DestinationEnum;
import email.model.Message;
import email.model.ProviderEnum;
import email.model.bitwarden.Item;
import email.service.BitwardenService;
import email.service.ImapService;
import email.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping(value = "/message")
public class MessageEndpoint {

    private final MessageService messageService;
    private final ImapService imapService;
    private final BitwardenService bitwardenService;

    public MessageEndpoint(MessageService messageService, ImapService imapService, BitwardenService bitwardenService) {
        this.messageService = messageService;
        this.imapService = imapService;
        this.bitwardenService = bitwardenService;
    }

    @GetMapping()
    public Set<Message> listMessages() {
        return messageService.list();
    }

    @DeleteMapping()
    public void deleteMessage(@RequestParam("id") long id) throws Exception {
        moveMessage(id, DestinationEnum.trash);
    }

    private void doMoveMessage(long id, DestinationEnum destination) throws Exception {
        imapService.moveMessage(id, destination);
        messageService.delete(id);
    }

    private void moveMessage(long id, DestinationEnum destination) throws Exception {
        Message message = messageService.get(id);
        Item login = bitwardenService.getLoginFromCache(message.getAccountBitwardenId());
        ProviderEnum provider = login.getProvider();
        if (provider != null && provider.isDoImapOperationsSynchronously()) {
            synchronized (this) {
                doMoveMessage(id, destination);
            }
        } else {
            doMoveMessage(id, destination);
        }
    }

    @PostMapping("/archive")
    public void archiveMessage(@RequestParam("id") long id) throws Exception {
        moveMessage(id, DestinationEnum.archive);
    }
}
