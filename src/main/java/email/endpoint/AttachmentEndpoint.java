package email.endpoint;

import email.model.Attachment;
import email.model.Message;
import email.service.ImapService;
import email.service.MessageService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/attachment")
public class AttachmentEndpoint {

    private final MessageService messageService;
    private final ImapService imapService;

    public AttachmentEndpoint(MessageService messageService, ImapService imapService) {
        this.messageService = messageService;
        this.imapService = imapService;
    }

    @GetMapping
    public ResponseEntity<Resource> get(@RequestParam("id") long id) {
        ResponseEntity<Resource> resourceResponseEntity = checkAttachments(id);
        if (resourceResponseEntity != null) {
            return resourceResponseEntity;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no message found with the corresponding attachment ID");
    }

    private ResponseEntity<Resource> checkAttachments(long id) {
        for (Message message : messageService.list()) {
            if (message.getAttachments() != null) {
                for (Attachment attachment : message.getAttachments()) {
                    if (attachment.getId() == id) {
                        if (!attachment.isLoaded()) {
                            try {
                                byte[] file = imapService.downloadAttachment(
                                        attachment.getMessageUid(),
                                        attachment.getAccountBitwardenId(),
                                        attachment.getName());
                                attachment.setFile(file);
                                attachment.setLoaded(true);
                            } catch (Exception e) {
                                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                        "Failed to download attachment: " + e.getMessage(), e);
                            }
                        }
                        return attachment.toResponseEntity();
                    }
                }
            }
        }
        return null;
    }
}
