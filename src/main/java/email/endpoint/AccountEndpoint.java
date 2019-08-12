package email.endpoint;

import email.model.Account;
import email.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountEndpoint {

    @Autowired
    private AccountService accountService;

    @PostMapping()
    public void insert(@RequestBody Account account) {
        accountService.insert(account);
    }

    @PatchMapping("/{accountId}/password")
    public Account updatePassword(@PathVariable long accountId, @RequestBody String newPassword) {
        return accountService.updatePassword(accountId, newPassword, false);
    }
}
