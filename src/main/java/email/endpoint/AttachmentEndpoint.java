package email.endpoint;

import email.model.Attachment;
import email.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/attachment")
public class AttachmentEndpoint {

    @Autowired
    private AttachmentService attachmentService;

    @GetMapping
    public ResponseEntity<Resource> get(@RequestParam("id") long id) {
        Attachment attachment = attachmentService.get(id);
        Resource file = new ByteArrayResource(attachment.getFile());
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + attachment.getName() + "\"").body(file);
    }
}
