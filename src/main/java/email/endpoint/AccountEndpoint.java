package email.endpoint;

import email.model.Account;
import email.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountEndpoint {

    @Autowired
    private AccountService accountService;

    @PostMapping("/createAccount")
    public void insert(@RequestBody Account account) {
        accountService.insert(account);
    }
}
