package email.endpoint;

import email.model.Attachment;
import email.model.Message;
import email.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/attachment")
public class AttachmentEndpoint {

    private final MessageService messageService;

    public AttachmentEndpoint(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<Resource> get(@RequestParam("id") long id) {
        ResponseEntity<Resource> resourceResponseEntity = checkAttachments(id);
        if (resourceResponseEntity != null) {
            return resourceResponseEntity;
        }

        resourceResponseEntity = checkCid(id);
        if (resourceResponseEntity != null) {
            return resourceResponseEntity;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no message found with the corresponding attachment ID");
    }

    private ResponseEntity<Resource> checkAttachments(long id) {
        Optional<Message> messageWithAttachment = messageService.list().stream()
                .filter(m -> m.getAttachments() != null && !m.getAttachments().isEmpty() && m.getAttachments().stream().anyMatch(a -> a.getId() == id)).findFirst();
        if (messageWithAttachment.isPresent()) {
            Attachment attachment = messageWithAttachment.get().getAttachments().stream().filter(a -> a.getId() == id).findFirst().orElse(null);
            if (attachment != null) {
                return attachment.toResponseEntity();
            }
        }
        return null;
    }

    private ResponseEntity<Resource> checkCid(long id) {
        Optional<Message> messageWithAttachment = messageService.list().stream()
                .filter(m -> m.getCidMap() != null && !m.getCidMap().isEmpty() && m.getCidMap().entrySet().stream().anyMatch(a -> a.getValue().getId() == id)).findFirst();
        if (messageWithAttachment.isPresent()) {
            Map.Entry<String, Attachment> attachment = messageWithAttachment.get().getCidMap().entrySet().stream().filter(a -> a.getValue().getId() == id).findFirst().orElse(null);
            if (attachment != null) {
                return attachment.getValue().toResponseEntity();
            }
        }
        return null;
    }
}
