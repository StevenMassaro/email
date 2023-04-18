package email.endpoint;

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

    @GetMapping("/listMessages")
    public Set<Message> listMessages() {
        return messageService.list();
    }

    @DeleteMapping()
    public void deleteMessage(@RequestParam("id") long id) throws Exception {
        Message message = messageService.get(id);
        Item login = bitwardenService.getLoginFromCache(message.getAccountBitwardenId());
        ProviderEnum provider = login.getProvider();
        if (provider != null && provider.isDoImapOperationsSynchronously()) {
            synchronized (this) {
                doDeleteMessage(id);
            }
        } else {
            doDeleteMessage(id);
        }
    }

    private void doDeleteMessage(long id) throws Exception {
        imapService.deleteMessage(id);
        messageService.delete(id);
    }
}
