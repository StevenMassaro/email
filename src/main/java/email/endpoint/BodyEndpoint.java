package email.endpoint;

import email.model.Attachment;
import email.model.BodyPart;
import email.model.ContentTypeEnum;
import email.model.Message;
import email.service.ImapService;
import email.service.MessageService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/body")
@Log4j2
public class BodyEndpoint {

    private final MessageService messageService;
    private final ImapService imapService;

    public BodyEndpoint(MessageService messageService, ImapService imapService) {
        this.messageService = messageService;
        this.imapService = imapService;
    }

    @GetMapping()
    public ResponseEntity<String> getBody(@RequestParam("id") long id) throws Exception {
        log.debug("{} - Loading email body", id);
        HttpHeaders responseHeaders = new HttpHeaders();

        Message message = messageService.get(id);
        imapService.setReadIndicator(id, true);
        messageService.setReadIndicator(id, true);
        List<BodyPart> bodyParts = message.getBodyParts();

        String body = "";
        ContentTypeEnum selectedContentType = null;

        // pick the most favorable body part
        for (ContentTypeEnum contentTypeEnum : ContentTypeEnum.values()) {
            for (BodyPart bodyPart : bodyParts) {
                if (StringUtils.isEmpty(body) && // stop after finding the best match
                        StringUtils.containsIgnoreCase(bodyPart.getContentType(), contentTypeEnum.getImapContentType())) {
                    body = bodyPart.getContentAsString();
                    selectedContentType = contentTypeEnum;
                    responseHeaders.set("Content-Type", contentTypeEnum.getReturnContentType() + "; charset=utf-8");
                }
            }
        }

        // replace all cid (Content-Id) identifiers with a link to the attachment endpoint
        for (Attachment attachment : message.getAttachments()) {
            String contentId = attachment.getContentId();
            if (StringUtils.isNotEmpty(contentId)) {
                body = body.replace("cid:" + contentId, "./attachment?id=" + attachment.getId());
            }
        }

        // add open in new tab to all links
        if (selectedContentType == ContentTypeEnum.TEXT_HTML) {
            Document parse = Jsoup.parse(body);
            Elements a = parse.select("a");
            for (Element element : a) {
                if (!element.getElementsByAttribute("href").isEmpty()) {
                    element.attr("target", "_blank");
                }
            }
            body = parse.toString();
        }

        return new ResponseEntity<>(body, responseHeaders, HttpStatus.OK);
    }
}
