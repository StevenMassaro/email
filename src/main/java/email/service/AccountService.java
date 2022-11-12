package email.service;

import email.model.Account;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Log4j2
public class AccountService {

    @Value("#{${accounts}}")
    private List<String> accounts;

    public List<Account> list() {
        List<Account> accountList = new ArrayList<>(accounts.size());
        for (String accountString : accounts) {
            String[] accountStringSplit = accountString.split("\\|");

            Account account = new Account();
            account.setId(Long.parseLong(accountStringSplit[0]));
            account.setBitwardenItemId(UUID.fromString(accountStringSplit[2]));
            account.setAuthentication("SSL");
            account.setHostname(accountStringSplit[1]);
            account.setInboxName("Inbox");
            account.setPort(993);
            accountList.add(account);
        }

        return accountList;
    }

    public Account get(long accountId) {
        return list().stream().filter(a -> a.getId() == accountId).findFirst().get();
    }
}
