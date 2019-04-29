package email.endpoint;

import email.model.BodyPart;
import email.model.ContentTypeEnum;
import email.model.Message;
import email.service.ImapService;
import email.service.MessageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping(value = "/body")
public class BodyEndpoint {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ImapService imapService;

    @GetMapping()
    public ResponseEntity<String> getBody(@RequestParam("id") long id) throws MessagingException {
        HttpHeaders responseHeaders = new HttpHeaders();

        Message message = messageService.get(id);
        List<BodyPart> bodyParts = message.getBodyParts();

        String body = "";

        // pick the most favorable body part
        for (ContentTypeEnum contentTypeEnum : ContentTypeEnum.values()) {
            for (BodyPart bodyPart : bodyParts) {
                if (StringUtils.isEmpty(body) && // stop after finding the best match
                        StringUtils.containsIgnoreCase(bodyPart.getContentType(), contentTypeEnum.getImapContentType())) {
                    body = bodyPart.getContentAsString();
                    responseHeaders.set("Content-Type", contentTypeEnum.getImapContentType());
                }
            }
        }

        return new ResponseEntity<>(body, responseHeaders, HttpStatus.OK);
    }
}
