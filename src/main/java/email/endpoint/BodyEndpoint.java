package email.endpoint;

import email.model.BodyPart;
import email.model.Message;
import email.processor.ImapReadIndicatorProcessor;
import email.service.ImapService;
import email.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public String getBody(@RequestParam("uid") long uid) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Runnable worker = new ImapReadIndicatorProcessor(imapService, uid, true);
        executor.execute(worker);
        executor.shutdown();

        messageService.setReadIndicator(uid, true);
        // todo, specify the type of body that is desired here, rather than just assuming a particular type is requested
        return messageService.get(uid).getBodyParts().get(0).getContentAsString();
    }
}
