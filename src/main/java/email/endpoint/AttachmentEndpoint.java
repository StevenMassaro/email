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
        Optional<Message> messageWithAttachment = messageService.list().stream()
                .filter(m -> m.getAttachments() != null && !m.getAttachments().isEmpty() && m.getAttachments().stream().anyMatch(a -> a.getId() == id)).findFirst();
        if (messageWithAttachment.isPresent()) {
            Attachment attachment = messageWithAttachment.get().getAttachments().stream().filter(a -> a.getId() == id).findFirst().orElse(null);
            if (attachment != null) {
                Resource file = new ByteArrayResource(attachment.getFile());
                return ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getName() + "\"")
                        .contentType(MediaType.parseMediaType(attachment.getContentType()))
                        .body(file);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no message found with the corresponding attachment ID");
    }
}
