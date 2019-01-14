package email.endpoint;

import email.model.Message;
import email.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/message")
public class MessageEndpoint {

    @Autowired
    private MessageService messageService;

    @GetMapping("/listMessages")
    public List<Message> listMessages() {
        return messageService.list();
    }
}
