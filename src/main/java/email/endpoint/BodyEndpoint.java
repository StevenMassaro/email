package email.endpoint;

import email.service.BodyService;
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

    @GetMapping()
    public String getBody(@RequestParam("bodyId") long bodyId) {
        return bodyService.get(bodyId);
    }
}
