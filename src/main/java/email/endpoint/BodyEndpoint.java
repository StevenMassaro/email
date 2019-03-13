package email.endpoint;

import email.model.BodyPart;
import email.model.ContentTypeEnum;
import email.model.Message;
import email.service.ImapService;
import email.service.MessageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String getBody(@RequestParam("id") long id) throws MessagingException {
        Message message = messageService.get(id);
        List<BodyPart> bodyParts = message.getBodyParts();

        // pick the most favorable body part
        for (ContentTypeEnum contentTypeEnum : ContentTypeEnum.values()) {
            for (BodyPart bodyPart : bodyParts) {
                if (StringUtils.containsIgnoreCase(bodyPart.getContentType(), contentTypeEnum.getImapContentType())) {
                    return bodyPart.getContentAsString();
                }
            }
        }

        return "";
    }
}
