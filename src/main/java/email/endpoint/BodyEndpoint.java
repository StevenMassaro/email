package email.endpoint;

import email.model.Message;
import email.service.BodyService;
import email.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/body")
public class BodyEndpoint {

    @Autowired
    private BodyService bodyService;

    @Autowired
    private MessageService messageService;

    @GetMapping()
    public String getBody(@RequestParam("bodyId") long bodyId) {
        Message message = messageService.getByBodyId(bodyId);
//        messageService.setReadIndicator(message.getId(), true);
        return message.getBody().getBody();
    }
}
